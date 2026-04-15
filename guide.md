## Repository: Java WPILib Swerve Robot — Code Structure

This document maps the main classes in `src/main/java/frc/robot` and `src/main/java/frc/robot/drive`, lists their key fields and methods, and explains how they interact at runtime. Use this as a quick-reference when making changes.

---

### High-level overview

- Main entry: `Main.main` starts the WPILib robot runtime which instantiates `Robot`.
- `Robot` creates a `RobotContainer` and runs the command scheduler every `robotPeriodic()`.
- `RobotContainer` wires subsystems, default commands, and button bindings. It also builds the autonomous `SwerveControllerCommand` example.
- `DriveSubsystem` composes four `SwerveModule` instances, talks to the IMU, and manages odometry and high-level drive commands.
- `SwerveModule` manages low-level motor controllers, encoders, and closed-loop setpoints for an individual wheel.
- `Constants` and `Configs` hold numeric configuration: geometry, CAN IDs, conversion factors, and motor controller presets.

---

### File-by-file mapping

- `Main.java`
	- main(): calls `RobotBase.startRobot(Robot::new)` to bootstrap the WPILib lifecycle. No other logic.

- `Robot.java` (extends `TimedRobot`)
	- Fields: `m_robotContainer`, `m_autonomousCommand`
	- robotPeriodic(): runs `CommandScheduler.getInstance().run()` (drives command framework)
	- autonomousInit(): retrieves command from `RobotContainer.getAutonomousCommand()` and schedules it
	- teleopInit(): cancels the autonomous command if still running
	- testInit(): cancels all commands (clean test state)
	- Other lifecycle hooks (disabled/test/periodic/exit) are present but empty — standard WPILib template.

- `RobotContainer.java`
	- Fields: `DriveSubsystem m_robotDrive`, `XboxController m_driverController`
	- Constructor:
		- Instantiates `DriveSubsystem` and `XboxController`.
		- Calls `configureButtonBindings()`.
		- Sets default command for `m_robotDrive` to a `RunCommand` that reads joystick inputs and calls `m_robotDrive.drive(...)` with deadband applied.
	- configureButtonBindings():
		- R1 (PS4 R1) held: runs `m_robotDrive.setX()` (places wheels in X to resist motion)
		- Start button pressed: runs `m_robotDrive.zeroHeading()` (resets the IMU)
	- getAutonomousCommand():
		- Builds an example `Trajectory` and `SwerveControllerCommand` using:
			- `m_robotDrive::getPose` as pose supplier
			- `m_robotDrive::setModuleStates` as module-state consumer
		- Calls `m_robotDrive.resetOdometry(initialPose)` and returns the composed command.

- `DriveSubsystem.java` (subsystem)
	- Fields (key):
		- Four `SwerveModule` instances: `m_frontLeft`, `m_frontRight`, `m_rearLeft`, `m_rearRight`.
		- `ADIS16470_IMU m_gyro` — gyro/IMU used for heading and field-relative driving.
		- `SwerveDriveOdometry m_odometry` — tracks robot pose using module positions + gyro.
	- Constructor: reports usage (HAL.report) and initializes odometry with current gyro angle + module positions.
	- periodic(): updates odometry from gyro + module positions each robot tick.
	- getPose(): returns `m_odometry.getPoseMeters()` — current estimated robot pose.
	- resetOdometry(Pose2d pose): resets odometry to a known pose (uses current gyro and module positions)
	- drive(double xSpeed, double ySpeed, double rot, boolean fieldRelative):
		- Input semantics: x/y/rot are normalized (-1..1) fractions and are scaled by `DriveConstants.kMaxSpeedMetersPerSecond` and `kMaxAngularSpeed`.
		- Produces `SwerveModuleState[]` via `DriveConstants.kDriveKinematics` (field-relative conversion if requested).
		- Calls `SwerveDriveKinematics.desaturateWheelSpeeds(...)` to clamp wheel speeds.
		- Sends each state to corresponding `SwerveModule.setDesiredState(...)`.
	- setX(): sets wheel angles into an X pattern to make the robot resist being pushed.
	- setModuleStates(SwerveModuleState[] desiredStates): desaturates and forwards the states to each module.
	- resetEncoders(): calls `resetEncoders()` on each `SwerveModule` (driving encoder reset).
	- zeroHeading(): resets the IMU (gyro)
	- getHeading(): returns gyro heading in degrees
	- getTurnRate(): returns gyro turn rate (deg/s), with `DriveConstants.kGyroReversed` applied

