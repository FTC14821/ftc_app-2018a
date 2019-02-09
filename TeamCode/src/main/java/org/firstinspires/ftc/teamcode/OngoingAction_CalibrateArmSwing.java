package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.scheduler.EndableAction;

import static org.firstinspires.ftc.teamcode.scheduler.Utils.safeStringFormat;

public class OngoingAction_CalibrateArmSwing extends EndableAction
{
    Robot robot = Robot.get();
    public OngoingAction_CalibrateArmSwing()
    {
        super("CalibraterArmSwing");
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
        if(robot.armSwingFrontLimit.isPressed())
            log("Arm is already on front limit switch");
        else
        {
            robot.armSwingSafetyIsDisabled = true;
            robot.setSwingArmSpeed_raw(-0.5);
        }

        return this;
    }

    @Override
    public boolean isDone(StringBuilder newStatus)
    {
        newStatus.append(safeStringFormat("limit switch: %s", robot.armSwingFrontLimit.isPressed()));

        if(robot.armSwingFrontLimit.isPressed())
            return true;
        else
            return false;
    }

    @Override
    public void cleanup(boolean actionWasCompleted)
    {
        robot.setSwingArmSpeed_raw(0.0);
        robot.armSwingSafetyIsDisabled=false;

        if (actionWasCompleted)
            robot.resetMotorEncoder("ArmSwing", robot.armSwingMotor);

        robot.armSwingCalibrated = actionWasCompleted;

        super.cleanup(actionWasCompleted);
    }
}
