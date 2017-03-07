package com.btxtech.server.rest;

import com.btxtech.server.web.Session;
import com.btxtech.shared.dto.LogRecordInfo;
import com.btxtech.shared.rest.LoggingProvider;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 18.02.2017.
 */
public class LoggingProviderImpl implements LoggingProvider {
    private Logger logger = Logger.getLogger(LoggingProviderImpl.class.getName());
    @Inject
    private Session session;

    @Override
    public void simpleLogger(String logString) {
        logger.severe("SimpleLogger: SessionId: " + session.getId() + " User " + session.getUser());
        logger.severe("SimpleLogger: " + logString);
    }

    @Override
    public void jsonLogger(LogRecordInfo logRecordInfo) {
        try {
            LogRecord logRecord = toLogRecord(logRecordInfo);
            logger.log(logRecord.getLevel(), "jsonLogger: SessionId: " + session.getId() + " User " + session.getUser());
            logger.log(logRecord);
            if (logRecordInfo.getThrown() != null) {
                logger.severe(logRecordInfo.getThrown());
            }
        } catch (Throwable throwable) {
            logger.log(Level.SEVERE, "Logging from client failed. LogRecordInfo: " + logRecordInfo, throwable);
        }
    }

    private LogRecord toLogRecord(LogRecordInfo logRecordInfo) {
        LogRecord logRecord = new LogRecord(Level.parse(logRecordInfo.getLevel()), logRecordInfo.getMessage());

        // TODO
        logRecord.setSourceClassName("setSourceClassName");
        logRecord.setSourceMethodName("setSourceMethodName");
        logRecord.setThreadID(1111111);
        // TODO

        // TODO not available in GWT logRecord.setSequenceNumber(Long.parseLong(logRecordInfo.getSequenceNumber()));
        // TODO not available in GWT logRecord.setSourceClassName(logRecordInfo.getSourceClassName());
        // TODO not available in GWT logRecord.setSourceMethodName(logRecordInfo.getSourceMethodName());
        // TODO not available in GWT logRecord.setThreadID(logRecordInfo.getThreadID());

        logRecord.setMillis(Long.parseLong(logRecordInfo.getMillis()));
        logRecord.setLoggerName(logRecordInfo.getLoggerName());
        return logRecord;
    }

}
