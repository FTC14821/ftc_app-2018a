package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;

abstract class AutonomousOpMode extends BaseLinearOpMode
{
    @Override
    void teamInit() {
            super.teamInit();
            robot.setDrivingZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    // What happens after Play is pressed
    abstract void teamRun();
}
