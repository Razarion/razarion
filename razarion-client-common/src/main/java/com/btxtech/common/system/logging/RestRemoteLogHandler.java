package com.btxtech.common.system.logging;

import com.google.gwt.logging.client.RemoteLogHandlerBase;
import com.google.gwt.xhr.client.XMLHttpRequest;
import elemental.client.Browser;
import elemental.js.util.Xhr;

import java.util.logging.LogRecord;

/**
 * Created by Beat
 * 18.02.2017.
 */
public class RestRemoteLogHandler extends RemoteLogHandlerBase {
    @Override
    public void publish(LogRecord logRecord) {
        Xhr.post("/o/rest/greetings/remote_logging/simple/", logRecord.getMessage(), "text/plain", new Xhr.Callback() {
            @Override
            public void onFail(XMLHttpRequest xhr) {
                Browser.getWindow().getConsole().log("Failure log to server: " + logRecord.getMessage() + "|  Status Text: " + xhr.getStatusText() + " Status: " + xhr.getStatus());
            }

            @Override
            public void onSuccess(XMLHttpRequest xhr) {
            }
        });
    }
}
