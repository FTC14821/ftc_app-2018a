package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcore.external.Func;

public abstract class BaseLinearOpMode extends LinearOpMode {
    Robot robot;

    // Info of what robot is doing
    String phase="", step="", operation="", status="";


    void teamInit()
    {
        telemetry.addLine("Info:")
            .addData("Phase", new Func<String>() {
                @Override
                public String value() {
                    return phase;
                }})
            .addData("Step", new Func<String>() {
                @Override
                public String value() {
                    return step;
                }})
            .addData("Op", new Func<String>() {
                @Override
                public String value() {
                    return operation;
                }})
            ;

        telemetry.addLine()
                .addData("Status", new Func<String>() {
                    @Override
                    public String value() {
                        return status;
                    }})
            ;

    }

    abstract void teamRun();


    @Override
    public void runOpMode() throws InterruptedException {
        robot = new Robot(hardwareMap, telemetry);

        teamInit();

        waitForStart();
        teamRun();
    }


    public void teamSleep(long sleep_ms)
    {
        long startTime_ms = System.currentTimeMillis();
        long stopTime_ms = startTime_ms + sleep_ms;

        while ( opModeIsActive() && System.currentTimeMillis() < stopTime_ms )
        {
            teamIdle();
            sleep(1);
        }
    }


    public void teamIdle()
    {
        telemetry.update();
        Thread.yield();
    }


    public void setPhase(String phase) {
        this.phase = phase;
        setStep("");
    }

    public void setStep(String step) {
        this.step = step;
        setOperation("");
    }

    public void setOperation(String operation) {
        this.operation = operation;
        setStatus("");
    }

    public void setStatus(String status) {
        this.status = status;
    }


    void inchmove(double inches, double power)
    {
        setOperation(String.format("InchMove(%.1f, %.1f", inches, power));

        double encoderClicksPerInch = 79.27;
        double encoderClicks = inches*encoderClicksPerInch;
        double stopPosition = robot.getWheelPosition() + encoderClicks;

        double startingHeading = robot.getHeading();

        while (opModeIsActive() && robot.getWheelPosition() <= stopPosition)
        {
            //Heading is larger to the left
            double currentHeading = robot.getHeading();
            double headingError = startingHeading - currentHeading;

            setStatus(String.format("%.1f inches to go. Heading error: %.1f degrees",
                    (stopPosition-robot.getWheelPosition()) / encoderClicksPerInch,
                    headingError));


            if(headingError > 0.0)
            {
                //The current heading is too big so we turn to the left
                robot.setPowerSteering(power, -0.05);
            }
            else if(headingError < 0.0)
            {
                //Current heading is too small, so we steer to the right
                robot.setPowerSteering(power,  0.05);
            }
            else
            {
                // Go Straight
                robot.driveStraight(power);
            }

            teamIdle();
        }
        setStatus("Done");

    }


}
