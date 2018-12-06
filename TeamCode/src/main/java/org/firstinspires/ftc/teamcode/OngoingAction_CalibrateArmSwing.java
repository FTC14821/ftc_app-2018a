package org.firstinspires.ftc.teamcode;

public class OngoingAction_CalibrateArmSwing extends AbstractOngoingAction
{
    public OngoingAction_CalibrateArmSwing(Robot robot)
    {
        super(robot);
    }

    @Override
    public void start()
    {
        robot.armSwingSafetyIsDisabled = true;
        robot.setSwingArmPower_raw(-0.3, 1);
    }

    @Override
    public void loop()
    {

    }

    @Override
    public void cleanup()
    {
        robot.swingMotor.setPower(0);
        robot.armSwingSafetyIsDisabled = false;
    }

    @Override
    public boolean isDone()
    {
        if(robot.armSwingSpeed < 10)
            return true;
        else
            return false;
    }
}
