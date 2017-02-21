package com.btxtech.common.system.logging;

import com.btxtech.shared.utils.ExceptionUtil;
import com.google.gwt.logging.client.RemoteLogHandlerBase;

import java.util.logging.LogRecord;

/**
 * Created by Beat
 * 18.02.2017.
 */
public class RestRemoteLogHandler extends RemoteLogHandlerBase {

    @Override
    public void publish(LogRecord logRecord) {
        try {
            JsonLogService.doLog(logRecord);
        } catch (Throwable throwable) {
            FallbackLog.fallbackXhrLog("JSON log failed: " + ExceptionUtil.setupStackTrace(null, throwable) + " Original log record: " + FallbackLog.toString(logRecord));
        }
    }
}
