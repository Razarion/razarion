package com.btxtech.shared.system.perfmon;

/**
 * Created by Beat
 * 05.11.2016.
 */
public class SampleEntry {
    private final PerfmonEnum perfmonEnum;
    private final long startTime;
    private final int duration;

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
