package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name = "T: OurFirstOpMode", group = "Tinkering")

public class OurFirstOpMode extends OpMode {
    DcMotor M0, M1;

    double power1;
    double power2;

    @Override
    public void init() {
        M0 = hardwareMap.dcMotor.get("M0");
        M1 = hardwareMap.dcMotor.get("M1");
    }

    @Override
    public void start() {
        power1=0.0;
        power2=0.0;
    }

    @Override
    public void loop() {

        power1=0.0;
        power2=0.0;

        /*
        if ( gamepad1.left_bumper ) {
            power = Math.max(0, power - 0.1);
        }
        if ( gamepad1.right_bumper ) {
            power = Math.min(1.0, power +  0.1);
        }
        */
        if(gamepad1.left_bumper){
            power1 = 1;
        }

        if(gamepad1.right_bumper){
            power2 = 1;
        }

        if(gamepad1.left_trigger > 0){
            power1 = -1;
        }

        if(gamepad1.right_trigger > 0){
            power2 = -1;
        }



        telemetry.addLine(String.format("M0=%.2f M1=%.2f", power1, power2));
        telemetry.addLine(String.format("lt=%.2f rt=%.2f", gamepad1.right_trigger, gamepad1.left_trigger));
        M0.setPower(power1);
        M1.setPower(power2);
    }
}
