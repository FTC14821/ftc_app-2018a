package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name = "T: MotorControllerTest", group = "Tinkering")
@Disabled
public class MotorControllerTest extends TeleOpMode
{

    @Override
    public void teleOpLoop()
    {
        super.teleOpLoop();
        int lCurrentPosition = robot.getLeftMotor().getCurrentPosition();
        int rCurrentPosition = robot.getRightMotor().getCurrentPosition();
        int clicksPerFoot = (int)(79.27 * 12);

        if (gamepad1.dpad_left.onPress)
        {
            robot.setDrivingMotorMode(DcMotor.RunMode.RUN_USING_ENCODER);

            robot.getLeftMotor().setPower(-Math.max(gamepad1.right_trigger,0.05));
            robot.getRightMotor().setPower(+Math.max(gamepad1.right_trigger,0.05));

            robot.setDrivingMotorMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            robot.startStopping().waitUntilFinished();
        }

        if(gamepad1.dpad_right.onPress)
        {
            robot.resetCorrectHeading("DPad: Turning right from current position");
            if (gamepad1.a.onPress)
                robot.startTurningRight(180);
            else
                robot.startTurningRight(90);
        }

        if(gamepad1.dpad_up.onPress)
        {
            robot.getLeftMotor().setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.getLeftMotor().setTargetPosition(lCurrentPosition + clicksPerFoot);
            robot.getLeftMotor().setPower(0.5);
            robot.getRightMotor().setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.getRightMotor().setTargetPosition(rCurrentPosition + clicksPerFoot);
            robot.getRightMotor().setPower(0.5);
        }
        if(gamepad1.dpad_down.onPress)
        {
            // Reset values and Spin for 10k clicks
            robot.resetCorrectHeading( "Measuring turning for 10k clicks");
            robot.resetDrivingEncoders("Measuring turning for 10k clicks");

            robot.setDrivingMotorMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.getLeftMotor().setTargetPosition(lCurrentPosition + 10000);
            robot.getLeftMotor().setPower(0.5);
            robot.getRightMotor().setTargetPosition(rCurrentPosition - 10000);
            robot.getRightMotor().setPower(-0.5);

            //Wait until robot has reached target positions
            while((robot.getLeftMotor().isBusy() || robot.getRightMotor().isBusy()) && shouldOpModeKeepRunning())
            {
            }
            robot.setDrivingMotorMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            robot.startStopping().waitUntilFinished();
        }
    }
}
