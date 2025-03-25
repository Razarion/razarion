package com.btxtech.shared.dto;

import java.util.List;

/**
 * Created by Beat
 * 14.03.2017.
 */
public class ThrownLogInfo {
    private String message;
    private String classInfo;
    private ThrownLogInfo cause;
    private List<StackTraceElementLogInfo> stackTrace;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getClassInfo() {
        return classInfo;
    }

    public void setClassInfo(String classInfo) {
        this.classInfo = classInfo;
    }

    public ThrownLogInfo getCause() {
        return cause;
    }

    public void setCause(ThrownLogInfo cause) {
        this.cause = cause;
    }

    public List<StackTraceElementLogInfo> getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(List<StackTraceElementLogInfo> stackTrace) {
        this.stackTrace = stackTrace;
    }
}
