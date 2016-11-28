package com.btxtech.shared.system.perfmon;

/**
 * Created by Beat
 * 05.11.2016.
 */
public class StatisticEntry {
    private PerfmonEnum perfmonEnum;
    private long fistSample;
    private long lastSample;
    private double samplingDuration;
    private int samples;
    private double frequency;
    private int totalDuration;
    private double avgDuration;

    public StatisticEntry(PerfmonEnum perfmonEnum) {
        this.perfmonEnum = perfmonEnum;
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

    public void analyze(SampleEntry sample) {
        samples++;
        totalDuration += sample.getDuration();
        if (fistSample != 0) {
            fistSample = Math.min(fistSample, sample.getStartTime());
        } else {
            fistSample = sample.getStartTime();
        }
        if (lastSample != 0) {
            lastSample = Math.max(lastSample, sample.getStartTime());
        } else {
            lastSample = sample.getStartTime();
        }
    }

    public void finalizeStatistic() {
        samplingDuration = (double) (lastSample - fistSample) / 1000.0;
        frequency = (double) samples / samplingDuration;
        avgDuration = (double) totalDuration / (double) samples / 1000.0;
    }

    public String toInfoString() {
        return perfmonEnum.getDisplayName() + "\tSampling duration: " + samplingDuration + "s\tFrequency=" + frequency + "hz\tAvg Duration: " + avgDuration + 's';
    }
}
