package com.btxtech.client.utils;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;

import java.util.Date;

/**
 * Created by Beat
 * 17.06.2016.
 */
public class DisplayUtils {
    public static final long MILLISECONDS_IN_HOUR = 1000 * 60 * 60;
    public static final long MILLISECONDS_IN_MINUTE = 1000 * 60;
    public static final NumberFormat NUMBER_FORMATTER_X_XX = NumberFormat.getFormat("#.##");
    public static final NumberFormat NUMBER_FORMATTER_X_XXX = NumberFormat.getFormat("#.###");
    public static final NumberFormat NUMBER_FORMATTER_X_XXXX = NumberFormat.getFormat("#.####");
    public static final DateTimeFormat DATE_TIME_FORMATTER = DateTimeFormat.getFormat("dd.MM.yyyy HH:mm:ss");
    public static final DateTimeFormat MINUTE_TIME_FORMATTER = DateTimeFormat.getFormat("mm:ss");
    public static final DateTimeFormat SECOND_TIME_FORMATTER = DateTimeFormat.getFormat("ss");

    public static String formatVertex(Vertex vertex) {
        return (NUMBER_FORMATTER_X_XX.format(vertex.getX()) + ":" + NUMBER_FORMATTER_X_XX.format(vertex.getY()) + ":" + NUMBER_FORMATTER_X_XX.format(vertex.getZ()));
    }

    public static String formatDate(Date date) {
        return DATE_TIME_FORMATTER.format(date);
    }

    public static String formatHourTimeStamp(long timeStamp) {
        int hours = (int) (timeStamp / MILLISECONDS_IN_HOUR);
        return hours + ":" + MINUTE_TIME_FORMATTER.format(new Date(timeStamp));
    }

    public static String formatMinuteTimeStamp(long timeStamp) {
        int minutes = (int) (timeStamp / MILLISECONDS_IN_MINUTE);
        return minutes + ":" + SECOND_TIME_FORMATTER.format(new Date(timeStamp));
    }

    public static String humanReadableSize(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return NUMBER_FORMATTER_X_XX.format(bytes / Math.pow(unit, exp)) + pre + "B";
    }

    public static String handleEmptyHtmlString(String string) {
        if (string == null || string.trim().isEmpty()) {
            return "&nbsp;";
        } else {
            return string;
        }
    }

    public static String handleString(String value) {
        if (value != null) {
            return value;
        } else {
            return "-";
        }
    }

    public static String handleInteger(Integer value) {
        if (value != null) {
            return Integer.toString(value);
        } else {
            return "-";
        }
    }

    public static String handleDouble2(Double value) {
        if (value != null) {
            return NUMBER_FORMATTER_X_XX.format(value);
        } else {
            return "-";
        }
    }

    public static String handleDouble4(Double value) {
        if (value != null) {
            return NUMBER_FORMATTER_X_XXXX.format(value);
        } else {
            return "-";
        }
    }

    public static String handleDouble3(Double value) {
        if (value != null) {
            return NUMBER_FORMATTER_X_XXX.format(value);
        } else {
            return "-";
        }
    }

    public static String handleIndex(Index index) {
        if (index != null) {
            return "x:" + index.getX() + " y: " + index.getY();
        } else {
            return "-";
        }
    }

    public static String handleRectangle(Rectangle rectangle) {
        if (rectangle != null) {
            return handleIndex(rectangle.getStart()) + " / " + handleIndex(rectangle.getEnd());
        } else {
            return "-";
        }
    }

    public static String handleDecimalPosition(DecimalPosition decimalPosition) {
        if (decimalPosition != null) {
            return "x:" + NUMBER_FORMATTER_X_XX.format(decimalPosition.getX()) + " y: " + NUMBER_FORMATTER_X_XX.format(decimalPosition.getY());
        } else {
            return "-";
        }
    }

    public static String handleRectangle2D(Rectangle2D rectangle2D) {
        if (rectangle2D != null) {
            return handleDecimalPosition(rectangle2D.getStart()) + " / " + handleDecimalPosition(rectangle2D.getEnd());
        } else {
            return "-";
        }
    }
}
