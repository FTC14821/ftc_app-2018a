package org.firstinspires.ftc.teamcode;

import org.firstinspires.ftc.teamcode.scheduler.EndableAction;

import static org.firstinspires.ftc.teamcode.scheduler.Utils.safeStringFormat;

public class OngoingAction_CalibrateArmSwing extends EndableAction
{
    Robot robot = Robot.get();
    public OngoingAction_CalibrateArmSwing()
    {
        super("CalibraterArmSwing");
    }

    @Override
    public EndableAction start()
    {
        super.start();
        robot.armSwingSafetyIsDisabled=true;
        robot.armSwingMotor.setPower(0.25);

        return this;
    }

    @Override
    public boolean isDone(StringBuilder newStatus)
    {
        newStatus.append(safeStringFormat("limit switch: %s", robot.armSwingFrontLimit.isPressed()));

        if ( robot.armSwingFrontLimit.isPressed())
            return true;
        else
            return false;
    }

    @Override
    public void cleanup(boolean actionWasCompleted)
    {
        robot.armSwingMotor.setPower(0.0);
        robot.armSwingSafetyIsDisabled=false;

        if (actionWasCompleted)
            robot.resetMotorEncoder("ArmSwing", robot.armSwingMotor);

        robot.armSwingCalibrated = actionWasCompleted;

        super.cleanup(actionWasCompleted);
    }
}
