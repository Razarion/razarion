package com.btxtech.shared.utils;

/**
 * Created by Beat
 * on 16.09.2017.
 */
public interface CommonUtils {
    static int valueOrDefault(Integer value, int defaultValue) {
        if (value != null) {
            return value;
        } else {
            return defaultValue;
        }
    }
}
