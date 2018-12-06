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

        robot.setDrivingZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    @Override
    void teamRun()
    {
        startPosition = robot.getWheelPosition();
        stopPosition = startPosition + 4000;

        robot.driveStraight(0.25);
        while ( shouldOpModeKeepRunning() && robot.getWheelPosition() < stopPosition )
            teamIdle();

        robot.stop(true);

        // Keep telemetry going for a while
        teamSleep(15000, "Show telemetry");
    }
}
