package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.TouchSensor;

import org.firstinspires.ftc.robotcore.external.Func;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.scheduler.Action;
import org.firstinspires.ftc.teamcode.scheduler.EndableAction;
import org.firstinspires.ftc.teamcode.scheduler.ImmediateAction;
import org.firstinspires.ftc.teamcode.scheduler.RepeatedAction;
import org.firstinspires.ftc.teamcode.scheduler.Scheduler;

import static org.firstinspires.ftc.teamcode.scheduler.Utils.*;
import static org.firstinspires.ftc.teamcode.scheduler.Utils.log;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Robot
{
    public static final String ROBOT_TAG = "team14821";

    // Found via experiment
    public static final double ENCODER_CLICKS_PER_INCH = 79.27;
    private static final double ENCODER_CLICKS_PER_ROTATION = 4350;
    public static final double TURN_POWER = 0.35;
    public static final double TURN_SLOWDOWN_POWER = 0.05;
    public static final int TURN_SLOWDOWN_DEGREES = 10;
    // Max height of hook (hook0 + MAX_HOOK_DISTANCE)
    public static final int MAX_HOOK_DISTANCE = 27500;
    public static final int MAX_SWING_ARM_DISTANCE = 2200;
    public static final int MAX_ARM_EXTENSION_DISTANCE = 12800;


    public static final double ARM_SPIN_SERVO_MIN_LOCATION = 0.01;
    public static final double ARM_SPIN_SERVO_MAX_LOCATION = 0.75;

    public static final int ARM_SWING_DOWN_IN_FRONT = 0;
    public static final int ARM_SWING_DOWN_IN_BACK = -7777;
    public static final int ARM_SWING_UP = -4324;
    public static final int ARM_EXTENTION_IN = 0;
    public static final int ARM_EXTENTION_OUT = MAX_ARM_EXTENSION_DISTANCE;
    public static final double ARM_SPIN_FOLDED_IN = 0;
    public static final double ARM_SPIN_STRAIGHT = .56;
    public static final double ARM_SPIN_TILTED = .46;
    public static final double MAX_ARM_SPIN_SERVO_CHANGE = 0.05;
    private static final double ARM_SPIN_INITIALIZE_LOCATION = 0.2;

    public static enum TURN_TYPE {SPIN, PIVOT};

    public static enum ARM_LOCATION {
        FOLDED(ARM_SWING_DOWN_IN_FRONT,ARM_SPIN_FOLDED_IN, ARM_EXTENTION_IN, 0),
        CRATER(ARM_SWING_DOWN_IN_BACK, ARM_SPIN_STRAIGHT, ARM_EXTENTION_OUT, 0),
        DUMP_GOLD(ARM_SWING_UP, ARM_SPIN_STRAIGHT, ARM_EXTENTION_OUT, 0),
        DUMP_SILVER(ARM_SWING_UP, ARM_SPIN_TILTED, ARM_EXTENTION_OUT, 0);

        int armSwingMotorLocation;
        double armSpinServoLocation;
        int armExtensionMotorLocation;
        double boxTiltServoLocation;

        ARM_LOCATION(int armSwingMotorLocation,
                            double armSpinServoLocation,
                            int armExtensionMotorLocation,
                            double boxTiltServoLocation)
        {
            this.armSwingMotorLocation=armSwingMotorLocation;
            this.armSpinServoLocation=armSpinServoLocation;
            this.armExtensionMotorLocation = armExtensionMotorLocation;
            this.boxTiltServoLocation=boxTiltServoLocation;
        }
    }

    ARM_LOCATION currentArmLocation = ARM_LOCATION.FOLDED;

    final Telemetry telemetry;
    final HardwareMap hardwareMap;
    final BaseLinearOpMode opMode;
    final DcMotor M0;
    final DcMotor M1;
    final DcMotor armExtensionMotor;
    final DcMotor hookMotor;
    final DcMotor armSwingMotor;

    final Servo armSpinServo;
    public boolean armSpinServoHasBeenSet=false;

    final Servo boxTiltServo;
    public boolean boxTiltServoHasBeenSet=false;

    final Servo leftBoxServo;
    final Servo rightBoxServo;

    final TouchSensor armSwingFrontLimit;
    final TouchSensor limitSwitch1;

    boolean allSafetysAreDisabled = false;
    boolean hookSafetyIsDisabled = false;
    boolean armSwingSafetyIsDisabled = false;
    boolean armExtensionSafetyIsDisabled = false;



    boolean motorsInFront = true;

    // Encoder when arm extension is at the top
    boolean armExtensionCalibrated = false;
    // Encoder when arm swing is at the bottom
    boolean armSwingCalibrated = false;
    // Encoder when hook is at bottom
    boolean hookCalibrated = false;

    TeamImu teamImu;

    // Initialize these in constructor, maintain them in trackMovements()
    int previousArmSwingPosition, previousHookPosition, previousArmExtensionPosition, previousM0Position, previousM1Position;
    int armSwingSpeed, hookSpeed, armExtensionSpeed, m0Speed, m1Speed;
    double previousArmSwingPower, previousHookPower, previousArmExtensionPower, previousM0Power, previousM1Power;

    double correctHeading;

    private RobotVision robotVision;

    // When to start stopping turns
    public static final double TURN_APPROXIMATION = 3;

    // What was logged most recently for each component... used to avoid duplicate messages
    private Map<String, String> component2MostRecentChange = new HashMap<>();


    // Used to log telemetry to RobotLog (in addition to driver station)
    Map<String, String> latestTelemetryData = new LinkedHashMap<>();
    long lastTelemetryLoggingTime_ms = 0;
    long lastTelemetryUpdateTime_ms = 0;

    private static Robot sharedInstance;

    public static Robot get()
    {
        return sharedInstance;
    }

    public Robot(BaseLinearOpMode baseLinearOpMode, HardwareMap hardwareMap, Telemetry telemetry)
    {
        sharedInstance = this;
        Action action = new ImmediateAction("RobotConstruction", null);

        action.setStatus("Gathering hardware information");

        this.hardwareMap = hardwareMap;
        this.telemetry   = telemetry;
        opMode = baseLinearOpMode;

        M0 = hardwareMap.dcMotor.get("M0");
        M0.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        M0.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        M1 = hardwareMap.dcMotor.get("M1");
        M1.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        M1.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        armSpinServo = hardwareMap.servo.get("ArmSpinServo");
        boxTiltServo = hardwareMap.servo.get("BoxTiltServo");
        leftBoxServo = hardwareMap.servo.get("LeftBoxServo");
        rightBoxServo = hardwareMap.servo.get("RightBoxServo");

        armSwingFrontLimit = hardwareMap.touchSensor.get("ArmSwingFrontLimit");
        limitSwitch1 = hardwareMap.touchSensor.get("LimitSwitch1");

        armExtensionMotor = hardwareMap.dcMotor.get("ArmExtensionMotor");
        armExtensionMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        hookMotor = hardwareMap.dcMotor.get("HookMotor");
        hookMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        armSwingMotor = hardwareMap.dcMotor.get("SwingMotor");
        armSwingMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        armSwingMotor.setDirection(DcMotor.Direction.REVERSE);
        armSwingMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        teamImu = new TeamImu(hardwareMap, telemetry);

        correctHeading = teamImu.getTotalDegreesTurned();

        setRobotOrientation(true);
        stopWithoutBraking();
        setArmSpinServoPosition_raw(ARM_SPIN_SERVO_MIN_LOCATION);

        setupRobotTelemetry(telemetry);

        action.finish("Robot is initialized");

        // Call trackMovements() twice so everything is zero'ed
        // (first time correctly sets previous values, second time sets speeds)
        trackMovements();
        trackMovements();

        new RepeatedAction("RobotMovementTracking") {
            @Override
            protected void doTask()
            {
                trackMovements();
            }
        }.start();

        new RepeatedAction("HealthChecks") {
            @Override
            protected void doTask()
            {
                healthCheck();
            }
        }.start();

        new RepeatedAction("Telemetry")
        {
            @Override
            protected void doTask()
            {
                telemetryLoop();
            }
        }.start();
    }

    private void telemetryLoop()
    {
        telemetry.update();

        // Has telemetry changed since we last logged it?
        if (lastTelemetryUpdateTime_ms==0 || lastTelemetryUpdateTime_ms>lastTelemetryLoggingTime_ms)
        {
            lastTelemetryLoggingTime_ms = System.currentTimeMillis();
            for (Map.Entry entry : latestTelemetryData.entrySet())
            {
                log("Telemetry: %12s: %s", entry.getKey(), entry.getValue());
            }
        }
    }


    public String saveTelemetryData(String key, String valueFormat, Object... valueArgs)
    {
        String value = safeStringFormat(valueFormat, valueArgs);
        latestTelemetryData.put(key, value);
        lastTelemetryUpdateTime_ms = System.currentTimeMillis();
        return value;
    }


    private void setupRobotTelemetry(Telemetry telemetry)
    {
        telemetry.addLine("Robot: ")
                .addData("Orientation", new Func<String>() {
                    @Override
                    public String value() {
                        return saveTelemetryData("Orientation", motorsInFront ? "MotorsFront" : "MotorsBack");
                    }
                })
        ;

        telemetry.addLine("Heading: ")
                .addData("", new Func<String>() {
                    @Override
                    public String value() {
                        return saveTelemetryData("Heading","|TotDeg=%.1f|Correct=%.1f|Error=%.1f",
                                teamImu.getTotalDegreesTurned(),
                                correctHeading,
                                getHeadingError());
                    }});

        telemetry.addLine("Touch Sensors: ")
                .addData("", new Func<String>() {
                    @Override
                    public String value() {
                        return saveTelemetryData("Touch sensors","|ArmSwingLimit %s|",
                                armSwingFrontLimit.isPressed() ? "Pressed" : "Not Pressed"); }

                });

        telemetry.addLine("Left: ")
                .addData("", new Func<String>() {
                    @Override
                    public String value() {
                        return saveTelemetryData("Left", "|p=%+.1f|s=+%d|Loc=%+d|Tgt=%+d|%s",
                                getLeftMotor().getPower(),
                                getLeftMotor() == M0 ? m0Speed : m1Speed,
                                getLeftMotor().getCurrentPosition(),
                                getRightMotor().getTargetPosition(),
                                getLeftMotor() == M0 ? "M0" : "M1");
                    }});
        telemetry.addLine("Right: ")
                    .addData("", new Func<String>() {
                    @Override
                    public String value() {
                        return saveTelemetryData("Right", "|p=%+.1f|s=+%d|Loc=%+d|Tgt=%+d|%s",
                                getRightMotor().getPower(),
                                getRightMotor() == M0 ? m0Speed : m1Speed,
                                getRightMotor().getCurrentPosition(),
                                getRightMotor().getTargetPosition(),
                                getRightMotor() == M0 ? "M0" : "M1");
                    }});
        telemetry.addLine("ArmServo: ")
                .addData("", new Func<String>() {
                    @Override
                    public String value() {
                        return saveTelemetryData("ArmServos", "|ArmSpin=%.2f|BoxTilt=+%.2f|LeftIntakeServo=%+.1f|RightIntakeServo=%+.1f|",
                                armSpinServo.getPosition(),
                                boxTiltServo.getPosition(),
                                leftBoxServo.getPosition(),
                                rightBoxServo.getPosition());
                    }});

        telemetry.addLine("Hook: ")
                .addData("", new Func<String>() {
                    @Override
                    public String value() {
                        return saveTelemetryData("Hook",
                                "|p=%+.1f|s=%+d|Loc=%d",
                                 hookMotor.getPower(), hookSpeed, hookMotor.getCurrentPosition());
                    }});
        telemetry.addLine("ArmSwing: ")
                .addData("", new Func<String>() {
                    @Override
                    public String value() {
                        return saveTelemetryData("ArmSwing",
                                "|p=%+.1f|s=%+d|Loc=%d",
                                armSwingMotor.getPower(), armSwingSpeed, armSwingMotor.getCurrentPosition()
                        );
                    }});
        telemetry.addLine("ArmExtension: ")
                .addData("", new Func<String>() {
                    @Override
                    public String value() {
                        return saveTelemetryData("ArmExtension",
                                "|p=%+.1f|s=%+d|Loc=%d",
                                armExtensionMotor.getPower(), armExtensionSpeed, armExtensionMotor.getCurrentPosition()
                        );
                    }});
    }


    public void logChange(String component, String format, Object... args) {
        String changeMessage = safeStringFormat(format, args);

        String previousChangeMessage = component2MostRecentChange.get(component);

        if ( previousChangeMessage==null || !previousChangeMessage.equals(changeMessage) )
        {
            log("Changing %s: %s", component, changeMessage);
            component2MostRecentChange.put(component, changeMessage);
        }
    }

    public void stopWithoutBraking()
    {
        logChange("Driving", "Stopping");
        setRightPower(0);
        setLeftPower(0);
    }

    public EndableAction startStopping()
    {
        EndableAction brakingAction = new EndableAction( "Stop")
        {
            DcMotor.ZeroPowerBehavior originalBehavior = M0.getZeroPowerBehavior();
            @Override
            public EndableAction start()
            {
                super.start();
                setDrivingZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
                setRightPower(0);
                setLeftPower(0);
                return this;
            }

            @Override
            public boolean isDone(StringBuilder statusMessage)
            {
                int maxSpeed = Math.max(Math.abs(m0Speed), Math.abs(m1Speed));
                if (maxSpeed > 5)
                {
                    statusMessage.append(safeStringFormat("Waiting for M0/M1 to stop: speeds are %d and %d", m0Speed, m1Speed ));
                    return false;
                }
                else
                {
                    statusMessage.append(safeStringFormat("Robot is stopped... M0Speed=%d|M1Speed=%d", m0Speed, m1Speed ));
                    return true;
                }
            }

            @Override
            protected void cleanup(boolean actionWasCompleted)
            {
                setDrivingZeroPowerBehavior(originalBehavior);
            }
        }.start();

        return brakingAction;
    }

    public void driveStraight(double power)
    {
        logChange("Driving", "Straight(pow=%.2f)", power);

        setLeftPower(power);
        setRightPower(power);
    }

    /**
     * Turn robot with opposite power to wheels
     *
     * @param power (-1..1) negative means to the left
     */
    public void spin(double power)
    {
        logChange("Driving", "Spin%s(%.2f)", power > 0 ? "Right" : "Left", power);
        setLeftPower(power);
        setRightPower(-power);
    }

    public void setDrivingPowers(double leftPower, double rightPower)
    {
        logChange("Driving","SetDrivingPowers(%.2f,%.2f)", leftPower, rightPower);
        setLeftPower(leftPower);
        setRightPower(rightPower);
    }

    public DcMotor.ZeroPowerBehavior setDrivingZeroPowerBehavior(DcMotor.ZeroPowerBehavior behavior)
    {
        DcMotor.ZeroPowerBehavior originalZeroPowerBehavior = getLeftMotor().getZeroPowerBehavior();

        logChange("DrivingMotorMode", "setDrivingZeroPowerBehavior(%s)", behavior);
        getLeftMotor().setZeroPowerBehavior(behavior);
        getRightMotor().setZeroPowerBehavior(behavior);

        return originalZeroPowerBehavior;
    }

    DcMotor getRightMotor()
    {
        if (motorsInFront)
            return M1;
        else
            return M0;
    }

    DcMotor getLeftMotor()
    {
        if (motorsInFront)
            return M0;
        else
            return M1;
    }

    public void setArmSpinServoPosition_raw(double position)
    {
        logChange("ArmSpinServo", "Position(%.2f)", position);

        if (!armSpinServoHasBeenSet && position != ARM_SPIN_INITIALIZE_LOCATION)
        {
            setArmSpinServoPosition_raw(ARM_SPIN_INITIALIZE_LOCATION);
            Scheduler.get().sleep(500, "Waiting to get arm-spin to safe location");
        }

        if (position < ARM_SPIN_SERVO_MIN_LOCATION)
            position = ARM_SPIN_SERVO_MIN_LOCATION;
        if (position > ARM_SPIN_SERVO_MAX_LOCATION)
            position = ARM_SPIN_SERVO_MAX_LOCATION;

        armSpinServo.setPosition(position);
        armSpinServoHasBeenSet=true;
    }


    public int getWheelPosition() {
        return M0.getCurrentPosition();
    }

    /**
     *
     * @return How many degrees the robot has turned. Negative: To the right, Positive: to the Left
     */

    public double getTotalDegreesTurned() {
        return teamImu.getTotalDegreesTurned();
    }

    public void reverseRobotOrientation() {
        log("Reversing robot orientation");
        setRobotOrientation(!motorsInFront);
    }

    private void setRobotOrientation(boolean motorsInFront) {
        logChange("RobotOrientation",
                "setRobotOrientation(%s)", motorsInFront ? "MotorsInFront" : "MotorsInBack");

        this.motorsInFront = motorsInFront;
        getRightMotor().setDirection(DcMotorSimple.Direction.REVERSE);
        getLeftMotor().setDirection(DcMotorSimple.Direction.FORWARD);
    }

    public void setHookPower(double power)
    {
        logChange("HookMotor", "power(%.2f)", power);

        if (isHookPowerOK(power)) {
            hookMotor.setPower(power);
        }
    }


    public void setArmExtensionPower(double power)
    {
        logChange("ArmExtension", "power(%.2f)", power);

        if(isArmExtensionPowerOK(power))
        {
            armExtensionMotor.setPower(power);
        }
    }

    public void setSwingArmSpeed(double speed)
    {
        logChange("ArmSwing", "speed(%.2f)", speed);
        armSwingMotor.setPower(speed);
    }


    public int getArmSwingZone()
    {
        int zone;
        int armLocation = armSwingMotor.getCurrentPosition();

        if(armLocation < 1000)
            zone = 1;
        else if(armLocation >= 1000 && armLocation < 1150)
            zone = 2;
        else
            zone = 3;
        return zone;
    }

    private void setLeftPower(double leftPower)
    {
        logChange(safeStringFormat("Left(Port%d)MotorPower", getLeftMotor().getPortNumber()),
            "Power(%.2f)", leftPower);

        getLeftMotor().setPower(leftPower);
    }

    private void setRightPower(double rightPower)
    {
        logChange(safeStringFormat("Right(Port%d)MotorPower", getRightMotor().getPortNumber()),
                "Power(%.2f)", rightPower);

        getRightMotor().setPower(rightPower);
    }

    /**
     * @param power    Positive power is forward, negative power is backwards (between -1, 1)
     * @param steering Positive steering is to the right, negative steering is to the left (between -1, 1)
     */
    void setPowerSteering(double power, double steering)
    {
        logChange("Driving", "Steer(pow=%.2f, steering=%.2f)", power, steering);

        double powerRight, powerLeft;

        if (steering > 0) {
            // Turning to right: powerLeft should be more than powerRight
            // Scale down powerRight
            powerRight = (1 - Math.abs(steering)) * power;
            powerLeft = power;
        } else {
            // Turning to left: powerLeft should be less than powerRight
            // Scale down powerLeft
            powerLeft = (1 - Math.abs(steering)) * power;
            powerRight = power;
        }

        setLeftPower(powerLeft);
        setRightPower(powerRight);
    }

    public void trackMovements()
    {
        int currentArmExtensionPosition = armExtensionMotor.getCurrentPosition();
        int currentHookPosition = hookMotor.getCurrentPosition();
        int currentArmSwingPosition = armSwingMotor.getCurrentPosition();
        int currentM0Position = M0.getCurrentPosition();
        int currentM1Position = M1.getCurrentPosition();

        armSwingSpeed = currentArmSwingPosition - previousArmSwingPosition;
        hookSpeed = currentHookPosition - previousHookPosition;
        armExtensionSpeed = currentArmExtensionPosition - previousArmExtensionPosition;
        m0Speed = currentM0Position - previousM0Position;
        m1Speed = currentM1Position - previousM1Position;

        
        previousArmExtensionPosition = currentArmExtensionPosition;
        previousHookPosition = currentHookPosition;
        previousArmSwingPosition = currentArmSwingPosition;
        previousM0Position = currentM0Position;
        previousM1Position = currentM1Position;

        previousArmSwingPower = armSwingMotor.getPower();
        previousHookPower = hookMotor.getPower();
        previousArmExtensionPower = armExtensionMotor.getPower();
        previousM0Power = M0.getPower();
        previousM1Power = M1.getPower();
    }


    // Protect robot
    public void healthCheck()
    {
        if(opMode instanceof TeleOpMode)
        {
            // Stop some motors that are getting power but are not moving

            if (Math.abs(previousHookPower) >= 0.25 && Math.abs(hookSpeed) < 10)
            {
                log("EMERGENCY HOOK STOP: Power was %.2f, speed was %d", previousHookPower, hookSpeed);
                setHookPower(0);
            }
            if (Math.abs(previousArmExtensionPower) >= 0.25 && Math.abs(armExtensionSpeed) < 10)
            {
                log("EMERGENCY ARM EXTENSION STOP: Power was %.2f, speed was %d", previousArmExtensionPower, armExtensionSpeed);
                setArmExtensionPower(0);
            }
        }


        if (!isArmExtensionPowerOK(armExtensionMotor.getPower()))
        {
            log("Stopping armExtension motor");
            armExtensionMotor.setPower(0);
        }

        if (!isHookPowerOK(hookMotor.getPower()))
        {
            log("Stopping hook motor");
            hookMotor.setPower(0);
        }
    }

    private boolean isHookPowerOK(double powerToCheck)
    {
        String problemReason;

        if(allSafetysAreDisabled)
            problemReason = null;
        else if (hookSafetyIsDisabled)
            problemReason = null;
        else if (!hookCalibrated)
            problemReason = null;
        else if (hookMotor.getCurrentPosition() >  MAX_HOOK_DISTANCE && powerToCheck > 0) {
            problemReason = String.format("Hook power %f > 0 and hook too high (%d > %d)", powerToCheck,
                    hookMotor.getCurrentPosition(),  MAX_HOOK_DISTANCE);
        } else if (hookMotor.getCurrentPosition() < 0 && powerToCheck < 0) {
            problemReason = String.format("Hook power %f < 0 and hook too low (%d < %d)", powerToCheck,
                    hookMotor.getCurrentPosition(), 0 );
        }
        else
            problemReason = null;

        if(problemReason != null)
            log("Hook power %.2f IS NOT OKAY at position %d: %s", powerToCheck, hookMotor.getCurrentPosition(), problemReason);

        return problemReason==null;
    }

    private boolean isSwingArmPowerOK(double powerToCheck)
    {
        boolean result;

        if(allSafetysAreDisabled)
            result = true;
        else if ( armSwingSafetyIsDisabled)
            result = true;

        else if (armSwingMotor.getCurrentPosition() >  MAX_SWING_ARM_DISTANCE && powerToCheck > 0) {
            result = false;
        } else if (armSwingMotor.getCurrentPosition() < 0 && powerToCheck < 0) {
            result = false;
        }
        else
            result = true;

        if(!result)
            log("Swing arm power %.2f IS NOT OKAY at position %d", powerToCheck, armSwingMotor.getCurrentPosition());

        return result;
    }

    private boolean isArmExtensionPowerOK(double powerToCheck)
    {
        boolean result;

        if(allSafetysAreDisabled)
            result = true;
        else if ( armExtensionSafetyIsDisabled )
            result = true;

        else if (armExtensionMotor.getCurrentPosition() > MAX_ARM_EXTENSION_DISTANCE && powerToCheck > 0) {
            result = false;
        } else if (armExtensionMotor.getCurrentPosition() < 0 && powerToCheck < 0) {
            result = false;
        }
        else
            result = true;

        if(!result)
            log("Arm extension power" +
                    " %.2f IS NOT OKAY at position %d", powerToCheck, armExtensionMotor.getCurrentPosition());

        return result;
    }

    public EndableAction startMovingHookUp(final double power)
    {
        return new EndableAction(10000,"MoveHookUp", "HookUp(pow=%.2f)", power)
        {
            @Override
            public EndableAction start()
            {
                super.start();

                setHookPower(Math.abs(power));
                return this;
            }

            @Override
            public boolean isDone(StringBuilder statusMessage)
            {
                statusMessage.append(safeStringFormat("HookMotor Power=%.2f, Position=%d, Speed=%d",
                    hookMotor.getPower(), hookMotor.getCurrentPosition(), hookSpeed));

                return hookMotor.getPower() != 0;
            }

            @Override
            protected void cleanup(boolean actionWasCompleted)
            {
                setHookPower(0);
                super.cleanup(actionWasCompleted);
            }
        }.start();
    }

    public EndableAction startMovingHookDown(final double power)
    {
        {
            return new EndableAction(10000,"MoveHookDown", "HookDown(pow=%.2f)", power)
            {
                @Override
                public EndableAction start()
                {
                    super.start();

                    setHookPower(-Math.abs(power));
                    return this;
                }

                @Override
                public boolean isDone(StringBuilder statusMessage)
                {
                    statusMessage.append(safeStringFormat("HookMotor Power=%.2f, Position=%d, Speed=%d",
                            hookMotor.getPower(), hookMotor.getCurrentPosition(), hookSpeed));

                    return hookMotor.getPower() != 0;
                }

                @Override
                protected void cleanup(boolean actionWasCompleted)
                {
                    setHookPower(0);
                    super.cleanup(actionWasCompleted);
                }
            }.start();
        }
    }

    public EndableAction startInchMove(final double inches, final double power)
    {
        return new EndableAction("InchMove", "InchMove(%.2f inches, %.2f power", inches, power)
        {
            int startPosition = getWheelPosition();
            int encoderClicks = (int) Math.round(inches * ENCODER_CLICKS_PER_INCH);
            int stopPosition = getWheelPosition() + encoderClicks;
            EndableAction stopAction=null;

            @Override
            public EndableAction start()
            {
                log("Starting to move for %d encoder clicks", encoderClicks);
                return super.start();
            }

            @Override
            public boolean isDone(StringBuilder statusMessage)
            {
                if (getWheelPosition() < stopPosition)
                    return false;
                if ( stopAction == null )
                    return false;

                return stopAction.hasFinished();
            }

            @Override
            protected void cleanup(boolean actionWasCompleted)
            {
                stopWithoutBraking();

                super.cleanup(actionWasCompleted);
            }

            @Override
            public void loop()
            {
                // Stop when we've gone far enough
                if (getWheelPosition() >= stopPosition)
                {
                    if (stopAction==null)
                        stopAction= startStopping();

                    return;
                }
                // avoid skidding by using less power for first 10 inches
                double wheelPower;
                if ( getWheelPosition() - startPosition < 10*ENCODER_CLICKS_PER_INCH )
                    wheelPower = 0.3;
                else
                    wheelPower = power;

                // Example:
                //   correctHeading: 0  (straight ahead)
                //   currentHeading: -5 (negative is to the right)
                //   HeadingError: 5 ==> Need to turn 5 degrees to the left
                double headingError = getHeadingError();

                setStatus("%d clicks, %.1f inches to go. Heading error: %.1f degrees (robot facing too far %s)",
                        stopPosition - getWheelPosition(),
                        (stopPosition - getWheelPosition()) / ENCODER_CLICKS_PER_INCH,
                        headingError,
                        headingError > 0 ? "right" : "left");

                if ( headingError > 10 )
                    // Too far off course, need to stop and turn
                    // Use 0 degrees so turn will just turn to the correct place
                    startTurningLeft(0, TURN_TYPE.SPIN);
                else if ( headingError < -10 )
                    startTurningRight(0, TURN_TYPE.SPIN);
                else
                {
                    if (headingError > 0.0)
                    {
                        //Steer to the left
                        // PowerSteering takes negative steering to the left
                        setPowerSteering(wheelPower, -0.05 * Math.abs(headingError));
                    } else if (headingError < 0.0)
                    {
                        //Steer to the right
                        // PowerSteering takes positive steering to the right

                        setPowerSteering(wheelPower, 0.05 * Math.abs(headingError));
                    } else
                    {
                        // Go Straight
                        driveStraight(wheelPower);
                    }
                }
            }
        }.start();
    }

    /**
     * How far off is the robot from the correct heading?
     * @return Degrees: < 0 ==> Robot needs to turn Right, >0 ==> Robot needs to turn Left
     */
    private double getHeadingError() {
        return correctHeading - getTotalDegreesTurned();
    }

    public EndableAction startInchMoveBack(final double inches, final double power)
    {
        return new EndableAction("InchMoveBack", "InchmoveBack(d=%.1fin, pow=%.1f)",
                inches, power)
        {
            int startPosition = getWheelPosition();

            int encoderClicks = (int)(inches * ENCODER_CLICKS_PER_INCH);
            int stopPosition = getWheelPosition() - encoderClicks;
            EndableAction stopAction=null;

            @Override
            public boolean isDone(StringBuilder statusMessage)
            {
                if (getWheelPosition() > stopPosition)
                    return false;
                if ( stopAction == null )
                    return false;

                return stopAction.hasFinished();
            }

            @Override
            public void loop()
            {
                // Stop when we've gone far enough
                if (getWheelPosition() <= stopPosition)
                {
                    if (stopAction==null)
                        stopAction= startStopping();

                    return;
                }

                // avoid skidding by using less power for first 10 inches
                double wheelPower;
                if ( startPosition - getWheelPosition() < 10 * ENCODER_CLICKS_PER_INCH )
                    wheelPower = 0.3;
                else
                    wheelPower = power;

                //Heading is larger to the left
                // Example:
                //   correctHeading: 0  (straight ahead)
                //   currentHeading: -5 (negative is to the right)
                //   HeadingError: 5 ==> Need to turn 5 degrees to the left

                double headingError = getHeadingError();

                setStatus(String.format("%.1f inches to go. Heading error: %.1f degrees(robot front facing too far %s)",
                        (stopPosition - getWheelPosition()) / ENCODER_CLICKS_PER_INCH,
                        headingError,
                        headingError > 0 ? "right" : "left"
                ));


                if ( headingError > 10 ) {
                    setStatus("Turning left because we're off by %.1f degrees (>10)", headingError);
                    // Use 0 degrees so turn will just turn to the correct place
                    startTurningLeft(0, TURN_TYPE.SPIN);
                }
                else if ( headingError < -10 ) {
                    setStatus("Turning right because we're off by %.1f degrees (<-10)", headingError);

                    // Use 0 degrees so turn will just turn to the correct place
                    startTurningRight(0, TURN_TYPE.SPIN);
                }
                else
                {
                    setStatus("Using proportional steering because we're off by %.1f degrees", headingError);

                    if (headingError > 0.0) {
                        //The current heading is too small so we turn to the right
                        // (opposite of inchMoveForward because we're going backwards)
                        setPowerSteering(-wheelPower, 0.1 * Math.abs(headingError));
                    } else if (headingError < 0.0) {
                        //Current heading is too big, so we steer to the left (again since we're going backwards)
                        setPowerSteering(-wheelPower, -0.1 * Math.abs(headingError));
                    } else {
                        // Go Straight
                        driveStraight(-wheelPower);
                    }
                }

            }
        }.start();
    }

    public void resetCorrectHeading(String reasonFormat, Object... reasonArgs)
    {
        String reason = safeStringFormat(reasonFormat, reasonArgs);
        logChange("Heading", "ResetHeading(%.1f deg, %s)", getTotalDegreesTurned(), reason);
        correctHeading = getTotalDegreesTurned();
    }

    // default to a SPIN turn
    public EndableAction startTurningRight(final double degrees)
    {
        return startTurningRight(degrees, TURN_TYPE.SPIN);
    }

    public EndableAction startTurningRight(final double degrees, final TURN_TYPE turnType)
    {
        return new EndableAction("TurnRight", "TurnRight(%.0f)", degrees)
        {
            DcMotor.RunMode originalMotorMode;
            DcMotor.ZeroPowerBehavior originalZeroPowerBehavior;
            EndableAction stopAction;

            @Override
            public EndableAction start()
            {
                super.start();
                correctHeading -= degrees;
                originalMotorMode = setDrivingMotorMode(DcMotor.RunMode.RUN_USING_ENCODER);
                originalZeroPowerBehavior = setDrivingZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

                return this;
            }

            @Override
            protected void cleanup(boolean actionWasCompleted)
            {
                setDrivingMotorMode(originalMotorMode);
                setDrivingZeroPowerBehavior(originalZeroPowerBehavior);
                setStatus("Finished turning. Wheels stopped. Robot is %.1f degrees off of correct heading", getHeadingError());

                super.cleanup(actionWasCompleted);
            }

            @Override
            public boolean isDone(StringBuilder statusMessage)
            {
                statusMessage.append(safeStringFormat("DegreesToGo: %.0f, needs to be >=%.0f. Stop action: %s",
                        getHeadingError(), -TURN_APPROXIMATION, stopAction));

                if (getHeadingError() < -TURN_APPROXIMATION)
                    return false;
                else if (stopAction == null)
                    return false;
                else
                    return stopAction.hasFinished();
            }

            @Override
            public void loop()
            {
                super.loop();
                double degreesToGo = getHeadingError();

                if ( degreesToGo >= -TURN_APPROXIMATION )
                {
                    setStatus("%.0f degrees to go. (stopping)", degreesToGo);
                    if (stopAction == null)
                        stopAction = startStopping();
                }
                else if (degreesToGo >= -TURN_SLOWDOWN_DEGREES) {
                    setStatus("%.0f degrees to go. (slower turn)", degreesToGo);

                    if (turnType==TURN_TYPE.SPIN)
                        spin(TURN_SLOWDOWN_POWER);
                    else
                        setDrivingPowers(0, TURN_SLOWDOWN_POWER);
                } else {
                    setStatus("%.0f degrees to go. (full speed)", degreesToGo);
                    if (turnType==TURN_TYPE.SPIN)
                        spin(TURN_POWER);
                    else
                        setDrivingPowers(0, TURN_POWER);
                }
            }
        }.start();
    }


    // Default to a SPIN turn
    public EndableAction startTurningLeft(final double degrees)
    {
        return startTurningLeft(degrees, TURN_TYPE.SPIN);
    }

    public EndableAction startTurningLeft(final double degrees, final TURN_TYPE turnType)
    {
        return new EndableAction("TurnLeft", "TurnLeft(%.0f)", degrees)
        {
            DcMotor.RunMode originalMotorMode;
            EndableAction stopAction;

            @Override
            public EndableAction start()
            {
                super.start();
                correctHeading += degrees;
                originalMotorMode = setDrivingMotorMode(DcMotor.RunMode.RUN_USING_ENCODER);

                return this;
            }

            @Override
            protected void cleanup(boolean actionWasCompleted)
            {
                setStatus("Finished turning. Starting to stop when robot is %.0f degrees off of correct heading", getHeadingError());
                setDrivingMotorMode(originalMotorMode);
                setStatus("Finished turning. Wheels stopped. Robot is %.0f degrees off of correct heading", getHeadingError());

                super.cleanup(actionWasCompleted);
            }

            @Override
            public boolean isDone(StringBuilder statusMessage)
            {
                statusMessage.append(safeStringFormat("DegreesToGo: %.0f, needs to be <%.0f. Stop action: %s",
                        getHeadingError(), TURN_APPROXIMATION, stopAction));

                if (getHeadingError() > TURN_APPROXIMATION)
                    return false;
                else if (stopAction == null)
                    return false;
                else
                    return stopAction.hasFinished();
            }

            @Override
            public void loop()
            {
                super.loop();
                double degreesToGo = getHeadingError();

                if ( degreesToGo <= TURN_APPROXIMATION )
                {
                    setStatus("%.0f degrees to go. (stopping)", degreesToGo);
                    if (stopAction == null)
                        stopAction = startStopping();
                }
                else if (degreesToGo <= TURN_SLOWDOWN_DEGREES) {
                    setStatus("%.0f degrees to go. (slower turn)", degreesToGo);

                    if (turnType==TURN_TYPE.SPIN)
                        spin(-TURN_SLOWDOWN_POWER);
                    else
                        setDrivingPowers(-TURN_SLOWDOWN_POWER, 0);
                } else {
                    setStatus("%.0f degrees to go. (full speed)", degreesToGo);
                    if (turnType==TURN_TYPE.SPIN)
                        spin(-TURN_POWER);
                    else
                        setDrivingPowers(-TURN_POWER, 0);
                }
            }
        }.start();
    }

    //Skooch
    public EndableAction startSkoochingRight()
    {
        return new EndableAction("SkoochRight")
        {
            boolean isDone = false;

            @Override
            public EndableAction start()
            {
                super.start();
                resetCorrectHeading("Skooching relative to where we were");
                return this;
            }

            @Override
            public boolean isDone(StringBuilder statusMessage)
            {
                return isDone;
            }

            @Override
            protected void cleanup(boolean actionWasCompleted)
            {
                stopWithoutBraking();
            }

            @Override
            public void loop()
            {
                waitFor(startInchMove(5,0.5));
                waitFor(startTurningRight(20, TURN_TYPE.SPIN));
                waitFor(startInchMove(5,0.5));
                waitFor(startTurningLeft(20, TURN_TYPE.SPIN));
                waitFor(startInchMoveBack(10,0.5));
                waitFor(startStopping());

                isDone = true;
            }
        }.start();
    }

    public EndableAction startSkoochingLeft()
    {
        return new EndableAction("SkoochLeft")
        {
            boolean isDone = false;

            @Override
            public EndableAction start()
            {
                super.start();
                resetCorrectHeading("Skooching relative to where we were");
                return this;
            }

            @Override
            public boolean isDone(StringBuilder statusMessage)
            {
                return isDone;
            }

            @Override
            public void loop()
            {
                waitFor(startInchMove(5, 0.5));
                waitFor(startTurningLeft(20, TURN_TYPE.SPIN));
                waitFor(startInchMove(5, 0.5));
                waitFor(startTurningRight(20, TURN_TYPE.SPIN));
                waitFor(startInchMoveBack(10, 0.5));
                waitFor(startStopping());

                isDone = true;
            }
        }.start();
    }


    public void resetMotorEncoder(String motorName, DcMotor motor)
    {
        logChange(motorName + "Motor-Encoder", "Reset encoder");

        DcMotor.RunMode originalMode = motor.getMode();
        motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motor.setMode(originalMode);
    }


    public EndableAction startCalibratingEverything()
    {
        return new EndableAction("CalibrateEverything")
        {
            @Override
            public EndableAction start()
            {
                super.start();
                new OngoingAction_CalibrateArmExtension().start();
                new OngoingAction_CalibrateArmSwing().start();
                new OngoingAction_CalibrateHook().start();
                return this;
            }

            @Override
            public boolean isDone(StringBuilder statusMessage)
            {
                return areChildrenDone(statusMessage);
            }
        }.start();
    }

    public RobotVision getRobotVision()
    {
        if ( robotVision == null )
        {
            robotVision = new RobotVision(this);
            robotVision.init();
        }
        return robotVision;
    }

    public void resetDrivingEncoders(String reasonFormat, Object... reasonArgs)
    {
        String reason = safeStringFormat(reasonFormat, reasonArgs);
        logChange("DrivingMotors", "Resetting encoders: %s", reason);

        resetMotorEncoder("Left", getLeftMotor());
        resetMotorEncoder("Right", getRightMotor());
    }

    public EndableAction startPushingIntoWall()
    {
        return new EndableAction("Pushing the wall")
        {
            int duration_ms=2500;

            @Override
            public EndableAction start()
            {
                super.start();
                setDrivingPowers(0.25, 0.25);
                return this;
            }

            @Override
            public boolean isDone(StringBuilder statusMessage)
            {
                statusMessage.append(safeStringFormat("%.1f secs to go", duration_ms-getAge_ms()));
                return getAge_ms()>=duration_ms;
            }
        }.start();
    }

    public DcMotor.RunMode setDrivingMotorMode(DcMotor.RunMode newMode)
    {
        DcMotor.RunMode originalMode = getLeftMotor().getMode();

        logChange("DrivingMotorMode", "Setting mode to %s (was %s)", newMode, originalMode);
        getLeftMotor().setMode(newMode);
        getRightMotor().setMode(newMode);

        return originalMode;
    }


    public EndableAction startMovingArmToPosition(final ARM_LOCATION desiredLocation)
    {
        return new EndableAction("movingArmToPosition", "movingArmToPosition(%s)", desiredLocation)
        {
            @Override
            public EndableAction start()
            {
                super.start();
                if (!armExtensionCalibrated || !armSwingCalibrated)
                {
                    abort("Must calibrate before setting arm position");
                    return this;
                }

                if (currentArmLocation==desiredLocation)
                {
                    log("No change to arm location, it is already %s", desiredLocation);
                    return this;
                }

                if(currentArmLocation == ARM_LOCATION.FOLDED){
                    setArmSpinServoPosition_raw(ARM_SPIN_STRAIGHT);
                }

                new MoveArmToSwingPositionAction(desiredLocation.armSwingMotorLocation).start();
                startMovingArmExtensionToPosition(desiredLocation.armExtensionMotorLocation);
                new MoveArmSpinToPositionAction(desiredLocation.armSpinServoLocation).start();

                return this;
            }

            @Override
            public boolean isDone(StringBuilder statusMessage)
            {
                return areChildrenDone(statusMessage);
            }
        }.start();
    }

    private EndableAction startMovingArmExtensionToPosition(final int armExtensionMotorLocation)
    {
        return new EndableAction("movingArmExtensionToPosition", "movingArmExtensionToPosition(%d)", armExtensionMotorLocation)
        {
            DcMotor.RunMode originalMode = armExtensionMotor.getMode();

            @Override
            public EndableAction start()
            {
                super.start();
                if (!armExtensionCalibrated)
                {
                    abort("Arm extension must be calibrated");
                    return this;
                }

                armExtensionMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                armExtensionMotor.setTargetPosition(armExtensionMotorLocation);
                return this;
            }

            @Override
            protected void cleanup(boolean actionWasCompleted)
            {
                armExtensionMotor.setPower(0);
                armExtensionMotor.setMode(originalMode);
                super.cleanup(actionWasCompleted);
            }

            @Override
            public boolean isDone(StringBuilder statusMessage)
            {
                return armExtensionMotor.isBusy();
            }
        }.start();
    }
}