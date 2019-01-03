package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Func;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class BaseLinearOpMode extends LinearOpMode {
    private static final long TELEMETRY_LOGGING_INTERVAL_MS = 260;
    Robot robot;

    // Info of what robot is doing
    String phase="", step="", operation="", status="";

    // Used to log telemtry to RobotLog (in addition to driver station)
    Map<String, String> latestTelemetryData = new HashMap<>();
    long lastTelemetryLoggingTime_ms = 0;


    long lastIdleTime_ms =0;

    // How fast are we calling teamIdle... ObservationTime (ms time) --> Idle-Time-Interval (ms)
    // This keeps the delays between the loops for the 1/2 second
    LinkedHashMap<Long, Long> loopIntervalHistory_ms = new LinkedHashMap<Long, Long>()
    {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long,Long>  eldest)
        {
            long age_ms = System.currentTimeMillis() - eldest.getKey();

            return age_ms > 500;
        }
    };


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

        telemetry.addLine()
                .addData("LoopsPerSec", new Func<String>() {
                    @Override
                    public String value() {
                        return String.format("%.2f", getRecentLoopFrequency_loopsPerSec());
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
        long t = System.currentTimeMillis();
        if (lastIdleTime_ms == 0)
            lastIdleTime_ms = t;
        else
        {
            long elapsed_ms = t - lastIdleTime_ms;
            lastIdleTime_ms = t;
            loopIntervalHistory_ms.put(t, elapsed_ms);
        }

        if (!latestTelemetryData.isEmpty())
        {
            long timeSinceLoggingTelemetry = System.currentTimeMillis() - lastTelemetryLoggingTime_ms;

            if ( timeSinceLoggingTelemetry >= TELEMETRY_LOGGING_INTERVAL_MS)
            {
                for (Map.Entry entry : latestTelemetryData.entrySet())
                {
                    RobotLog.ww(Robot.ROBOT_TAG, "Telemetry: %12s: %s", entry.getKey(), entry.getValue());
                }
            }
        }

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

    public String saveTelemetryData(String key, String valueFormat, Object... valueArgs)
    {
        String value = String.format(valueFormat, valueArgs);
        latestTelemetryData.put(key,value);
        return value;
    }

    public double getRecentLoopFrequency_loopsPerSec()
    {
        if ( loopIntervalHistory_ms.isEmpty() )
            return 0;

        long totalLoopIntervals_ms=0;
        for (Map.Entry<Long, Long> entry : loopIntervalHistory_ms.entrySet())
            totalLoopIntervals_ms += entry.getValue();

        double loopIntervalAverage_ms = 1.0*totalLoopIntervals_ms / loopIntervalHistory_ms.size();

        return 1000.0/loopIntervalAverage_ms;
    }
}
