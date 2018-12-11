package org.firstinspires.ftc.teamcode;

public class OngoingAction_ArmReset extends AbstractOngoingAction
{
    public OngoingAction_ArmReset(ActionTracker callingActionTracker, Robot robot)
    {
        super(callingActionTracker, robot, "ArmReset" ,null );
    }

    @Override
    public void start()
    {
        robot.setArmExtensionPower(actionTracker, -1);
    }

    @Override
    public void loop()
    {
        if ( robot.getArmSwingZone() == 3 )
        {
            robot.setSwingArmPower(actionTracker,-1);
        }
        else
        {
            if ( robot.armExtensionMotor.getPower() == 0 )
                robot.setSwingArmPower(actionTracker, -0.3);
            else
                robot.setSwingArmPower(actionTracker, 0);
        }
    }

    @Override
    public void cleanup()
    {
        robot.armExtensionMotor.setPower(0);
        robot.swingMotor.setPower(0);
        super.cleanup();
    }

    @Override
    public boolean isDone()
    {
        if (robot.armExtensionMotor.getPower() == 0 && robot.swingMotor.getPower() == 0)
            return true;
        else
            return false;
    }
}
