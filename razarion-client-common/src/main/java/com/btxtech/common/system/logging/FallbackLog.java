package com.btxtech.common.system.logging;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.utils.ExceptionUtil;
import com.google.gwt.xhr.client.XMLHttpRequest;
import elemental.client.Browser;
import elemental.js.util.Xhr;

import java.util.logging.LogRecord;

/**
 * Created by Beat
 * 21.02.2017.
 */
public class FallbackLog {

    public static void fallbackXhrLog(LogRecord logRecord) {
        fallbackXhrLog(toString(logRecord));
    }

    public static void fallbackXhrLog(String message) {
        Xhr.post(CommonUrl.getSimpleLoggingUrl(), message, "text/plain", new Xhr.Callback() {
            @Override
            public void onFail(XMLHttpRequest xhr) {
                if (xhr.getStatus() == 204) {
                    return;
                }
                Browser.getWindow().getConsole().log("Failure log to server: " + message + "|  Status Text: " + xhr.getStatusText() + " Status: " + xhr.getStatus());
            }

            @Override
            public void onSuccess(XMLHttpRequest xhr) {
            }
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
