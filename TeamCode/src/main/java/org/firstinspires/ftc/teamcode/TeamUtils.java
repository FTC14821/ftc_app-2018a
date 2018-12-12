package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.util.RobotLog;

public class TeamUtils
{
    public static String safeStringFormat(String format, Object... args) {
        try {
            String result = String.format(format, args);
            return result;
        }
        catch (RuntimeException e)
        {
            RobotLog.ww(Robot.ROBOT_TAG, "FORMATTING ERROR: format=%s: %s", format, e.getMessage());

            return String.format("FORMAT ERROR: %s", format);
        }
    }

}
