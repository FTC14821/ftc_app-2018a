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
        void teamRun() {
                while(opModeIsActive() ) {
                        inchmove(10, 0.5);
                        turnRight(90, 0.5);
                }
        }
}
