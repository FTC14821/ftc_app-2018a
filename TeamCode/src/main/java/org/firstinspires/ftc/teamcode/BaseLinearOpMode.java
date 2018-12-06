package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Func;

public abstract class BaseLinearOpMode extends LinearOpMode {
    Robot robot;

    // Info of what robot is doing
    String phase="", step="", operation="", status="";


    // First thing to init
    final void baseInit()
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

        setStep("ContructingRobot");
        robot = new Robot(this, hardwareMap, telemetry);
        robot.init();
        setStatus("Robot constructed");
    }

    // Where OpModes can initialize themselves
    void teamInit()
    {
    }

    // What happens after Play is pressed
    abstract void teamRun();


    @Override
    public void runOpMode() throws InterruptedException {
        setPhase("Initializing");
        baseInit();
        teamInit();

        setPhase("Waiting for start");

        while(!isStarted())
            teamIdle();

        setPhase("Running");
        teamRun();

        setPhase("OpMode has finished");
    }


    public void teamSleep(long sleep_ms)
    {
        teamSleep(sleep_ms, "");
    }

    public void teamSleep(long sleep_ms, String reason)
    {
        setOperation(String.format("Sleep(%d ms, '%s')", sleep_ms, reason));

        long startTime_ms = System.currentTimeMillis();
        long stopTime_ms = startTime_ms + sleep_ms;

        while(shouldOpModeKeepRunning() && System.currentTimeMillis() < stopTime_ms)
        {
            setStatus(String.format("nap time (for '%s') left: %d sec",
                    reason,
                    (stopTime_ms - System.currentTimeMillis())/1000));

            teamIdle();
        }
        setStatus(String.format("nap (for '%s') is over", reason));
    }


    public void teamIdle()
    {
        if(robot != null)
            robot.loop();

        telemetry.update();
        idle();
    }


    public void setPhase(String phase) {
        this.phase = phase;
        RobotLog.ww("team14821", "Staring Phase: %s", phase);
        setStep("");
        telemetry.update();
    }

    public void setStep(String step) {
        this.step = step;
        RobotLog.ww("team14821", "Starting Step: %s", step);
        setOperation("");
        telemetry.update();
    }

    public void setOperation(String operation) {
        this.operation = operation;
        RobotLog.ww("team14821","Starting Operation: %s", operation);
        setStatus("");
        telemetry.update();
    }

    public void setStatus(String status) {
        this.status = status;
        RobotLog.ww("team14821", "Status: %s", status);
        telemetry.update();
    }

    // Team's version of opModeIsActive
    // Checks robot health

    public boolean shouldOpModeKeepRunning()
    {
        teamIdle();
        return ! isStarted() || opModeIsActive();
    }
}
