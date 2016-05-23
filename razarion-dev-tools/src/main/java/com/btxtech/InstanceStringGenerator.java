package com.btxtech;

import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.client.common.Line2I;

/**
 * Created by Beat
 * 17.05.2016.
 */
public class InstanceStringGenerator {
    private static final String NULL_STRING = "null";

    public static String generate(Index index) {
        if (index != null) {
            return "new Index(" + index.getX() + ", " + index.getY() + ")";
        } else {
            return NULL_STRING;
        }
    }

    public static String generate(DecimalPosition decimalPosition) {
        if (decimalPosition != null) {
            return "new DecimalPosition(" + decimalPosition.getX() + ", " + decimalPosition.getY() + ")";
        } else {
            return NULL_STRING;
        }
    }

    public static String generate(Line2I line) {
        if (line != null) {
            return "new Line2I(" + generate(line.getPoint1()) + ", " + generate(line.getPoint2()) + ")";
        } else {
            return NULL_STRING;
        }
    }
}
