package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.scheduler.Action;
import org.firstinspires.ftc.teamcode.scheduler.ImmediateAction;

@TeleOp(name = "A: Land-Depot", group = "Autonomous")

public class Autonomous_Depot extends AutonomousOpMode
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
        int goldLocation = getGoldLocation(1000, 0);
        opmodeAction.setStatus("Gold location: %d", goldLocation);

        boolean debug = false;

        robot.resetCorrectHeading("Perpendicular to lander");
        robot.startMovingHookUp( 1).waitUntilFinished();
        opmodeAction.setStatus("Landed");

        robot.setDrivingPowers_raw(0, -1);
        robot.startMovingHookDown(1);
        teamSleep(1000, "Get unhooked");
        robot.stopDrivingWheels_raw();

        teamSleep(750, "Getting hook out of way");

        switch(goldLocation)
        {
            case 2:
                boopRightMineral();
                break;
            case 1:
                boopMiddleMineral();
                break;
            default:
                boopLeftMineral();
                break;
        }
        robot.startStopping().waitUntilFinished();
        robot.startMovingHookDown(1).waitUntilFinished();
    }

    private void boopLeftMineral()
    {
        Action action = new ImmediateAction("LeftMineral");
        action.setStatus("Moving away from lander");
        robot.startInchMove(2, MOVING_SPEED).waitUntilFinished();
        action.setStatus("Turning and moving towards mineral");
        robot.startTurningLeft(40).waitUntilFinished();
        robot.startInchMove(40, 0.6).waitUntilFinished();
        robot.startTurningLeft(100).waitUntilFinished();
        robot.startInchMoveBack(12, MOVING_SPEED).waitUntilFinished();
        robot.setSwingArmSpeed_raw(1);

        action.finish();
    }

    private void boopMiddleMineral()
    {
        // We start 20deg turned right from the lander
        Action action = new ImmediateAction("middleMineral");
        action.setStatus("Heading towards mineral");
        robot.startInchMove(42, 0.75).waitUntilFinished();
        robot.startTurningRight(180).waitUntilFinished();
        robot.startInchMove(5, MOVING_SPEED).waitUntilFinished();
        robot.setSwingArmSpeed_raw(1);

        action.finish();
    }

    private void boopRightMineral()
    {
        // We start 20deg turned right from the lander
        Action action = new ImmediateAction("rightMineral");
        action.setStatus("Moving away from lander");
        robot.startInchMove(2, MOVING_SPEED).waitUntilFinished();
        action.setStatus("Turning and moving towards mineral");
        robot.startTurningRight(40).waitUntilFinished();
        robot.startInchMove(40, 0.6).waitUntilFinished();
        robot.startTurningRight(100).waitUntilFinished();
        robot.startInchMoveBack(12, MOVING_SPEED).waitUntilFinished();
        robot.setSwingArmSpeed_raw(1);

        action.finish();
    }

}
