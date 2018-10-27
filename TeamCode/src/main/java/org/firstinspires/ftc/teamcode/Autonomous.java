package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name = "T: Autonomous", group = "Tinkering")

public class Autonomous extends BaseOpMode
{

        boolean firstTime=true;

        @Override
        public void init() {
                super.init();

                M0.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
                M1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        }

        @Override
        public void loop() {
                super.loop();

                if (firstTime)
                {
                        inchmove(50, 0.05);

                        setLeftPower(0);
                        setRightPower(0);

                }

                firstTime=false;
        }
}
