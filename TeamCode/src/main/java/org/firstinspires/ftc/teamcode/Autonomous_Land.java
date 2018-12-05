package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "A: Land", group = "Tinkering")

public class Autonomous_Land extends AutonomousOpMode
{
    @Override
    void teamInit()
    {
        super.teamInit();
        robot.calibrateHook();
        robot.getRobotVision().activate();
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

        robot.getRobotVision().activate();

        //robot.hookUp(1, false);
        robot.turnRight(15, 1);
        // Make sure hook is out of the way
            robot.hookDown(1, false);
        //    teamSleep(1000);
        robot.turnLeft(15, 1);
        robot.setDrivingPowers(-0.3,-0.3);
        teamSleep(500);
        robot.resetCorrectHeading();
        robot.inchmove(10,1);

        switch(getGoldLocation())
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
        robot.getRobotVision().deactivate();
        robot.stop(true);
        if(debug)
        {
            while(shouldOpModeKeepRunning() && !gamepad1.a)
                teamIdle();
        }
        robot.inchmove(45,1);
        robot.stop(true);
        if(debug)
        {
            while(shouldOpModeKeepRunning() && !gamepad1.a)
                teamIdle();
        }
        robot.turnRight(120, 1);
        robot.inchmoveBack(27,1);
        robot.stop(true);
        robot.setSwingArmPower_raw(0.4, 1);
        while(robot.getArmSwingZone() != 3 && shouldOpModeKeepRunning())
            teamIdle();
        robot.setSwingArmPower_raw(0, 1);
        robot.startArmReset();
        robot.inchmove(42, 1);
        robot.turnRight(45,1);
        robot.inchmove(80,1);
        robot.turnLeft(135,1);
        robot.hookDown(1, true);
        teamSleep(120*1000);


    }

    private void boopLeftMineral()
    {
        robot.turnLeft(45,1);
        robot.inchmove(20,1);
        robot.inchmoveBack(20,1);
        robot.turnLeft(45,1);
    }

    private void boopMiddleMineral()
    {
        robot.inchmove(12,1);
        robot.inchmoveBack(12,1);
        robot.turnLeft(90,1);
    }

    private void boopRightMineral()
    {
        robot.turnRight(45,1);
        robot.inchmove(20,1);
        robot.inchmoveBack(20,1);
        robot.turnLeft(135,1);
    }

}
