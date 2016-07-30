package com.btxtech.uiservice.renderer;

/**
 * Created by Beat
 * 30.07.2016.
 */
public class ProgressAnimationSamples {
    private double progress;
    private double value;

    public ProgressAnimationSamples(double progress, double value) {
        this.progress = progress;
        this.value = value;
    }

    public double getProgress() {
        return progress;
    }

    public double getValue() {
        return value;
    }
}
