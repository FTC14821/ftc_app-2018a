package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "A: Land", group = "Tinkering")

public class Autonomous_Land extends AutonomousOpMode
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
        double movingSpeed = 0.5;

        ///////////
        // Ken : Shouldn't this go down below to right before the switch?
        //robot.getRobotVision(opmodeAction).activate(opmodeAction);
        ///////////

        robot.resetCorrectHeading(opmodeAction, "Perpendicular to lander");
        robot.hookUp( opmodeAction, 1, false);
        robot.turnRight(opmodeAction,15, 3);
        // Make sure hook is out of the way
            robot.hookDown(opmodeAction,1, false);
            teamSleep(opmodeAction,250, "Hook to lower");
        robot.turnLeft(opmodeAction,15, 3);

        robot.inchmove(opmodeAction,10,movingSpeed );

        ////////////
        // Right here? so the vision doesn't activate until we've straightened out?
        robot.getRobotVision(opmodeAction).activate(opmodeAction);
        ////////////

        switch(getGoldLocation())
        {
            case 2:
                boopRightMineral(movingSpeed);
                break;
            case 1:
                boopMiddleMineral(movingSpeed);
                break;
            default:
                boopLeftMineral(movingSpeed);
                break;
        }
        opmodeAction.setStatus("Done from boopping mineral");
        robot.getRobotVision(opmodeAction).deactivate(opmodeAction);

        // go to wall
        robot.stop(opmodeAction,true);
        robot.inchmove(opmodeAction,48,movingSpeed);
        opmodeAction.setStatus("At wall");
        //robot.stop(true);

        // turn towards crater and go (backwards) to depot
        robot.turnRight(opmodeAction,155, 3);
        robot.inchmoveBack(opmodeAction,28,movingSpeed, true);
        opmodeAction.setStatus("At Depot");
        //robot.stop(true);

        // At depot. Drop marker
        robot.setSwingArmPower(opmodeAction,1);
        while(robot.getArmSwingZone() != 3 && shouldOpModeKeepRunning(opmodeAction)) {
            opmodeAction.setStatus("Arm not yet in swingZone 3");
        }
        robot.setSwingArmPower(opmodeAction, 0);
        robot.startArmReset(opmodeAction);
        opmodeAction.setStatus("Dropped marker, heading to crater");

        // go to crater
        robot.inchmove(opmodeAction,61, movingSpeed);
        robot.hookDown(opmodeAction, 1, true);
        robot.calibrateEverything(opmodeAction);
    }

    private void boopLeftMineral(double movingSpeed)
    {
        robot.turnLeft(opmodeAction, 45, 3);
        robot.inchmove(opmodeAction, 20,movingSpeed);
        robot.inchmoveBack(opmodeAction, 14,movingSpeed, false);
        robot.turnLeft(opmodeAction, 45, 3);
    }

    private void boopMiddleMineral(double movingSpeed)
    {
        robot.inchmove(opmodeAction, 15,movingSpeed);
        robot.inchmoveBack(opmodeAction, 6,movingSpeed, false);
        robot.turnLeft(opmodeAction, 90, 3);
    }

    private void boopRightMineral(double movingSpeed)
    {
        robot.turnRight(opmodeAction, 45, 3);
        robot.inchmove(opmodeAction, 20,movingSpeed);
        robot.inchmoveBack(opmodeAction, 14,movingSpeed, false);
        robot.turnLeft(opmodeAction, 140, 3);
    }

}
