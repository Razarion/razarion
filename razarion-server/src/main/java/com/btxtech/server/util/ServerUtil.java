package com.btxtech.server.util;

/**
 * Created by Beat
 * 29.04.2017.
 */
public interface ServerUtil {
    static String generateSimpleUuid() {
        return Long.toHexString(System.currentTimeMillis()) + Integer.toHexString((int) (Math.random() * Integer.MAX_VALUE));
    }
}
