package org.firstinspires.ftc.teamcode;

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
    public EndableAction start()
    {
        super.start();
        robot.armExtensionSafetyIsDisabled = true;
        robot.armExtensionMotor.setPower(-0.1);
        return this;
    }


    @Override
    public boolean isDone(StringBuilder statusMessage)
    {
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
        cleanup(actionWasCompleted);
    }
}
