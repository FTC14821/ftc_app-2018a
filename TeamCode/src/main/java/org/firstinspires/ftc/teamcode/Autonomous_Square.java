package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "A: Square", group = "Tinkering")

public class Autonomous_Square extends AutonomousOpMode
{
        @Override
        void teamRun()
        {
                while(true)
                {
                        robot.inchmove(opmodeAction,  80, 1);
                        robot.turnLeft(opmodeAction, 90, 3);
                }
        }
}
