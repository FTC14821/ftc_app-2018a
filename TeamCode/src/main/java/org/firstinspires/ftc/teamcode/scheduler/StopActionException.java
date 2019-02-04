package org.firstinspires.ftc.teamcode.scheduler;

public class StopActionException extends RuntimeException
{
    public StopActionException(String detailMessage)
    {
        super(detailMessage);
    }

    public StopActionException(String detailMessage, Throwable throwable)
    {
        super(detailMessage, throwable);
    }
}
