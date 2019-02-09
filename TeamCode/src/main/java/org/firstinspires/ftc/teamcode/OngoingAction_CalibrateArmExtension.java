package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.scheduler.EndableAction;

import static org.firstinspires.ftc.teamcode.scheduler.Utils.safeStringFormat;

public class OngoingAction_CalibrateArmExtension extends EndableAction
{
    Robot robot = Robot.get();

    public OngoingAction_CalibrateArmExtension()
    {
        super("CalibrateArmExtenstion");
    }


    @Override
    public boolean isUsingDcMotor(DcMotor motor)
    {
        if(motor == robot.armExtensionMotor)
            return true;
        else
            return super.isUsingDcMotor(motor);
    }


    @Override
    public EndableAction start()
    {
        super.start();

        // Make sure the robot arm-spin is not in the way of the extension
        if ( robot.armSpinServo.getPosition() < robot.ARM_SPIN_INITIALIZE_LOCATION )
            robot.setArmSpinServoPosition_raw(robot.ARM_SPIN_INITIALIZE_LOCATION);

        robot.armExtensionSafetyIsDisabled = true;
        robot.setArmExtensionPower_raw(-0.25);
        return this;
    }


    @Override
    public boolean isDone(StringBuilder statusMessage)
    {
        if ( getAge_ms() < 200 )
            return false;

        statusMessage.append(safeStringFormat("ArmExtension speed=%d", robot.armExtensionSpeed));
        return robot.armExtensionSpeed>-5;
    }

    @Override
    protected void cleanup(boolean actionWasCompleted)
    {
        robot.setArmExtensionPower_raw(0);
        robot.armExtensionSafetyIsDisabled=false;

        if (actionWasCompleted)
        {
            robot.resetMotorEncoder("ArmExtensiopn", robot.armExtensionMotor);
            robot.armExtensionCalibrated=true;
        }
        super.cleanup(actionWasCompleted);
    }
}
