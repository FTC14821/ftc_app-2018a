package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "T: TankMode", group = "Tinkering")

public class TankMode extends TeleOpMode
{

    @Override
    public void teleOpLoop() {
        super.teleOpLoop();

        double powerLeft = -gamepad1.left_stick_y;
        double powerRight = -gamepad1.right_stick_y;

        if(gamepad1.left_trigger > 0)
        {
            gamepad1Action.setStatus("GP1.left_trigger ==> half power");
            powerLeft /= 2;
            powerRight /= 2;
        }

        robot.setDrivingPowers(gamepad1Action, powerLeft, powerRight);
    }
}
