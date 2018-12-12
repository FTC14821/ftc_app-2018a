package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Func;
import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.ArrayList;
import java.util.List;

public class Robot
{
    public static final String ROBOT_TAG = "team14821";

    // Found via experiment
    public static final double ENCODER_CLICKS_PER_INCH = 79.27;
    private static final double ENCODER_CLICKS_PER_ROTATION = 4350;
    // Max height of hook (hook0 + MAX_HOOK_DISTANCE)
    public final int MAX_HOOK_DISTANCE = 27500;
    public final int MAX_SWING_ARM_DISTANCE = 2200;
    public final int MAX_ARM_EXTENSION_DISTANCE = 12800;

    // Does not skid
    public static final double TURN_POWER = 0.5;

    final Telemetry telemetry;
    final HardwareMap hardwareMap;
    final BaseLinearOpMode opMode;
    final DcMotor M0;
    final DcMotor M1;
    final DcMotor armExtensionMotor;
    final DcMotor hookMotor;
    final DcMotor swingMotor;

    boolean allSafetysAreDisabled = false;
    boolean hookSafetyIsDisabled = false;
    boolean armSwingSafetyIsDisabled = false;
    boolean armExtensionSafetyIsDisabled = false;

    boolean motorsInFront = true;

    // Encoder when arm extension is at the top
    boolean armExtensionCalibrated = false;
    int extension0;
    // Encoder when arm swing is at the bottom
    boolean armSwingCalibrated = false;
    int swing0;
    // Encoder when hook is at bottom
    boolean hookCalibrated = false;
    int hook0;

    List<AbstractOngoingAction> ongoingActions = new ArrayList<>();

    TeamImu teamImu;

    // Initialize these in constructor, maintain them in loop()
    int previousArmSwingPosition, previousHookPosition, previousArmExtensionPosition, previousM0Position, previousM1Position;
    int armSwingSpeed, hookSpeed, armExtensionSpeed, m0Speed, m1Speed;
    double previousArmSwingPower, previousHookPower, previousArmExtensionPower, previousM0Power, previousM1Power;

    double correctHeading;

    private RobotVision robotVision;

    // When to start stopping turns
    public static final double TURN_APPROXIMATION = 3;

    public Robot(ActionTracker callingAction, BaseLinearOpMode baseLinearOpMode, HardwareMap hardwareMap, Telemetry telemetry)
    {
        ActionTracker action = callingAction.startChildAction("RobotConstruction", null);

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

        hookMotor = hardwareMap.dcMotor.get("HookMotor");
        armExtensionMotor = hardwareMap.dcMotor.get("ArmExtensionMotor");
        armExtensionMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        hookMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        swingMotor = hardwareMap.dcMotor.get("SwingMotor");
        swingMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        swingMotor.setDirection(DcMotor.Direction.REVERSE);

        teamImu = new TeamImu().initialize(action, hardwareMap, telemetry);

        previousArmExtensionPosition = armExtensionMotor.getCurrentPosition();
        previousHookPosition = hookMotor.getCurrentPosition();
        previousArmSwingPosition = swingMotor.getCurrentPosition();
        previousM0Position = M0.getCurrentPosition();
        previousM1Position = M1.getCurrentPosition();

        previousArmSwingPower = 0;
        previousHookPower = 0;
        previousArmExtensionPower = 0;
        previousM0Power = 0;
        previousM1Power = 0;
        
        armSwingSpeed = 0;
        hookSpeed = 0;
        armExtensionSpeed = 0;
        m0Speed = 0;
        m1Speed = 0;

        correctHeading = teamImu.getTotalDegreesTurned();

        setRobotOrientation(action, true);
        stop(action, true);

        setupRobotTelemetry(telemetry);

        action.finish();
    }

    public void init(ActionTracker callingAction)
    {

    }

    boolean shouldRobotKeepRunning(ActionTracker callingAction)
    {
        return opMode.shouldOpModeKeepRunning(callingAction);
    }

