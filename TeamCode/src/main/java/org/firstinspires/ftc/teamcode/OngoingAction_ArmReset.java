package org.firstinspires.ftc.teamcode;

public class OngoingAction_ArmReset extends AbstractOngoingAction
{
    public OngoingAction_ArmReset(Robot robot)
    {
        super(robot);
    }

    @Override
    public void start()
    {
        robot.setArmExtensionPower(-1);
    }

    @Override
    public void loop()
    {
        if ( robot.getArmSwingZone() == 3 )
        {
            robot.setSwingArmPower(-1);
        }
        else
        {
            if ( robot.armExtensionMotor.getPower() == 0 )
                robot.setSwingArmPower(-0.3);
            else
                robot.setSwingArmPower(0);
        }
    }

    @Override
    public void cleanup()
    {
        robot.armExtensionMotor.setPower(0);
        robot.swingMotor.setPower(0);
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
