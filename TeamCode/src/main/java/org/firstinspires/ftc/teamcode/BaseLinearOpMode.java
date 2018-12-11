package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

public abstract class BaseLinearOpMode extends LinearOpMode {
    Robot robot;

    ActionTracker opmodeAction = ActionTracker.startAction(null, "OpMode", "");

    // First thing to init
    final void baseInit()
    {
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
        opmodeAction.setStatus("Initializing: baseInit()");
        baseInit();
        opmodeAction.setStatus("Initializing: teamInit()");
        teamInit();

        opmodeAction.setStatus("Waiting for start");

        while(!isStarted())
            teamIdle(opmodeAction);

        opmodeAction.setStatus("Running: teamRun()");
        teamRun();

        opmodeAction.finish();
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

        telemetry.update();
        idle();
    }


    // Team's version of opModeIsActive

    public boolean shouldOpModeKeepRunning(ActionTracker callingAction)
    {
        teamIdle(callingAction);
        return ! isStarted() || opModeIsActive();
    }
}
