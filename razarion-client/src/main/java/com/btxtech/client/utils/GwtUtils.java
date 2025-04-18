package com.btxtech.client.utils;

import com.btxtech.shared.datatypes.Index;

/**
 * Created by Beat
 * 20.06.2016.
 */
public class GwtUtils {
    /**
     * @param integer to correct
     * @return corrected integer
     */
    public static int correctInt(int integer) {
        return (int) Math.floor(integer);
    }

    public static Index correctIndex(int x, int y) {
        return new Index(correctInt(x), correctInt(y));
    }
}
