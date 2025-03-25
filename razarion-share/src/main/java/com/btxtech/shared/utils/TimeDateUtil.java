package com.btxtech.shared.utils;

/**
 * Created by Beat
 * 28.07.2016.
 */
public class TimeDateUtil {
    public static final long MILLIS_IN_SECOND = 1000;
    public static final long MILLIS_IN_MINUTE = MILLIS_IN_SECOND * 60;
    public static final long MILLIS_IN_HOUR = MILLIS_IN_MINUTE * 60;
    public static final long MILLIS_IN_DAY = MILLIS_IN_HOUR * 24;
    public static final long MILLIS_IN_WEEK = MILLIS_IN_DAY * 7;


    public static long second2MilliS(double seconds) {
        return (long) (seconds * 1000.0);
    }
}
