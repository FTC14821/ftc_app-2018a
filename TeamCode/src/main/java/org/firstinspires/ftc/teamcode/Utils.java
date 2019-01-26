package org.firstinspires.ftc.teamcode;

import java.awt.*;
import java.util.Date;

public class Utils {
    public static String safeStringFormat(String format, Object... args) {
        try {
            String result = String.format(format, args);
            return result;
        } catch (RuntimeException e) {
            //TODO: Logging
            log("Formatting Error: format=%s: %s", format, e.getMessage());
            return String.format("Format Error: %s", format);
        }
    }
    public static void log(String format, Object... args){
        System.err.println(safeStringFormat("%s %s", new Date(), safeStringFormat(format, args)));
    }
    public static void sleep(long time_ms){
        try {
            Thread.sleep(time_ms);
        } catch (InterruptedException e){
        }
    }
}
