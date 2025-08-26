package com.btxtech.shared.system;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Consumer;

public class ExceptionHandler {
    private static final Collection<LogElement> alreadyLoggedLogElement = new HashSet<LogElement>();

    public static void handleExceptionOnlyOnce(Consumer<String> logCallback, String message, Throwable t) {
        LogElement logElement = new LogElement(t, message);
        if (!alreadyLoggedLogElement.contains(logElement)) {
            logCallback.accept("!!Further exception will be suppressed!! " + message);
            alreadyLoggedLogElement.add(logElement);
        }
    }

    public static boolean compareExceptionsDeep(Throwable t1, Throwable t2) {
        if (compareExceptions(t1, t2)) {
            if (t1.getCause() != null && t2.getCause() != null) {
                return compareExceptionsDeep(t1.getCause(), t2.getCause());
            }
            return !(t1.getCause() != null && t2.getCause() == null) && !(t1.getCause() == null && t2.getCause() != null);
        } else {
            return false;
        }
    }

    public static boolean compareExceptions(Throwable t1, Throwable t2) {
        return equals(t1.getMessage(), t2.getMessage()) && compareStackTrace(t1.getStackTrace(), t2.getStackTrace());
    }

    public static boolean compareStackTrace(StackTraceElement[] sA1, StackTraceElement[] sA2) {
        if (sA1 == sA2) {
            return true;
        }
        if (sA1 == null || sA2 == null) {
            return false;
        }

        int length = sA1.length;
        if (sA2.length != length) {
            return false;
        }

        for (int i = 0; i < length; i++) {
            StackTraceElement s1 = sA1[i];
            StackTraceElement s2 = sA2[i];
            if (!compareStackTraceElement(s1, s2)) {
                return false;
            }
        }

        return true;
    }


    private static boolean compareStackTraceElement(StackTraceElement s1, StackTraceElement s2) {
        // GWT does not override StackTraceElement.equals()
        return s1 == s2 || s1 != null && s2 != null && equals(s1.toString(), s2.toString());
    }

    public static boolean equals(Object o1, Object o2) {
        return o1 == o2 || (o1 != null && o1.equals(o2));
    }

    private static class LogElement {
        private final Throwable throwable;
        private final String message;

        private LogElement(Throwable throwable, String message) {
            this.throwable = throwable;
            this.message = message;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            LogElement that = (LogElement) o;

            if (message != null ? !message.equals(that.message) : that.message != null) return false;
            if (throwable != null && that.throwable != null) {
                return compareExceptionsDeep(throwable, that.throwable);
            } else if (throwable == null && that.throwable != null) {
                return false;
            } else if (throwable != null && that.throwable == null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = throwable != null ? (throwable.getMessage() != null ? throwable.getMessage().hashCode() : 0) : 0;
            result = 31 * result + (throwable != null ? hashCode(throwable.getStackTrace()) : 0);
            result = 31 * result + (message != null ? message.hashCode() : 0);
            return result;
        }

        private int hashCode(StackTraceElement sA[]) {
            // GWT does not override hasCode() in StackTraceElement
            if (sA == null) {
                return 0;
            }
            int result = 1;
            for (StackTraceElement stackTraceElement : sA) {
                result = 31 * result + (stackTraceElement == null ? 0 : stackTraceElement.toString().hashCode());
            }
            return result;
        }
    }
}
