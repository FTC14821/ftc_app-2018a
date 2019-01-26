package org.firstinspires.ftc.teamcode;

import static org.firstinspires.ftc.teamcode.Utils.*;

public abstract class EndableAction extends OngoingAction{
    private final long timeLimit_ms;
    public String stuffLeftToDO = "";
    public EndableAction(long timeLimit_ms, String label, String descriptionFormat, Object...descriptionArgs){
        super(label, descriptionFormat, descriptionArgs);
        this.timeLimit_ms = timeLimit_ms;
    }
}
