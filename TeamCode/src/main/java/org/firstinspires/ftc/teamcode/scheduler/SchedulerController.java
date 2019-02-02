package org.firstinspires.ftc.teamcode.scheduler;

import org.firstinspires.ftc.robotcore.external.Function;
import org.firstinspires.ftc.robotcore.external.Telemetry;

public interface SchedulerController
{
    /**
     *
     * @return true when scheduler should abort
     */
    public boolean shouldSchedulerStop();

    /**
     *  What the scheduler uses to sleep... Responsible for keeping the robot running, eg,
     *  calling OpMode.idle() in FTC.
     *
     *  Note: This cannot use Scheduler.sleep() as that would be recursive!
     */
    public void schedulerSleep(long time_ms);
    public Telemetry getTelemetry();

    // not used yet... hoping to separate Scheduler from FTC classes
    //public void registerTelemetryFunction(String label, Function telemetryFunction);
}
