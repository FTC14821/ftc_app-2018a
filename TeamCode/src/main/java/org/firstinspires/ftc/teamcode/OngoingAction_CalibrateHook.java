package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.scheduler.EndableAction;

import static org.firstinspires.ftc.teamcode.scheduler.Utils.safeStringFormat;

public class OngoingAction_CalibrateHook extends EndableAction
{
    Robot robot = Robot.get();

    public OngoingAction_CalibrateHook()
    {
        super("CalibrateHook");
    }

    @Override
    public boolean isUsingDcMotor(DcMotor motor)
    {
        if(motor == robot.hookMotor)
            return true;
        else
            return super.isUsingDcMotor(motor);
    }

    @Override
    public EndableAction start()
    {
        super.start();
        robot.hookSafetyIsDisabled=true;
        robot.setHookPower_raw(-0.25);
        return this;
    }

    @Override
    protected void cleanup(boolean actionWasCompleted)
    {
        robot.hookSafetyIsDisabled=false;
        robot.setHookPower_raw(0);

        if (actionWasCompleted)
        {
            robot.resetMotorEncoder("Hook", robot.hookMotor);
            robot.hookCalibrated = true;
        }
        super.cleanup(actionWasCompleted);
    }

    @Override
    public boolean isDone(StringBuilder statusMessage)
    {
        // the hook motor will stop when the hook reaches the bottom
        statusMessage.append(safeStringFormat("Hook speed is %d", robot.hookSpeed));
        return ( robot.hookSpeed > -5 );
    }
}
