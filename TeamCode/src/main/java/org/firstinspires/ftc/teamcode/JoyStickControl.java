package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import static java.lang.Math.abs;

@TeleOp(name = "T: JoyStickControl", group = "Tinkering")

public class JoyStickControl extends BaseOpMode {

    @Override
    public void start()
    {
        super.start();
    }

    @Override
    public void loop() {
        super.loop();

        double powerLeft = 0.0;
        double powerRight = 0.0;
        double power = -gamepad1.left_stick_y;


        //Joystick Control

        // Low power: Spin (opposite power to wheels)
        if(abs(power) < 0.1)
        {
            // left_stick_x: negative to left
            powerLeft = gamepad1.left_stick_x;
            powerRight = -powerLeft;
        }
        else if(gamepad1.left_stick_x > 0)
        {
            // Turning to right: powerLeft should be more than powerRight
            powerRight = power;
            powerLeft = power + 2*gamepad1.left_stick_x;
        }
        else
        {
            // Turning to left: powerLeft should be less than powerRight
            // (powerRight is bigger after subtraction because left_stick_x < 0)
            powerLeft = power;
            powerRight = power - 2*gamepad1.left_stick_x;
        }

        setLeftPower(powerLeft);
        setRightPower(powerRight);
    }
}