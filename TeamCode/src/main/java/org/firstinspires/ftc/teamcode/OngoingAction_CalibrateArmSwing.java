package org.firstinspires.ftc.teamcode;

public class OngoingAction_CalibrateArmSwing extends AbstractOngoingAction
{
    public OngoingAction_CalibrateArmSwing(ActionTracker callingActionTracker, Robot robot)
    {
        super(callingActionTracker, robot, "CalibrateArmSwing" , null);
    }

    @Override
    public void start()
    {
        robot.armSwingSafetyIsDisabled = true;
        robot.setSwingArmPower_raw(null, -0.3, 1, "DirectArmSwingControl-Calibration");
    }

    @Override
    public void loop()
    {

    }

    @Override
    public void cleanup()
    {
        robot.setSwingArmSpeed(actionTracker, 0);
        robot.armSwingSafetyIsDisabled = false;

        super.cleanup();
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
