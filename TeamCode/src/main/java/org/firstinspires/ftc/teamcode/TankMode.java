package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import static java.lang.Math.abs;

@TeleOp(name = "T: TankMode", group = "Tinkering")

public class TankMode extends BaseOpMode {

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void loop() {
        super.loop();

        double powerLeft = -gamepad1.left_stick_y;
        double powerRight = -gamepad1.right_stick_y;

        setRightPower(powerRight);
        setLeftPower(powerLeft);
    }
}
