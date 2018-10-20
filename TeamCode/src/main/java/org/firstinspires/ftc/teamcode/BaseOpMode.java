package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Func;
import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.Velocity;

import java.util.Locale;

abstract class BaseOpMode extends OpMode {
    DcMotor rightMotor;
    DcMotor leftMotor;
    DcMotor armExtensionMotor;
    Servo grabberServo;
    double leftPower = 0;
    double rightPower = 0;
    // Gyro
    BNO055IMU imu;

    // Needed for gyro telemetry
    Orientation angles;
    Acceleration gravity;
    Position position;
    Velocity velocity;
    Acceleration linearAcceleration;
    private Acceleration acceleration;
    private ImuIntegrator imuIntegrator;


    @Override
    public void init() {
        rightMotor = hardwareMap.dcMotor.get("RightMotor");
        leftMotor = hardwareMap.dcMotor.get("LeftMotor");
        grabberServo = hardwareMap.servo.get("GrabberServo");
        armExtensionMotor = hardwareMap.dcMotor.get("ArmExtensionMotor");

        // Retrieve and initialize the IMU. We expect the IMU to be attached to an I2C port
        // on a Core Device Interface Module, configured to be a sensor of type "AdaFruit IMU",
        // and named "imu".
        imu = hardwareMap.get(BNO055IMU.class, "imu");

        // Set up the parameters with which we will use our IMU. Note that integration
        // algorithm here just reports accelerations to the logcat log; it doesn't actually
        // provide positional information.
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit           = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit           = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.calibrationDataFile = "BNO055IMUCalibration.json"; // see the calibration sample opmode
        parameters.loggingEnabled      = true;
        parameters.loggingTag          = "IMU";

        imuIntegrator = new ImuIntegrator(imu);
        parameters.accelerationIntegrationAlgorithm = imuIntegrator;

        imu.initialize(parameters);

        // Set up our telemetry dashboard
        setUpImuTelemetry();

        imuIntegrator.calibrate();

        telemetry.addLine("GP1 JS: ")
                .addData("L: ", new Func<String>() {
                    @Override public String value() {
                        return String.format("(%.2f, %.2f)", gamepad1.left_stick_x, gamepad1.left_stick_y);
                    }})
                .addData("R: ", new Func<String>() {
                    @Override public String value() {
                        return String.format("(%.2f, %.2f)", gamepad1.right_stick_x, gamepad1.right_stick_y);
                    }});

        telemetry.addLine("GP2 JS: ")
                .addData("L: ", new Func<String>() {
                    @Override public String value() {
                        return String.format("(%.2f, %.2f)", gamepad1.left_stick_x, gamepad1.left_stick_y);
                    }})
                .addData("R: ", new Func<String>() {
                    @Override public String value() {
                        return String.format("(%.2f, %.2f)", gamepad1.right_stick_x, gamepad1.right_stick_y);
                    }});

        telemetry.addLine("Motors: ")
                .addData("Right: ", new Func<String>() {
                    @Override public String value() {
                        return String.format("%.2f", rightPower);
                    }})
                .addData("Left: ", new Func<String>() {
                    @Override public String value() {
                        return String.format("%.2f", leftPower);
                    }});
    }

    @Override
    public void start() {
        // Start the logging of measured acceleration
        imu.startAccelerationIntegration(new Position(), new Velocity(), 10);
    }

    @Override
    public void loop() {
    }

    void setLeftPower(double leftPower)
    {
        this.leftPower = leftPower;
        // Forward is positive which is the same for the motor
        leftMotor.setPower(leftPower);
    }

    void setRightPower(double rightPower)
    {
        this.rightPower = rightPower;
        // Forward is positive which is the opposite for the motor because it is mounted
        // backwards
        rightMotor.setPower(-rightPower);

    }

    void inchmove(double inches, double power)
    {
        double inchesPerRotation = 3.14159265 * 3.75;
        double rotations = inches / inchesPerRotation;
        leftMotor.getCurrentPosition();

    }

    void setUpImuTelemetry() {
        // At the beginning of each telemetry update, grab a bunch of data
        // from the IMU that we will then display in separate lines.
        telemetry.addAction(new Runnable() { @Override public void run()
        {
            // Acquiring the angles is relatively expensive; we don't want
            // to do that in each of the three items that need that info, as that's
            // three times the necessary expense.
            angles   = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
            gravity  = imu.getGravity();
            position = imu.getPosition();
            linearAcceleration = imu.getLinearAcceleration();
            acceleration = imu.getAcceleration();
            velocity = imu.getVelocity();
        }
        });

        telemetry.addLine()
                .addData("heading", new Func<String>() {
                    @Override public String value() {
                        return formatAngle(angles.angleUnit, angles.firstAngle);
                    }
                })
                .addData("roll", new Func<String>() {
                    @Override public String value() {
                        return formatAngle(angles.angleUnit, angles.secondAngle);
                    }
                })
                .addData("pitch", new Func<String>() {
                    @Override public String value() {
                        return formatAngle(angles.angleUnit, angles.thirdAngle);
                    }
                });
        telemetry.addLine()
                .addData("P: ", new Func<String>() {
                    @Override public String value() {
                        return String.format("(%.1f, %.1f, %.1f)", position.x, position.y, position.z);
                    }
                })
                .addData("V: ", new Func<String>() {
                @Override public String value() {
                        return String.format("(%.1f, %.1f, %.1f)", velocity.xVeloc, velocity.yVeloc,velocity.zVeloc);
                    }
                })
                .addData("A: ", new Func<String>() {
                    @Override public String value() {
                        return String.format("(%.1f, %.1f, %.1f)", acceleration.xAccel, acceleration.yAccel,acceleration.zAccel);
                    }
                })
                .addData("LinA: ", new Func<String>() {
                    @Override public String value() {
                        return String.format("(%.1f, %.1f, %.1f)", linearAcceleration.xAccel, linearAcceleration.yAccel,linearAcceleration.zAccel);
                    }
                })
                .addData("CorrectedAccel: ", new Func<String>() {
                    @Override public String value() {
                        return String.format("(%.1f, %.1f, %.1f)",
                                imuIntegrator.getAcceleration().xAccel,
                                imuIntegrator.getAcceleration().yAccel,
                                imuIntegrator.getAcceleration().zAccel);
                    }
                })
            ;
    }

    //----------------------------------------------------------------------------------------------
    // Formatting
    //----------------------------------------------------------------------------------------------

    String formatAngle(AngleUnit angleUnit, double angle) {
        return formatDegrees(AngleUnit.DEGREES.fromUnit(angleUnit, angle));
    }

    String formatDegrees(double degrees){
        return String.format(Locale.getDefault(), "%.1f", AngleUnit.DEGREES.normalize(degrees));
    }
}
