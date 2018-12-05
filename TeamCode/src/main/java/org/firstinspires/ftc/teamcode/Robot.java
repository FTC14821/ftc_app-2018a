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
    final Telemetry telemetry;
    final HardwareMap hardwareMap;
    final BaseLinearOpMode opMode;
    final DcMotor M0;
    final DcMotor M1;
    final DcMotor armExtensionMotor;
    final DcMotor hookMotor;
    final DcMotor swingMotor;

    boolean safetysAreDisabled = false;

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
    // Max height of hook (hook0 + MAX_HOOK_DISTANCE)
    public final double MAX_HOOK_DISTANCE = 27500;
    public final double MAX_SWING_ARM_DISTANCE = 2200;
    public final double MAX_ARM_EXTENSION_DISTANCE = 12800;

    List<AbstractOngoingAction> ongoingActions = new ArrayList<>();

    TeamImu teamImu;

    // Telemetry
    String drivingCommand = "";

    // Initialize these in constructor, maintain them in loop()
    int previousArmSwingPosition, previousHookPosition, previousArmExtensionPosition, previousM0Position;
    int armSwingSpeed, hookSpeed, armExtensionSpeed, m0Speed;
    double previousArmSwingPower, previousHookPower, previousArmExtensionPower, previousM0Power;

    double correctHeading;

    private RobotVision robotVision;

    public static final double ENCODER_CLICKS_PER_INCH = 79.27;

        public Robot(BaseLinearOpMode baseLinearOpMode, HardwareMap hardwareMap, Telemetry telemetry)
    {
        this.hardwareMap = hardwareMap;
        this.telemetry   = telemetry;
        opMode = baseLinearOpMode;
        M0 = hardwareMap.dcMotor.get("M0");
        M1 = hardwareMap.dcMotor.get("M1");

        hookMotor = hardwareMap.dcMotor.get("HookMotor");
        armExtensionMotor = hardwareMap.dcMotor.get("ArmExtensionMotor");
        armExtensionMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        hookMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        swingMotor = hardwareMap.dcMotor.get("SwingMotor");
        swingMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        swingMotor.setDirection(DcMotor.Direction.REVERSE);

        teamImu = new TeamImu().initialize(hardwareMap, telemetry);

        previousArmExtensionPosition = armExtensionMotor.getCurrentPosition();
        previousHookPosition = hookMotor.getCurrentPosition();
        previousArmSwingPosition = swingMotor.getCurrentPosition();
        previousM0Position = M0.getCurrentPosition();

        previousArmSwingPower = 0;
        previousHookPower = 0;
        previousArmExtensionPower = 0;
        previousM0Power = 0;
        
        armSwingSpeed = 0;
        hookSpeed = 0;
        armExtensionSpeed = 0;
        m0Speed = 0;

        correctHeading = teamImu.getTotalDegreesTurned();

        setRobotOrientation(true);
        stop(true);

        setupRobotTelemetry(telemetry);
    }

    public void init()
    {
    }

    boolean shouldRobotKeepRunning()
    {
        return opMode.shouldOpModeKeepRunning();
    }

    private void setupRobotTelemetry(Telemetry telemetry)
    {
        telemetry.addLine("Robot: ")
                .addData("TotDeg", new Func<String>() {
                    @Override
                    public String value() {
                        return String.format("%.1f", teamImu.getTotalDegreesTurned());
                    }
                })
                .addData("CorrectHdg", new Func<String>() {
                    @Override
                    public String value() {
                        return String.format("%.1f", correctHeading);
                    }
                })
                .addData("Cmd", new Func<String>() {
                    @Override
                    public String value() {
                        return drivingCommand;
                    }
                })
                .addData("Orientation", new Func<String>() {
                    @Override
                    public String value() {
                        return motorsInFront ? "MotorsFront" : "MotorsBack";
                    }
                })
        ;

        telemetry.addLine("Driving: ")
                .addData(String.format("L/%s", getLeftMotor()==M0 ? "M0" : "M1"), new Func<String>() {
                    @Override
                    public String value() {
                        return String.format("%+.1f@%d", getLeftMotor().getPower(), getLeftMotor().getCurrentPosition());
                    }
                })
                .addData(String.format("R/%s", getRightMotor()==M0 ? "M0" : "M1"), new Func<String>() {
                    @Override
                    public String value() {
                        return String.format("%+.1f@%d", getRightMotor().getPower(), getRightMotor().getCurrentPosition());
                    }
                });

        telemetry.addLine("Hook")
                .addData("Pow", new Func<String>() {
                    @Override
                    public String value() {
                        return String.format("%+.1f", hookMotor.getPower());
                    }
                })
                .addData("Spd", new Func<String>() {
                    @Override
                    public String value() {
                        return String.format("%+d", hookSpeed);
                    }
                })
                .addData("Loc", new Func<String>() {
                    @Override
                    public String value() {
                        return String.format("%d z=%d", hookMotor.getCurrentPosition(), hook0);
                    }
                });

        telemetry.addLine("Arm Swing")
                .addData("Pow", new Func<String>() {
                    @Override
                    public String value() {
                        return String.format("%+.1f", swingMotor.getPower());
                    }
                })
                .addData("Spd", new Func<String>() {
                    @Override
                    public String value() {
                        return String.format("%+d", armSwingSpeed);
                    }
                })
                .addData("Loc", new Func<String>() {
                    @Override
                    public String value() {
                        return String.format("%d zone=%d zero=%d", swingMotor.getCurrentPosition(), getArmSwingZone(), swing0);
                    }
                });
        telemetry.addLine("Arm Extension")
                .addData("Pow", new Func<String>() {
                    @Override
                    public String value() {
                        return String.format("%+.1f", armExtensionMotor.getPower());
                    }
                })
                .addData("Spd", new Func<String>() {
                    @Override
                    public String value() {
                        return String.format("%+d", armExtensionSpeed);
                    }
                })
                .addData("Loc", new Func<String>() {
                    @Override
                    public String value() {
                        return String.format("%d z=%d", armExtensionMotor.getCurrentPosition(), extension0);
                    }
                });
    }


    public void stop(boolean brake)
    {
        if(brake)
        {
            drivingCommand = "stop and brake";
            setDrivingPowers(-0.1, -0.1);
            opMode.teamSleep(2);
            setDrivingPowers(0.1,0.1);
            opMode.teamSleep(2);
            setDrivingPowers(0,0);
        }
        else
        {
            drivingCommand = "stop";
            setRightPower(0);
            setLeftPower(0);
        }
    }

    public void driveStraight(double power)
    {
        setDrivingCommand("Straight(%.2f)", power);

        setLeftPower(power);
        setRightPower(power);

    }

    private void setDrivingCommand(String format, Object... args)
    {
        String newDrivingCommand = String.format(format, args);
        if(!drivingCommand.equals(newDrivingCommand))
            RobotLog.ww("team14821", "New driving command: %s", newDrivingCommand);
        drivingCommand = newDrivingCommand;
    }

    /**
     * Turn robot with opposite power to wheels
     *
     * @param power (-1..1) negative means to the left
     */
    public void spin(double power)
    {
        setDrivingCommand("Spin%s(%.2f)", power > 0 ? "Right" : "Left", power);
        setLeftPower(power);
        setRightPower(-power);
    }

    public void setDrivingPowers(double leftPower, double rightPower)
    {
        setDrivingCommand("SetDrivingPowers(%.2f,%.2f)", leftPower, rightPower);
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

    public double getTotalDegreesTurned() {
        return teamImu.getTotalDegreesTurned();
    }

    public double getImuHeading() {
        return teamImu.getHeading();
    }


    public void reverseRobotOrientation() {
        setRobotOrientation(!motorsInFront);
    }

    public void setRobotOrientation(boolean motorsInFront)
    {
        RobotLog.ww("team14821", "Setting robot orientation: %s", motorsInFront ? "MotorsInFront" : "MotorsInBack");

        this.motorsInFront = motorsInFront;
        getRightMotor().setDirection(DcMotorSimple.Direction.REVERSE);
        getLeftMotor().setDirection(DcMotorSimple.Direction.FORWARD);
    }

    public void setHookPower(double power)
    {
        if(!hookCalibrated && !safetysAreDisabled)
            return;
        if(powersAreDifferent(hookMotor.getPower(), power) )
        {
            RobotLog.ww("team14821","Setting hook power to %.2f", power);
        }

        if (isHookPowerOK(power)) {
            hookMotor.setPower(power);
        }
    }


    public void setArmExtensionPower(double power)
    {
        if(powersAreDifferent(armExtensionMotor.getPower(), power))
        {
            RobotLog.ww("team14821","Setting arm extension power to %.2f", power);
        }

        if(isArmExtensionPowerOK(power))
        {
            armExtensionMotor.setPower(power);
        }
    }

    public void setSwingArmPower(double power)
    {
        if (!isSwingArmPowerOK(power))
        {
            return;
        }

        // Zero power is simple
        if(power == 0)
        {
            setSwingArmPower_raw(0,0);
            return;
        }

        boolean directionIsUp;
        if(power > 0)
            directionIsUp = true;
        else
            directionIsUp = false;


        // When not calibrated:  Power/4
        if(!armSwingCalibrated)
        {
            setSwingArmPower_raw(power, 1.0/4);
            return;
        }

        double powerMultiple = 1.0;
        int zone;
        boolean armIsExtended;
        if(armExtensionMotor.getCurrentPosition() >= extension0 + MAX_ARM_EXTENSION_DISTANCE / 3)
            armIsExtended = true;
        else
            armIsExtended = false;


        zone = getArmSwingZone();

        if(zone == 1)
        {
            if(!directionIsUp)
            {
                powerMultiple /= 4;

                if (armIsExtended)
                    powerMultiple /= 10;
            }
            else
                powerMultiple /= 2;
        }

        if(zone == 2)
        {
            if(!directionIsUp)
            {
                if(armIsExtended)
                    powerMultiple /= 4;
                else
                    powerMultiple /= 3;
            }
            else
                powerMultiple /= 3;

        }

        if(zone == 3)
        {
            if(directionIsUp)
            {
                powerMultiple /= 4;

                if (armIsExtended)
                    powerMultiple /= 5;
            }
            else
                powerMultiple /= 2;
        }

        setSwingArmPower_raw(power, powerMultiple);
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


    public void setSwingArmPower_raw(double power, double powerMultiple)
    {
    double actualPower = power * powerMultiple;
    if(powersAreDifferent(actualPower, swingMotor.getPower()))
        RobotLog.ww("team14821", "Setting arm-swing power to %.2f (%.2f x %.2f)", actualPower, power, powerMultiple);
    swingMotor.setPower(actualPower);
    }

    private void setLeftPower(double leftPower)
    {
        if(powersAreDifferent(leftPower, getLeftMotor().getPower()))
            RobotLog.ww("team14821", "Setting left power to %.2f", leftPower);
        getLeftMotor().setPower(leftPower);
    }

    private boolean powersAreDifferent(double power1, double power2)
    {
        // Compare powers to 2 decimal places (multiply by 100 and remove remaining decimals)
        return  Math.floor(power1*100) != Math.floor(power2*100);
    }

    private void setRightPower(double rightPower)
    {
        if(powersAreDifferent(rightPower, getRightMotor().getPower()))
            RobotLog.ww("team14821", "Setting right power to %.2f", rightPower);
        getRightMotor().setPower(rightPower);
    }

    /**
     * @param power    Positive power is forward, negative power is backwards (between -1, 1)
     * @param steering Positive steering is to the right, negative steering is to the left (between -1, 1)
     */
    void setPowerSteering(double power, double steering)
    {
        setDrivingCommand("Steer(%.2f, %.2f)", power, steering);

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

    public void loop()
    {
        int currentArmExtensionPosition = armExtensionMotor.getCurrentPosition();
        int currentHookPosition = hookMotor.getCurrentPosition();
        int currentArmSwingPosition = swingMotor.getCurrentPosition();
        int currentM0Position = M0.getCurrentPosition();

        armSwingSpeed = currentArmSwingPosition - previousArmSwingPosition;
        hookSpeed = currentHookPosition - previousHookPosition;
        armExtensionSpeed = currentArmExtensionPosition - previousArmExtensionPosition;
        m0Speed = currentM0Position - previousM0Position;

        previousArmExtensionPosition = currentArmExtensionPosition;
        previousHookPosition = currentHookPosition;
        previousArmSwingPosition = currentArmSwingPosition;
        previousM0Position = currentM0Position;

        healthCheck();

        teamImu.loop();
        if ( robotVision != null )
            robotVision.loop();


        // Loop through a copy of the ongoing actions so we can remove completed actions
        for(AbstractOngoingAction action : new ArrayList<>(ongoingActions))
        {
            action.loop();
            if(action.isDone())
                ongoingActions.remove(action);
        }

        previousArmSwingPower = swingMotor.getPower();
        previousHookPower = hookMotor.getPower();
        previousArmExtensionPower = armExtensionMotor.getPower();
        previousM0Power = M0.getPower();
    }


    // Protect robot
    public void healthCheck()
    {
        if(opMode instanceof TeleOpMode)
        {
            if (Math.abs(previousArmSwingPower) >= 0.25 && Math.abs(armSwingSpeed) < 10)
            {
                RobotLog.ww(ROBOT_TAG, "EMERGENCY ARM SWING STOP: Power was %.2f, speed was %d", previousArmSwingPower, armSwingSpeed);
                setSwingArmPower(0);
            }
            if (Math.abs(previousHookPower) >= 0.25 && Math.abs(hookSpeed) < 10)
            {
                RobotLog.ww(ROBOT_TAG, "EMERGENCY HOOK STOP: Power was %.2f, speed was %d", previousHookPower, hookSpeed);
                setHookPower(0);
            }
            if (Math.abs(previousArmExtensionPower) >= 0.25 && Math.abs(armExtensionSpeed) < 10)
            {
                RobotLog.ww(ROBOT_TAG, "EMERGENCY ARM EXTENSION STOP: Power was %.2f, speed was %d", previousArmExtensionPower, armExtensionSpeed);
                setArmExtensionPower(0);
            }
            if (Math.abs(previousM0Power) >= 0.25 && Math.abs(m0Speed) < 10)
            {
                RobotLog.ww(ROBOT_TAG, "EMERGENCY WHEEL STOP: Power was %.2f, speed was %d", previousM0Power, m0Speed);
                M0.setPower(0);
            }
        }


        if (!isArmExtensionPowerOK(armExtensionMotor.getPower()))
        {
            armExtensionMotor.setPower(0);
        }

        if (!isHookPowerOK(hookMotor.getPower()))
        {
            hookMotor.setPower(0);
        }

        if (!isSwingArmPowerOK((swingMotor.getPower())))
        {
            swingMotor.setPower(0);
        }
    }

    private boolean isHookPowerOK(double powerToCheck)
    {
        boolean result;

        if(safetysAreDisabled)
            result = true;

        else if (!hookCalibrated)
            result = true;

        else if (hookMotor.getCurrentPosition() > hook0 + MAX_HOOK_DISTANCE && powerToCheck > 0) {
            result = false;
        } else if (hookMotor.getCurrentPosition() < hook0 && powerToCheck < 0) {
            result = false;
        }
        else
            result = true;

        if(!result)
            RobotLog.ww(ROBOT_TAG, "Hook power %.2f IS NOT OKAY at position %d", powerToCheck, hookMotor.getCurrentPosition());

        return result;
    }

    private boolean isSwingArmPowerOK(double powerToCheck)
    {
        boolean result;

        if(safetysAreDisabled)
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

        if(safetysAreDisabled)
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

    public void hookUp(double power, boolean wait)
    {
        setHookPower(power);
        if (wait) {
            // Wait until the safety stops the hookMotor
            while (shouldRobotKeepRunning() && hookMotor.getPower() != 0) {
            }
        }
    }

    public void hookDown(double power, boolean wait)
    {
        setHookPower(-Math.abs(power));
        if (wait) {
            while (shouldRobotKeepRunning() && hookMotor.getPower() != 0) {
            }
        }
    }

    public void inchmove(double inches, double power)
    {
        opMode.setOperation(String.format("InchMove(%.1f, %.1f", inches, power));

        double startPosition = getWheelPosition();

        double encoderClicks = inches * ENCODER_CLICKS_PER_INCH;
        double stopPosition = getWheelPosition() + encoderClicks;

        while (shouldRobotKeepRunning() && getWheelPosition() <= stopPosition) {

            // avoid skidding by using less power for first 10 inches
            double wheelPower;
            if ( getWheelPosition() - startPosition < 10*ENCODER_CLICKS_PER_INCH )
                wheelPower = 0.3;
            else
                wheelPower = power;

            //Heading is larger to the left
            double currentHeading = getTotalDegreesTurned();
            double headingError = correctHeading - currentHeading;

            opMode.setStatus(String.format("%.1f inches to go. Heading error: %.1f degrees",
                    (stopPosition - getWheelPosition()) / ENCODER_CLICKS_PER_INCH,
                    headingError));

            if ( headingError > 10 )
                // Use 0 degrees so turn will just turn to the correct place
                turnLeft(0, 1);
            else if ( headingError < -10 )
                turnRight(0, 1);
            else
            {
                if (headingError > 0.0)
                {
                    //The current heading is too big so we turn to the left
                    setPowerSteering(wheelPower, -0.1 * headingError);
                } else if (headingError < 0.0)
                {
                    //Current heading is too small, so we steer to the right
                    setPowerSteering(wheelPower, 0.1 * headingError);
                } else
                {
                    // Go Straight
                    driveStraight(wheelPower);
                }
            }
        }
        stop(true);

        opMode.setStatus("Done");
    }

    public void inchmoveBack(double inches, double power)
    {
        opMode.setOperation(String.format("InchmoveBack(%.1f, %.1f", inches, power));

        double startPosition = getWheelPosition();

        double encoderClicks = inches * ENCODER_CLICKS_PER_INCH;
        double stopPosition = getWheelPosition() - encoderClicks;

        while (shouldRobotKeepRunning() && getWheelPosition() >= stopPosition) {

            // avoid skidding by using less power for first 10 inches
            double wheelPower;
            if ( startPosition - getWheelPosition() < 10 * ENCODER_CLICKS_PER_INCH )
                wheelPower = 0.3;
            else
                wheelPower = power;

            //Heading is larger to the left
            double currentHeading = getTotalDegreesTurned();
            double headingError = correctHeading - currentHeading;

            opMode.setStatus(String.format("%.1f inches to go. Heading error: %.1f degrees",
                    (stopPosition - getWheelPosition()) / ENCODER_CLICKS_PER_INCH,
                    headingError));


            if (headingError > 0.0) {
                //The current heading is too big so we turn to the right
                setPowerSteering(-wheelPower, 0.1 * headingError);
            } else if (headingError < 0.0) {
                //Current heading is too small, so we steer to the left
                setPowerSteering(-wheelPower, -0.1 * headingError);
            } else {
                // Go Straight
                driveStraight(-wheelPower);
            }
        }
        stop(true);

        opMode.setStatus("Done");
    }

    public void resetCorrectHeading()
    {
        correctHeading = getTotalDegreesTurned();
    }

    public void turnRight(double degrees, double speed)
    {
        correctHeading -= degrees;

        opMode.setOperation(String.format("TurnRight(d=%.1f, s=%.1f", degrees, speed));
        double turnApproximation = 2;
        double startingHeading = getTotalDegreesTurned();
        double endHeading = correctHeading + turnApproximation;

        double degreesToGo = endHeading - getTotalDegreesTurned();

        while (shouldRobotKeepRunning() && degreesToGo < 0) {
            // Goal - Current
            degreesToGo = endHeading - getTotalDegreesTurned();

            opMode.setStatus(String.format("%.1f degrees to go. ", degreesToGo));
            if (degreesToGo > -40) {
                spin(0.2);
            } else {
                spin(speed);
            }
        }
        opMode.setStatus("Right turn is done");
        stop(true);
    }

    public void turnLeft(double degrees, double speed)
    {
        correctHeading += degrees;

        opMode.setOperation(String.format("TurnLeft(d=%.1f, s=%.1f", degrees, speed));
        double turnApproximation = 2;
        double startingHeading = getTotalDegreesTurned();
        double endHeading = correctHeading - turnApproximation;

        double degreesToGo = endHeading - getTotalDegreesTurned();

        while (shouldRobotKeepRunning() && degreesToGo > 0)
        {
            // Goal - Current
            degreesToGo = endHeading - getTotalDegreesTurned();

            opMode.setStatus(String.format("%.1f degrees to go. ", degreesToGo));
            if(degreesToGo < 40)
                spin(-0.2);
            else
                spin(-speed);
        }
        opMode.setStatus("Turn left is done");
        stop(true);
    }

    //Scooch
    public void skoochRight()
    {
        inchmove(5,0.5);
        turnRight(20,0.5);
        inchmove(5,0.5);
        turnLeft(20,0.5);
        inchmoveBack(10,0.5);
    }
    public void skoochLeft()
    {
        inchmove(5,0.5);
        turnLeft(20,0.5);
        inchmove(5,0.5);
        turnRight(20,0.5);
        inchmoveBack(10,0.5);
    }

    // Calibrations
    public void calibrateHook()
    {
        stop(false);
        safetysAreDisabled = true;
        opMode.setOperation("Calibrating hook");
        int change;
        int oldValue = hookMotor.getCurrentPosition();
        setHookPower(-0.15);

        while (shouldRobotKeepRunning()) {
            opMode.teamIdle();
            opMode.sleep(250);
            int newValue = hookMotor.getCurrentPosition();
            change = newValue - oldValue;
            oldValue = newValue;

            opMode.setStatus(String.format("Position=%d, change=%d", newValue, change));
            // If it isn't getting more negative
            if (change > -5) {
                setHookPower(0);
                break;
            }
        }
        hook0 = hookMotor.getCurrentPosition() + 500;
        hookCalibrated = true;
        opMode.setStatus("Hook calibration done");
        safetysAreDisabled = false;
    }

    public void calibrateArmSwing()
    {
        stop(false);
        safetysAreDisabled = true;
        opMode.setOperation("Calibrating arm swing");
        int change;
        int oldValue = swingMotor.getCurrentPosition();
        setSwingArmPower_raw(-0.3, 1.0);

        while (shouldRobotKeepRunning()) {
            opMode.sleep(250);
            int newValue = swingMotor.getCurrentPosition();
            change = newValue - oldValue;
            oldValue = newValue;

            opMode.setStatus(String.format("Position=%d, change=%d", newValue, change));
            // If it isn't getting more negative
            if (change > -5) {
                setSwingArmPower_raw(0, 0);
                break;
            }
        }

        // Let the foam spring back
        opMode.sleep(1000);

        swing0 = swingMotor.getCurrentPosition() + 50;
        armSwingCalibrated = true;
        opMode.setStatus("Arm swing calibration done");
        safetysAreDisabled = false;
    }

    public void calibrateArmExtension()
    {
        stop(false);
        safetysAreDisabled = true;
        opMode.setOperation("Calibrating arm extension");
        int change;
        int oldValue = armExtensionMotor.getCurrentPosition();
        opMode.setStatus(String.format("Initial Position=%d", oldValue));

        setArmExtensionPower(-0.1);

        while (shouldRobotKeepRunning()) {
            opMode.sleep(250);
            int newValue = armExtensionMotor.getCurrentPosition();
            change = newValue - oldValue;
            oldValue = newValue;

            opMode.setStatus(String.format("Position=%d, change=%d", newValue, change));
            // If it isn't getting more negative
            if (change > -5) {
                setArmExtensionPower(0);
                break;
            }
        }

        extension0 = armExtensionMotor.getCurrentPosition() + 500;
        armExtensionCalibrated = true;
        opMode.setStatus("Arm extension calibration done");
        safetysAreDisabled = false;
    }

    public void calibrateEverything()
    {
        calibrateHook();
        calibrateArmExtension();
        calibrateArmSwing();
    }

    public AbstractOngoingAction startOngoingAction( AbstractOngoingAction action)
    {
        ongoingActions.add(action);
        action.start();
        return action;
    }
    public AbstractOngoingAction startArmReset()
    {
        return startOngoingAction(new OngoingAction_ArmReset(this));
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
}