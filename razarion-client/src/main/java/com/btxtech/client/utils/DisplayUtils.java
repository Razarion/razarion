package com.btxtech.client.utils;

import com.btxtech.shared.datatypes.Vertex;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;

import java.util.Date;

/**
 * Created by Beat
 * 17.06.2016.
 */
public class DisplayUtils {
    public static final NumberFormat NUMBER_FORMATTER_X_XX = NumberFormat.getFormat("#.##");
    public static final NumberFormat NUMBER_FORMATTER_X_XXX = NumberFormat.getFormat("#.###");
    public static final NumberFormat NUMBER_FORMATTER_X_XXXX = NumberFormat.getFormat("#.####");
    public static final DateTimeFormat DATE_TIME_FORMATTER = DateTimeFormat.getFormat("dd.MM.yyyy HH:mm:ss");

    public static String formatVertex(Vertex vertex) {
        return (NUMBER_FORMATTER_X_XX.format(vertex.getX()) + ":" + NUMBER_FORMATTER_X_XX.format(vertex.getY()) + ":" + NUMBER_FORMATTER_X_XX.format(vertex.getZ()));
    }

    public static String formatDate(Date date) {
        return DATE_TIME_FORMATTER.format(date);
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
}
