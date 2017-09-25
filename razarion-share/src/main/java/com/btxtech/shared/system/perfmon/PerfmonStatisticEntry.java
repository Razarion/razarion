package com.btxtech.shared.system.perfmon;

import java.util.Date;
import java.util.List;

/**
 * Created by Beat
 * on 25.09.2017.
 */
public class PerfmonStatisticEntry {
    private double frequency;
    private double avgDuration;
    private Date date;

    public double getFrequency() {
        return frequency;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    public double getAvgDuration() {
        return avgDuration;
    }

    public void setAvgDuration(double avgDuration) {
        this.avgDuration = avgDuration;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
