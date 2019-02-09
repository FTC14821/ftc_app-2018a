package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.scheduler.EndableAction;
import org.firstinspires.ftc.teamcode.scheduler.Utils;

public class MoveArmToExtensionPositionAction extends EndableAction
{
    Robot robot = Robot.get();
    boolean stopWhenGreaterThan;

    private final int armEncoderPosition;

    public MoveArmToExtensionPositionAction(int armEncoderPosition)
    {
        super("ArmExtensionToPosition", "Extend arm to %d", armEncoderPosition);
        
        this.armEncoderPosition = armEncoderPosition;
    }

    @Override
    public boolean isUsingDcMotor(DcMotor motor)
    {
        if(motor == robot.armExtensionMotor)
            return true;
        else
            return super.isUsingDcMotor(motor);
    }

    @Override
    public EndableAction start()
    {
        super.start();
        if (!robot.armExtensionCalibrated)
        {
            abort("Arm extension must be calibrated");
            return this;
        }

        double power;
        if(robot.armExtensionMotor.getCurrentPosition()<armEncoderPosition)
        {
            stopWhenGreaterThan = true;
            power=0.5;
        }
        else
        {
            stopWhenGreaterThan = false;
            power=-0.5;
        }
        log("Setting arm-extension power to %+.1f to move from %d to %d",
                power,robot.armExtensionMotor.getCurrentPosition(), armEncoderPosition);
        robot.setArmExtensionPower_raw(power);
        return this;
    }

    @Override
    protected void cleanup(boolean actionWasCompleted)
    {
        robot.setArmExtensionPower_raw(0);
        super.cleanup(actionWasCompleted);
    }

    @Override
    public boolean isDone(StringBuilder statusMessage)
    {
        int currentPosition = robot.armExtensionMotor.getCurrentPosition();

        statusMessage.append(Utils.safeStringFormat("Still to go: %d -->> %d", currentPosition, armEncoderPosition));

        if(Math.abs(currentPosition-armEncoderPosition) < Robot.MIN_ARM_EXTENSION_DISTANCE)
        {
            statusMessage.append("(Close enough to stop)");
            return true;
        }
        // We're done if something stopped the motor
        if(robot.armSwingMotor.getPower()==0)
        {
            statusMessage.append("(Stopping because power is 0)");
            return true;
        }

        if(stopWhenGreaterThan)
            return currentPosition >=armEncoderPosition;
        else
            return currentPosition <=armEncoderPosition;
    }
}
