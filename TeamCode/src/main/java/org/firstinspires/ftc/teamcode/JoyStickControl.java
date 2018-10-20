package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import static java.lang.Math.abs;

@TeleOp(name = "T: JoyStickControl", group = "Tinkering")

public class JoyStickControl extends BaseOpMode {

    @Override
    public void start()
    {

    }

    @Override
    public void loop() {
        super.loop();

        double powerLeft = 0.0;
        double powerRight = 0.0;
        double power = -gamepad1.left_stick_y;


        //Joystick Control
        if(abs(power) < 0.1)
        {
            powerLeft = gamepad1.left_stick_x;
            powerRight = -powerLeft;
        }
        else if(gamepad1.left_stick_x < 0)
        {
            powerRight = power;
            powerLeft = power + 2*gamepad1.left_stick_x;
        }
        else
        {
            powerLeft = power;
            powerRight = power - 2*gamepad1.left_stick_x;
        }

        setLeftPower(powerLeft);
        setRightPower(powerRight);
        armExtensionMotor.setPower(gamepad2.left_stick_y);
        grabberServo.setPosition(-gamepad2.right_stick_x);

    }
}