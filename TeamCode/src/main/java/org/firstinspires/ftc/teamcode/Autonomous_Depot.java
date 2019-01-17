package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "A: Land-Depot", group = "Autonomous")

public class Autonomous_Depot extends AutonomousOpMode
{
    @Override
    void teamInit()
    {
        super.teamInit();

        robot.getRobotVision(opmodeAction).activate(opmodeAction);

        robot.calibrateEverything(opmodeAction);
    }

    int getGoldLocation()
    {
        ActionTracker action = opmodeAction.startChildAction("GetGoldLocation", null);

        long start = System.currentTimeMillis();
        long endTime = start + 1000;
        RobotVision robotVision = robot.getRobotVision(action);

        while(shouldOpModeKeepRunning(action) && robotVision.objectColorsFromLeftToRight.size() != 2 && System.currentTimeMillis() < endTime)
        {
            action.setStatus("Seeing %d objects instead of 2: %s",
                    robotVision.objectColorsFromLeftToRight.size(), robotVision.objectColorStringFromLeftToRight );
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
            return 2;
        else
            return 0;
    }

    @Override
    void teamRun()
    {
        int goldLocation = getGoldLocation();
        opmodeAction.setStatus("Gold location: %d", goldLocation);

        boolean debug = false;
        double movingSpeed = 1;

        robot.resetCorrectHeading(opmodeAction, "Perpendicular to lander");
        robot.hookUp( opmodeAction, 1, true);
        opmodeAction.setStatus("Landed");

        robot.setDrivingPowers(opmodeAction, 0, -1);
        robot.hookDown(opmodeAction, 1, false);
        teamSleep(opmodeAction, 1000, "Get unhooked");
        robot.stop(opmodeAction, false);

        teamSleep(opmodeAction, 750, "Getting hook out of way");

        switch(goldLocation)
        {
            case 2:
                boopRightMineral(opmodeAction, movingSpeed);
                break;
            case 1:
                boopMiddleMineral(opmodeAction, movingSpeed);
                break;
            default:
                boopLeftMineral(opmodeAction, movingSpeed);
                break;
        }
        robot.stop(opmodeAction, true);
        robot.hookDown(opmodeAction, 1, true);
    }

    private void boopLeftMineral(ActionTracker callingAction, double movingSpeed)
    {
        ActionTracker action = callingAction.startChildAction("LeftMineral", null);
        action.setStatus("Moving away from lander");
        robot.inchmove(opmodeAction, 2, movingSpeed);
        action.setStatus("Turning and moving towards mineral");
        robot.turnLeft(opmodeAction, 40);
        robot.inchmove(opmodeAction, 40, 0.6);
        robot.turnLeft(opmodeAction, 100);
        robot.inchmoveBack(opmodeAction, 12, movingSpeed);
        robot.setSwingArmPower(opmodeAction, 1);

        action.finish();
    }

    private void boopMiddleMineral(ActionTracker callingAction, double movingSpeed)
    {
        // We start 20deg turned right from the lander
        ActionTracker action = callingAction.startChildAction("middleMineral", null);
        action.setStatus("Heading towards mineral");
        robot.inchmove(opmodeAction, 42, 0.75);
        robot.turnRight(opmodeAction, 180);
        robot.inchmove(opmodeAction, 5, movingSpeed);
        robot.setSwingArmPower(opmodeAction, 1);

        action.finish();
    }

    private void boopRightMineral(ActionTracker callingAction, double movingSpeed)
    {
        // We start 20deg turned right from the lander
        ActionTracker action = callingAction.startChildAction("rightMineral", null);
        action.setStatus("Moving away from lander");
        robot.inchmove(opmodeAction, 2, movingSpeed);
        action.setStatus("Turning and moving towards mineral");
        robot.turnRight(opmodeAction, 40);
        robot.inchmove(opmodeAction, 40, 0.6);
        robot.turnRight(opmodeAction, 100);
        robot.inchmoveBack(opmodeAction, 12, movingSpeed);
        robot.setSwingArmPower(opmodeAction, 1);

        action.finish();
    }

}
