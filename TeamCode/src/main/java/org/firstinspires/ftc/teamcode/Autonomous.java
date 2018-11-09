package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name = "T: Autonomous", group = "Tinkering")

public class Autonomous extends BaseLinearOpMode
{
        @Override
        void teamInit() {
                super.teamInit();
                robot.setDrivingZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        }

        @Override
        void teamRun()
        {
            /*
                while(opModeIsActive() ) {
                        inchmove(10, 0.5);
                        turnRight(90, 0.5);
                }
               */

                robot.hookUp(1, true);
                robot.getHeading();
                double startHeading = robot.getHeading();
                double endHeading = startHeading - 45;
                double degreesToGo = 45;
                robot.getRightMotor().setPower(0);
                robot.getLeftMotor().setPower(0.5);
                while(opModeIsActive() && robot.getHeading() > endHeading);
                {
                    degreesToGo = robot.getHeading() - endHeading;

                    setStatus(String.format("%. 1f degrees To Go ",
                            degreesToGo));
                    teamIdle();
                }
                robot.hookDown(1, false);
                inchmove( 20, 0.5);
                robot.stop();
        }
}
