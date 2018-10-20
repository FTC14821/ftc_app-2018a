package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.Velocity;

public class ImuIntegrator implements BNO055IMU.AccelerationIntegrator
{
    BNO055IMU imu;

    BNO055IMU.Parameters parameters;
    Acceleration accelrationDrift;

    Acceleration previousAcceleration = new Acceleration();
    Position position = new Position();
    Velocity velocity = new Velocity();

    public ImuIntegrator(BNO055IMU imu)
    {
        this.imu = imu;
    }

    @Override public void initialize(BNO055IMU.Parameters parameters, Position initialPosition, Velocity initialVelocity)
    {
        this.parameters = parameters;
    }

    @Override public Position getPosition() { return position; }
    @Override public Velocity getVelocity() { return velocity; }
    @Override public Acceleration getAcceleration() { return previousAcceleration; }

    @Override public void update(Acceleration linearAcceleration)
    {
        Acceleration correctedAcceleration =
                new Acceleration(
                        linearAcceleration.unit,
                        linearAcceleration.xAccel - accelrationDrift.xAccel,
                        linearAcceleration.yAccel - accelrationDrift.yAccel,
                        linearAcceleration.zAccel - accelrationDrift.zAccel,
                        linearAcceleration.acquisitionTime);

        if ( previousAcceleration == null )
        {
            previousAcceleration = correctedAcceleration;
            return;
        }

        long timeDiff_ns = correctedAcceleration.acquisitionTime - previousAcceleration.acquisitionTime;

        if (parameters.loggingEnabled)
        {
            RobotLog.vv(parameters.loggingTag, "dt=%dns accel=%s",
                    timeDiff_ns, correctedAcceleration);
        }

        previousAcceleration = correctedAcceleration;
    }

    public void calibrate()
    {
        Acceleration totalAccelerationsMeasured=null;
        for (int i=0; i<10; i++)
        {
            Acceleration anAccelerationMeasurement = imu.getLinearAcceleration();
            if ( totalAccelerationsMeasured == null )
            {
                totalAccelerationsMeasured = anAccelerationMeasurement;
            }
            else {
                totalAccelerationsMeasured.xAccel += anAccelerationMeasurement.xAccel;
                totalAccelerationsMeasured.yAccel += anAccelerationMeasurement.yAccel;
                totalAccelerationsMeasured.zAccel += anAccelerationMeasurement.zAccel;
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        accelrationDrift = new Acceleration(
                totalAccelerationsMeasured.unit,
                totalAccelerationsMeasured.xAccel/10,
                totalAccelerationsMeasured.yAccel/10,
                totalAccelerationsMeasured.zAccel/10,
                totalAccelerationsMeasured.acquisitionTime);
    }
}
