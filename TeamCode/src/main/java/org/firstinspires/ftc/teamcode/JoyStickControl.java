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

        double power = -gamepad1.left_stick_y;
        double steering = gamepad1.left_stick_x;


        //Joystick Control
        // Low power: Spin (opposite power to wheels)
        if(abs(power) < 0.1)
        {
            // left_stick_x: negative to left
            setLeftPower(steering);
            setRightPower(-steering);
        }
        else
        {
            setPowerSteering(power, steering);
        }
    }
}