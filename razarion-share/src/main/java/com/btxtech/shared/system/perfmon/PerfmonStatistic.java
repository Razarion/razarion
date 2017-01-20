package com.btxtech.shared.system.perfmon;

import java.util.List;

/**
 * Created by Beat
 * 20.01.2017.
 */
public class PerfmonStatistic {
    private PerfmonEnum perfmonEnum;
    private List<Double> frequency;
    private List<Double> avgDuration;

    public PerfmonEnum getPerfmonEnum() {
        return perfmonEnum;
    }

    public void setPerfmonEnum(PerfmonEnum perfmonEnum) {
        this.perfmonEnum = perfmonEnum;
    }

    public List<Double> getFrequency() {
        return frequency;
    }

    public double getFrequency(int index) {
        return frequency.get(index);
    }

    public void setFrequency(List<Double> frequency) {
        this.frequency = frequency;
    }

    public List<Double> getAvgDuration() {
        return avgDuration;
    }

    public void setAvgDuration(List<Double> avgDuration) {
        this.avgDuration = avgDuration;
    }

    public double getAvgDuration(int index) {
        return avgDuration.get(index);
    }

    public int size() {
        return frequency.size();
    }

    public String toInfoString(int index) {
        return perfmonEnum.getDisplayName() + "\tFrequency=" + getFrequency(index) + "hz\tAvg Duration: " + getAvgDuration(index) + "s";
    }
}
