# Cursor Instructions (Team 6809)

Project scope is Java Command-Based robot code in `src/main/java/frc/robot/**`.

## Core Map

- `Main.java`: robot entry point
- `Robot.java`: lifecycle shell, runs `CommandScheduler` in `robotPeriodic`
- `RobotContainer.java`: controller bindings + default drive command
- `Constants.java`: drivetrain, module, OI, and tuning constants
- `Configs.java`: REV Spark config presets
- `drive/DriveSubsystem.java`: swerve drive logic, odometry, gyro helpers
- `drive/SwerveModule.java`: per-module closed-loop control

## Current Behavior

- Default command drives field-relative from Xbox sticks.
- `R1` holds X-lock (`setX()`).
- `Start` zeros heading (`zeroHeading()`).
- Odometry updates in `DriveSubsystem.periodic()` from navX + module positions.

## Agent Rules

1. Keep changes inside Java source under `src/main/java`.
2. Do **not** directly edit Gradle or JSON files.
3. Keep `Robot.java` thin; put behavior in subsystems/commands/container.
4. Put tunables in `Constants.java`; avoid magic numbers in logic.
5. Preserve swerve safety flow:
   - kinematic desaturation
   - module state optimization
   - existing `setModuleStates()` / `setDesiredState()` path
6. Keep diffs minimal; do not mass-reformat unrelated code.

## Preferred Implementation Pattern

- Input mapping and button wiring: `RobotContainer`
- Mechanism behavior: subsystem classes
- Multi-step actions: dedicated command classes (not large inline lambdas)

## Validation Checklist

- `CommandScheduler` still runs in `robotPeriodic`.
- Command requirements are correct for drivetrain commands.
- Field-relative control behavior remains intentional.
- Gyro sign handling stays consistent with `kGyroReversed`.
- New constants are clearly named and grouped.
- If hardware cannot be tested in-session, provide a short on-robot test checklist.

## Known Risks

- Template-derived code may still contain placeholder gains/comments.
- Drive/turn PID and feedforward values likely require robot-side tuning.
- navX is currently configured with `NavXComType.kUSB1`; deployment assumptions must match hardware.
