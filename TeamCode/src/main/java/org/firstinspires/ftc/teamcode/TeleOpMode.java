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
                .addData("B", new Func<String>() {
                    @Override public String value() {
                        return String.format("ABXY=%s%s%s%s", bool2tf(gamepad1.a), bool2tf(gamepad1.b), bool2tf(gamepad1.x), bool2tf(gamepad1.y));
                    }})
                .addData("D", new Func<String>() {
                    @Override public String value() {
                        return String.format("LUDR=%s%s%s%s", bool2tf(gamepad1.dpad_left), bool2tf(gamepad1.dpad_up), bool2tf(gamepad1.dpad_down), bool2tf(gamepad1.dpad_right));
                    }})
                .addData("LJS", new Func<String>() {
                    @Override public String value() {
                        return String.format("(%.2f, %.2f)", gamepad1.left_stick_x, gamepad1.left_stick_y);
                    }})
                .addData("LTrig/Bump", new Func<String>() {
                    @Override public String value() {
                        return String.format("%.2f/%s", gamepad1.left_trigger, bool2tf(gamepad1.left_bumper));
                    }})
                ;

        telemetry.addLine("GP2:")
                .addData("B", new Func<String>() {
                    @Override public String value() {
                        return String.format("ABXY=%s%s%s%s", bool2tf(gamepad2.a), bool2tf(gamepad2.b), bool2tf(gamepad2.x), bool2tf(gamepad2.y));
                    }})
                .addData("D", new Func<String>() {
                    @Override public String value() {
                        return String.format("LUDR=%s%s%s%s", bool2tf(gamepad2.dpad_left), bool2tf(gamepad2.dpad_up), bool2tf(gamepad2.dpad_down), bool2tf(gamepad2.dpad_right));
                    }})
                .addData("LJS", new Func<String>() {
                    @Override public String value() {
                        return String.format("(%.2f, %.2f)", gamepad2.left_stick_x, gamepad2.left_stick_y);
                    }})
                .addData("LTrig/Bump", new Func<String>() {
                    @Override public String value() {
                        return String.format("%.2f/%s", gamepad2.left_trigger, bool2tf(gamepad2.left_bumper));
                    }})
        ;
    }

    private String bool2tf(boolean b) {
        return b ? "t" : "f";
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

        if(gamepad1.dpad_left)
        {
            robot.resetCorrectHeading(gamepad1Action, "DPad: Turning left from current position");
            robot.turnLeft(gamepad1Action,90,1);

            //Wait until button is released before switching front and back
            while(shouldOpModeKeepRunning(gamepad1Action) && gamepad1.dpad_left)
            {}
        }
        if(gamepad1.dpad_right)
        {
            robot.resetCorrectHeading(gamepad1Action, "DPad: Turning right from current position");
            robot.turnRight(gamepad1Action, 90,1);

            //Wait until button is released before switching front and back
            while(shouldOpModeKeepRunning(gamepad1Action) && gamepad1.dpad_right)
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
