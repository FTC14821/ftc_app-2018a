package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.scheduler.OngoingAction;

@TeleOp(name = "A: Pacing", group = "Tinkering")

public class Autonomous_Pacing extends AutonomousOpMode
{
        @Override
        public void teamRun()
        {
                // Set up a pacing loop
                new OngoingAction("Pacing")
                {
                        @Override
                        protected void loop()
                        {
                                robot.startInchMove(70, 0.5).waitUntilFinished();
                                robot.startTurningLeft(180).waitUntilFinished();
                        }
                }.start();
        }
}
