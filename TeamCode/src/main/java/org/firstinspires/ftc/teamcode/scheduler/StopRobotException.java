package org.firstinspires.ftc.teamcode.scheduler;

public class StopRobotException extends RuntimeException
{
    public StopRobotException(String detailMessage)
    {
        super(detailMessage);
    }

    public StopRobotException(String detailMessage, Throwable throwable)
    {
        super(detailMessage, throwable);
    }
}
