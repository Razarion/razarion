package com.btxtech.server.persistence.tracker;

import java.util.Date;

/**
 * Created by Beat
 * on 03.01.2018.
 */
public class PerfmonTrackerDetail {
    private Date clientStartTime;
    private String type;
    private double frequency;
    private double duration;

    public Date getClientStartTime() {
        return clientStartTime;
    }

    public PerfmonTrackerDetail setClientStartTime(Date clientStartTime) {
        this.clientStartTime = clientStartTime;
        return this;
    }

    public String getType() {
        return type;
    }

    public PerfmonTrackerDetail setType(String type) {
        this.type = type;
        return this;
    }

    public double getFrequency() {
        return frequency;
    }

    public PerfmonTrackerDetail setFrequency(double frequency) {
        this.frequency = frequency;
        return this;
    }

    public double getDuration() {
        return duration;
    }

    public PerfmonTrackerDetail setDuration(double duration) {
        this.duration = duration;
        return this;
    }
}
