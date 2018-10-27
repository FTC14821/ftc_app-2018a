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
                teamSleep(5000);
                inchmove(50, 0.5);
                robot.stop();
                teamSleep(30000);
        }
}
