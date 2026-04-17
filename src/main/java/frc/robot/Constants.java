package frc.robot;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.math.util.Units;

public final class Constants {
  public static final class DriveConstants {

    public static final double kMaxDriveSpeed = 4.8;
    public static final double kMaxTurnSpeed = 2 * Math.PI; // radians per second
    public static final boolean kGyroReversed = false;

    public static final double kDriveWidth = Units.inchesToMeters(26.5);
    public static final double kDriveLength = Units.inchesToMeters(26.5);

    public static final SwerveDriveKinematics kDriveKinematics = new SwerveDriveKinematics(
        new Translation2d(kDriveLength / 2, kDriveWidth / 2),
        new Translation2d(kDriveLength / 2, -kDriveWidth / 2),
        new Translation2d(-kDriveLength / 2, kDriveWidth / 2),
        new Translation2d(-kDriveLength / 2, -kDriveWidth / 2));

    public static final int kFrontLeftDrivingCanId  = 1;
    public static final int kRearLeftDrivingCanId   = 3;
    public static final int kFrontRightDrivingCanId = 5;
    public static final int kRearRightDrivingCanId  = 7;

    public static final int kFrontLeftTurningCanId  = 2;
    public static final int kRearLeftTurningCanId   = 4;
    public static final int kFrontRightTurningCanId = 6;
    public static final int kRearRightTurningCanId  = 8;

    public static final double kFrontLeftAngularOffset  = -Math.PI / 2;
    public static final double kFrontRightAngularOffset = 0;
    public static final double kBackLeftAngularOffset   = Math.PI;
    public static final double kBackRightAngularOffset  = Math.PI / 2;
  }

  public static final class ModuleConstants {

    public static final int kDrivingMotorPinionTeeth = 14;

    public static final double kDrivingMotorFreeSpeedRps = NeoMotorConstants.kFreeSpeedRpm / 60;
    public static final double kWheelDiameterMeters = 0.0762;
    public static final double kWheelCircumferenceMeters = kWheelDiameterMeters * Math.PI;

    // 45 teeth on the wheel's bevel gear, 22 teeth on the first-stage spur gear, 15
    public static final double kDrivingMotorReduction = (45.0 * 22) / (kDrivingMotorPinionTeeth * 15);
    public static final double kDriveWheelFreeSpeedRps = (kDrivingMotorFreeSpeedRps * kWheelCircumferenceMeters)
        / kDrivingMotorReduction;
  }

  public static final class OIConstants {
    public static final int kDriverControllerPort = 0;
    public static final double kDriveDeadband = 0.05;
  }

  public static final class AutoConstants {
    public static final double kMaxSpeedMetersPerSecond = 3;
    public static final double kMaxAccelerationMetersPerSecondSquared = 3;
    public static final double kMaxAngularSpeedRadiansPerSecond = Math.PI;
    public static final double kMaxAngularSpeedRadiansPerSecondSquared = Math.PI;

    public static final double kPXController = 1;
    public static final double kPYController = 1;
    public static final double kPThetaController = 1;

    public static final TrapezoidProfile.Constraints kThetaControllerConstraints = new TrapezoidProfile.Constraints(
        kMaxAngularSpeedRadiansPerSecond, kMaxAngularSpeedRadiansPerSecondSquared);
  }

  public static final class NeoMotorConstants {
    public static final double kFreeSpeedRpm = 5676;
  }
}
