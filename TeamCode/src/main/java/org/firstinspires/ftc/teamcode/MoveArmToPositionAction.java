package org.firstinspires.ftc.teamcode;

public class MoveArmToPositionAction extends EndableAction
{

    private final int armEncoderPosition;

    public MoveArmToPositionAction(long timeLimit_ms, int armEncoderPosition)
    {
        super(timeLimit_ms, "SwingArmToPosition", "Swing arm to %d", armEncoderPosition);
        
        this.armEncoderPosition = armEncoderPosition;
    }

    @Override
    public boolean loop()
    {
        int currentPosition = Robot.get().swingMotor.getCurrentPosition();
        if(Math.abs(currentPosition - armEncoderPosition)<= 100)
        {
            return true;
        }

        if(currentPosition > armEncoderPosition)
        {
            Robot.get().swingMotor.setPower(-0.25);
        } else {
            Robot.get().swingMotor.setPower(0.25);
        }

        return false;
    }

    @Override
    public void done()
    {
        super.done();
        Robot.get().swingMotor.setPower(0.0);
    }
}
