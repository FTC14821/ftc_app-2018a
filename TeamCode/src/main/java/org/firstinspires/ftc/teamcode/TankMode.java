package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "T: TankMode", group = "Tinkering")

public class TankMode extends BaseOpMode {

    @Override
    public void loop() {
        super.loop();

        double powerLeft = -gamepad1.left_stick_y;
        double powerRight = -gamepad1.right_stick_y;

        robot.setDrivingPowers(powerLeft, powerRight);
    }
}
