package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "T: Testing", group = "Tinkering")

public class TestingOpMode extends TeleOpMode
{

    @Override
    public void teleOpLoop()
    {
        super.teleOpLoop();
        if(gamepad1.dpad_left.onPress)
        {
            robot.resetCorrectHeading("DPad: Turning left from current position");
            if(gamepad1.a.isPressed)
                robot.startTurningLeft(90, Robot.TURN_TYPE.PIVOT);
            else
                robot.startTurningLeft(90);
        }
        
        if(gamepad1.dpad_right.onPress)
        {
            robot.resetCorrectHeading("DPad: Turning right from current position");
            if(gamepad1.a.isPressed)
                robot.startTurningRight(90, Robot.TURN_TYPE.PIVOT);
            else
                robot.startTurningRight(90);
        }

        if(gamepad1.dpad_up.onPress)
            robot.startInchMove(12, 1);

        if(gamepad1.dpad_down.onPress)
            robot.startInchMoveBack(12, 1);
    }
}
