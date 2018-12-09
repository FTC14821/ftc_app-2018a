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
            while(shouldOpModeKeepRunning() && gamepad1.dpad_up)
                teamIdle();
        }
    }
}
