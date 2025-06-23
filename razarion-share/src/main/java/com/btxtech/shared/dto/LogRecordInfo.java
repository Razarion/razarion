package com.btxtech.shared.dto;

import com.btxtech.shared.system.Nullable;

/**
 * Created by Beat
 * 21.02.2017.
 */
public class LogRecordInfo {
    private String level;
    private String message;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMillis() {
        return millis;
    }

    public void setMillis(String millis) {
        this.millis = millis;
    }

    public @Nullable ThrownLogInfo getThrown() {
        return thrown;
    }

    public void setThrown(@Nullable ThrownLogInfo thrown) {
        this.thrown = thrown;
    }

    public @Nullable String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(@Nullable String loggerName) {
        this.loggerName = loggerName;
    }

    public @Nullable String getGwtStrongName() {
        return gwtStrongName;
    }

    public void setGwtStrongName(@Nullable String gwtStrongName) {
        this.gwtStrongName = gwtStrongName;
    }

    public @Nullable String getGwtModuleName() {
        return gwtModuleName;
    }

    public void setGwtModuleName(@Nullable String gwtModuleName) {
        this.gwtModuleName = gwtModuleName;
    }

    @Override
    public String toString() {
        return "LogRecordInfo{" +
                "level='" + level + '\'' +
                ", message='" + message + '\'' +
                ", millis='" + millis + '\'' +
                ", thrown='" + thrown + '\'' +
                ", loggerName='" + loggerName + '\'' +
                '}';
    }
}
