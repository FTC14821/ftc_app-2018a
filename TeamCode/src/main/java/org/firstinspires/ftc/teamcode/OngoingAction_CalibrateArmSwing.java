package org.firstinspires.ftc.teamcode;

public class OngoingAction_CalibrateArmSwing extends EndableAction
{
    public OngoingAction_CalibrateArmSwing()
    {
        super(10000, "CalibraterArmSwing", "CalibrateArmSwing");
        Robot.get().swingMotor.setPower(0.25);
    }

    @Override
    public boolean loop()
    {
        if ( Robot.get().armSwingFrontLimit.isPressed())
            return true;
        else
            return false;
    }

    @Override
    public void done()
    {
        super.done();
        Robot.get().swingMotor.setPower(0.0);
    }
}
