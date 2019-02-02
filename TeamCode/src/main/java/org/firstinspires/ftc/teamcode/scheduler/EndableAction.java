package org.firstinspires.ftc.teamcode.scheduler;

public abstract class EndableAction extends OngoingAction{
    private final long timeLimit_ms;
    public Boolean endedSuccessfully=null;

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
        return this;
    }

    abstract public boolean isDone(StringBuilder statusMessage);

    public void abort(String reasonFormat, Object... reasonArgs)
    {
        endedSuccessfully = false;
        finish("ABORTED: " + reasonFormat, reasonArgs);
    }

    protected void cleanup(boolean actionWasCompletedsSuccessfully)
    {

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
}
