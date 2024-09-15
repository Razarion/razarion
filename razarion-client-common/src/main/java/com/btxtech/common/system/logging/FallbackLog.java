package com.btxtech.common.system.logging;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.utils.ExceptionUtil;
import elemental2.dom.DomGlobal;
import elemental2.dom.RequestInit;

import java.util.logging.LogRecord;

import static elemental2.dom.DomGlobal.fetch;

/**
 * Created by Beat
 * 21.02.2017.
 */
public class FallbackLog {

    public static void fallbackXhrLog(LogRecord logRecord) {
        fallbackXhrLog(toString(logRecord));
    }

    public static void fallbackXhrLog(String message) {
        RequestInit requestInit = RequestInit.create();
        requestInit.setMethod("POST");
        fetch(CommonUrl.getSimpleLoggingUrl(), requestInit)
                .catch_(error -> {
                    DomGlobal.console.log("Failure log to server: " + message + "| " + error);
                    return null;
                });
    }

    public static String toString(LogRecord logRecord) {
        String string = logRecord.getMessage();
        if (logRecord.getThrown() != null) {
            string += logRecord.getThrown().getMessage() + " stacktrace(" + ExceptionUtil.setupStackTrace(null, logRecord.getThrown()) + ")";
        }
        return string;
    }
}
