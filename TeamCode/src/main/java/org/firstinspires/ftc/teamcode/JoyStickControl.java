package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import static java.lang.Math.abs;

@TeleOp(name = "T: JoyStickControl", group = "Tinkering")
@Disabled
public class JoyStickControl extends TeleOpMode
{
    @Override
    public void teleOpLoop() {
        super.teleOpLoop();

        // Forward is positive
        double power = -gamepad1.left_stick_y;

        // Left is negative
        double steering = gamepad1.left_stick_x;

        if(gamepad1.left_trigger > 0)
            power /= 2;

        //Joystick Control
        if ( power == 0 && steering == 0 )
        {
            robot.stopWithoutBraking();
        }
        else if(abs(power) < 0.1)
        {
            // Low power: Spin (opposite power to wheels)

            // left_stick_x: negative to left which matches robot.spin
            robot.spin(steering);
        }
        else if (steering == 0 )
        {
            robot.driveStraight(power);
        }
        else
        {
            robot.setPowerSteering(power, steering);
        }
    }
}