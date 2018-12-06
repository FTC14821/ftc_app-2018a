package org.firstinspires.ftc.teamcode;

public class OngoingAction_CalibrateHook extends AbstractOngoingAction
{
    public OngoingAction_CalibrateHook(Robot robot)
    {
        super(robot);
    }

    @Override
    public void start()
    {
        robot.hookSafetyIsDisabled = true;
        robot.setHookPower(-0.15);
    }

    @Override
    public void loop()
    {

    }

    @Override
    public void cleanup()
    {
        robot.hookMotor.setPower(0);
        robot.hookSafetyIsDisabled = false;
    }

    @Override
    public boolean isDone()
    {
        if(robot.hookSpeed < 10)
            return true;
        else
            return false;
    }
}
