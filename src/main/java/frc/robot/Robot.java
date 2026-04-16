package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.CommandScheduler;

public class Robot extends TimedRobot {
  private final RobotContainer m_robotContainer;



  public Robot() {
    m_robotContainer = new RobotContainer();

  }

  @Override
  public void robotPeriodic() {
  CommandScheduler.getInstance().run();
  SmartDashboard.putNumber("Front Left Max", m_robotContainer.m_robotDrive.getFrontLeftTurningSetpoint());
  SmartDashboard.putNumber("Front Right Max", m_robotContainer.m_robotDrive.getFrontRightTurningSetpoint());
  SmartDashboard.putNumber("Back Left Max", m_robotContainer.m_robotDrive.getRearLeftTurningSetpoint());
  SmartDashboard.putNumber("Back Right Max", m_robotContainer.m_robotDrive.getRearRightTurningSetpoint());


  }
  @Override
  public void disabledInit() {}

  @Override
  public void disabledPeriodic() {}

  @Override
  public void disabledExit() {}

  @Override
  public void autonomousInit() {}

  @Override
  public void autonomousPeriodic() {}

  @Override
  public void autonomousExit() {}

  @Override
  public void teleopInit() {}

  @Override
  public void teleopPeriodic() {}

  @Override
  public void teleopExit() {}

  @Override
  public void testInit() {
    CommandScheduler.getInstance().cancelAll();
  }
  @Override
  public void testPeriodic() {}

  @Override
  public void testExit() {}
}
