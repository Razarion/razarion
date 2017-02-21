package com.btxtech.common.system.logging;

import com.btxtech.shared.dto.LogRecordInfo;
import com.btxtech.shared.rest.LoggingProvider;
import com.btxtech.shared.utils.ExceptionUtil;
import com.google.gwt.logging.impl.StackTracePrintStream;
import org.jboss.errai.enterprise.client.jaxrs.api.ResponseCallback;
import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;

import java.util.logging.LogRecord;

/**
 * Created by Beat
 * 21.02.2017.
 */
public class JsonLogService {
    public static void doLog(LogRecord logRecord) {
        RestClient.create(LoggingProvider.class, (ResponseCallback) response -> {
        }, (message, throwable) -> {
            FallbackLog.fallbackXhrLog("Error callback: JSON log failed: " + ExceptionUtil.setupStackTrace(message + "", throwable) + " Original log record: " + FallbackLog.toString(logRecord));
            return false;
        }).jsonLogger(toLogRecordInfo(logRecord));
    }

    private static LogRecordInfo toLogRecordInfo(LogRecord logRecord) {
        LogRecordInfo logRecordInfo = new LogRecordInfo();
        logRecordInfo.setLevel(logRecord.getLevel() != null ? logRecord.getLevel().toString() : null);
        // TODO not available in GWT  logRecordInfo.setSequenceNumber(Long.toString(logRecord.getSequenceNumber()));
        // TODO not available in GWT logRecordInfo.setSourceClassName(logRecord.getSourceClassName());
        // TODO not available in GWT logRecordInfo.setSourceMethodName(logRecord.getSourceMethodName());
        logRecordInfo.setMessage(logRecord.getMessage());
        // TODO not available in GWT logRecordInfo.setThreadID(logRecord.getThreadID());
        logRecordInfo.setMillis(Long.toString(logRecord.getMillis()));
        if (logRecord.getThrown() != null) {
            StringBuilder stringBuilder = new StringBuilder();
            logRecord.getThrown().printStackTrace(new StackTracePrintStream(stringBuilder));
            logRecordInfo.setThrown(stringBuilder.toString());
        }
        logRecordInfo.setLoggerName(logRecord.getLoggerName());
        return logRecordInfo;
    }
}
