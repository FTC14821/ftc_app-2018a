package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "A: Land", group = "Tinkering")

public class Autonomous_Land extends AutonomousOpMode
{
        @Override
        void teamRun()
        {
                robot.hookUp(1, true);
                robot.getHeading();
                double startHeading = robot.getHeading();
                double endHeading = startHeading - 45;
                double degreesToGo = 45;
                robot.getRightMotor().setPower(0);
                robot.getLeftMotor().setPower(0.5);
                while(shouldOpModeKeepRunning() && robot.getHeading() > endHeading);
                {
                    degreesToGo = robot.getHeading() - endHeading;

                    setStatus(String.format("%. 1f degrees To Go ", degreesToGo));
                }
                robot.hookDown(1, false);
                robot.inchmove( 20, 0.5);
                robot.stop();

//                robot.hookUp(1, true);
//                robot.turnRight(45, 0.5);
//                robot.hookDown(1, false);
//                robot.inchmove( 20, 0.4);
//                robot.stop();
        }
}
