package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name = "T: Testing", group = "Tinkering")

public class TestingOpMode extends TeleOpMode
{

    @Override
    public void teleOpLoop()
    {
        super.teleOpLoop();
        int lCurrentPosition = robot.getLeftMotor().getCurrentPosition();
        int rCurrentPosition = robot.getRightMotor().getCurrentPosition();
        int clicksPerFoot = (int)(79.27 * 12);

        if(gamepad1.dpad_left && !gamepad1.a)
        {
            robot.resetCorrectHeading(gamepad1Action, "DPad: Turning left from current position");
            if(gamepad1.a)
            {
                robot.pivotTurnLeft(gamepad1Action, 90);
            }
            else
            {
                robot.turnLeft(gamepad1Action, 90);
            }

            //Wait until button is released
            while(shouldOpModeKeepRunning(gamepad1Action) && gamepad1.dpad_left)
            {}
        }
        
        if(gamepad1.dpad_right)
        {
            robot.resetCorrectHeading(gamepad1Action, "DPad: Turning right from current position");
            if(gamepad1.a)
                robot.pivotTurnRight(gamepad1Action, 90);
            else
                robot.turnRight(gamepad1Action, 90);

            //Wait until button is released
            while(shouldOpModeKeepRunning(gamepad1Action) && gamepad1.dpad_right)
            {}
        }

        if(gamepad1.dpad_up)
        {
            robot.inchmove(gamepad1Action, 12, 1);
            //Wait until button is released
            while(shouldOpModeKeepRunning(gamepad1Action) && gamepad1.dpad_up)
            {}
        }
        if(gamepad1.dpad_down)
        {
            robot.inchmoveBack(gamepad1Action, 12, 1);
            //Wait until button is released
            while(shouldOpModeKeepRunning(gamepad1Action) && gamepad1.dpad_down)
            {}
        }
    }
}
