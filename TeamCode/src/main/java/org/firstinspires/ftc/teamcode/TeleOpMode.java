package org.firstinspires.ftc.teamcode;

import org.firstinspires.ftc.robotcore.external.Func;

abstract class TeleOpMode extends BaseLinearOpMode {

    ActionTracker gamepad1Action, gamepad2Action;
    AbstractOngoingAction armResetAction = null;

    @Override
    public void teamInit() {
        gamepad1Action = opmodeAction.startChildAction("GamePad1", null);
        gamepad2Action = opmodeAction.startChildAction("GamePad2", null);

        telemetry.addLine("GP1:")
                .addData("", new Func<String>() {
                    @Override public String value() {
                        return saveTelemetryData("GP1", gamepad1.toString());
                    }});
        telemetry.addLine("GP2:")
                .addData("", new Func<String>() {
                    @Override public String value() {
                        return saveTelemetryData("GP2", gamepad2.toString());
                    }});
    }

    // First TeleOp thing that happens after play button is pressed
    public void teleOpStart()
    {
    }

    // Convert LinearOpMode into OpMode
    @Override
    void teamRun()
    {
        opmodeAction.setStatus("teleOpStart()");
        teleOpStart();

        opmodeAction.setStatus("Running and looping");
        while (shouldOpModeKeepRunning(opmodeAction))
        {
            teleOpLoop();
        }
    }


    // Called over and over until stop button is pressed
    public void teleOpLoop() {
        if (gamepad1.a)
            gamepad2Action.startImmediateChildAction("GAMEPAD1 ALERT", null);

        if (gamepad2.a)
            gamepad2Action.startImmediateChildAction("GAMEPAD2 ALERT", null);

        if(gamepad2.x)
            robot.allSafetysAreDisabled = true;
        else
            robot.allSafetysAreDisabled = false;

        if(gamepad2.y)
        {
            robot.calibrateEverything(gamepad2Action);

            while(shouldOpModeKeepRunning(gamepad2Action) && gamepad2.y)
            {}
        }

        if(gamepad1.back)
        {
            robot.reverseRobotOrientation(gamepad1Action);

            //Wait until button is released before switching front and back
            while(shouldOpModeKeepRunning(gamepad1Action) && gamepad1.back)
            {}
        }

        if(gamepad2.left_stick_button || gamepad2.right_stick_button)
        {
            armResetAction = robot.startArmReset(gamepad2Action);
        }

        if ( armResetAction != null && armResetAction.isDone() )
            armResetAction = null;


        // Control Arm if arm-reset is not happening
        if(armResetAction == null)
        {
            if (gamepad2.right_bumper)
                robot.setArmExtensionPower(gamepad2Action, -gamepad2.left_stick_y / 4);
            else
                robot.setArmExtensionPower(gamepad2Action, -gamepad2.left_stick_y);

            robot.setSwingArmPower(gamepad2Action, -gamepad2.right_stick_y);
        }


        double hookPower = 1;
        if(gamepad2.dpad_up)
            robot.setHookPower(gamepad2Action, hookPower);
        else if(gamepad2.dpad_down)
            robot.setHookPower(gamepad2Action, -hookPower);
        else
            robot.setHookPower(gamepad2Action,0);

        if(gamepad1.right_bumper)
        {
            robot.skoochRight(gamepad1Action);

            //Wait until button is released before switching front and back
            while(shouldOpModeKeepRunning(gamepad1Action) && gamepad1.right_bumper)
            {}
        }

        if(gamepad1.left_bumper)
        {
            robot.skoochLeft(gamepad1Action);

            //Wait until button is released before switching front and back
            while(shouldOpModeKeepRunning(gamepad1Action) && gamepad1.left_bumper)
            {}
        }

        if(gamepad2.right_bumper)
        {
            robot.hookUp(gamepad2Action, hookPower, false);

            //Wait until button is released before switching front and back
            while(shouldOpModeKeepRunning(gamepad2Action) && gamepad2.right_bumper)
            {}
        }


        /*
        if(gamepad1.dpad_up)
        {
            robot.resetCorrectHeading();
            robot.inchmove(48, .5);
            //Wait until button is released before switching front and back
            while(shouldOpModeKeepRunning() && gamepad1.dpad_up)
                teamIdle();
        }
        if(gamepad1.dpad_down)
        {
            robot.resetCorrectHeading();
            robot.inchmoveBack(48, 0.5, true);

            //Wait until button is released before switching front and back
            while(shouldOpModeKeepRunning() && gamepad1.dpad_down)
                teamIdle();
        }
        */
    }
}
