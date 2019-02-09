package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.scheduler.EndableAction;

public class OngoingAction_MoveArmToPosition extends EndableAction
{
    Robot robot = Robot.get();
    Robot.ARM_LOCATION desiredLocation;
    EndableAction armSwingAction;
    EndableAction armExtensionAction;
    EndableAction armSpinAction;

    private Robot.ARM_LOCATION intermediateLocation=null;


    public OngoingAction_MoveArmToPosition(Robot.ARM_LOCATION desiredLocation)
    {
        super("MoveArm", "MoveArm(-->%s)", desiredLocation );
        this.desiredLocation = desiredLocation;
    }

    @Override
    public EndableAction start()
    {
        super.start();
        armExtensionAction=armSpinAction=armSwingAction=null;
        return this;
    }

    @Override
    public boolean isUsingDcMotor(DcMotor motor)
    {
        if(motor == robot.armSwingMotor || motor == robot.armExtensionMotor)
            return true;
        else
            return super.isUsingDcMotor(motor);
    }

    @Override
    public boolean isUsingServo(Servo servo)
    {
        if(servo==robot.boxTiltServo || servo==robot.armSpinServo)
            return true;
        else
            return super.isUsingServo(servo);
    }

    @Override
    protected void loop()
    {
        super.loop();
        if (robot.currentArmLocation==desiredLocation)
            return;

        // Follow the chart of what locations can move directly to other locations
        switch (robot.currentArmLocation)
        {
            case FOLDED:
                moveArmDirectly(desiredLocation);
                break;
            case FRONT_LEVEL:
                if (desiredLocation == Robot.ARM_LOCATION.FOLDED)
                    moveArmDirectly(Robot.ARM_LOCATION.FOLDED);
                else
                    moveArmDirectly(Robot.ARM_LOCATION.BACK_LEVEL);
                break;
            case BACK_LEVEL:
                if (desiredLocation == Robot.ARM_LOCATION.FRONT_LEVEL || desiredLocation == Robot.ARM_LOCATION.FOLDED)
                    moveArmDirectly(Robot.ARM_LOCATION.FRONT_LEVEL);
                else if (desiredLocation == Robot.ARM_LOCATION.FAR_MINING)
                    moveArmDirectly(Robot.ARM_LOCATION.MINING);
                else
                    moveArmDirectly(desiredLocation);
                break;
            case MINING:
                if (desiredLocation == Robot.ARM_LOCATION.FAR_MINING)
                    moveArmDirectly(Robot.ARM_LOCATION.FAR_MINING);
                else
                    moveArmDirectly(Robot.ARM_LOCATION.BACK_LEVEL);
                break;
            case SILVER_DUMP:
                if (desiredLocation == Robot.ARM_LOCATION.GOLD_DUMP)
                    moveArmDirectly(Robot.ARM_LOCATION.GOLD_DUMP);
                else
                    moveArmDirectly(Robot.ARM_LOCATION.BACK_LEVEL);
                break;
            case GOLD_DUMP:
                if (desiredLocation == Robot.ARM_LOCATION.SILVER_DUMP)
                    moveArmDirectly(Robot.ARM_LOCATION.SILVER_DUMP);
                else
                    moveArmDirectly(Robot.ARM_LOCATION.BACK_LEVEL);
                break;
            case FAR_MINING:
                moveArmDirectly(Robot.ARM_LOCATION.MINING);
                break;
        }
    }

    /**
     * Start the process of moving the motors/servos and wait for it to finish
     * @param directLocation
     */
    private void moveArmDirectly(Robot.ARM_LOCATION directLocation)
    {
        if ( intermediateLocation != null )
            throw new IllegalStateException("ArmMovement must wait before moving to a second intermediateLocation");

        intermediateLocation = directLocation;
        setStatus("Creating subactions to get to %s", directLocation);
        armExtensionAction = new MoveArmToExtensionPositionAction(directLocation.armExtensionMotorLocation).start();
        armSwingAction = new MoveArmToSwingPositionAction(directLocation.armSwingMotorLocation).start();
        armSpinAction = new MoveArmSpinToPositionAction(directLocation.armSpinServoLocation).start();
        waitForPendingArmActions();
        robot.setBoxTiltServoPosition_raw(directLocation.boxTiltServoLocation);

        intermediateLocation = null;
        setStatus("Subactions are done to get to %s.", directLocation);
        robot.currentArmLocation=directLocation;
    }

    private void waitForPendingArmActions()
    {
        waitFor(armExtensionAction, armSwingAction, armSpinAction);
        armExtensionAction=armSpinAction=armSwingAction=null;
    }

    @Override
    protected void cleanup(boolean actionWasCompletedsSuccessfully)
    {
        super.cleanup(actionWasCompletedsSuccessfully);
    }

    @Override
    public boolean isDone(StringBuilder newStatus)
    {
        if (desiredLocation==robot.currentArmLocation)
            return true;
        if ( armExtensionAction==null && armSwingAction==null && armSpinAction==null)
            return true;

        return false;
    }
}
