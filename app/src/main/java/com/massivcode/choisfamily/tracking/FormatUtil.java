package com.massivcode.choisfamily.tracking;

import java.util.Locale;

/**
 * Created by massivcode@gmail.com on 2017. 1. 5. 15:06
 */

public class FormatUtil {
    public static String getTime(long millis) {
        long hour = millis / (60 * 60 * 1000);
        long minute = (millis % (60 * 60 * 1000)) / (60 * 1000);
        long seconds = (millis % (60 * 60 * 1000) % (60 * 1000)) / 1000;
        return String.format(Locale.KOREAN, "%02d:%02d:%02d", hour, minute, seconds);
    }

    public static String getDouble(double origin) {
        return String.format(Locale.KOREAN, "%.2f", origin);
    }
}
