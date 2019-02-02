package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Func;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.scheduler.Action;
import org.firstinspires.ftc.teamcode.scheduler.ImmediateAction;
import org.firstinspires.ftc.teamcode.scheduler.RepeatedAction;
import org.firstinspires.ftc.teamcode.scheduler.Scheduler;
import org.firstinspires.ftc.teamcode.scheduler.SchedulerController;
import org.firstinspires.ftc.teamcode.scheduler.StopRobotException;

import static org.firstinspires.ftc.teamcode.scheduler.Utils.*;

public abstract class BaseLinearOpMode extends LinearOpMode implements SchedulerController
{
    public enum OPMODE_STATE {NEW, INITIALIZING, WAITING_FOR_START, RUNNING, STOPPING};

    private static final long TELEMETRY_LOGGING_INTERVAL_MS = 250L;
    Robot robot;
    OPMODE_STATE state = OPMODE_STATE.NEW;
    long opmodeStateChanged_ms = System.currentTimeMillis();


    Action opmodeAction = new ImmediateAction("OpMode", "%s", getClass().getSimpleName());


    final Scheduler scheduler = new Scheduler(this);

    // First thing to init
    final void baseInit()
    {
        robot = new Robot(this, hardwareMap, telemetry);

        telemetry.addLine()
                .addData("OpMode", new Func<Object>()
                {
                    @Override
                    public Object value()
                    {
                        return robot.saveTelemetryData("OpMode", "%s [%d secs].",
                                state, (System.currentTimeMillis() - opmodeStateChanged_ms) / 1000);
                    }
                });

        new RepeatedAction("OpmodeMaintenance")
        {
            @Override
            protected void doTask()
            {

            }
        }.start();
    }

    // Where OpModes can initialize themselves
    void teamInit()
    {

    }

    // What happens after Play is pressed
    protected void teamRun()
    {

    }


    /**
     * Use this in loops to abort them when the OpMode should stop.
     * Note: This calls scheduler.loop() to keep the robot running while your
     * while loop waits for something
     *
     * for example: while (shouldOpModeKeepRunning() && distanceMoved<10) {//keep moving}
     *
     * @return Whether the OpMode should keep running
     */
    public boolean shouldOpModeKeepRunning()
    {
        // We run until stop is requested
        if (isStopRequested())
            return false;

        scheduler.loop();

        return true;
    }


    @Override
    public void runOpMode() throws InterruptedException {
        try
        {
            setOpModeState(OPMODE_STATE.INITIALIZING);
            opmodeAction.setStatus("Initializing: baseInit()");
            baseInit();
            opmodeAction.setStatus("Initializing: teamInit()");
            teamInit();

            setOpModeState(OPMODE_STATE.WAITING_FOR_START);
            opmodeAction.setStatus("Waiting for start");

            while (!isStarted())
                scheduler.loop();

            setOpModeState(OPMODE_STATE.RUNNING);
            opmodeAction.setStatus("Running: teamRun()");
            teamRun();

            opmodeAction.setStatus("Passing control to scheduler");
            while (shouldOpModeKeepRunning())
            {
            }
        }
        catch (StopRobotException e)
        {
            log_raw("Robot stopping: %s", e.getMessage());
        }

        setOpModeState(OPMODE_STATE.STOPPING);
        opmodeAction.finish("runOpMode() is done");
    }

    private void setOpModeState(OPMODE_STATE newState)
    {
        opmodeAction.setStatus("Starting opmode state %s", newState);
        state = newState;
        opmodeStateChanged_ms = System.currentTimeMillis();
    }

    public void teamSleep(long time_ms, String reason)
    {
        Scheduler.get().sleep(time_ms, reason);
    }

    public boolean shouldSchedulerStop()
    {
        return isStopRequested();
    }

    public void schedulerSleep(long time_ms)
    {
        long stopTime_ms = System.currentTimeMillis() + time_ms;
        try
        {
            while (!isStopRequested() && System.currentTimeMillis()<stopTime_ms)
            {
                idle();
                Thread.sleep(1);
            }
        }
        catch (InterruptedException e)
        {}
    }

    public Telemetry getTelemetry()
    {
        return telemetry;
    }

}
