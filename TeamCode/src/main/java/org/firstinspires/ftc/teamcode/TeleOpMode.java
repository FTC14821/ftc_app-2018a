package org.firstinspires.ftc.teamcode;

import org.firstinspires.ftc.robotcore.external.Func;
import org.firstinspires.ftc.teamcode.scheduler.EndableAction;
import org.firstinspires.ftc.teamcode.scheduler.RepeatedAction;
import org.firstinspires.ftc.teamcode.scheduler.Scheduler;

import static org.firstinspires.ftc.teamcode.scheduler.Utils.*;

abstract class TeleOpMode extends BaseLinearOpMode {
    EndableAction armMovementAction=null;

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
        robot.allSafetysAreDisabled = gamepad2.x.isPressed;

        if(gamepad1.x.onPress)
            Scheduler.get().abortAllEndableActions("Gamepad1 aborting actions");

        if(gamepad2.y.onPress)
            robot.startCalibratingEverything();

        if(gamepad2.left_stick_x > 0.1)
            robot.setBoxTiltServoPosition_raw(robot.boxTiltServo.getPosition() + 0.05);
        if(gamepad2.left_stick_x < 0.1)
            robot.setBoxTiltServoPosition_raw(robot.boxTiltServo.getPosition() - 0.05);

        robot.setLeftBoxServo_teleop(gamepad2.left_bumper.isPressed);
        robot.setRightBoxServo_teleop(gamepad2.right_bumper.isPressed);

        if(gamepad2.left_trigger > 0)
            robot.setArmSpinServoPosition_teleop(robot.armSpinServo.getPosition() - Robot.MAX_ARM_SPIN_SERVO_CHANGE);

        if(gamepad2.right_trigger > 0)
            robot.setArmSpinServoPosition_teleop(robot.armSpinServo.getPosition() + Robot.MAX_ARM_SPIN_SERVO_CHANGE);

        if(gamepad1.a.onPress)
            log("GAMEPAD1 BOOKMARK-ButtonA");

        if(gamepad2.a.onPress)
            log("GAMEPAD2 BOOKMARK-ButtonA");

        robot.setArmExtensionPower_teleop(-gamepad2.left_stick_y);

        robot.setSwingArmSpeed_teleop(-gamepad2.right_stick_y / 2);


        double hookPower = 1;
        if(gamepad1.dpad_up.isPressed)
            robot.setHookPower_teleop(hookPower);
        else if(gamepad1.dpad_down.isPressed)
            robot.setHookPower_teleop(-hookPower);
        else
            robot.setHookPower_teleop(0);

        if(gamepad1.right_bumper.onPress)
            robot.startSkoochingRight();

        if(gamepad1.left_bumper.onPress)
            robot.startSkoochingLeft();

        // Move the arm if it's not already moving
        if(armMovementAction == null || armMovementAction.hasFinished())
        {
            if (gamepad2.dpad_up.onPress)
                armMovementAction = new OngoingAction_MoveArmToPosition(Robot.ARM_LOCATION.BACK_LEVEL).start();
            if (gamepad2.dpad_down.onPress)
            {
                if (robot.currentArmLocation == Robot.ARM_LOCATION.MINING)
                    armMovementAction = new OngoingAction_MoveArmToPosition(Robot.ARM_LOCATION.FAR_MINING).start();
                else
                    armMovementAction = new OngoingAction_MoveArmToPosition(Robot.ARM_LOCATION.MINING).start();
            }

            if (gamepad2.dpad_left.onPress)
                armMovementAction = new OngoingAction_MoveArmToPosition(Robot.ARM_LOCATION.SILVER_DUMP).start();
            if (gamepad2.dpad_right.onPress)
                armMovementAction = new OngoingAction_MoveArmToPosition(Robot.ARM_LOCATION.GOLD_DUMP).start();
            if (gamepad2.a.onPress)
                armMovementAction = new OngoingAction_MoveArmToPosition(Robot.ARM_LOCATION.FOLDED).start();
        }
    }
}
