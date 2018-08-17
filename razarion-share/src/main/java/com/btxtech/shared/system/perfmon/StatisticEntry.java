package com.btxtech.shared.system.perfmon;

/**
 * Created by Beat
 * 05.11.2016.
 */
public class StatisticEntry {
    private PerfmonEnum perfmonEnum;
    private long samplingPeriodStart;
    private long samplingDuration;
    private int samples;
    private double frequency;
    private int totalDuration;
    private double avgDuration;

    public StatisticEntry(PerfmonEnum perfmonEnum, long samplingPeriodStart, long samplingDuration) {
        this.perfmonEnum = perfmonEnum;
        this.samplingPeriodStart = samplingPeriodStart;
        this.samplingDuration = samplingDuration;
    }

    public PerfmonEnum getPerfmonEnum() {
        return perfmonEnum;
    }

    public double getFrequency() {
        return frequency;
    }

    public double getAvgDuration() {
        return avgDuration;
    }

    public long getSamplingPeriodStart() {
        return samplingPeriodStart;
    }

    public int getSamples() {
        return samples;
    }

    public void analyze(SampleEntry sample) {
        samples++;
        totalDuration += sample.getDuration();
    }

    public void finalizeStatistic() {
        if (samplingDuration > 0) {
            frequency = 1000.0 * (double) samples / (double) samplingDuration;
        }
        avgDuration = (double) totalDuration / (double) samples / 1000.0;
    }
}
