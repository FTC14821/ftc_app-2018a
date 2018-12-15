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
        long endTime = start + 5000;
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
        if(robotVision.objectColorStringFromLeftToRight .equals("SS"))
            return 2;
        else
            return 0;
    }

    @Override
    void teamRun()
    {
        boolean debug = false;
        double movingSpeed = 1;

        ///////////
        //Ken : Shouldn't this go down below to right before the switch?
        //robot.getRobotVision(opmodeAction).activate(opmodeAction);
        ///////////

        robot.resetCorrectHeading(opmodeAction, "Perpendicular to lander");
        robot.hookUp( opmodeAction, 1, true);
        opmodeAction.setStatus("Landed");

        teamSleep(opmodeAction, 750, "Looking at minerals");
        int goldLocation = getGoldLocation();
        robot.getRobotVision(opmodeAction).deactivate(opmodeAction);

        robot.setDrivingPowers(opmodeAction, 0, -1);
        teamSleep(opmodeAction, 250, "Turn off of the hook");
        robot.hookDown(opmodeAction, 1, false);

        switch(goldLocation)
        {
            case 2:
                boopRightMineralAndGoToWall(movingSpeed);
                break;
            case 1:
                boopMiddleMineralAndGoToWall(movingSpeed);
                break;
            default:
                boopLeftMineralAndGoToWall(movingSpeed);
                break;
        }

        opmodeAction.setStatus("Done from boopping mineral and getting to wall");

        robot.pushIntoWall(opmodeAction);
        robot.resetCorrectHeading(opmodeAction, "Aligned with the wall");
        robot.inchmoveBack(opmodeAction, 2, 1);

        // Turn towards crater
        robot.turnLeft(opmodeAction, 90);
        robot.inchmove(opmodeAction, 20, 0.5);

        //Turn towards crater and go (backwards) to depot
        robot.turnRight(opmodeAction,90);
        robot.inchmoveBack(opmodeAction,28, movingSpeed);
        opmodeAction.setStatus("At Depot");

        //At depot. Drop marker
        robot.setSwingArmPower(opmodeAction,1);
        while(robot.getArmSwingZone() != 3 && shouldOpModeKeepRunning(opmodeAction)) {
            opmodeAction.setStatus("Arm not yet in swingZone 3");
        }
        robot.setSwingArmPower(opmodeAction, 0);
        robot.startArmReset(opmodeAction);
        opmodeAction.setStatus("Dropped marker, heading to crater");

        //Go to crater
        robot.inchmove(opmodeAction,61, movingSpeed);
        robot.hookDown(opmodeAction, 1, true);
        robot.calibrateEverything(opmodeAction);
    }

    private void boopLeftMineralAndGoToWall(double movingSpeed)
    {
        robot.turnLeft(opmodeAction, 45);
        robot.inchmove(opmodeAction, 20,movingSpeed);
        robot.inchmoveBack(opmodeAction, 14,movingSpeed);
        robot.turnLeft(opmodeAction, 45);
    }

    private void boopMiddleMineralAndGoToWall(double movingSpeed)
    {
        robot.inchmove(opmodeAction, 15,movingSpeed);
        robot.inchmoveBack(opmodeAction, 6,movingSpeed);
        robot.turnLeft(opmodeAction, 90);
    }

    private void boopRightMineralAndGoToWall(double movingSpeed)
    {
        robot.turnRight(opmodeAction, 45);
        robot.inchmove(opmodeAction, 20,movingSpeed);
        robot.inchmoveBack(opmodeAction, 14,movingSpeed);
        robot.turnLeft(opmodeAction, 140);
    }

}
