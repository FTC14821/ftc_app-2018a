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
    }

    @Override
    void teamRun()
    {
        boolean debug = false;

        robot.hookUp(1, false);
        robot.turnRight(15, 1);
        robot.hookDown(1, false);
        robot.inchmove( 10, 0.75);
        robot.turnLeft(15, 1);
        robot.inchmove(15, 0.75);
        robot.inchmoveBack(10,0.75);
        robot.stop(true);
        if(debug)
        {
            while(shouldOpModeKeepRunning() && !gamepad1.a)
                teamIdle();
        }
        robot.turnLeft(75,1);
        robot.inchmove(55,1);
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
}
