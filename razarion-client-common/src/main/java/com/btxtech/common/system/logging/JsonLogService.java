package com.btxtech.common.system.logging;

import com.btxtech.shared.dto.LogRecordInfo;
import com.btxtech.shared.dto.StackTraceElementLogInfo;
import com.btxtech.shared.dto.ThrownLogInfo;
import com.btxtech.shared.rest.LoggingControllerFactory;
import com.btxtech.shared.utils.ExceptionUtil;
import com.google.gwt.core.client.GWT;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogRecord;

/**
 * Created by Beat
 * 21.02.2017.
 */
public class JsonLogService {
    public static void doLog(LogRecord logRecord) {
        LoggingControllerFactory.INSTANCE.jsonLogger(toLogRecordInfo(logRecord))
                .onFailed(fail -> FallbackLog.fallbackXhrLog("Error callback: JSON log failed: "
                        + ExceptionUtil.setupStackTrace(fail.getStatusText(), fail.getThrowable())
                        + " Original log record: " + FallbackLog.toString(logRecord)))
                .send();
    }

    private static LogRecordInfo toLogRecordInfo(LogRecord logRecord) {
        LogRecordInfo logRecordInfo = new LogRecordInfo();
        logRecordInfo.setLevel(logRecord.getLevel() != null ? logRecord.getLevel().toString() : null);
        logRecordInfo.setMessage(logRecord.getMessage());
        logRecordInfo.setMillis(Long.toString(logRecord.getMillis()));

        logRecordInfo.setThrown(setupThrownLogInfo(logRecord.getThrown()));
        logRecordInfo.setLoggerName(logRecord.getLoggerName());
        logRecordInfo.setGwtStrongName(GWT.getPermutationStrongName());
        logRecordInfo.setGwtModuleName(GWT.getModuleName());
        return logRecordInfo;
    }

    private static ThrownLogInfo setupThrownLogInfo(Throwable thrown) {
        if (thrown == null) {
            return null;
        }
        ThrownLogInfo thrownLogInfo = new ThrownLogInfo();
        thrownLogInfo.setMessage(thrown.getMessage());
        thrownLogInfo.setClassInfo(thrown.getClass().getName());
        thrownLogInfo.setStackTrace(setupStackTrace(thrown.getStackTrace()));
        if (thrown.getCause() != null && thrown.getCause() != thrown) {
            thrownLogInfo.setCause(setupThrownLogInfo(thrown.getCause()));
        }

        return thrownLogInfo;
    }

    private static List<StackTraceElementLogInfo> setupStackTrace(StackTraceElement[] stackTrace) {
        if (stackTrace == null || stackTrace.length == 0) {
            return null;
        }
        List<StackTraceElementLogInfo> result = new ArrayList<>();
        for (StackTraceElement stackTraceElement : stackTrace) {
            result.add(new StackTraceElementLogInfo().setFileName(stackTraceElement.getFileName()).setDeclaringClass(stackTraceElement.getClassName())
                    .setMethodName(stackTraceElement.getMethodName()).setLineNumber(stackTraceElement.getLineNumber()));
        }
        return result;
    }
}
