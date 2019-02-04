package org.firstinspires.ftc.teamcode.scheduler;

import org.firstinspires.ftc.robotcore.external.Func;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Robot;

import static org.firstinspires.ftc.teamcode.scheduler.Utils.*;

public abstract class EndableAction extends OngoingAction{
    private final long timeLimit_ms;
    public Boolean endedSuccessfully=null;
    private boolean wasAborted =false;

    Telemetry.Line telemetryStatusLine;

    public EndableAction(String name){
        this(0, name, null);
    }

    public EndableAction(String name, String descriptionFormat, Object...descriptionArgs){
        this(0, name, descriptionFormat, descriptionArgs);
    }

    public EndableAction(long timeLimit_ms, String name, String descriptionFormat, Object...descriptionArgs){
        super(name, descriptionFormat, descriptionArgs);
        this.timeLimit_ms = timeLimit_ms;
    }

    public EndableAction start()
    {
        super.start();
        endedSuccessfully=null;
        wasAborted =false;

        telemetryStatusLine = telemetryStatusLine = Scheduler.get().getTelemetry().addLine();
        telemetryStatusLine.addData("Action", new Func<Object>() {
            @Override
            public Object value() {
                return Robot.get().saveTelemetryData("EA-"+label, "%s -- %s", toShortString(), status);
            }
        });

        return this;
    }

    abstract public boolean isDone(StringBuilder statusMessage);

    public void abort(String reasonFormat, Object... reasonArgs)
    {
        String reason =safeStringFormat(reasonFormat, reasonArgs);

        log("Action aborted: %s. Stopping any children and then myself", reason);

        endedSuccessfully = false;
        wasAborted =true;

        // Abort children first
        for(Action childAction : childActions)
        {
            if (childAction instanceof EndableAction)
            {
                EndableAction endableChild = (EndableAction) childAction;
                endableChild.abort("%s was aborted: %s", label, reason);
            }
        }
        finish("ABORTED: " + reasonFormat, reasonArgs);
    }

    protected void cleanup(boolean actionWasCompletedsSuccessfully)
    {
        Scheduler.get().getTelemetry().removeLine(telemetryStatusLine);
        Robot.get().removeTelemetryData("EA-"+label);
    }

    @Override
    public final void finish(String messsageFormat, Object... messageArgs)
    {
        if (endedSuccessfully==null)
            endedSuccessfully=true;

        cleanup(endedSuccessfully);
        super.finish(messsageFormat, messageArgs);
    }

    public boolean waitUntilFinished()
    {
        Scheduler.get().waitForActionToFinish(this);
        return endedSuccessfully;
    }

    public boolean wasAborted()
    {
        return wasAborted;
    }

    @Override
    public void waitFor(EndableAction... actions)
    {
        super.waitFor(actions);

        // Have we been aborted since we started waiting?
        // If so, throw an exception that will not only abort this Waiting, but whatever
        // follows the Wait. This exception rises all the way to the scheduler
        if (wasAborted())
            throw new StopActionException("Was aborted while waiting");
    }
}
