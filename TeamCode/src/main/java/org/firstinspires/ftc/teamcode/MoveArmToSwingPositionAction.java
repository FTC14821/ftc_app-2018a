package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.scheduler.EndableAction;
import org.firstinspires.ftc.teamcode.scheduler.Utils;

public class MoveArmToSwingPositionAction extends EndableAction
{
    Robot robot = Robot.get();

    boolean stopWhenGreatherThan;
    private final int armEncoderPosition;

    public MoveArmToSwingPositionAction(int armEncoderPosition)
    {
        super("SwingArmToPosition", "SwingArmToPosition(%d)", armEncoderPosition);
        
        this.armEncoderPosition = armEncoderPosition;
    }

    @Override
    public boolean isUsingDcMotor(DcMotor motor)
    {
        if(motor == robot.armSwingMotor)
            return true;
        else
            return super.isUsingDcMotor(motor);
    }

    @Override
    public EndableAction start()
    {
        super.start();
        if (!robot.armSwingCalibrated)
        {
            abort("Arm swing must be calibrated");
            return this;
        }

        if(robot.armSwingMotor.getCurrentPosition()<armEncoderPosition)
        {
            stopWhenGreatherThan = true;
            robot.setSwingArmSpeed_raw(0.5);
        }
        else
        {
            stopWhenGreatherThan=false;
            robot.setSwingArmSpeed_raw(-0.5);
        }
        return this;
    }

    @Override
    protected void cleanup(boolean actionWasCompleted)
    {
        robot.setSwingArmSpeed_raw(0);
        super.cleanup(actionWasCompleted);
    }

    @Override
    public boolean isDone(StringBuilder statusMessage)
    {
        int currentPosition = robot.armSwingMotor.getCurrentPosition();

        statusMessage.append(Utils.safeStringFormat("Still to go: %d --> %d", currentPosition, armEncoderPosition));

        // We're done if something stopped the motor
        if(robot.armSwingMotor.getPower()==0)
            return true;

        if(Math.abs(currentPosition-armEncoderPosition) < Robot.MIN_SWING_ARM_DISTANCE)
            return true;

        if(stopWhenGreatherThan)
            return currentPosition >=armEncoderPosition;
        else
            return currentPosition <=armEncoderPosition;
    }
}
