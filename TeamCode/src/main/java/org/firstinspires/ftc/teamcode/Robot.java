package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Func;
import org.firstinspires.ftc.robotcore.external.Telemetry;

public class Robot  {
    DcMotor M0;
    DcMotor M1;
    DcMotor armExtensionMotor;
    DcMotor hookMotor;
    Servo grabberServo;
    Servo mineralPlowServo;

    boolean motorsInFront = true;
    double currentLeftPower = 0;
    double currentRightPower = 0;

    TeamImu teamImu;
    private double drivingSlowDown=1;

    private double armSlowdown= 1;

    private double hookSlowdown= 1;

    String motorCommand="";


    public Robot(HardwareMap hardwareMap, Telemetry telemetry)
    {
        M0 = hardwareMap.dcMotor.get("M0");
        M1 = hardwareMap.dcMotor.get("M1");

        mineralPlowServo = hardwareMap.servo.get("MineralPlowServo");
        grabberServo = hardwareMap.servo.get("GrabberServo");
        hookMotor = hardwareMap.dcMotor.get("HookMotor");
        armExtensionMotor = hardwareMap.dcMotor.get("ArmExtensionMotor");
        armExtensionMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        hookMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        teamImu = new TeamImu().initialize(hardwareMap, telemetry);

        setRobotOrientation(true);
        stop();


        telemetry.addLine("Robot: ")

                .addData("Cmd", new Func<String>() {
                    @Override public String value() {
                        return motorCommand;
                    }})
                .addData("Orientation", new Func<String>() {
                    @Override public String value() {
                        return motorsInFront ? "MotorsFront" : "MotorsBack";
                    }})
                ;

        telemetry.addLine("Motors: ")
                .addData("L", new Func<String>() {
                    @Override public String value() {
                        return String.format("%.1f@%d", currentLeftPower, getLeftMotor().getCurrentPosition());
                    }})
                .addData("R", new Func<String>() {
                    @Override public String value() {
                        return String.format("%.1f@%d", currentRightPower, getRightMotor().getCurrentPosition());
                    }})
                .addData("M0", new Func<String>() {
                    @Override public String value() {
                        return String.format("%.1f@%d", M0.getPower(), M0.getCurrentPosition());
                    }})
                .addData("M1", new Func<String>() {
                    @Override public String value() {
                        return String.format("%.1f@%d", M1.getPower(), M1.getCurrentPosition());
                    }})
                .addData("Plow", new Func<String>() {
                    @Override public String value() {
                        return String.format("%.1f", mineralPlowServo.getPosition());
                    }})
                ;
    }



    public void stop()
    {
        motorCommand = "stop";
        setRightPower(0);
        setLeftPower(0);
    }

    public void driveStraight(double power)
    {
        motorCommand = String.format("Straight(%.2f)",  power);
        setLeftPower(power);
        setRightPower(power);

    }
    /**
     * Turn robot with opposite power to wheels
     * @param power (-1..1) negative means to the left
     */
    public void spin(double power)
    {
        motorCommand = String.format("Spin%s(%.2f)", power > 0 ? "Right" : "Left", power);
        setLeftPower(power);
        setRightPower(-power);
    }

    public void setDrivingPowers(double leftPower, double rightPower)
    {
        motorCommand = String.format("Set(%.2f,%.2f)", leftPower, rightPower);
        setLeftPower(leftPower);
        setRightPower(rightPower);
    }

    public void setDrivingZeroPowerBehavior(DcMotor.ZeroPowerBehavior behavior)
    {
        M0.setZeroPowerBehavior(behavior);
        M1.setZeroPowerBehavior(behavior);
    }

    DcMotor getRightMotor()
    {
        if(motorsInFront)
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
        if(motorsInFront)
        {
            return M0;
        }
        else
        {
            return M1;
        }
    }

    public int getWheelPosition()
    {
        return M0.getCurrentPosition();
    }


    public double getHeading()
    {
        return teamImu.getHeading();
    }


    public void reverseRobotOrientation() {
        setRobotOrientation(!motorsInFront);
    }

    public void setRobotOrientation(boolean armFacesForward)
    {
        this.motorsInFront = armFacesForward;
        getRightMotor().setDirection(DcMotorSimple.Direction.REVERSE);
        getLeftMotor().setDirection(DcMotorSimple.Direction.FORWARD);
    }

    public void setHookPower(double power)
    {
        power = power / hookSlowdown;

        hookMotor.setPower(power);
    }


    public void setArmExtensionPower(double power)
    {
        power = power / armSlowdown;

        armExtensionMotor.setPower(power);
    }

    public void setGrabberServoPosition(double position)
    {
        grabberServo.setPosition(position);

    }

    private void setLeftPower(double leftPower)
    {
        leftPower = leftPower / drivingSlowDown;
        currentLeftPower = leftPower;

        getLeftMotor().setPower(leftPower);
    }

    private void setRightPower(double rightPower)
    {
        rightPower = rightPower / drivingSlowDown;
        this.currentRightPower = rightPower;

        getRightMotor().setPower(rightPower);
    }

    /**
     *
     * @param power Positive power is forward, negative power is backwards (between -1, 1)
     * @param steering Positive steering is to the right, negative steering is to the left (between -1, 1)
     *
     */
    void setPowerSteering(double power, double steering)
    {
        motorCommand = String.format("Steer(%.2f, %.2f)", power, steering);

        double powerRight, powerLeft;

        if(steering > 0)
        {
            // Turning to right: powerLeft should be more than powerRight
            // Scale down powerRight
            powerRight = (1-Math.abs(steering)) * power;
            powerLeft = power;
        }
        else
        {
            // Turning to left: powerLeft should be less than powerRight
            // Scale down powerLeft
            powerLeft = (1-Math.abs(steering)) * power;
            powerRight = power;
        }

        setLeftPower(powerLeft);
        setRightPower(powerRight);
    }

    public void setDrivingSlowdown(double drivingSlowDown) {
        this.drivingSlowDown = drivingSlowDown;
    }

    public void setArmSlowdown(double armSlowDown) {
        this.armSlowdown = armSlowDown;
    }

    public void setHookSlowdown(double hookSlowDown) {
        this.hookSlowdown = hookSlowDown;
    }

    public void mineralPlowUp()
    {
        mineralPlowServo.setPosition(0.85);
    }

    public void mineralPlowDown()
    {
        mineralPlowServo.setPosition(0);
    }

    public void hookActivate()
    {

    }
}
