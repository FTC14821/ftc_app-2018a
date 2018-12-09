package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "A: Land", group = "Tinkering")

public class Autonomous_Land extends AutonomousOpMode
{
    @Override
    void teamInit()
    {
        super.teamInit();

        setPhase("Activating vision");
        robot.getRobotVision().activate();

        setPhase("Calibrating...");
        robot.calibrateEverything();
    }

    int getGoldLocation()
    {
        long start = System.currentTimeMillis();
        long endTime = start + 5000;
        while(shouldOpModeKeepRunning() && robot.getRobotVision().objectColorsFromLeftToRight.size() != 2 && System.currentTimeMillis() < endTime)
        {
            teamIdle();
        }
        if(robot.getRobotVision().objectColorStringFromLeftToRight .equals("GS"))
            return 0;
        if(robot.getRobotVision().objectColorStringFromLeftToRight .equals("SG"))
            return 1;
        if(robot.getRobotVision().objectColorStringFromLeftToRight .equals("SS"))
            return 2;
        else
            return 0;
    }

    @Override
    void teamRun()
    {
        boolean debug = false;
        double movingSpeed = 0.5;

        robot.getRobotVision().activate();

        robot.hookUp(1, false);
        robot.turnRight(15, 1);
        // Make sure hook is out of the way
            robot.hookDown(1, false);
            teamSleep(250, "Hook to lower");
        robot.turnLeft(15, 1);
        robot.hookDown(1,true);
        /*
        robot.setDrivingPowers(0,-0.3);
        teamSleep(500, "Let robot stabilize");
        robot.resetCorrectHeading();
        robot.inchmove(10,movingSpeed );

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
        robot.getRobotVision().deactivate();

        // go to wall
        robot.stop(true);
        robot.inchmove(48,movingSpeed);
        //robot.stop(true);

        // turn towards crater and go (backwards) to depot
        robot.turnRight(155, 1);
        robot.inchmoveBack(28,movingSpeed, true);
        //robot.stop(true);

        // At depot. Drop marker
        robot.setSwingArmPower_raw(0.4, 1);
        while(robot.getArmSwingZone() != 3 && shouldOpModeKeepRunning())
            teamIdle();
        robot.setSwingArmPower_raw(0, 1);
        robot.startArmReset();
        // go to crater
        robot.inchmove(61, movingSpeed);
        robot.hookDown(1, true);
        robot.calibrateEverything();
        teamSleep(120*1000, "Keep telemetry");
        */


    }

    private void boopLeftMineral(double movingSpeed)
    {
        robot.turnLeft(45, 1);
        robot.inchmove(20,movingSpeed);
        robot.inchmoveBack(14,movingSpeed, false);
        robot.turnLeft(45,1);
    }

    private void boopMiddleMineral(double movingSpeed)
    {
        robot.inchmove(15,movingSpeed);
        robot.inchmoveBack(6,movingSpeed, false);
        robot.turnLeft(90,1);
    }

    private void boopRightMineral(double movingSpeed)
    {
        robot.turnRight(45,1);
        robot.inchmove(20,movingSpeed);
        robot.inchmoveBack(14,movingSpeed, false);
        robot.turnLeft(140,1);
    }

}
