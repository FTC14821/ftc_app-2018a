package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.robotcore.external.Func;

abstract class BaseOpMode extends OpMode {
    Robot robot;

    void sleep(int msec)
    {
        try {
            Thread.sleep(msec);
        } catch (InterruptedException e) {
        }
    }


    @Override
    public void init() {
        robot = new Robot(hardwareMap, telemetry);

        telemetry.addLine("GP1:")
                .addData("B", new Func<String>() {
                    @Override public String value() {
                        return String.format("ABXY=%s%s%s%s", bool2tf(gamepad1.a), bool2tf(gamepad1.b), bool2tf(gamepad1.x), bool2tf(gamepad1.y));
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


    @Override
    public void start() {
    }


    public double getArmSlowDown()
    {
        // Slowdown will be 1-->7
        double armSlowDown = 1 + gamepad2.left_trigger * 6;
        return armSlowDown;
    }

    @Override
    public void loop() {
        robot.setDrivingSlowdown(getDrivingSlowDown());
        robot.setArmSlowdown(getArmSlowDown());


        if(gamepad1.a)
        {
            robot.reverseRobotOrientation();

            //Wait until button is released before switching front and back
            while (gamepad1.a)
            {
                sleep(1);
            }
        }

        robot.setArmPower(-gamepad2.left_stick_y / getArmSlowDown());
        robot.setGrabberServoPosition(-gamepad2.right_stick_x);

    }

    public double getDrivingSlowDown()
    {
        // Slowdown will be 1-->5
        double drivingSlowDown = 1 + gamepad1.left_trigger * 4;
        return drivingSlowDown;
    }
}
