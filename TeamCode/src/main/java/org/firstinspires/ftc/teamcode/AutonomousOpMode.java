package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;

abstract class AutonomousOpMode extends BaseLinearOpMode
{
    @Override
    void teamInit() {
            super.teamInit();
            robot.setDrivingZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    abstract public void teamRun();
}
