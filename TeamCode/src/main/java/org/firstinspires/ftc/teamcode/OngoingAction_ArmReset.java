package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.scheduler.EndableAction;

public class OngoingAction_ArmReset extends EndableAction
{
    Robot robot = Robot.get();

    public OngoingAction_ArmReset()
    {
        super("ArmReset" );
    }

    @Override
    public boolean isUsingDcMotor(DcMotor motor)
    {
        if(motor == robot.armSwingMotor || motor == robot.armExtensionMotor)
            return true;
        else
            return super.isUsingDcMotor(motor);
    }


    @Override
    public EndableAction start()
    {
        new OngoingAction_CalibrateArmSwing().start();
        new OngoingAction_CalibrateArmExtension().start();
        return this;
    }

    @Override
    protected void cleanup(boolean actionWasCompletedsSuccessfully)
    {
        super.cleanup(actionWasCompletedsSuccessfully);
    }

    @Override
    public boolean isDone(StringBuilder newStatus)
    {
        return areChildrenDone(newStatus);
    }
}
