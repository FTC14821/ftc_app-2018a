package org.firstinspires.ftc.teamcode;

public class OngoingAction_CalibrateArmExtensionAndSwing extends AbstractOngoingAction
{
    public OngoingAction_CalibrateArmExtensionAndSwing(ActionTracker callingActionTracker, Robot robot)
    {
        super(callingActionTracker, robot, "CalibrateArmExtenstionAndSwing" ,null);
    }

    @Override
    public void start()
    {
        robot.armExtensionSafetyIsDisabled = true;
        robot.armExtensionMotor.setPower(-0.1);
    }

    @Override
    public void loop()
    {

    }

    @Override
    public void cleanup()
    {
        robot.setArmExtensionPower(actionTracker, 0);
        robot.armExtensionSafetyIsDisabled = false;

        super.cleanup();
    }

    @Override
    public boolean isDone()
    {
        if(robot.armExtensionSpeed < 10)
        {
            // Once arm extension is retracted, calibrate the arm swing
            robot.startOngoingAction(new OngoingAction_CalibrateArmSwing(actionTracker, robot));
            return true;
        }
        else
            return false;
    }
}
