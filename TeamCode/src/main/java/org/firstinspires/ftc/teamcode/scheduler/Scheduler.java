package org.firstinspires.ftc.teamcode.scheduler;

import org.firstinspires.ftc.robotcore.external.Func;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Robot;
import static org.firstinspires.ftc.teamcode.scheduler.Utils.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

public class Scheduler {
    final static long MINIMUM_TIME_BETWEEN_ACTION_LOOPS_MS = 20L;

    // Create a set that keeps items in order
    Set<OngoingAction> ongoingActions
            = Collections.newSetFromMap(new LinkedHashMap<OngoingAction,Boolean>());

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
    private RollingStatistics loopDurationHistory_ns = new RollingStatistics(LOOP_TIME_MOVING_AVERAGE_MS);

    private long schedulerStatusLogInterval_ms = 5000;
    private long schedulerStatusLog_lastLogTime_ms = 0;

    private static Scheduler sharedInstance;

    // How many scheduling loops have we started
    private long loopNumber;

    public static Scheduler get(){
        if ( sharedInstance==null )
            throw new IllegalStateException("Can only use Scheduler.get() after new Scheduler(controller) is called");

        return sharedInstance;
    }

    public Scheduler(SchedulerController schedulerController)
    {
        sharedInstance = this;
        this.schedulerController = schedulerController;

        getTelemetry().addLine()
                .addData("Scheduler", new Func<Object>() {
                    @Override
                    public Object value() {
                        return Robot.get().saveTelemetryData("Scheduler", "% loops. Average loop time over last %.1f seconds: ~%.0f)",
                                loopNumber,
                                LOOP_TIME_MOVING_AVERAGE_MS/1000,
                                loopDurationHistory_ns.getAverage()/1e6);
                    }
                });
    }

    Telemetry getTelemetry()
    {
        return schedulerController.getTelemetry();
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

        if ( System.currentTimeMillis() >= schedulerStatusLog_lastLogTime_ms+schedulerStatusLogInterval_ms )
            logSchedulerStatus();

        // Are we starting a loop or are we re-entering loop?...
        Action currentActionWhenSchedulerLoopStarted = currentAction;

        if ( currentActionWhenSchedulerLoopStarted==null )
        {
            loopNumber++;
            // starting a new loop. Give other things a chance first
            // TODO: This could sleep long enough to slow down to a target looping rate (eg 20 loops/sec)
            schedulerController.schedulerSleep(1);
            lastLoopStartTime_ns = System.nanoTime();
        } else {
            // Has our original caller been aborted? If so, then we need to throw an exception back out
            if (currentActionWhenSchedulerLoopStarted instanceof EndableAction)
            {
                if (((EndableAction) currentActionWhenSchedulerLoopStarted).wasAborted())
                {
                    log_raw("Action was aborted after it called loop(): %s", currentActionWhenSchedulerLoopStarted);
                    throw new StopActionException("Cleanup after being aborted");
                }
            }
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

            try
            {
                currentAction = a;
                runOngoingActionLoopMethod(a);
            } finally
            {
                currentAction = currentActionWhenSchedulerLoopStarted;
            }
        }
        // We reached the end of all the actions.
        lastLoopEndTime_ns = System.nanoTime();
        lastLoopDuration_ns = lastLoopEndTime_ns - lastLoopStartTime_ns;
        loopDurationHistory_ns.put(lastLoopDuration_ns);
    }

    private void runOngoingActionLoopMethod(OngoingAction a)
    {
        // Check to see if EnableAction is done
        if (a instanceof EndableAction)
        {
            EndableAction endableActionA = ((EndableAction) a);
            StringBuilder statusMessage = new StringBuilder();

            boolean isDone = endableActionA.isDone(statusMessage);

            if (statusMessage.length() > 0)
                endableActionA.setStatus("(done)" + statusMessage.toString());

            if (isDone)
            {
                endableActionA.finish(statusMessage.toString());
                actionFinished(a);
                return;
            }
        }

        // Run loop() if it has been long enough
        long timeSinceLoopWasCalled_ns = System.nanoTime() - a.lastLoopStart_ns;

        if (timeSinceLoopWasCalled_ns > 1e6 * MINIMUM_TIME_BETWEEN_ACTION_LOOPS_MS)
        {
            long startTime_ns = System.nanoTime();

            try
            {
                a.numberOfLoops++;
                a.lastLoopStart_ns = startTime_ns;
                a.loop();
            } catch (StopRobotException e)
            {
                log_raw("Robot is stopping");
                throw e;
            } catch (StopActionException e)
            {
                log_raw("Action was stopped");
                // This is where the StopActionException is handled.
                // There is nothing left to do as the Exception is thrown after other cleanup has occurred
            } catch (RuntimeException e)
            {
                log_raw("%s threw exception %s", a, e.getMessage());
                throw e;
            } finally
            {
                long endTime_ns = System.nanoTime();
                a.lastLoopDuration_ns = endTime_ns - startTime_ns;
            }
        }
    }

    private void logSchedulerStatus()
    {
        for (OngoingAction ongoingAction : ongoingActions)
            log_raw("ScheduledAction %30s [dur_ms=%d] %s",
                    ongoingAction.label,
                    (int)(ongoingAction.lastLoopDuration_ns/1e6),
                    ongoingAction.status);

        schedulerStatusLog_lastLogTime_ms = System.currentTimeMillis();
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

    public void abortAllEndableActions(String reasonFormat, Object... reasonArgs)
    {
        String reason = safeStringFormat(reasonFormat, reasonArgs);

        log("Aborting all EndableActions: %s", reason);
        int count=0;
        for(OngoingAction action: new ArrayList<>(ongoingActions))
        {
            if(!(action instanceof EndableAction))
                continue;

            // Has this action finished since we started?
            if(action.hasFinished())
                continue;
            count++;
            EndableAction endableAction = (EndableAction) action;
            endableAction.abort(reason);
        }
        log("Aborted %d EndableActions", count);
    }
}
