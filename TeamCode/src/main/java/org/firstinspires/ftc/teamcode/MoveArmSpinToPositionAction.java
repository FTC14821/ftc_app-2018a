package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.scheduler.EndableAction;
import org.firstinspires.ftc.teamcode.scheduler.OngoingAction;

import static org.firstinspires.ftc.teamcode.scheduler.Utils.*;

public class MoveArmSpinToPositionAction extends EndableAction
{
    Robot robot = Robot.get();

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
    public boolean isUsingDcMotor(DcMotor motor)
    {
        if(motor == robot.armSwingMotor)
            return true;
        else
            return super.isUsingDcMotor(motor);
    }

    @Override
    public boolean isUsingServo(Servo servo)
    {
        if(servo == robot.armSpinServo)
                return true;
        else
            return super.isUsingServo(servo);
    }

    @Override
    public boolean isDone(StringBuilder statusMessage)
    {
        statusMessage.append(safeStringFormat("Still to go: %.2f --> %.2f", Robot.get().armSpinServo.getPosition(), desiredArmSpinLocation));
        return robot.armSpinServo.getPosition() == desiredArmSpinLocation;
    }

    @Override
    public void loop()
    {
        double currentPosition = robot.armSpinServo.getPosition();
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
        robot.setArmSpinServoPosition_raw(newPosition);
    }
}