    private void setupRobotTelemetry(Telemetry telemetry)
    {
        telemetry.addLine("Robot: ")
                .addData("Orientation", new Func<String>() {
                    @Override
                    public String value() {
                        return opMode.saveTelemetryData("Orientation", motorsInFront ? "MotorsFront" : "MotorsBack");
                    }
                })
        ;

        telemetry.addLine("Heading: ")
                .addData("", new Func<String>() {
                    @Override
                    public String value() {
                        return opMode.saveTelemetryData("Heading","|TotDeg=%.1f|Correct=%.1f|Error=%.1f", 
                                teamImu.getTotalDegreesTurned(),
                                correctHeading,
                                getHeadingError());
                    }});

        telemetry.addLine("Left: ")
                .addData("", new Func<String>() {
                    @Override
                    public String value() {
                        return opMode.saveTelemetryData("Left", "|p=%+.1f|s=+%d|Loc=%+d|Tgt=%+d|%s",
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
                        return opMode.saveTelemetryData("Right", "|p=%+.1f|s=+%d|Loc=%+d|Tgt=%+d|%s",
                                getRightMotor().getPower(),
                                getRightMotor() == M0 ? m0Speed : m1Speed,
                                getRightMotor().getCurrentPosition(),
                                getRightMotor().getTargetPosition(),
                                getRightMotor() == M0 ? "M0" : "M1");
                    }});

        telemetry.addLine("Hook: ")
                .addData("", new Func<String>() {
                    @Override
                    public String value() {
                        return opMode.saveTelemetryData("Hook",
                                "|p=%+.1f|s=%+d|Loc=%d z=%d",
                                 hookMotor.getPower(), hookSpeed, hookMotor.getCurrentPosition(), hook0);
                    }});
        telemetry.addLine("ArmSwing: ")
                .addData("", new Func<String>() {
                    @Override
                    public String value() {
                        return opMode.saveTelemetryData("ArmSwing",
                                "|p=%+.1f|s=%+d|Loc=%d z=%d",
                                swingMotor.getPower(), armSwingSpeed, swingMotor.getCurrentPosition(), swing0
                        );
                    }});
        telemetry.addLine("ArmExtension: ")
                .addData("", new Func<String>() {
                    @Override
                    public String value() {
                        return opMode.saveTelemetryData("ArmExtension",
                                "|p=%+.1f|s=%+d|Loc=%d z=%d",
                                armExtensionMotor.getPower(), armExtensionSpeed, armExtensionMotor.getCurrentPosition(), extension0
                        );
                    }});
    }


    public void stop(ActionTracker callingAction, boolean brake)
    {
        ActionTracker action = callingAction.startChildAction(
                "stopWheels",
                "stopWheels(%s)", brake?"brake" : "drift");

        DcMotor.ZeroPowerBehavior originalBehavior = M0.getZeroPowerBehavior();

        if(brake)
        {
            M0.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            M1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            setRightPower(action, 0);
            setLeftPower(action, 0);
            while ( shouldRobotKeepRunning(action) && (Math.abs(m0Speed) > 5 || Math.abs(m1Speed) > 5) )
                action.setStatus("Waiting for M0/M1 to stop: speeds are %d and %d", m0Speed, m1Speed);
            action.setStatus("M0/M1 are not moving");
            M0.setZeroPowerBehavior(originalBehavior);
            M1.setZeroPowerBehavior(originalBehavior);
        }
        else
        {
            setRightPower(action, 0);
            setLeftPower(action, 0);
        }
        action.finish();
    }

    public void driveStraight(ActionTracker callingAction, double power)
    {
        ActionTracker action = callingAction.startChildAction("Straight", "Straight(pow=%.2f)", power);

        setLeftPower(action, power);
        setRightPower(action, power);

        action.finish();
    }

    /**
     * Turn robot with opposite power to wheels
     *
     * @param power (-1..1) negative means to the left
     */
    public void spin(ActionTracker callingAction, double power)
    {
        ActionTracker action = callingAction.startChildAction("Spin", "Spin%s(%.2f)", power > 0 ? "Right" : "Left", power);
        setLeftPower(action, power);
        setRightPower(action, -power);
        action.finish();
    }

    public void setDrivingPowers(ActionTracker callingAction, double leftPower, double rightPower)
    {
        ActionTracker action = callingAction.startChildAction("SetDrivingPowers", "SetDrivingPowers(%.2f,%.2f)", leftPower, rightPower);
        setLeftPower(action, leftPower);
        setRightPower(action, rightPower);
        action.finish();
    }

    public void setDrivingZeroPowerBehavior(ActionTracker callingAction, DcMotor.ZeroPowerBehavior behavior)
    {
        callingAction.startImmediateChildAction("setDrivingZeroPowerBehavior", "setDrivingZeroPowerBehavior(%s)", behavior);
        M0.setZeroPowerBehavior(behavior);
        M1.setZeroPowerBehavior(behavior);
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

    public void reverseRobotOrientation(ActionTracker callingAction) {
        setRobotOrientation(callingAction, !motorsInFront);
    }

    private void setRobotOrientation(ActionTracker callingAction, boolean motorsInFront) {
        callingAction.startImmediateChildAction("setRobotOrientation",
                "setRobotOrientation(%s)", motorsInFront ? "MotorsInFront" : "MotorsInBack");

        this.motorsInFront = motorsInFront;
        getRightMotor().setDirection(DcMotorSimple.Direction.REVERSE);
        getLeftMotor().setDirection(DcMotorSimple.Direction.FORWARD);
    }

    public void setHookPower(ActionTracker callingAction, double power)
    {
        if(powersAreDifferent(hookMotor.getPower(), power) )
            callingAction.startImmediateChildAction("setHookPower", "setHookPower(%.2f)", power);

        if (isHookPowerOK(power)) {
            hookMotor.setPower(power);
        }
    }


    public void setArmExtensionPower(ActionTracker callingAction, double power)
    {
        if(powersAreDifferent(armExtensionMotor.getPower(), power))
            callingAction.startImmediateChildAction("setArmExtensionPower", "setArmExtensionPower(%.2f)", power);

        if(isArmExtensionPowerOK(power))
        {
            armExtensionMotor.setPower(power);
        }
    }

    public void setSwingArmPower(ActionTracker callingAction, double power)
    {
        if ( power == 0 && armExtensionMotor.getPower() == 0 )
            return;

        ActionTracker action = callingAction.startChildAction("setSwingArmPower", "setSwingArmPower(%.1f)", power);

        // Zero power is simple
        if(power == 0)
        {
            setSwingArmPower_raw(action, 0,0, "Zero");
            return;
        }

        if (!isSwingArmPowerOK(power))
        {
            action.finish("Power not okay");
            return;
        }

        // When not calibrated:  Power/4
        if(!armSwingCalibrated)
        {
            setSwingArmPower_raw(action, power, 1.0/4, "NotCalibrated");
            return;
        }

        String powerAdjustmentLabels="";

        boolean directionIsUp;
        if(power > 0)
            directionIsUp = true;
        else
            directionIsUp = false;


        double powerMultiple = 1.0;
        int zone;
        boolean armIsExtended;
        if(armExtensionMotor.getCurrentPosition() >= extension0 + MAX_ARM_EXTENSION_DISTANCE / 3)
        {
            armIsExtended = true;
            powerAdjustmentLabels += "|+Extended";
        }
        else
        {
            armIsExtended = false;
            powerAdjustmentLabels += "|-Extended";
        }


        zone = getArmSwingZone();

        if(zone == 1)
        {
            if(!directionIsUp)
            {
                powerAdjustmentLabels += "|DownInZone1=0.25";
                powerMultiple /= 4;

                if (armIsExtended)
                    powerMultiple /= 10;
            }
            else
            {
                powerAdjustmentLabels += "|UpInZone1=0.5";
                powerMultiple /= 2;
            }
        }

        if(zone == 2)
        {
            if(!directionIsUp)
            {
                if(armIsExtended)
                {
                    powerAdjustmentLabels += "|DownInZone2&Extended=0.25";
                    powerMultiple /= 4;
                }
                else
                {
                    powerAdjustmentLabels += "|DownInZone2&!Extended=0.3";
                    powerMultiple /= 3;
                }
            }
            else
            {
                powerAdjustmentLabels += "|UpInZone2=0.3";
                powerMultiple /= 3;
            }

        }

        if(zone == 3)
        {
            if(directionIsUp)
            {
                powerAdjustmentLabels += "|+Power/DownInZone3=0.25";
                powerMultiple /= 4;

                if (armIsExtended)
                {
                    powerAdjustmentLabels += "|+Extended=0.2 more";
                    powerMultiple /= 5;
                }
            }
            else
            {
                powerAdjustmentLabels += "|-Power/UpInZone3=0.5";
                powerMultiple /= 2;
            }
        }

        setSwingArmPower_raw(action, power, powerMultiple, powerAdjustmentLabels);
    }

    public int getArmSwingZone()
    {
        int zone;
        int armLocation = swingMotor.getCurrentPosition();

        if(armLocation < swing0 + 1000)
            zone = 1;
        else if(armLocation >= swing0 + 1000 && armLocation < swing0 + 1150)
            zone = 2;
        else
            zone = 3;
        return zone;
    }


    public void setSwingArmPower_raw(ActionTracker actionToFinish, double power, double powerMultiple, String powerAdjustmentLabels)
    {
        double actualPower = power * powerMultiple;
        if(powersAreDifferent(actualPower, swingMotor.getPower()))
            actionToFinish.finish("Setting arm-swing power to %.2f (%.2f x %.2f): %s", actualPower, power, powerMultiple, powerAdjustmentLabels);
        else
            actionToFinish.finish("No arm-swing power change");

        swingMotor.setPower(actualPower);
    }

    private void setLeftPower(ActionTracker callingAction, double leftPower)
    {
        if(powersAreDifferent(leftPower, getLeftMotor().getPower()))
            callingAction.setStatus("Setting left power to %.2f", leftPower);

        getLeftMotor().setPower(leftPower);
    }

    private boolean powersAreDifferent(double power1, double power2)
    {
        // Compare powers to 2 decimal places (multiply by 100 and remove remaining decimals)
        return  Math.floor(power1*100) != Math.floor(power2*100);
    }

    private void setRightPower(ActionTracker callingAction, double rightPower)
    {
        if(powersAreDifferent(rightPower, getRightMotor().getPower()))
            callingAction.setStatus("Setting right power to %.2f", rightPower);

        getRightMotor().setPower(rightPower);
    }

    /**
     * @param power    Positive power is forward, negative power is backwards (between -1, 1)
     * @param steering Positive steering is to the right, negative steering is to the left (between -1, 1)
     */
    void setPowerSteering(ActionTracker callingAction, double power, double steering)
    {
        ActionTracker action = callingAction.startChildAction("Steer", "Steer(pow=%.2f, %.2f)", power, steering);

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

        setLeftPower(action, powerLeft);
        setRightPower(action, powerRight);
        action.finish();
    }

    public void loop(ActionTracker callingAction)
    {
        ActionTracker action = callingAction.startChildAction("Robot.loop", null);
        
        int currentArmExtensionPosition = armExtensionMotor.getCurrentPosition();
        int currentHookPosition = hookMotor.getCurrentPosition();
        int currentArmSwingPosition = swingMotor.getCurrentPosition();
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

        healthCheck(action);

        teamImu.loop(action);
        if ( robotVision != null )
            robotVision.loop(action);


        // Loop through a copy of the ongoing actions so we can remove completed actions
        for(AbstractOngoingAction ongoingAction : new ArrayList<>(ongoingActions))
        {
            ongoingAction.loop();
            if(ongoingAction.isDone())
                ongoingActions.remove(ongoingAction);
        }

        previousArmSwingPower = swingMotor.getPower();
        previousHookPower = hookMotor.getPower();
        previousArmExtensionPower = armExtensionMotor.getPower();
        previousM0Power = M0.getPower();
        previousM1Power = M1.getPower();

        action.finish();
    }


    // Protect robot
    public void healthCheck(ActionTracker callingAction)
    {
        ActionTracker action = callingAction.startChildAction("RobotHealthCheck", null);
        if(opMode instanceof TeleOpMode)
        {
            if (Math.abs(previousArmSwingPower) >= 0.25 && Math.abs(armSwingSpeed) < 10)
            {
                action.setStatus("EMERGENCY ARM SWING STOP: Power was %.2f, speed was %d", previousArmSwingPower, armSwingSpeed);
                setSwingArmPower(action,0);
            }
            if (Math.abs(previousHookPower) >= 0.25 && Math.abs(hookSpeed) < 10)
            {
                action.setStatus("EMERGENCY HOOK STOP: Power was %.2f, speed was %d", previousHookPower, hookSpeed);
                setHookPower(action,0);
            }
            if (Math.abs(previousArmExtensionPower) >= 0.25 && Math.abs(armExtensionSpeed) < 10)
            {
                action.setStatus("EMERGENCY ARM EXTENSION STOP: Power was %.2f, speed was %d", previousArmExtensionPower, armExtensionSpeed);
                setArmExtensionPower(action, 0);
            }
        }


        if (!isArmExtensionPowerOK(armExtensionMotor.getPower()))
        {
            action.setStatus("Stopping armExtension motor");
            armExtensionMotor.setPower(0);
        }

        if (!isHookPowerOK(hookMotor.getPower()))
        {
            action.setStatus("Stopping hook motor");
            hookMotor.setPower(0);
        }

        if (!isSwingArmPowerOK((swingMotor.getPower())))
        {
            action.setStatus("Stopping armSwing motor");
            swingMotor.setPower(0);
        }

        action.finish();
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
        else if (hookMotor.getCurrentPosition() > hook0 + MAX_HOOK_DISTANCE && powerToCheck > 0) {
            problemReason = String.format("Hook power %f > 0 and hook too high (%d > %d)", powerToCheck,
                    hookMotor.getCurrentPosition(), hook0 + MAX_HOOK_DISTANCE);
        } else if (hookMotor.getCurrentPosition() < hook0 && powerToCheck < 0) {
            problemReason = String.format("Hook power %f < 0 and hook too low (%d < %d)", powerToCheck,
                    hookMotor.getCurrentPosition(), hook0 );
        }
        else
            problemReason = null;

        if(problemReason != null)
            RobotLog.ww(ROBOT_TAG, "Hook power %.2f IS NOT OKAY at position %d: %s", powerToCheck, hookMotor.getCurrentPosition(), problemReason);

        return problemReason==null;
    }

    private boolean isSwingArmPowerOK(double powerToCheck)
    {
        boolean result;

        if(allSafetysAreDisabled)
            result = true;
        else if ( armSwingSafetyIsDisabled)
            result = true;

        else if (swingMotor.getCurrentPosition() > swing0 + MAX_SWING_ARM_DISTANCE && powerToCheck > 0) {
            result = false;
        } else if (swingMotor.getCurrentPosition() < swing0 && powerToCheck < 0) {
            result = false;
        }
        else
            result = true;

        if(!result)
            RobotLog.ww(ROBOT_TAG, "Swing arm power %.2f IS NOT OKAY at position %d", powerToCheck, swingMotor.getCurrentPosition());

        return result;
    }

    private boolean isArmExtensionPowerOK(double powerToCheck)
    {
        boolean result;

        if(allSafetysAreDisabled)
            result = true;
        else if ( armExtensionSafetyIsDisabled )
            result = true;

        else if (armExtensionMotor.getCurrentPosition() > extension0 + MAX_ARM_EXTENSION_DISTANCE && powerToCheck > 0) {
            result = false;
        } else if (armExtensionMotor.getCurrentPosition() < extension0 && powerToCheck < 0) {
            result = false;
        }
        else
            result = true;

        if(!result)
            RobotLog.ww(ROBOT_TAG, "Arm extension power" +
                    " %.2f IS NOT OKAY at position %d", powerToCheck, armExtensionMotor.getCurrentPosition());

        return result;
    }

    public void hookUp(ActionTracker callingAction, double power, boolean wait)
    {
        ActionTracker action = callingAction.startChildAction("HookUp",
                "HookUp(pow=%.1f, %s)", power, wait ? "wait" : "nowait");

        setHookPower(action, power);
        if (wait) {
            // Wait until the safety stops the hookMotor
            while (shouldRobotKeepRunning(action) && hookMotor.getPower() != 0) {
                action.setStatus("Hook still moving");
            }
        }
        action.finish();
    }

    public void hookDown(ActionTracker callingAction, double power, boolean wait)
    {
        ActionTracker action = callingAction.startChildAction("HookDown",
                "HookDown(pow=%.1f, %s)", power, wait ? "wait" : "nowait");

        setHookPower(action, -Math.abs(power));
        if (wait) {
            while (shouldRobotKeepRunning(action) && hookMotor.getPower() != 0) {
                action.setStatus("Hook still moving");
            }
        }
        action.finish();
    }

    public void inchmove(ActionTracker callingAction, double inches, double power)
    {
        ActionTracker action = callingAction.startChildAction("InchMove", "InchMove(%.1f, %.1f", inches, power);

        double startPosition = getWheelPosition();

        double encoderClicks = inches * ENCODER_CLICKS_PER_INCH;
        double stopPosition = getWheelPosition() + encoderClicks;

        action.setStatus("Starting to move for %d encoder clicks", encoderClicks);

        while (shouldRobotKeepRunning(action) && getWheelPosition() <= stopPosition) {

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

            action.setStatus("%d clicks, %.1f inches to go. Heading error: %.1f degrees (robot facing too far %s)",
                    stopPosition - getWheelPosition(),
                    (stopPosition - getWheelPosition()) / ENCODER_CLICKS_PER_INCH,
                    headingError,
                    headingError > 0 ? "right" : "left");

            if ( headingError > 10 )
                // Too far off course, need to stop and turn
                // Use 0 degrees so turn will just turn to the correct place
                turnLeft(action, 0, 3);
            else if ( headingError < -10 )
                turnRight(action, 0, 3);
            else
            {
                if (headingError > 0.0)
                {
                    //Steer to the left
                    // PowerSteering takes negative steering to the left
                    setPowerSteering(action, wheelPower, -0.05 * Math.abs(headingError));
                } else if (headingError < 0.0)
                {
                    //Steer to the right
                    // PowerSteering takes positive steering to the right

                    setPowerSteering(action, wheelPower, 0.05 * Math.abs(headingError));
                } else
                {
                    // Go Straight
                    driveStraight(action, wheelPower);
                }
            }
        }
        stop(action, true);

        action.finish();
    }

    /**
     * How far off is the robot from the correct heading?
     * @return Degrees: < 0 ==> Robot needs to turn Right, >0 ==> Robot needs to turn Left
     */
    private double getHeadingError() {
        return correctHeading - getTotalDegreesTurned();
    }

    public void inchmoveBack(ActionTracker callingAction, double inches, double power, boolean correctSteering)
    {
        ActionTracker action = callingAction.startChildAction(
                "InchMoveBack", "InchmoveBack(d=%.1fin, pow=%.1f, steer=%s)",
                inches, power, correctSteering ? "corrected" : "no-correction");

        int startPosition = getWheelPosition();

        int encoderClicks = (int)(inches * ENCODER_CLICKS_PER_INCH);
        int stopPosition = getWheelPosition() - encoderClicks;

        action.setStatus("Starting to move for %d encoder clicks", encoderClicks);

        while (shouldRobotKeepRunning(action) && getWheelPosition() >= stopPosition) {

            // avoid skidding by using less power for first 10 inches
            double wheelPower;
            if ( startPosition - getWheelPosition() < 10 * ENCODER_CLICKS_PER_INCH )
                wheelPower = 0.3;
            else
                wheelPower = power;

            if(correctSteering)
            {
                //Heading is larger to the left
                // Example:
                //   correctHeading: 0  (straight ahead)
                //   currentHeading: -5 (negative is to the right)
                //   HeadingError: 5 ==> Need to turn 5 degrees to the left

                double headingError = getHeadingError();

                action.setStatus(String.format("%.1f inches to go. Heading error: %.1f degrees(robot front facing too far %s)",
                        (stopPosition - getWheelPosition()) / ENCODER_CLICKS_PER_INCH,
                        headingError,
                        headingError > 0 ? "right" : "left"
                        ));


                if ( headingError > 10 ) {
                    action.setStatus("Turning left because we're off by %.1f degrees (>10)", headingError);
                    // Use 0 degrees so turn will just turn to the correct place
                    turnLeft(action, 0, 3);
                }
                else if ( headingError < -10 ) {
                    action.setStatus("Turning right because we're off by %.1f degrees (<-10)", headingError);

                    // Use 0 degrees so turn will just turn to the correct place
                    turnRight(action, 0, 3);
                }
                else
                {
                    action.setStatus("Using proportional steering because we're off by %.1f degrees", headingError);

                    if (headingError > 0.0) {
                        //The current heading is too small so we turn to the right
                        // (opposite of inchMoveForward because we're going backwards)
                        setPowerSteering(action, -wheelPower, 0.1 * Math.abs(headingError));
                    } else if (headingError < 0.0) {
                        //Current heading is too big, so we steer to the left (again since we're going backwards)
                        setPowerSteering(action, -wheelPower, -0.1 * Math.abs(headingError));
                    } else {
                        // Go Straight
                        driveStraight(action, -wheelPower);
                    }
                }
            }
            else
                driveStraight(action, -wheelPower);
        }
        stop(action, true);
        action.finish();
    }

    public void resetCorrectHeading(ActionTracker callingAction, String reason)
    {
        callingAction.startImmediateChildAction(
                "ResetHeading", "ResetHeading(%.1f deg, %s)", getTotalDegreesTurned(), reason);
        correctHeading = getTotalDegreesTurned();
    }

    public void turnRight(ActionTracker callingAction, double degrees, int turnTries) {
        ActionTracker action = callingAction.startChildAction(
                "TurnRight", "TurnRightV2(%.0f)", degrees);
        correctHeading -= degrees;

        int count=0;
        while ( Math.abs(getHeadingError()) > TURN_APPROXIMATION && count < turnTries && shouldRobotKeepRunning(callingAction) )
        {
            count++;
            action.setStatus("Turn attempt %d", count);
            turnToProperHeading(action);
        }
        action.finish();
    }

    public void turnLeft(ActionTracker callingAction, double degrees, int turnTries) {
        ActionTracker action = callingAction.startChildAction(
                "TurnLeft", "TurnLeftV2(%.0f)", degrees);
        correctHeading += degrees;

        int count = 0;
        while ( Math.abs(getHeadingError()) > TURN_APPROXIMATION && count < turnTries && shouldRobotKeepRunning(callingAction) )
        {
            count++;
            action.setStatus("Turn attempt %d", count);
            turnToProperHeading(action);
        }
        action.finish();
    }

    public void turnToProperHeading(ActionTracker callingAction)
    {
        ActionTracker action = callingAction.startChildAction(
                "TurnToHeading", "TurnToHeding(%.2f degrees)", getHeadingError());
        int encoderClicksToGo = (int) Math.abs(ENCODER_CLICKS_PER_ROTATION/360 * getHeadingError());

        action.setStatus("Starting to turn: HeadingError=%+.1f and wheelEncoderClicks=%d",
                getHeadingError(), encoderClicksToGo);

        DcMotor.RunMode oldLeftMode = getLeftMotor().getMode();
        DcMotor.RunMode oldRightMode = getRightMotor().getMode();

        int leftPowerMultiple;
        int rightPowerMultiple;

        if(getHeadingError() < 0)
        {
            leftPowerMultiple = +1;
            rightPowerMultiple = -1;
        }
        else
        {
            leftPowerMultiple = -1;
            rightPowerMultiple = +1;
        }

        getLeftMotor().setPower(0);
        getLeftMotor().setMode(DcMotor.RunMode.RUN_TO_POSITION);
        getLeftMotor().setPower(leftPowerMultiple * TURN_POWER);
        getLeftMotor().setTargetPosition(getLeftMotor().getCurrentPosition() + leftPowerMultiple * encoderClicksToGo);

        getRightMotor().setPower(0);
        getRightMotor().setMode(DcMotor.RunMode.RUN_TO_POSITION);
        getRightMotor().setPower(rightPowerMultiple * TURN_POWER);
        getRightMotor().setTargetPosition(getRightMotor().getCurrentPosition() + rightPowerMultiple * encoderClicksToGo);

        int i = 1;
        while(shouldRobotKeepRunning(action) && (getLeftMotor().isBusy() || getRightMotor().isBusy()))
        {
            action.setStatus("Day %d: I'm still turning. I have %.1f degrees remaining", i, getHeadingError());
            i++;
        }

        getLeftMotor().setMode(oldLeftMode);
        getRightMotor().setMode(oldRightMode);

        stop(action, true);
        action.finish("Day %d: I finally finished my turn. The heading error is $.1f", i, getHeadingError());
    }


    //Skooch
    public void skoochRight(ActionTracker callingAction)
    {
        ActionTracker action = callingAction.startChildAction("SkoochRight", null);
        resetCorrectHeading(action, "Skooching relative to where we were");
        inchmove(action,5,0.5);
        turnRight(action, 20, 1);
        inchmove(action, 5,0.5);
        turnLeft(action,20, 1);
        inchmoveBack(action,10,0.5, true);
        stop(action, true);
        action.finish();
    }
    public void skoochLeft(ActionTracker callingAction)
    {
        ActionTracker action = callingAction.startChildAction("SkoochLeft", null);
        resetCorrectHeading(action, "Skooching relative to where we were");
        inchmove(action,5,0.5);
        turnLeft(action,20, 1);
        inchmove(action,5,0.5);
        turnRight(action,20, 1);
        inchmoveBack(action,10,0.5, true);
        stop(action, true);
        action.finish();
    }

    // Calibrations
    public void calibrateHook(ActionTracker callingAction)
    {
        ActionTracker action = callingAction.startChildAction("calibrateHook", null);

        stop(action, false);
        hookSafetyIsDisabled = true;
        int change;
        int oldValue = hookMotor.getCurrentPosition();
        setHookPower(action,-0.15);

        while (shouldRobotKeepRunning(action)) {
            opMode.teamSleep(action,250, "Letting hook lower");
            int newValue = hookMotor.getCurrentPosition();
            change = newValue - oldValue;
            oldValue = newValue;

            action.setStatus("Position=%d, change=%d", newValue, change);
            // If it isn't getting more negative
            if (change > -5) {
                action.setStatus("Hook has stopped moving: change was %d", change);
                setHookPower(action,0);
                break;
            }
        }
        hook0 = hookMotor.getCurrentPosition() + 500;
        hookCalibrated = true;
        hookSafetyIsDisabled = false;
        action.finish();
    }

    public void calibrateArmSwing(ActionTracker callingAction)
    {
        ActionTracker action = callingAction.startChildAction("calibrateArmSwing", null);

        stop(action, false);
        armExtensionSafetyIsDisabled = true;
        int change;
        int oldValue = swingMotor.getCurrentPosition();
        setSwingArmPower_raw(action, -0.3, 1.0, "Direct arm control");

        while (shouldRobotKeepRunning(action)) {
            opMode.sleep(250);
            int newValue = swingMotor.getCurrentPosition();
            change = newValue - oldValue;
            oldValue = newValue;

            action.setStatus("Position=%d, change=%d", newValue, change);
            // If it isn't getting more negative
            if (change > -5) {
                setSwingArmPower(action, 0);
                break;
            }
        }

        // Let the foam spring back
        opMode.sleep(1000);

        swing0 = swingMotor.getCurrentPosition() + 50;
        armSwingCalibrated = true;
        armExtensionSafetyIsDisabled = false;
        action.finish();
    }

    public void calibrateArmExtension(ActionTracker callingAction)
    {
        ActionTracker action = callingAction.startChildAction("calibrateArmExtension", null);

        stop(action, false);
        armExtensionSafetyIsDisabled = true;
        int change;
        int oldValue = armExtensionMotor.getCurrentPosition();

        setArmExtensionPower(action,-0.1);

        while (shouldRobotKeepRunning(action)) {
            opMode.sleep(250);
            int newValue = armExtensionMotor.getCurrentPosition();
            change = newValue - oldValue;
            oldValue = newValue;

            action.setStatus("Position=%d, change=%d", newValue, change);
            // If it isn't getting more negative
            if (change > -5) {
                setArmExtensionPower(action,0);
                break;
            }
        }

        extension0 = armExtensionMotor.getCurrentPosition() + 500;
        armExtensionCalibrated = true;
        armExtensionSafetyIsDisabled = false;
    }

    public void calibrateEverything(ActionTracker callingAction)
    {
        ActionTracker action = callingAction.startChildAction("CalibrateEverything", null);

        //startOngoingAction(new OngoingAction_CalibrateHook(this));
        //startOngoingAction(new OngoingAction_CalibrateArmExtensionAndSwing(this));
        calibrateHook(action);
        calibrateArmExtension(action);
        calibrateArmSwing(action);
    }

    public AbstractOngoingAction startOngoingAction( AbstractOngoingAction action)
    {
        ongoingActions.add(action);
        action.start();
        return action;
    }
    public AbstractOngoingAction startArmReset(ActionTracker callingAction)
    {
        return startOngoingAction(new OngoingAction_ArmReset(callingAction, this));
    }

    public RobotVision getRobotVision(ActionTracker callingAction)
    {
        if ( robotVision == null )
        {
            ActionTracker action = callingAction.startChildAction("Setting up vision", null);
            robotVision = new RobotVision(action, this);
            robotVision.init(action);
            action.finish();
        }
        return robotVision;
    }

    public void resetDrivingEncoders(ActionTracker gamepad1Action, String s)
    {
        DcMotor.RunMode originalLeftMode = getLeftMotor().getMode();
        DcMotor.RunMode originalRightMode = getRightMotor().getMode();

        getLeftMotor().setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        getRightMotor().setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        getLeftMotor().setMode(originalLeftMode);
        getRightMotor().setMode(originalRightMode);
    }
}