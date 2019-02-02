package org.firstinspires.ftc.teamcode;

import org.firstinspires.ftc.robotcore.external.Func;
import org.firstinspires.ftc.teamcode.scheduler.EventGamepad;
import org.firstinspires.ftc.teamcode.scheduler.RepeatedAction;

import static org.firstinspires.ftc.teamcode.scheduler.Utils.*;

abstract class TeleOpMode extends BaseLinearOpMode {
    EventGamepad gamepad1 = new EventGamepad("GP1", super.gamepad1);
    EventGamepad gamepad2 = new EventGamepad("GP2", super.gamepad2);

    @Override
    public void teamInit()
    {
        telemetry.addLine("GP1:")
                .addData("", new Func<String>() {
                    @Override public String value() {
                        return robot.saveTelemetryData("GP1", gamepad1.toString());
                    }});
        telemetry.addLine("GP2:")
                .addData("", new Func<String>() {
                    @Override public String value() {
                        return robot.saveTelemetryData("GP2", gamepad2.toString());
                    }});
    }

    @Override
    public void teamRun()
    {
        new RepeatedAction("TeleOpLoop")
        {
            @Override
            protected void doTask()
            {
                teleOpLoop();
            }
        }.start();
    }


    // Called over and over until stop button is pressed
    public void teleOpLoop()
    {
        if(gamepad2.left_bumper.isPressed)
            robot.leftBoxServo.setPosition(20 + robot.leftBoxServo.getPosition());

        if(gamepad2.right_bumper.isPressed)
            robot.rightBoxServo.setPosition(20 - robot.rightBoxServo.getPosition());

        if(gamepad2.left_trigger > 0)
            robot.armSpinServo.setPosition(robot.armSpinServo.getPosition() - Robot.MAX_ARM_SPIN_SERVO_CHANGE);

        if(gamepad2.right_trigger > 0)
            robot.armSpinServo.setPosition(robot.armSpinServo.getPosition() + Robot.MAX_ARM_SPIN_SERVO_CHANGE);

        if(gamepad1.a.onPress)
            log("GAMEPAD1 BOOKMARK-ButtonA");

        if(gamepad2.a.onPress)
            log("GAMEPAD2 BOOKMARK-ButtonA");

        if(gamepad2.x.isPressed)
            robot.allSafetysAreDisabled = true;
        else
            robot.allSafetysAreDisabled = false;

        if(gamepad2.y.onPress)
            new OngoingAction_CalibrateArmSwing();

        if(gamepad1.back.onPress)
            robot.reverseRobotOrientation();

        if(gamepad2.right_bumper.isPressed)
            robot.setArmExtensionPower(-gamepad2.left_stick_y / 4);
        else
            robot.setArmExtensionPower(-gamepad2.left_stick_y);

        robot.setSwingArmSpeed(-gamepad2.right_stick_y / 2);


        double hookPower = 1;
        if(gamepad2.dpad_up.isPressed)
            robot.setHookPower(hookPower);
        else if(gamepad2.dpad_down.isPressed)
            robot.setHookPower(-hookPower);
        else
            robot.setHookPower(0);

        if(gamepad1.right_bumper.onPress)
            robot.startSkoochingRight();

        if(gamepad1.left_bumper.onPress)
            robot.startSkoochingLeft();

        if(gamepad2.a.onPress)
            robot.startMovingArmToPosition(Robot.ARM_LOCATION.DUMP_GOLD);
    }
}
