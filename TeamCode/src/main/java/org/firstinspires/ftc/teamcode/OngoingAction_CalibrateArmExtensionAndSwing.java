package org.firstinspires.ftc.teamcode;

public class OngoingAction_CalibrateArmExtensionAndSwing extends AbstractOngoingAction
{
    public OngoingAction_CalibrateArmExtensionAndSwing(Robot robot)
    {
        super(robot);
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
        robot.armExtensionMotor.setPower(0);
        robot.armExtensionSafetyIsDisabled = false;
    }

    @Override
    public boolean isDone()
    {
        if(robot.armExtensionSpeed < 10)
        {
            // Once arm extension is retracted, calibrate the arm swing
            robot.startOngoingAction(new OngoingAction_CalibrateArmSwing(robot));
            return true;
        }
        else
            return false;
    }
}