- `SwerveModule.java` (per-wheel hardware wrapper)
	- Fields (key):
		- `SparkFlex m_drivingSpark` — brushless drive motor (velocity control)
		- `SparkMax m_turningSpark` — brushless turning motor (position control)
		- `RelativeEncoder m_drivingEncoder` — measures wheel travel
		- `AbsoluteEncoder m_turningEncoder` — measures steering angle (absolute)
		- `SparkClosedLoopController` instances for closed-loop control on drive and turning
		- `double m_chassisAngularOffset` — per-module rotation offset used to align module angle to chassis
		- `SwerveModuleState m_desiredState` — last commanded state
	- Constructor(int drivingCANId, int turningCANId, double chassisAngularOffset):
		- Instantiates the motor controllers and encoders, then configures them with `Configs.SwerveModule.drivingConfig` and `turningConfig`.
		- Initializes `m_desiredState.angle` from the absolute turning encoder and zeroes the driving encoder position.
	- getState(): returns `SwerveModuleState(velocity, angle)` using current encoder readings (velocity in m/s, angle corrected by chassis offset)
	- getPosition(): returns `SwerveModulePosition(positionMeters, angle)` for odometry
	- setDesiredState(SwerveModuleState desiredState):
		- Creates a corrected desired state that adds chassis offset to the requested angle.
		- Calls `optimize(...)` relative to the current turning encoder position (to minimize spin).
		- Sets closed-loop setpoints:
			- driving controller: velocity setpoint (ControlType.kVelocity)
			- turning controller: position setpoint (ControlType.kPosition)
		- Stores the requested desired state.
	- resetEncoders(): sets the driving encoder position to zero.

- `Configs.java`
	- Contains a nested `SwerveModule` class with static `SparkFlexConfig drivingConfig` and `SparkMaxConfig turningConfig`.
	- Static initializer computes conversion factors from `Constants.ModuleConstants` (wheel diameter, gear reduction) and sets example PID gains, feedforward, current limits, encoder conversion factors, and absolute encoder presets.
	- This is where Spark-specific configuration (encoder conversion factors, PID gains, idle modes) is centralized.

- `Constants.java`
	- Central numeric config grouped into nested static classes:
		- `DriveConstants` — geometry (kTrackWidth, kWheelBase), `kDriveKinematics`, chassis angular offsets for each module, CAN IDs for driving/turning motors, kMaxSpeed values, `kGyroReversed`.
		- `ModuleConstants` — pinion teeth, wheel diameter, drive reduction, free speeds, used by `Configs` to compute conversion factors.
		- `OIConstants` — controller port and deadband.
		- `AutoConstants` — trajectory controller gains and motion constraints used by `RobotContainer`.
		- `NeoMotorConstants` — motor base free-speed (RPM).

---

### Runtime call flow (common path)

1. `Main.main()` -> WPILib starts -> `Robot` constructed -> `RobotContainer` constructed.
2. `RobotContainer` constructs `DriveSubsystem`.
3. `DriveSubsystem` constructs 4x `SwerveModule` instances; each `SwerveModule` configures its Spark controllers using `Configs.SwerveModule` presets.
4. Operator moves joysticks; default `RunCommand` calls `DriveSubsystem.drive(x,y,rot,fieldRelative)` every tick.
5. `DriveSubsystem.drive()` computes target `SwerveModuleState[]` and calls each `SwerveModule.setDesiredState(...)`.
6. Each `SwerveModule` sets closed-loop controllers (velocity for drive, position for turn). Encoders provide feedback used by odometry and for optimization.
7. `DriveSubsystem.periodic()` updates `SwerveDriveOdometry` with positions and gyro angle.

---

### Quick edit checklist (where to change common things)

- Change motor CAN IDs: update `Constants.DriveConstants` (kFrontLeftDrivingCanId, etc.).
- Change chassis module angular offsets: update `DriveConstants.kFrontLeftChassisAngularOffset`, etc.
- Tune motor controller PID/FF/current limits: edit `Configs.SwerveModule.drivingConfig` and `turningConfig` (they contain PID/FF fields and example gains).
- Change unit/geometry constants or gear ratios: edit `Constants.ModuleConstants` (used by `Configs` conversion factors).
- Add or remove a module: update `DriveSubsystem` to instantiate or remove `SwerveModule` instances, and adjust odometry arrays / `kDriveKinematics` if geometry changes.

---

### Notes & gotchas

- Units: Configs encoders are configured to convert to meters / meters per second and radians / radians per second. Be careful when changing gear ratios or wheel diameter; `Configs` and `Constants.ModuleConstants` must remain consistent.
- CAN IDs live in `Constants.DriveConstants` (contrary to some templates that put hardware IDs in a `Configs` class). Search for `kFrontLeftDrivingCanId` when updating wiring.
- `SwerveModule.setDesiredState(...)` applies `optimize(...)` using the current turning encoder value. This avoids unnecessary wheel rotation but requires the turning encoder to be correctly zeroed/aligned.
- The absolute turning encoder is applied with `positionConversionFactor` to radians in `Configs` and `positionWrappingEnabled(true)` is set for turning closed loop.
- If you change motor controller vendor classes or wiring, update `vendordeps/` and `build.gradle` accordingly.

---