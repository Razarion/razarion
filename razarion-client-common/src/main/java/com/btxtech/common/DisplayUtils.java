package com.btxtech.common;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.google.gwt.dom.client.Style;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.UIObject;

import java.util.Date;
import java.util.StringTokenizer;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 17.06.2016.
 */
public class DisplayUtils {
    public static final long MILLISECONDS_IN_HOUR = 1000 * 60 * 60;
    public static final long MILLISECONDS_IN_MINUTE = 1000 * 60;
    public static final NumberFormat NUMBER_FORMATTER_X_XX = NumberFormat.getFormat("0.00");
    public static final NumberFormat NUMBER_FORMATTER_X_XXX = NumberFormat.getFormat("0.000");
    public static final NumberFormat NUMBER_FORMATTER_X_XXXX = NumberFormat.getFormat("0.0000");
    public static final DateTimeFormat DATE_FORMATTER = DateTimeFormat.getFormat("dd.MM.yyyy");
    public static final DateTimeFormat DATE_TIME_FORMATTER = DateTimeFormat.getFormat("dd.MM.yyyy HH:mm:ss");
    public static final DateTimeFormat DATE_TIME_FORMATTER_MILLIS = DateTimeFormat.getFormat("dd.MM.yyyy HH:mm:ss.SSS");
    public static final DateTimeFormat M_S_MILIIS_TIME_FORMATTER = DateTimeFormat.getFormat("mm:ss.SSS");
    public static final DateTimeFormat MINUTE_TIME_FORMATTER = DateTimeFormat.getFormat("mm:ss");
    public static final DateTimeFormat SECOND_TIME_FORMATTER = DateTimeFormat.getFormat("ss");

    public static String formatVertex(Vertex vertex) {
        return (NUMBER_FORMATTER_X_XX.format(vertex.getX()) + ":" + NUMBER_FORMATTER_X_XX.format(vertex.getY()) + ":" + NUMBER_FORMATTER_X_XX.format(vertex.getZ()));
    }

    public static String formatDateOnly(Date date) {
        return DATE_FORMATTER.format(date);
    }

    public static Date toDateOnly(String stringDate) {
        return DATE_FORMATTER.parse(stringDate);
    }

    public static String formatDate(Date date) {
        return DATE_TIME_FORMATTER.format(date);
    }

    public static String formatDateMillis(Date date) {
        return DATE_TIME_FORMATTER_MILLIS.format(date);
    }

    public static String formatDateMillis(int time) {
        int hours = (int) (time / MILLISECONDS_IN_HOUR);
        String hoursString;
        if (hours > 10) {
            hoursString = Integer.toString(hours);
        } else {
            hoursString = "0" + hours;
        }
        return hoursString + ":" + M_S_MILIIS_TIME_FORMATTER.format(new Date(time));
    }

    public static int parsDateMillis(String time) {
        if (!time.contains(":") && !time.contains(".")) {
            // Seconds
            return Integer.parseInt(time) * 1000;
        }
        int intTime = 0;
        String hmsTime = time;
        if (time.contains(".")) {
            intTime += Integer.parseInt(time.substring(time.indexOf(".") + 1));
            hmsTime = time.substring(0, time.indexOf("."));
        }
        if (!time.contains(":")) {
            if (hmsTime.trim().isEmpty()) {
                return intTime;
            } else {
                return Integer.parseInt(hmsTime) * 1000 + intTime;
            }
        }

        String[] array = hmsTime.split(":");
        if (array.length == 2) {
            return Integer.parseInt(array[0]) * 60 * 1000 + Integer.parseInt(array[1]) * 1000 + intTime;
        } else if (array.length == 3) {
            return Integer.parseInt(array[0]) * 60 * 60 * 1000 + Integer.parseInt(array[1]) * 60 * 1000 + Integer.parseInt(array[2]) * 1000 + intTime;
        } else {
            throw new IllegalArgumentException("Can not parse: " + time);
        }
    }

    public static String formatHourTimeStamp(long timeStamp) {
        int hours = (int) (timeStamp / MILLISECONDS_IN_HOUR);
        String hoursString;
        if (hours > 10) {
            hoursString = Integer.toString(hours);
        } else {
            hoursString = "0" + hours;
        }
        return hoursString + ":" + MINUTE_TIME_FORMATTER.format(new Date(timeStamp));
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

    public static String handleDouble3(Double value) {
        if (value != null) {
            return NUMBER_FORMATTER_X_XXX.format(value);
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

    public static Double parseDouble(String doubleString) {
        if (doubleString != null && !doubleString.trim().isEmpty()) {
            return Double.parseDouble(doubleString);
        } else {
            return null;
        }
    }

    public static void divDisplayState(UIObject div, boolean show) {
        if (show) {
            div.getElement().getStyle().setDisplay(Style.Display.BLOCK);
        } else {
            div.getElement().getStyle().setDisplay(Style.Display.NONE);
        }
    }
}
