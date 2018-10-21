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


        telemetry.addLine("GP1 JS: ")
                .addData("L: ", new Func<String>() {
                    @Override public String value() {
                        return String.format("(%.2f, %.2f)", gamepad1.left_stick_x, gamepad1.left_stick_y);
                    }})
                .addData("R: ", new Func<String>() {
                    @Override public String value() {
                        return String.format("(%.2f, %.2f)", gamepad1.right_stick_x, gamepad1.right_stick_y);
                    }})
                ;

        telemetry.addLine("GP2 JS: ")
                .addData("L: ", new Func<String>() {
                    @Override public String value() {
                        return String.format("(%.2f, %.2f)", gamepad2.left_stick_x, gamepad2.left_stick_y);
                    }})
                .addData("R: ", new Func<String>() {
                    @Override public String value() {
                        return String.format("(%.2f, %.2f)", gamepad2.right_stick_x, gamepad2.right_stick_y);
                    }})
                ;

        telemetry.addLine("Motors: ")

                .addData("Left: ", new Func<String>() {
                    @Override public String value() {
                        return String.format("%.2f @%d", currentLeftPower, getLeftMotor().getCurrentPosition());
                    }})
                .addData("Right: ", new Func<String>() {
                    @Override public String value() {
                        return String.format("%.2f @%d", currentRightPower, getRightMotor().getCurrentPosition());
                    }})
                ;
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
        double drivingSlowDown = 1 + gamepad1.left_trigger * 4;
        return drivingSlowDown;
    }

    void setLeftPower(double leftPower)
    {
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
        double inchesPerRotation = 3.14159265 * 3.75;
        double rotations = inches / inchesPerRotation;
        double encoderTicksPerRotation = 1000;
        double stopPosition = M0.getCurrentPosition() + rotations * encoderTicksPerRotation;
        double startingHeading = teamImu.getHeading();
        M0.getCurrentPosition();
        while (M0.getCurrentPosition() <= stopPosition)
        {
            double currentHeading = teamImu.getHeading();
            double headingError = currentHeading - startingHeading;
            if(headingError > 0.0)
            {
                //Assuming we steer too far to the right
                setRightPower(power);
                setLeftPower(0.9 * power);
            }
            else if(headingError < 0.0)
            {
                //Assuming we steer too far to the left
                setLeftPower(power);
                setRightPower(0.9 * power);
            }
            else
            {
                setLeftPower(power);
                setRightPower(power);
            }
        }

    }
}
