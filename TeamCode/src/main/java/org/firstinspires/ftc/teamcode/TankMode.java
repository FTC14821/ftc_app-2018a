package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import static java.lang.Math.abs;

@TeleOp(name = "T: TankMode", group = "Tinkering")

public class TankMode extends BaseOpMode {

    double powerLeft;
    double powerRight;

    @Override
    public void start() {
        powerLeft = 0.0;
        powerRight = 0.0;
    }

    @Override
    public void loop() {
        super.loop();

        powerLeft = 0.0;
        powerRight = 0.0;

        powerLeft = -gamepad1.left_stick_y;
        powerRight = -gamepad1.right_stick_y;


        setLeftPower(powerLeft);
        setRightPower(powerRight);
    }
}
