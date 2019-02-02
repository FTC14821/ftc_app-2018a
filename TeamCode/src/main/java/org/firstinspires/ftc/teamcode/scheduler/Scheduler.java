package org.firstinspires.ftc.teamcode.scheduler;

import org.firstinspires.ftc.robotcore.external.Func;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Robot;
import static org.firstinspires.ftc.teamcode.scheduler.Utils.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

public class Scheduler {
    Set<OngoingAction> ongoingActions = new HashSet<>();
    final SchedulerController schedulerController;

    Action currentAction;

    // Used to measure how long between loops
    long lastLoopStartTime_ns = 0;
    long lastLoopEndTime_ns = 0;
    long lastLoopDuration_ns = 0;

    // Keep recent loop intervals
    // How fast are the loops... ObservationTime (ms time) --> LoopEnd-LoopStart (ms)
    // This keeps the delays between the loops for LOOP_TIME_MOVING_AVERAGE_MS

    private static final long LOOP_TIME_MOVING_AVERAGE_MS = 2000L;
    LinkedHashMap<Long, Long> loopDurationHistory_ns = new LinkedHashMap<Long, Long>() {
        @Override
        protected boolean removeEldestEntry(Entry<Long, Long> eldest) {
            long age_ms = System.currentTimeMillis() - eldest.getKey();
            return age_ms > LOOP_TIME_MOVING_AVERAGE_MS;
        }
    };


    private static Scheduler sharedInstance;

    // How many scheduling loops have we started
    private long loopNumber;

    public static Scheduler get(){
        return sharedInstance;
    }

    public Scheduler(SchedulerController schedulerController)
    {
        if (sharedInstance!=null)
            throw new IllegalStateException("Can only have one Scheduler");
        else
            sharedInstance = this;

        this.schedulerController = schedulerController;

        schedulerController.getTelemetry().addLine()
                .addData("Scheduler", new Func<Object>() {
                    @Override
                    public Object value() {
                        return Robot.get().saveTelemetryData("Scheduler", "% loops. Last loop time: %.0fms (Average over last %.1f seconds: ~%.0f)",
                                loopNumber,
                                lastLoopDuration_ns/1e6,
                                LOOP_TIME_MOVING_AVERAGE_MS/1000,
                                getRecentAverageLoopDuration_ns()/1e6);
                    }
                });

    }

    void actionStarted(Action a)
    {
        if (a instanceof  ImmediateAction)
        {
            currentAction = a;
        }
        else if (a instanceof OngoingAction)
        {
            ongoingActions.add((OngoingAction) a);
        }
    }

    void actionFinished(Action a)
    {
        if (a == currentAction)
        {
            currentAction = a.parentAction;
        }

        if (a instanceof OngoingAction)
        {
            ongoingActions.remove(a);
        }
    }

    public void loop(){
        if (schedulerController.shouldSchedulerStop())
        {
            log_raw("Scheduler is stopping!");
            throw new StopRobotException("Scheduler stopping");
        }

        // Are we starting a loop or are we re-entering loop?...
        Action currentActionWhenSchedulerLoopStarted = currentAction;

        if ( currentActionWhenSchedulerLoopStarted==null )
        {
            loopNumber++;
            // starting a new loop. Give other things a chance first
            // TODO: This could sleep long enough to slow down to a target looping rate (eg 20 loops/sec)
            schedulerController.schedulerSleep(1);
            lastLoopStartTime_ns = System.nanoTime();
        }

        // Run .loop on all ongoing actions
        for(OngoingAction a : new ArrayList<>(ongoingActions))
        {
            // Skip/Clean up any ongoing actions that have finished.
            // Note: it's possible that we're iterating over a stale list and that
            // an action finished and was removed from ongoingActions
            if ( a.hasFinished() )
            {
                ongoingActions.remove(a);
                continue;
            }

            // Don't run any actions that are blocked/waiting
            if ( a.isWaiting() )
            {
                continue;
            }

            currentAction = a;

            // Check to see if EnableAction is done
            if ( a instanceof EndableAction )
            {
                EndableAction endableActionA = ((EndableAction) a);
                StringBuilder statusMessage = new StringBuilder();

                boolean isDone = endableActionA.isDone(statusMessage);

                if ( statusMessage.length() > 0 )
                    endableActionA.setStatus("(done)" + statusMessage.toString());

                if ( isDone )
                {
                    endableActionA.finish(statusMessage.toString());
                    actionFinished(a);
                    continue;
                }
            }

            a.numberOfLoops++;
            long startTime_ns = System.nanoTime();
            a.lastLoopStart_ns = startTime_ns;

            a.loop();

            long endTime_ns = System.nanoTime();
            a.lastLoopDuration_ns = endTime_ns - startTime_ns;

            currentAction = null;
        }
        // We reached the end of all the actions.
        lastLoopEndTime_ns = System.nanoTime();
        lastLoopDuration_ns = lastLoopEndTime_ns - lastLoopStartTime_ns;
        loopDurationHistory_ns.put(lastLoopStartTime_ns/1000000, lastLoopDuration_ns);
    }

    public int getRecentAverageLoopDuration_ns()
    {
        long measurementTotal = 0;
        long count=0;
        Iterator<Long> loopIntervalHistory_iterator = loopDurationHistory_ns.keySet().iterator();
        while (loopIntervalHistory_iterator.hasNext())
        {
            Long time_ms = loopIntervalHistory_iterator.next();
            Long measurement = loopDurationHistory_ns.get(time_ms);

            long age_ms  = System.currentTimeMillis() - time_ms;
            if (age_ms <= LOOP_TIME_MOVING_AVERAGE_MS)
            {
                count++;
                measurementTotal += measurement;
            }
            else
                // The loopDurationHistory_ns is somewhat self-cleaning (one added, one removed)
                // but this cleans it more if old entries accumulate
                loopIntervalHistory_iterator.remove();
        }
        if (count == 0)
            return 0;

        double averageMeasurement = 1.0*measurementTotal / count;

        return (int) averageMeasurement;
    }

    public void sleep(long time_ms, String reasonFormat, Object... reasonArgs)
    {
        String reason = safeStringFormat(reasonFormat, reasonArgs);
        if (currentAction != null )
            currentAction.actionSleep(time_ms, reason);
        else
        {
            long sleepStop_ms = System.currentTimeMillis() + time_ms;
            while ( System.currentTimeMillis() < sleepStop_ms )
            {
                loop();
            }
        }
    }

    public void waitForActionToFinish(EndableAction endableAction)
    {
        if (currentAction!=null)
            currentAction.waitFor(endableAction);
        else
        {
            while (!endableAction.hasFinished())
            {
                sleep(10, "Waiting for %s to finish", endableAction.toShortString());
            }
        }
    }
}
