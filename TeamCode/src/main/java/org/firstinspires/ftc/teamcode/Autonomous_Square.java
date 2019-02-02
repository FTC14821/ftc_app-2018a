package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "A: Square", group = "Tinkering")

public class Autonomous_Square extends AutonomousOpMode
{
        @Override
        public void teamRun()
        {
                while(shouldOpModeKeepRunning())
                {
                        robot.startInchMove(10, 0.5);
                        robot.startTurningLeft(90);
                }
        }
}
