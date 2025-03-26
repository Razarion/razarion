package com.btxtech.server.rest;

import com.btxtech.shared.dto.LogRecordInfo;
import com.btxtech.shared.dto.StackTraceElementLogInfo;
import com.btxtech.shared.dto.ThrownLogInfo;
import com.btxtech.shared.rest.LoggingController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.MediaType;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static com.btxtech.shared.CommonUrl.*;

@RestController
@RequestMapping(APPLICATION_PATH + "/" + REMOTE_LOGGING)
public class LoggingControllerImpl implements LoggingController {
    private final Logger logger = LoggerFactory.getLogger(LoggingControllerImpl.class);

    @Override
    @PostMapping(value = LOGGING_SIMPLE, consumes = MediaType.TEXT_PLAIN)
    public void simpleLogger(String logString) {
        logger.warn("GWT simpleLogger: " + logString);
    }

    @Override
    @PostMapping(value = LOGGING_JSON, consumes = MediaType.APPLICATION_JSON)
    public void jsonLogger(@RequestBody LogRecordInfo logRecordInfo) {
        String s = "Gwt Message: " + logRecordInfo.getMessage();
        s += "\n - Logger name: " + logRecordInfo.getLoggerName();
        s += "\n - GWT module name: " + logRecordInfo.getGwtModuleName();
        s += "\n - GWT strong name: " + logRecordInfo.getGwtStrongName();
        logger.warn(s);
    }

    private String thrownToString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    private Throwable setupThrown(ThrownLogInfo thrownLogInfo) {
        Throwable throwable;
        if (thrownLogInfo.getCause() != null) {
            throwable = new Throwable(thrownLogInfo.getClassInfo() + ": " + thrownLogInfo.getMessage(), setupThrown(thrownLogInfo.getCause()));
        } else {
            throwable = new Throwable(thrownLogInfo.getClassInfo() + ": " + thrownLogInfo.getMessage());
        }
        throwable.setStackTrace(setupStackTrace(thrownLogInfo.getStackTrace()));
        return throwable;
    }

    private StackTraceElement[] setupStackTrace(List<StackTraceElementLogInfo> elementLogInfos) {
        if (elementLogInfos == null || elementLogInfos.isEmpty()) {
            return null;
        }
        StackTraceElement[] stackTraceElements = new StackTraceElement[elementLogInfos.size()];
        for (int i = 0; i < elementLogInfos.size(); i++) {
            StackTraceElementLogInfo elementLogInfo = elementLogInfos.get(i);
            stackTraceElements[i] = new StackTraceElement(elementLogInfo.getDeclaringClass(), elementLogInfo.getMethodName(), elementLogInfo.getFileName(), elementLogInfo.getLineNumber());
        }
        return stackTraceElements;
    }
}
