package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name = "T: encoderTest", group = "Tinkering")

public class encoderTest extends BaseOpMode {

    int startPosition, stopPosition;

    @Override
    public void init() {
        super.init();

        robot.setDrivingZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    @Override
    public void start() {
        super.start();
        startPosition = robot.getWheelPosition();
        stopPosition = startPosition + 4000;
    }

    @Override
    public void loop() {
        super.loop();

        int currentPosition = robot.getWheelPosition();

        // Go forward until robot hits the stop position
        if ( currentPosition < stopPosition )
        {
            robot.driveStraight(0.25);
        }
        else
        {
            robot.stop();
        }
    }
}
