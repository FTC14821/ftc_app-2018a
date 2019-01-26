package org.firstinspires.ftc.teamcode;

import static org.firstinspires.ftc.teamcode.Utils.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class Scheduler {
    Set<OngoingAction> ongoingActions = new HashSet<>();
    Action currentAction;
    public final static Scheduler sharedInstance = new Scheduler();
    public static Scheduler get(){
        return sharedInstance;
    }

    public void actionStarted(Action a){
        currentAction = a;
        if (a instanceof OngoingAction){
            ongoingActions.add((OngoingAction) a);
        }
    }
    public void actionDone(Action a){
        if (a != currentAction){
            log("Waring: %s is not currentAction. (currentAction is %s)", a.label, currentAction.ancesterLabels);
        }
        else {
            currentAction = a.parentAction;
        }

        if (a instanceof OngoingAction){
            ongoingActions.remove(a);
        }
    }

    public void loop(){
        for (OngoingAction a : new ArrayList<>(ongoingActions)){
            if (a.childActions.size() == 0){
                currentAction = a;
                boolean isDone = a.loop();
                if (isDone){
                    log("%s loop method returned true", a.label);
                    a.done();
                }
            }
        }
    }
}
