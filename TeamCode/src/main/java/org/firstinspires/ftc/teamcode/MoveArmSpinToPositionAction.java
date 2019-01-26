package org.firstinspires.ftc.teamcode;

import static org.firstinspires.ftc.teamcode.Utils.*;

public class MoveArmSpinToPositionAction extends EndableAction
{

    private final double desiredArmSpinLocation;

    public MoveArmSpinToPositionAction(double desiredArmSpinLocation)
    {
        super(5000, "ArmSpinToPosition", "Spin arm to %.2f", desiredArmSpinLocation);
        
        this.desiredArmSpinLocation = desiredArmSpinLocation;

        if ( desiredArmSpinLocation <= Robot.ARM_TILT_SERVO_MIN_LOCATION ) {
            Robot.get().armSpinServo.setPosition(0.2);
        }
    }

    @Override
    public boolean loop()
    {
        double currentPosition = Robot.get().armSpinServo.getPosition();
        double movementLeft = Math.abs(currentPosition - desiredArmSpinLocation);

        if ( movementLeft == 0 ) {
            return true;
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

        return false;
    }
}
