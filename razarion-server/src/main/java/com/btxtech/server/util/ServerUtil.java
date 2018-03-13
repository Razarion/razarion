package com.btxtech.server.util;

/**
 * Created by Beat
 * 29.04.2017.
 */
public interface ServerUtil {
    static String generateSimpleUuid() {
        return Long.toHexString(System.currentTimeMillis()) + Integer.toHexString((int) (Math.random() * Integer.MAX_VALUE));
    }

    static String removeTrailingQuotas(String input) {
        boolean changed = false;
        if (input.startsWith("\"")) {
            input = input.substring(1, input.length());
            changed = true;
        }
        if (input.endsWith("\"")) {
            input = input.substring(0, input.length() - 1);
            changed = true;
        }
        if (changed) {
            return removeTrailingQuotas(input);
        } else {
            return input;
        }
    }
}
