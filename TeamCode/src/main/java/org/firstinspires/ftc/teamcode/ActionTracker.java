package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.util.RobotLog;
import static org.firstinspires.ftc.teamcode.TeamUtils.*;

/**
 * Keep track of robot actions: what is happening and reason?
 */
public class ActionTracker {
    public static enum LEVEL {LOW, HIGH};

    private static int actionCounter=1;


    private final int actionNumber = actionCounter++;

    // Is this action a part of a different action
    private ActionTracker parentAction=null;

    // How many parents/grandparents do we have (helps with indenting log)
    private final int ancestryCount;
    private final String actionChainString;
    private final String messageIndentationString;

    private final long startTime_ms = System.currentTimeMillis();
    private long stopTime_ms = -1;

    private final String actionShortName;
    private final String actionLongName;
    private String status="";

    public static void startImmediateAction(ActionTracker parentAction, String actionShortName, String actionLongNameFormat, Object... actionLongNameArgs)
    {
        ActionTracker result = new ActionTracker(parentAction, actionShortName, actionLongNameFormat, actionLongNameArgs);
        result.stopTime_ms = result.startTime_ms;

        RobotLog.ww(Robot.ROBOT_TAG, "%s*%s: Performed", result.messageIndentationString, result);
    }

    public static ActionTracker startAction(
            ActionTracker parentAction, String actionShortName,
            String actionLongNameFormat, Object... actionLongNameArgs)
    {
        ActionTracker result = new ActionTracker(parentAction, actionShortName, actionLongNameFormat, actionLongNameArgs);

        RobotLog.ww(Robot.ROBOT_TAG, "%s>%s: Starting", result.messageIndentationString, result);
        return result;
    }

    private ActionTracker(
            ActionTracker parentAction, String actionShortName,
            String actionLongNameFormat, Object... actionLongNameArgs) {
        this.parentAction = parentAction;
        this.actionShortName = actionShortName;
        if ( actionLongNameFormat != null )
            this.actionLongName = safeStringFormat(actionLongNameFormat, actionLongNameArgs);
        else
            this.actionLongName = actionShortName;

        if ( parentAction != null )
        {
            ancestryCount = parentAction.ancestryCount + 1;
            actionChainString = safeStringFormat("%s-->#%d-%s", parentAction.actionChainString, actionNumber, actionShortName);
            messageIndentationString = parentAction.messageIndentationString + "  ";
        }
        else
            {
            ancestryCount = 0;
            actionChainString = safeStringFormat("#%d-%s", actionNumber, actionShortName);
            messageIndentationString = "";
        }
    }

    public void startImmediateChildAction(String actionShortName, String actionLongNameFormat, Object... actionLongNameArgs)
    {
        startImmediateAction(this, actionShortName, actionLongNameFormat, actionLongNameArgs);
    }

    public ActionTracker startChildAction(
            String actionShortName,
            String actionLongNameFormat, Object... actionLongNameArgs)
    {
        return startAction(this, actionShortName, actionLongNameFormat, actionLongNameArgs);
    }



    public String toShortString()
    {
        return safeStringFormat("%d-%s%s", actionNumber, actionShortName, getTimingString());
    }

    @Override
    public String toString() {
        return safeStringFormat("%d-%s|%s|%s|%s",
                actionNumber, actionLongName, getTimingString(), actionChainString, status);
    }

    private String getTimingString()
    {
        if ( stopTime_ms == startTime_ms )
            return "[quick]";
        else if ( stopTime_ms > 0 )
            return safeStringFormat("[dur=%.2f secs]", 1.0*(stopTime_ms - startTime_ms)/1000);
        else
            return safeStringFormat("[age=%.2f secs]", 1.0*(System.currentTimeMillis() - startTime_ms)/1000);

    }

    public void setStatus(String statusFormat, Object... args)
    {
        String newStatus = safeStringFormat(statusFormat, args);

        // Any change in status?
        if (status.equals(newStatus))
            return;

        status = newStatus;
        RobotLog.ww(Robot.ROBOT_TAG, "%s-%s: Status %s", messageIndentationString, toShortString(), status);
    }

    public void finish(String messageFormat, Object... args)
    {
        stopTime_ms = System.currentTimeMillis();
        if ( messageFormat != null )
            RobotLog.ww(Robot.ROBOT_TAG, "%s<%s: Done: %s",
                messageIndentationString, toShortString(), safeStringFormat(messageFormat, args));
        else
            RobotLog.ww(Robot.ROBOT_TAG, "%s<%s: Done",
                    messageIndentationString, toShortString());
    }


    public void finish()
    {
        finish(null);
    }
}
