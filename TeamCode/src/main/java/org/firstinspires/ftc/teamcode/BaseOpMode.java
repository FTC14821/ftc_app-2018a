package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Func;

abstract class BaseOpMode extends OpMode {
    DcMotor M0;
    DcMotor M1;
    DcMotor armExtensionMotor;
    Servo grabberServo;
    boolean armWasMovingLastLoop;
    boolean armFacesForward;
    double currentLeftPower = 0;
    double currentRightPower = 0;

    TeamImu teamImu;


    void sleep(int msec)
    {
        try {
            Thread.sleep(msec);
        } catch (InterruptedException e) {
        }
    }


    @Override
    public void init() {
        M0 = hardwareMap.dcMotor.get("M1");
        M1 = hardwareMap.dcMotor.get("M0");
        grabberServo = hardwareMap.servo.get("GrabberServo");
        armExtensionMotor = hardwareMap.dcMotor.get("ArmExtensionMotor");

        teamImu = new TeamImu().initialize(hardwareMap, telemetry);


        telemetry.addLine("GP1: ")
                .addData("B: ", new Func<String>() {
                    @Override public String value() {
                        return String.format("ABXY=%s%s%s%s", bool2tf(gamepad1.a), bool2tf(gamepad1.b), bool2tf(gamepad1.x), bool2tf(gamepad1.y));
                    }})
                .addData("LJS: ", new Func<String>() {
                    @Override public String value() {
                        return String.format("(%.2f, %.2f)", gamepad1.left_stick_x, gamepad1.left_stick_y);
                    }})
                .addData("LTrig/Bump: ", new Func<String>() {
                    @Override public String value() {
                        return String.format("%.2f/%s", gamepad1.left_trigger, bool2tf(gamepad1.left_bumper));
                    }})
                ;

        telemetry.addLine("GP2: ")
                .addData("B: ", new Func<String>() {
                    @Override public String value() {
                        return String.format("ABXY=%s%s%s%s", bool2tf(gamepad2.a), bool2tf(gamepad1.b), bool2tf(gamepad1.x), bool2tf(gamepad1.y));
                    }})
                .addData("LJS: ", new Func<String>() {
                    @Override public String value() {
                        return String.format("(%.2f, %.2f)", gamepad1.left_stick_x, gamepad1.left_stick_y);
                    }})
                .addData("LTrig/Bump: ", new Func<String>() {
                    @Override public String value() {
                        return String.format("%.2f/%s", gamepad1.left_trigger, bool2tf(gamepad1.left_bumper));
                    }})
        ;

        telemetry.addLine("Motors: ")

                .addData("Left: ", new Func<String>() {
                    @Override public String value() {
                        return String.format("%.2f@%d", currentLeftPower, getLeftMotor().getCurrentPosition());
                    }})
                .addData("Right: ", new Func<String>() {
                    @Override public String value() {
                        return String.format("%.2f@%d", currentRightPower, getRightMotor().getCurrentPosition());
                    }})
                .addData("M0: ", new Func<String>() {
                    @Override public String value() {
                        return String.format("%.2f@%d", M0.getPower(), M0.getCurrentPosition());
                    }})
                .addData("M1: ", new Func<String>() {
                    @Override public String value() {
                        return String.format("%.2f@%d", M1.getPower(), M1.getCurrentPosition());
                    }})
                ;
    }

    private String bool2tf(boolean b) {
        return b ? "t" : "f";
    }

    DcMotor getRightMotor()
    {
        if(armFacesForward)
        {
            return M1;
        }
        else
        {
            return M0;
        }
    }

    DcMotor getLeftMotor()
    {
        if(armFacesForward)
        {
            return M0;
        }
        else
        {
            return M1;
        }
    }

    @Override
    public void start() {
        teamImu.start();

        armWasMovingLastLoop = false;
        armFacesForward = true;

        setRightPower(0);
        setLeftPower(0);
    }

    public double getArmSlowDown()
    {
        // Slowdown will be 1-->7
        double armSlowDown = 1 + gamepad2.left_trigger * 6;
        return armSlowDown;
    }

    @Override
    public void loop() {
        if(gamepad2.left_stick_y == 0)
        {
            if(armWasMovingLastLoop)
            {
                armExtensionMotor.setPower(-.1*armExtensionMotor.getPower());
                sleep(100);
                //armExtensionMotor.setPower(-0.1);
                //sleep(100);
                armExtensionMotor.setPower(0);
            }
            armWasMovingLastLoop = false;
        }
        else
        {
            armWasMovingLastLoop = true;
        }

        if(gamepad1.a)
        {
            armFacesForward = !armFacesForward;
            //Wait until button is released before switching front and back
            while (gamepad1.a)
            {
                sleep(1);
            }
        }

        armExtensionMotor.setPower(-gamepad2.left_stick_y / getArmSlowDown());
        grabberServo.setPosition(-gamepad2.right_stick_x);

    }

    public double getDrivingSlowDown()
    {
        // Slowdown will be 1-->5
        double drivingSlowDown = 1 + gamepad1.left_trigger * 4;
        return drivingSlowDown;
    }

    void setLeftPower(double leftPower)
    {
        // Adjust power based on slowdown
        leftPower = leftPower / getDrivingSlowDown();

        if (armFacesForward == true)
        {
            M1.setPower(leftPower);
        }
        else
        {
            M0.setPower(-leftPower);
        }

        this.currentLeftPower = leftPower;
    }

    void setRightPower(double rightPower)
    {
        rightPower = rightPower / getDrivingSlowDown();

        if(armFacesForward == true)
        {
            M0.setPower(-rightPower);
        }
        else
        {
            M1.setPower(rightPower);
        }

        this.currentRightPower = rightPower;
    }

    void inchmove(double inches, double power)
    {
        double encoderClicksPerInch = 79.27;
        double encoderClicks = inches*encoderClicksPerInch;
        double stopPosition = M1.getCurrentPosition() + encoderClicks;
        double startingHeading = teamImu.getHeading();
        M1.getCurrentPosition();
        while (M1.getCurrentPosition() <= stopPosition)
        {
            //Heading is larger to the left
            double currentHeading = teamImu.getHeading();
            double headingError = startingHeading - currentHeading;
            if(headingError > 0.0)
            {
                //The current heading is too big so we turn to the right
                setPowerSteering(power, -0.1);
            }
            else if(headingError < 0.0)
            {
                //Assuming we steer too far to the right
                setPowerSteering(power,  0.1);
            }
            else
            {
                // Go Straight
                setLeftPower(power);
                setRightPower(power);
            }
        }

    }

    /**
     *
     * @param power Positive power is forward, negative power is backwards (between -1, 1)
     * @param steering Positive steering is to the right, negative steering is to the left (between -1, 1)
     *
     */
    void setPowerSteering(double power, double steering)
    {
        double powerRight, powerLeft;

        if(steering > 0)
        {
            // Turning to right: powerLeft should be more than powerRight
            powerRight = power;
            powerLeft = power + 2*steering;
        }
        else
        {
            // Turning to left: powerLeft should be less than powerRight
            // (powerRight is bigger after subtraction because left_stick_x < 0)
            powerLeft = power;
            powerRight = power - 2*steering;
        }

        setLeftPower(powerLeft);
        setRightPower(powerRight);
    }
}
