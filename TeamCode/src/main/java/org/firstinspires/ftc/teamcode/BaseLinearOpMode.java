package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcore.external.Func;

public abstract class BaseLinearOpMode extends LinearOpMode {
    Robot robot;

    // Info of what robot is doing
    String phase="", step="", operation="", status="";


    // First thing to init
    final void baseInit()
    {
        telemetry.addLine("Info:")
            .addData("Phase", new Func<String>() {
                @Override
                public String value() {
                    return phase;
                }})
            .addData("Step", new Func<String>() {
                @Override
                public String value() {
                    return step;
                }})
            .addData("Op", new Func<String>() {
                @Override
                public String value() {
                    return operation;
                }})
            ;
        telemetry.addLine()
                .addData("Status", new Func<String>() {
                    @Override
                    public String value() {
                        return status;
                    }})
            ;

        setStep("ContructingRobot");
        robot = new Robot(this, hardwareMap, telemetry);
        setStatus("Robot constructed");
    }

    // Where OpModes can initialize themselves
    void teamInit()
    {

    }

    // What happens after Play is pressed
    abstract void teamRun();


    @Override
    public void runOpMode() throws InterruptedException {
        setPhase("Initializing");
        baseInit();
        teamInit();

        setPhase("Waiting for start");

        while ( !isStarted() )
            teamIdle();

        setPhase("Running");
        teamRun();
    }


    public void teamSleep(long sleep_ms)
    {
        setOperation(String.format("Sleep(%d ms)", sleep_ms));

        long startTime_ms = System.currentTimeMillis();
        long stopTime_ms = startTime_ms + sleep_ms;

        while ( shouldOpModeKeepRunning() && System.currentTimeMillis() < stopTime_ms )
        {
            setStatus(String.format("nap time left: %d sec",
                    (stopTime_ms - System.currentTimeMillis())/1000));

            teamIdle();
            sleep(1);
        }
        setStatus("nap is over");
    }


    public void teamIdle()
    {
        if ( robot != null )
        {
            robot.healthCheck();
        }

        telemetry.update();
        idle();
    }


    public void setPhase(String phase) {
        this.phase = phase;
        setStep("");
    }

    public void setStep(String step) {
        this.step = step;
        setOperation("");
    }

    public void setOperation(String operation) {
        this.operation = operation;
        setStatus("");
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Team's version of opModeIsActive
    // Checks robot health

    public boolean shouldOpModeKeepRunning()
    {
        teamIdle();
        return opModeIsActive();
    }
}
