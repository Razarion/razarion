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
    private String userAgent;
    private Integer hardwareConcurrency;
    private Double deviceMemory;

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

    /**
     * Which device wrote this line. Nothing else in the forwarded client log says whether it came
     * from a phone or a desktop, which makes a report like "the tick times are bad" impossible to
     * attribute - the only hint left was the locale in a Date.toString().
     * <p>
     * Deliberately kept in the message body rather than promoted to a Loki label: the user agent is
     * as good as unique per client, and a label that varies per client blows up the index.
     */
    public @Nullable String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(@Nullable String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * navigator.hardwareConcurrency - the core count, and the closest thing to "how fast is this
     * device" that a browser volunteers.
     */
    public @Nullable Integer getHardwareConcurrency() {
        return hardwareConcurrency;
    }

    public void setHardwareConcurrency(@Nullable Integer hardwareConcurrency) {
        this.hardwareConcurrency = hardwareConcurrency;
    }

    /** navigator.deviceMemory in GiB, coarse by design and absent outside Chromium. */
    public @Nullable Double getDeviceMemory() {
        return deviceMemory;
    }

    public void setDeviceMemory(@Nullable Double deviceMemory) {
        this.deviceMemory = deviceMemory;
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
