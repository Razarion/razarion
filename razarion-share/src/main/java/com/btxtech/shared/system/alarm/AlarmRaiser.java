package com.btxtech.shared.system.alarm;

public class AlarmRaiser {

    public static void onNull(Object toBeChecked, Alarm.Type type) {
        onNull(toBeChecked, type, null);
    }

    public static void onNull(Object toBeChecked, Alarm.Type type, String text) {
        onNull(toBeChecked, type, text, null);
    }

    public static void onNull(Object toBeChecked, Alarm.Type type, String text, Integer id) {
        if (toBeChecked == null) {
            throw new AlarmRaisedException(type, text, id);
        }
    }
}
