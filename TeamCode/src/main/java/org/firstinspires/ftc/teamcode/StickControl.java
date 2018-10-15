package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name = "T: StickControl", group = "Tinkering")

public class StickControl extends OpMode {
    DcMotor M0, M1;

    double power1;
    double power2;
    //double power3;
    //private Servo S;

    @Override
    public void init() {
        M0 = hardwareMap.dcMotor.get("M0");
        M1 = hardwareMap.dcMotor.get("M1");
        //S = hardwareMap.servo.get("S");
    }

    @Override
    public void start() {
        power1=0.0;
        power2=0.0;
        //power3 = 20.0;
    }

    @Override
    public void loop() {

        power1=0.0;
        power2=0.0;

        if(gamepad1.left_stick_y < 0){
            power1 = -gamepad1.left_stick_y;
            power2 = -gamepad1.left_stick_y;
        }
        if(gamepad1.left_stick_y > 0){
            power1 = -gamepad1.left_stick_y;
            power2 = -gamepad1.left_stick_y;
        }
        if(gamepad1.left_stick_x < 0){
            power1 = gamepad1.left_stick_x;
            power2 = -gamepad1.left_stick_x;
        }

        if(gamepad1.left_stick_x > 0) {
            power1 = gamepad1.left_stick_x;
            power2 = -gamepad1.left_stick_x;
        }


        telemetry.addLine(String.format("M0=%.2f M1=%.2f", power1, power2));
        telemetry.addLine(String.format("lt=%.2f rt=%.2f", gamepad1.right_trigger, gamepad1.left_trigger));
        telemetry.addLine(String.format("lx=%.2f ly=%.2f rx=%.2f ry=%.2f", gamepad1.left_stick_x, gamepad1.left_stick_y, gamepad1.right_stick_x, gamepad1.right_stick_y));
        M0.setPower(power1);
        M1.setPower(power2);
        //S.setPosition(power3);
    }
}
