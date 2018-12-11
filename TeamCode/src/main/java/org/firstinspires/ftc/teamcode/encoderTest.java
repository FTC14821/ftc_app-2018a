package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name = "T: encoderTest", group = "Tinkering")

public class encoderTest extends AutonomousOpMode
{

    int startPosition, stopPosition;

    @Override
    public void teamInit() {
        super.init();

        robot.setDrivingZeroPowerBehavior(opmodeAction, DcMotor.ZeroPowerBehavior.BRAKE);
    }

    @Override
    void teamRun()
    {
        startPosition = robot.getWheelPosition();
        stopPosition = startPosition + 4000;

        robot.driveStraight(opmodeAction, 0.25);
        while ( shouldOpModeKeepRunning(opmodeAction) && robot.getWheelPosition() < stopPosition )
        {}

        robot.stop(opmodeAction, true);

        // Keep telemetry going for a while
        teamSleep(opmodeAction, 15000, "Show telemetry");
    }
}
