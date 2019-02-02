package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.scheduler.EndableAction;

public class MoveArmToSwingPositionAction extends EndableAction
{
    private final int armEncoderPosition;
    DcMotor.RunMode originalMode = Robot.get().armSwingMotor.getMode();

    public MoveArmToSwingPositionAction(int armEncoderPosition)
    {
        super("SwingArmToPosition", "Swing arm to %d", armEncoderPosition);
        
        this.armEncoderPosition = armEncoderPosition;
    }


    @Override
    public EndableAction start()
    {
        super.start();
        if (!Robot.get().armSwingCalibrated)
        {
            abort("Arm swing must be calibrated");
            return this;
        }

        Robot.get().armSwingMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        Robot.get().armSwingMotor.setTargetPosition(armEncoderPosition);
        return this;
    }

    @Override
    protected void cleanup(boolean actionWasCompleted)
    {
        Robot.get().armSwingMotor.setPower(0);
        Robot.get().armSwingMotor.setMode(originalMode);
        super.cleanup(actionWasCompleted);
    }

    @Override
    public boolean isDone(StringBuilder statusMessage)
    {
        return Robot.get().armSwingMotor.isBusy();
    }
}
