package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name = "T: MotorControllerTest", group = "Tinkering")

public class MotorControllerTest extends TeleOpMode
{

    @Override
    public void teleOpLoop()
    {
        super.teleOpLoop();
        int lCurrentPosition = robot.getLeftMotor().getCurrentPosition();
        int rCurrentPosition = robot.getRightMotor().getCurrentPosition();
        int clicksPerFoot = (int)(79.27 * 12);

        if(gamepad1.dpad_left)
        {
            robot.resetCorrectHeading(gamepad1Action, "DPad: Turning left from current position");
            robot.turnLeft(gamepad1Action,90, 3);

            //Wait until button is released before switching front and back
            while(shouldOpModeKeepRunning(gamepad1Action) && gamepad1.dpad_left)
            {}
        }
        
        if(gamepad1.dpad_right)
        {
            robot.resetCorrectHeading(gamepad1Action, "DPad: Turning right from current position");
            if (gamepad1.a)
                robot.turnRight(gamepad1Action, 180, 3);
            else
                robot.turnRight(gamepad1Action, 90, 3);

            //Wait until button is released before switching front and back
            while(shouldOpModeKeepRunning(gamepad1Action) && gamepad1.dpad_right)
            {}
        }

        if(gamepad1.dpad_up)
        {
            int oneFoot = (int)(Robot.ENCODER_CLICKS_PER_INCH / 50);
            robot.getLeftMotor().setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.getLeftMotor().setTargetPosition(lCurrentPosition + clicksPerFoot);
            robot.getLeftMotor().setPower(0.5);
            robot.getRightMotor().setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.getRightMotor().setTargetPosition(rCurrentPosition + clicksPerFoot);
            robot.getRightMotor().setPower(0.5);

            //Wait until button is released before switching front and back
            while(shouldOpModeKeepRunning(gamepad1Action) && gamepad1.dpad_up)
            {}
        }
        if(gamepad1.dpad_down)
        {
            // Reset values and Spin for 10k clicks
            robot.resetCorrectHeading( gamepad1Action, "Measuring turning for 10k clicks");
            robot.resetDrivingEncoders(gamepad1Action, "Measuring turning for 10k clicks");

            robot.getLeftMotor().setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.getLeftMotor().setTargetPosition(lCurrentPosition + 10000);
            robot.getLeftMotor().setPower(0.5);
            robot.getRightMotor().setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.getRightMotor().setTargetPosition(rCurrentPosition - 10000);
            robot.getRightMotor().setPower(-0.5);

            //Wait until robot has reached target positions
            while((robot.getLeftMotor().isBusy() || robot.getRightMotor().isBusy()) && shouldOpModeKeepRunning(gamepad1Action))
            {
            }
            robot.stop(gamepad1Action, true);

            //Wait until button is released before switching front and back
            while(shouldOpModeKeepRunning(gamepad1Action) && gamepad1.dpad_up)
            {}
        }
    }
}
