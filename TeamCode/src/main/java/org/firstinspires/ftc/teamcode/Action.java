package org.firstinspires.ftc.teamcode;

import java.util.HashSet;
import java.util.Set;

import static org.firstinspires.ftc.teamcode.Utils.*;

public abstract class Action {
    static int actionCounterSequence= 0;
    final int actionID = ++actionCounterSequence;
    final Action parentAction;
    final Set<Action> childActions = new HashSet<>();
    final String label;
    final String fullDesc;
    final String ancesterLabels;
    long startTime_ns = System.nanoTime();
    public Action( String label, String descriptionFormat, Object...descriptionArgs){
        this.parentAction = Scheduler.get().currentAction;

        if ( descriptionFormat != null ) {
            this.fullDesc = safeStringFormat(descriptionFormat, descriptionArgs);
        }
        else {
            this.fullDesc = label;
        }

        this.label = safeStringFormat("%s(#%d)", label, actionID);
        if (parentAction == null){
            ancesterLabels = label;
        }
        else {
            ancesterLabels = safeStringFormat("%s --> %s", parentAction.ancesterLabels, label);
            parentAction.childStarted(this);
        }
        //TODO: Logging
        log("%d - %s: Starting (%s)", actionID, fullDesc, ancesterLabels);
        Scheduler.get().actionStarted(this);
    }
    public void done(){
        //TODO: Logging
        log("Label: %s: Finished(%d) ms", label, (System.nanoTime() - startTime_ns) / 1e6);
        if (parentAction != null){
            parentAction.childActionsFinished(this);
        }
        Scheduler.get().actionDone(this);
    }
    protected void childStarted(Action childAction) {
        childActions.add(childAction);
    }

    protected void childActionsFinished(Action childAction) {
        childActions.remove(childAction);
    }
}
