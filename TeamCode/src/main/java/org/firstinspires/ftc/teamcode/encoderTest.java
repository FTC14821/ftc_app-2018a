package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name = "T: encoderTest", group = "Tinkering")

public class encoderTest extends BaseOpMode {

    int startPosition, stopPosition;

    @Override
    public void init() {
        super.init();

        M0.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        M1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    @Override
    public void start() {
        super.start();
        startPosition = M1.getCurrentPosition();
        stopPosition = startPosition + 4000;
    }

    @Override
    public void loop() {
        super.loop();

        int currentPosition = M1.getCurrentPosition();

        // Go forward until robot hits the stop position
        if ( currentPosition < stopPosition )
        {
            setLeftPower(0.25);
            setRightPower(0.25);
        }
        else
        {
            setLeftPower(0);
            setRightPower(0);
        }
    }
}
