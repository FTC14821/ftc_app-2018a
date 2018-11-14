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
                        robot.inchmove( 20, 0.3);
                        robot.turnRight(90, 0.4);
                }
        }
}
