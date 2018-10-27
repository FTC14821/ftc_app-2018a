package org.firstinspires.ftc.teamcode;

public class Team14821Utils
{
    /**
     * Calculate a new heading which will always be between (-180,180]
     *
     * @param degrees1
     * @param degrees2
     * @return
     */
    public static double addHeadings(double degrees1, double degrees2)
    {
        double result = degrees1 + degrees2;

        return normalizedHeading(result);
    }

    public static double normalizedHeading(double rawHeading) {
        while(rawHeading <= -180){
            rawHeading += 360;
        }
        while(rawHeading > 180){
            rawHeading -= 360;
        }
        return rawHeading;
    }
}
