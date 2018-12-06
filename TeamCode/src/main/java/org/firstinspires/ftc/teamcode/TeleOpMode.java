package org.firstinspires.ftc.teamcode;

import org.firstinspires.ftc.robotcore.external.Func;

abstract class TeleOpMode extends BaseLinearOpMode {

    AbstractOngoingAction armResetAction = null;

    @Override
    public void teamInit() {
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
        teleOpStart();

        while (shouldOpModeKeepRunning())
        {
            teleOpLoop();
        }
    }


    // Called over and over until stop button is pressed
    public void teleOpLoop() {
        if(gamepad2.x)
            robot.allSafetysAreDisabled = true;
        else
            robot.allSafetysAreDisabled = false;

        if(gamepad2.y)
        {
            robot.calibrateEverything();

            while(shouldOpModeKeepRunning() && gamepad2.y)
                teamIdle();
        }

        if(gamepad1.back)
        {
            robot.reverseRobotOrientation();

            //Wait until button is released before switching front and back
            while(shouldOpModeKeepRunning() && gamepad1.back)
                teamIdle();
        }

        if(gamepad2.left_stick_button || gamepad2.right_stick_button)
        {
            armResetAction = robot.startArmReset();
        }

        if ( armResetAction != null && armResetAction.isDone() )
            armResetAction = null;


        // Control Arm if arm-reset is not happening
        if(armResetAction == null)
        {
            if (gamepad2.right_bumper)
                robot.setArmExtensionPower(-gamepad2.left_stick_y / 4);
            else
                robot.setArmExtensionPower(-gamepad2.left_stick_y);

            robot.setSwingArmPower(-gamepad2.right_stick_y);
        }


        double hookPower = 1;
        if(gamepad2.dpad_up)
            robot.setHookPower(hookPower);
        else if(gamepad2.dpad_down)
            robot.setHookPower(-hookPower);
        else
            robot.setHookPower(0);

        if(gamepad1.right_bumper)
        {
            robot.skoochRight();

            //Wait until button is released before switching front and back
            while(shouldOpModeKeepRunning() && gamepad1.right_bumper)
                teamIdle();
        }

        if(gamepad1.left_bumper)
        {
            robot.skoochLeft();

            //Wait until button is released before switching front and back
            while(shouldOpModeKeepRunning() && gamepad1.left_bumper)
                teamIdle();
        }

        if(gamepad2.right_bumper)
        {
            robot.hookUp(hookPower, false);

            //Wait until button is released before switching front and back
            while(shouldOpModeKeepRunning() && gamepad2.right_bumper)
                teamIdle();
        }

        if(gamepad1.dpad_left)
        {
            robot.resetCorrectHeading();
            robot.turnLeft(90,1);

            //Wait until button is released before switching front and back
            while(shouldOpModeKeepRunning() && gamepad2.dpad_left)
                teamIdle();
        }
        if(gamepad1.dpad_right)
        {
            robot.resetCorrectHeading();
            robot.turnRight(90,1);

            //Wait until button is released before switching front and back
            while(shouldOpModeKeepRunning() && gamepad2.dpad_right)
                teamIdle();
        }

        if(gamepad1.dpad_up)
        {
            robot.resetCorrectHeading();
            robot.inchmove(48, .5);
            //Wait until button is released before switching front and back
            while(shouldOpModeKeepRunning() && gamepad2.dpad_up)
                teamIdle();
        }
        if(gamepad1.dpad_down)
        {
            robot.resetCorrectHeading();
            robot.inchmoveBack(48, 0.5, true);

            //Wait until button is released before switching front and back
            while(shouldOpModeKeepRunning() && gamepad2.dpad_down)
                teamIdle();
        }
    }
}
