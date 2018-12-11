package org.firstinspires.ftc.teamcode;

public class OngoingAction_CalibrateHook extends AbstractOngoingAction
{
    public OngoingAction_CalibrateHook(ActionTracker parentActionTracker, Robot robot)
    {
        super(parentActionTracker, robot, "CalibrateHook", null);
    }

    @Override
    public void start()
    {
        robot.hookSafetyIsDisabled = true;
        robot.setHookPower(actionTracker, -0.15);
    }

    @Override
    public void loop()
    {

    }

    @Override
    public void cleanup()
    {
        robot.setHookPower(actionTracker, 0);
        robot.hookSafetyIsDisabled = false;

        super.cleanup();
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
