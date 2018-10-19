package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import static java.lang.Math.abs;

@TeleOp(name = "T: JoyStickControl", group = "Tinkering")

public class JoyStickControl extends OpMode {
    DcMotor M0, M1;

    double powerLeft;
    double powerRight;
    double power;

    @Override
    public void init() {
        M0 = hardwareMap.dcMotor.get("M0");
        M1 = hardwareMap.dcMotor.get("M1");
    }

    @Override
    public void start() {
        powerLeft = 0.0;
        powerRight = 0.0;
        power = 0.0;
    }

    @Override
    public void loop() {

        powerLeft = 0.0;
        powerRight = 0.0;
        power = -gamepad1.left_stick_y;


        //Joystick Control
        if(abs(power) < 0.1) {
            powerLeft = gamepad1.left_stick_x;
            powerRight = -powerLeft;
        }
        else if(gamepad1.left_stick_x < 0){
            powerRight = power;
            powerLeft = power + 2*gamepad1.left_stick_x;
        }
        else{
            powerLeft = power;
            powerRight = power - 2*gamepad1.left_stick_x;
        }




        telemetry.addLine(String.format("M0=%.2f M1=%.2f", powerLeft, powerRight));
        telemetry.addLine(String.format("lt=%.2f rt=%.2f", gamepad1.right_trigger, gamepad1.left_trigger));
        telemetry.addLine(String.format("lx=%.2f ly=%.2f rx=%.2f ry=%.2f", gamepad1.left_stick_x, gamepad1.left_stick_y, gamepad1.right_stick_x, gamepad1.right_stick_y));
        M0.setPower(powerLeft);
        M1.setPower(-powerRight);
    }
}