package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Func;
import static org.firstinspires.ftc.teamcode.TeamUtils.*;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class BaseLinearOpMode extends LinearOpMode {
    public enum OPMODE_STATE {NEW, INITIALIZING, WAITING_FOR_START, RUNNING, STOPPING};

    private static final long TELEMETRY_LOGGING_INTERVAL_MS = 250L;
    Robot robot;
    OPMODE_STATE state = OPMODE_STATE.NEW;
    long opmodeStateChanged_ms = System.currentTimeMillis();


    ActionTracker opmodeAction = ActionTracker.startAction(null, "OpMode", "");

    // Used to log telemetry to RobotLog (in addition to driver station)
    Map<String, String> latestTelemetryData = new LinkedHashMap<>();
    long lastTelemetryLoggingTime_ms = 0;
    long lastTelemetryUpdateTime_ms = 0;

    // Used to measure how long between idle loops
    long lastIdleStartTime_ms = 0;
    double lastLoopDuration_secs = 0;
    private static final long LOOP_TIME_MOVING_AVERAGE_MS = 1000L;
    Scheduler scheduler = Scheduler.get();

    // Keep recent loop intervals
    // How fast are we calling teamIdle... ObservationTime (ms time) --> Idle-Time_interval (ms)
    // This keeps the delays between the loops for 1 second
    LinkedHashMap<Long, Long> loopIntervalHistory_ms = new LinkedHashMap<Long, Long>() {
        @Override
        protected boolean removeEldestEntry(Entry<Long, Long> eldest) {
            long age_ms = System.currentTimeMillis() - eldest.getKey();
            return age_ms > LOOP_TIME_MOVING_AVERAGE_MS;
        }
    };


    // First thing to init
    final void baseInit()
    {
        telemetry.addLine()
                .addData("OpMode", new Func<Object>() {
                    @Override
                    public Object value() {
                        return saveTelemetryData("OpMode", "%s [%d secs]. Loop time: %.0fms (~%d/sec)",
                                state, (System.currentTimeMillis()-opmodeStateChanged_ms)/1000,
                                lastLoopDuration_secs*1000,
                                getRecentLoopFrequencey_loopsPerSec());
                    }
                });

        opmodeAction.setStatus("baseInit()");

        robot = new Robot(opmodeAction, this, hardwareMap, telemetry);
        robot.init(opmodeAction);
    }

    // Where OpModes can initialize themselves
    void teamInit()
    {
        opmodeAction.setStatus("starting team init");
    }

    // What happens after Play is pressed
    abstract void teamRun();


    @Override
    public void runOpMode() throws InterruptedException {
        setOpModeState(OPMODE_STATE.INITIALIZING);
        opmodeAction.setStatus("Initializing: baseInit()");
        baseInit();
        opmodeAction.setStatus("Initializing: teamInit()");
        teamInit();

        setOpModeState(OPMODE_STATE.WAITING_FOR_START);
        opmodeAction.setStatus("Waiting for start");

        while(!isStarted())
            teamIdle(opmodeAction);

        setOpModeState(OPMODE_STATE.RUNNING);
        opmodeAction.setStatus("Running: teamRun()");
        teamRun();

        setOpModeState(OPMODE_STATE.STOPPING);
        opmodeAction.finish();
    }

    private void setOpModeState(OPMODE_STATE newState)
    {
        opmodeAction.setStatus("Starting opmode state %s", newState);
        state = newState;
        opmodeStateChanged_ms = System.currentTimeMillis();
    }

    public void teamSleep(ActionTracker callingAction, long sleep_ms, String reason)
    {
        ActionTracker action = callingAction.startChildAction("sleep", "Sleep(%d ms, %s)", sleep_ms, reason);

        long startTime_ms = System.currentTimeMillis();
        long stopTime_ms = startTime_ms + sleep_ms;

        while(shouldOpModeKeepRunning(action) && System.currentTimeMillis() < stopTime_ms)
        {
            action.setStatus(String.format("%d sec of sleep left",
                    (stopTime_ms - System.currentTimeMillis())/1000));

            teamIdle(callingAction);
        }
        action.finish();
    }


    public void teamIdle(ActionTracker callingAction)
    {
        if(robot != null)
            robot.loop(callingAction);

        scheduler.loop();
        telemetry.update();

        long t = System.currentTimeMillis();
        if ( lastIdleStartTime_ms == 0 )
            lastIdleStartTime_ms = t;
        else{
            long elapsed_ms = t- lastIdleStartTime_ms;
            lastIdleStartTime_ms = t;
            lastLoopDuration_secs = 0.001 * elapsed_ms;
            loopIntervalHistory_ms.put(t, elapsed_ms);
        }

        // Has telemetry changed since we last logged it?
        if (lastTelemetryUpdateTime_ms==0 || lastTelemetryUpdateTime_ms>lastTelemetryLoggingTime_ms)
        {
            lastTelemetryLoggingTime_ms = System.currentTimeMillis();
            for (Map.Entry entry : latestTelemetryData.entrySet())
            {
                RobotLog.ww(Robot.ROBOT_TAG, "Telemetry: %12s: %s", entry.getKey(), entry.getValue());
            }
        }

        idle();
    }


    // Team's version of opModeIsActive

    public boolean shouldOpModeKeepRunning(ActionTracker callingAction)
    {
        teamIdle(callingAction);
        return ! isStarted() || opModeIsActive();
    }

    public String saveTelemetryData(String key, String valueFormat, Object... valueArgs)
    {
        String value = safeStringFormat(valueFormat, valueArgs);
        latestTelemetryData.put(key, value);
        lastTelemetryUpdateTime_ms = System.currentTimeMillis();
        return value;
    }

    public int getRecentLoopFrequencey_loopsPerSec()
    {
        long totalLoopIntervals_ms = 0;
        long count=0;
        for (Map.Entry<Long, Long> entry : loopIntervalHistory_ms.entrySet())
        {
            long age_ms = System.currentTimeMillis() - entry.getKey();
            if (age_ms <= LOOP_TIME_MOVING_AVERAGE_MS)
            {
                count++;
                totalLoopIntervals_ms += entry.getValue();
            }
        }
        if (count == 0)
            return 0;

        double loopIntervalAverage_ms = 1.0*totalLoopIntervals_ms / count;
        return (int) Math.round(1000.0/loopIntervalAverage_ms);
    }
}
