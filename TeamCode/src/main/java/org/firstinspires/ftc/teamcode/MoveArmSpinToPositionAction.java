package org.firstinspires.ftc.teamcode;

import org.firstinspires.ftc.teamcode.scheduler.EndableAction;

import static org.firstinspires.ftc.teamcode.scheduler.Utils.*;

public class MoveArmSpinToPositionAction extends EndableAction
{
    private final double desiredArmSpinLocation;

    public MoveArmSpinToPositionAction(double desiredArmSpinLocation)
    {
        super("ArmSpinToPosition", "Spin arm to %.2f", desiredArmSpinLocation);

        if ( desiredArmSpinLocation <= Robot.ARM_SPIN_SERVO_MIN_LOCATION)
        {
            log("Fixing desiredArmSpinLocation to minimum %.2f", Robot.ARM_SPIN_SERVO_MIN_LOCATION);
            desiredArmSpinLocation = Robot.ARM_SPIN_SERVO_MIN_LOCATION;
        }

        if ( desiredArmSpinLocation > Robot.ARM_SPIN_SERVO_MAX_LOCATION)
        {
            log("Fixing desiredArmSpinLocation to maximum %.2f", Robot.ARM_SPIN_SERVO_MAX_LOCATION);
            desiredArmSpinLocation = Robot.ARM_SPIN_SERVO_MAX_LOCATION;
        }

        this.desiredArmSpinLocation = desiredArmSpinLocation;
    }

    @Override
    public boolean isDone(StringBuilder statusMessage)
    {
        statusMessage.append(safeStringFormat("%d --> %d", Robot.get().armSpinServo.getPosition(), desiredArmSpinLocation));
        return Robot.get().armSpinServo.getPosition() == desiredArmSpinLocation;
    }

    @Override
    public void loop()
    {
        double currentPosition = Robot.get().armSpinServo.getPosition();
        double movementLeft = Math.abs(currentPosition - desiredArmSpinLocation);

        if ( movementLeft == 0 ) {
            return;
        }

        double newPosition;

        if(movementLeft < Robot.MAX_ARM_SPIN_SERVO_CHANGE){
            newPosition = desiredArmSpinLocation;
        } else if ( currentPosition > desiredArmSpinLocation ) {
            newPosition = currentPosition - Robot.MAX_ARM_SPIN_SERVO_CHANGE;
        } else {
            newPosition = currentPosition + Robot.MAX_ARM_SPIN_SERVO_CHANGE;
        }

        log("Setting Arm Spin position to %.2f", newPosition);
        Robot.get().armSpinServo.setPosition(newPosition);
    }
}
