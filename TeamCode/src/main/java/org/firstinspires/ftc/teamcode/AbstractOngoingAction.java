package org.firstinspires.ftc.teamcode;

public abstract class AbstractOngoingAction
{
    Robot robot;
    final ActionTracker actionTracker;

    public AbstractOngoingAction(
            ActionTracker callingAction, Robot robot,
            String actionShortName, String actionLongNameFormat, Object... actionLongNameArgs)
    {
        this.robot = robot;
        actionTracker = callingAction.startChildAction(actionShortName,actionLongNameFormat,actionLongNameArgs);
    }
    
    abstract public void start();
    abstract public void loop();

    public void cleanup()
    {
        actionTracker.finish();
    }
    abstract public boolean isDone();
}
