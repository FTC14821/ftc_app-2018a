package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "A: Pacing", group = "Tinkering")

public class Autonomous_Pacing extends AutonomousOpMode
{
        @Override
        void teamRun()
        {
                while(shouldOpModeKeepRunning(opmodeAction))
                {
                        robot.inchmove(opmodeAction,  70, 0.5);
                        robot.turnLeft(opmodeAction, 180);
                }
        }
}
