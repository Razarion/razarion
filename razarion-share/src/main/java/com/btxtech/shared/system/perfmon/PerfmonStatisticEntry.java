package com.btxtech.shared.system.perfmon;

import jsinterop.annotations.JsType;

import java.util.Date;

/**
 * Created by Beat
 * on 25.09.2017.
 */
@JsType
public class PerfmonStatisticEntry {
    private double frequency;
    private double avgDuration;
    private int samples;
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

    public int getSamples() {
        return samples;
    }

    public void setSamples(int samples) {
        this.samples = samples;
    }

    public Date getDate() {
        return date;
    }

    @SuppressWarnings("unused") // Called by Angular
    public long getDateAsLong() {
        return date.getTime();
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
