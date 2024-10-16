package com.btxtech.shared.utils;

public class ExceptionUtil {

    public static String setupStackTrace(String message, Throwable throwable) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("(See GWT log for stack trace) ");
            if (message != null) {
                stringBuilder.append(message);
                stringBuilder.append(" ");
            }
            boolean isCause = false;
            while (true) {
                setupStackTrace(stringBuilder, throwable, isCause);
                Throwable inner = throwable.getCause();
                if (inner == null || inner == throwable) {
                    break;
                }
                throwable = inner;
                isCause = true;
            }
            return stringBuilder.toString();
        } catch (Throwable ignore) {
            return "failed to setup stacktrace: " + ignore;
        }
    }

    public static void setupStackTrace(StringBuilder builder, Throwable throwable, boolean isCause) {
        if (isCause) {
            builder.append(" Caused by: ");
        } else {
            builder.append(" ");
        }
        builder.append(throwable.toString());
    }

    public static Throwable getMostInnerThrowable(Throwable t) {
        if (t.getCause() == null) {
            return t;
        } else if (t.getCause() == t) {
            return t;
        } else {
            return getMostInnerThrowable(t.getCause());
        }
    }

}
