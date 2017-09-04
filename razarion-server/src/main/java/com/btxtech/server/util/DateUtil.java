package com.btxtech.server.util;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * User: beat
 * Date: 18.09.2011
 * Time: 22:49:13
 */
public class DateUtil {
    public static final String DATE_TIME_FORMAT_STRING = "dd.MM.yyyy HH:mm:ss";
    public static final String DATE_TIME_FORMAT_STRING_MILIS = "dd.MM.yyyy HH:mm:ss.SSS";
    public static final String TIME_FORMAT_STRING = "HH:mm:ss";
    public static final String DATE_FORMAT_STRING = "dd.MM.yyyy";
    public static final String FACEBOOK_DATE_TIME_FORMAT_STRING = "yyyy-MM-dd HH:mm:ssX";
    public static final String DB_DATE_TIME_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss";
    public static final String JSON_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss.S";

    /**
     * Strip of: minutes, seconds and milli seconds
     *
     * @param date input
     * @return stripped date
     */
    public static Date hourStart(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new Date(cal.getTimeInMillis());
    }

    /**
     * Strip of: hour, minutes, seconds and milli seconds
     *
     * @param date input
     * @return stripped date
     */
    public static Date dayStart(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new Date(cal.getTimeInMillis());
    }

    /**
     * Strip of: day, hour, minutes, seconds and milli seconds. The week start is always Monday.
     *
     * @param date input
     * @return stripped date
     */
    public static Date weekStart(Date date) {
        date = dayStart(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return new Date(cal.getTimeInMillis());
    }

    /**
     * Strip of: month, week, hour, minutes, seconds and milli seconds
     *
     * @param date input
     * @return stripped date
     */
    public static Date monthStart(Date date) {
        date = dayStart(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return new Date(cal.getTimeInMillis());
    }

    public static Date createDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new Date(cal.getTimeInMillis());
    }

    public static long stripOfMillis(long time) {
        return (time / 1000) * 1000;
    }

    public static String getDateStringMilis() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_TIME_FORMAT_STRING_MILIS);
        return simpleDateFormat.format(new Date());
    }

    /**
     * @param duration time in ms
     * @return String representing time h:mm:ss
     */
    static public String formatDuration(long duration) {
        duration = duration / 1000;
        return String.format("%d:%02d:%02d", duration / 3600, (duration % 3600) / 60, (duration % 60));
    }

    /**
     * @param duration time in ms
     * @return String representing time s:ms
     */
    static public String formatDurationMilis(long duration) {
        return String.format("%.2f", duration / 1000.0);
    }

    static public String formatDateTime(Date date) {
        if (date != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_TIME_FORMAT_STRING);
            return simpleDateFormat.format(date);
        } else {
            return "-";
        }
    }

    static public String formatDate(Date date) {
        if (date != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT_STRING);
            return simpleDateFormat.format(date);
        } else {
            return "-";
        }
    }

    static public String formatTime(Date date) {
        if (date != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TIME_FORMAT_STRING);
            return simpleDateFormat.format(date);
        } else {
            return "-";
        }
    }

    static public String formatTime(Long time) {
        if (time != null) {
            return formatTime(new Date(time));
        } else {
            return "-";
        }
    }

    public static String getTimeDiff(Date start, Date end) {
        long diffMs = end.getTime() - start.getTime();
        diffMs /= 1000;
        return Long.toString(diffMs);
    }

    public static String getTimeDiff(long start, long end) {
        long diffMs = end - start;
        diffMs /= 1000;
        return Long.toString(diffMs);
    }

    public static String toFacebookTimeString(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(FACEBOOK_DATE_TIME_FORMAT_STRING);
        return simpleDateFormat.format(date);
    }

    public static Date fromDbTimeString(String dateString) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DB_DATE_TIME_FORMAT_STRING);
            return simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static Date fromJsonTimeString(String dateString) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(JSON_FORMAT_STRING);
            return simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static String tpoJsonTimeString(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(JSON_FORMAT_STRING);
        return simpleDateFormat.format(date);
    }
}
