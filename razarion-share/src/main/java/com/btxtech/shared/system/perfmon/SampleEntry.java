package com.btxtech.shared.system.perfmon;

/**
 * Created by Beat
 * 05.11.2016.
 */
public class SampleEntry {
    private PerfmonEnum perfmonEnum;
    private long startTime;
    private int duration;

    public SampleEntry(PerfmonEnum perfmonEnum, long startTime) {
        this.perfmonEnum = perfmonEnum;
        this.startTime = startTime;
        duration = (int) (System.currentTimeMillis() - startTime);
    }

    public long getStartTime() {
        return startTime;
    }

    public PerfmonEnum getPerfmonEnum() {
        return perfmonEnum;
    }

    public int getDuration() {
        return duration;
    }

}
