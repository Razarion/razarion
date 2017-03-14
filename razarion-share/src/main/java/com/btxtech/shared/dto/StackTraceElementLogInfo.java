package com.btxtech.shared.dto;

/**
 * Created by Beat
 * 14.03.2017.
 */
public class StackTraceElementLogInfo {
    private String declaringClass;
    private String methodName;
    private String fileName;
    private int lineNumber;

    public String getDeclaringClass() {
        return declaringClass;
    }

    public StackTraceElementLogInfo setDeclaringClass(String declaringClass) {
        this.declaringClass = declaringClass;
        return this;
    }

    public String getMethodName() {
        return methodName;
    }

    public StackTraceElementLogInfo setMethodName(String methodName) {
        this.methodName = methodName;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public StackTraceElementLogInfo setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public StackTraceElementLogInfo setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
        return this;
    }
}
