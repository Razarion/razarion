package com.btxtech.shared.dto;

/**
 * Created by Beat
 * 21.02.2017.
 */
public class LogRecordInfo {
    private String level;
    private String sequenceNumber;
    private String sourceClassName;
    private String sourceMethodName;
    private String message;
    private int threadID;
    private String millis;
    private ThrownLogInfo thrown;
    private String loggerName;
    private String gwtStrongName;
    private String gwtModuleName;

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(String sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getSourceClassName() {
        return sourceClassName;
    }

    public void setSourceClassName(String sourceClassName) {
        this.sourceClassName = sourceClassName;
    }

    public String getSourceMethodName() {
        return sourceMethodName;
    }

    public void setSourceMethodName(String sourceMethodName) {
        this.sourceMethodName = sourceMethodName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getThreadID() {
        return threadID;
    }

    public void setThreadID(int threadID) {
        this.threadID = threadID;
    }

    public String getMillis() {
        return millis;
    }

    public void setMillis(String millis) {
        this.millis = millis;
    }

    public ThrownLogInfo getThrown() {
        return thrown;
    }

    public void setThrown(ThrownLogInfo thrown) {
        this.thrown = thrown;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public String getGwtStrongName() {
        return gwtStrongName;
    }

    public void setGwtStrongName(String gwtStrongName) {
        this.gwtStrongName = gwtStrongName;
    }

    public String getGwtModuleName() {
        return gwtModuleName;
    }

    public void setGwtModuleName(String gwtModuleName) {
        this.gwtModuleName = gwtModuleName;
    }

    @Override
    public String toString() {
        return "LogRecordInfo{" +
                "level='" + level + '\'' +
                ", sequenceNumber='" + sequenceNumber + '\'' +
                ", sourceClassName='" + sourceClassName + '\'' +
                ", sourceMethodName='" + sourceMethodName + '\'' +
                ", message='" + message + '\'' +
                ", threadID=" + threadID +
                ", millis='" + millis + '\'' +
                ", thrown='" + thrown + '\'' +
                ", loggerName='" + loggerName + '\'' +
                '}';
    }
}
