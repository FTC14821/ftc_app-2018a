package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.scheduler.Action;
import org.firstinspires.ftc.teamcode.scheduler.ImmediateAction;
import org.firstinspires.ftc.teamcode.scheduler.Utils;

@TeleOp(name = "A: Land-Crater", group = "Autonomous")

public class Autonomous_Crater extends AutonomousOpMode
{
    private static final double MOVING_SPEED = 0.8;

    @Override
    void teamInit()
    {
        super.teamInit();

        robot.getRobotVision().activate();

        robot.startCalibratingEverything();
    }

    int getGoldLocation(long timeout_ms, int resultIfTimeout)
    {
        Action action = new ImmediateAction("GetGoldLocation");

        long start = System.currentTimeMillis();
        long endTime = start + timeout_ms;
        RobotVision robotVision = robot.getRobotVision();

        while(robotVision.objectColorsFromLeftToRight.size() != 2 && System.currentTimeMillis() < endTime)
        {
            action.setStatus("Seeing %d objects instead of 2: %s",
                    robotVision.objectColorsFromLeftToRight.size(), robotVision.objectColorStringFromLeftToRight );
            action.actionSleep(1, "Waiting to see if Vision sees correct objects");
        }
        action.finish("Final objects: %s", robotVision.objectColorStringFromLeftToRight);

        if(robotVision.objectColorStringFromLeftToRight .equals("GS"))
            return 0;
        if(robotVision.objectColorStringFromLeftToRight .equals("SG"))
            return 1;
        if(robotVision.objectColorStringFromLeftToRight .equals("SSS"))
            return 2;
        if(robotVision.objectColorStringFromLeftToRight .equals("SS"))
            return 2;
        if(robotVision.objectColorStringFromLeftToRight .equals("S"))
            return 0;
        if(robotVision.objectColorStringFromLeftToRight .equals("G"))
            return 1;
        else
        {
            action.log("Vision timed out, returning default (%d)", resultIfTimeout);
            return resultIfTimeout;
        }
    }

    @Override
    public void teamRun()
    {
        int goldLocation = -1;

        opmodeAction.setStatus("Gold location: %d", goldLocation);

        robot.resetCorrectHeading("Perpendicular to lander");
        robot.startMovingHookUp( 1).waitUntilFinished();
        opmodeAction.setStatus("Landed");
        if (robot.getRobotVision().objectColorStringFromLeftToRight.contains("G"))
            goldLocation=1;

        robot.startTurningRight(37).waitUntilFinished();
        robot.startMovingHookDown(1);
        teamSleep(1500, "Getting hook out of way");

        if (goldLocation==-1 && robot.getRobotVision().objectColorStringFromLeftToRight.contains("G"))
            goldLocation=2;

        if (goldLocation==-1)
            goldLocation=0;

        Utils.log("Found gold: %d", goldLocation);
        switch(goldLocation)
        {
            case 0: boopLeftMineral();break;
            case 1: boopMiddleMineral();break;
            case 2: boopRightMineral();
        }

        robot.startStopping();
        robot.startMovingHookDown(1);
        opmodeAction.waitForChildren();
    }

    private void boopLeftMineral()
    {
        Action action = new ImmediateAction("LeftMineral");

        action.setStatus("Heading towards mineral");
        robot.startTurningLeft(57).waitUntilFinished();
        robot.startInchMove(34, MOVING_SPEED).waitUntilFinished();
        action.finish();
    }

    private void boopMiddleMineral()
    {
        // We start 20deg turned right from the lander
        Action action = new ImmediateAction("middleMineral");
        action.setStatus("Turing towards mineral");
        robot.startTurningLeft(37).waitUntilFinished();
        action.setStatus("Heading towards mineral");
        robot.startInchMove(25, MOVING_SPEED).waitUntilFinished();

        action.finish();
    }

    private void boopRightMineral()
    {
        // We start 20deg turned right from the lander
        Action action = new ImmediateAction("rightMineral");
        action.setStatus("Moving away from lander");
        robot.startInchMove(42, MOVING_SPEED).waitUntilFinished();

        action.finish();
    }

}
