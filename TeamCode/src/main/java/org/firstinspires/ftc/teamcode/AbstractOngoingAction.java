package org.firstinspires.ftc.teamcode;

public abstract class AbstractOngoingAction
{
    Robot robot;
    public AbstractOngoingAction(Robot robot)
    {
        this.robot = robot;
    }
    
    abstract public void start();
    abstract public void loop();
    abstract public void cleanup();
    abstract public boolean isDone();
}
