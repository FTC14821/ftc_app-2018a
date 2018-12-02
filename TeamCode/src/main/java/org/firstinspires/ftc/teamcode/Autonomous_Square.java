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
                        robot.inchmove( 80, 0.3);
                        robot.turnLeft(90, 1);
                }
        }
}
