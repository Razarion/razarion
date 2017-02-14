package com.btxtech.uiservice.item;

import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;

/**
 * Created by Beat
 * 13.02.2017.
 */
public class StatusBarModelMatrices extends ModelMatrices {
    private double progress;
    private Color color;
    private Color bgColor;

    public StatusBarModelMatrices(Matrix4 model, Color color, Color bgColor, double progress) {
        super(model);
        this.color = color;
        this.bgColor = bgColor;
        this.progress = progress;
    }

    @Override
    public ModelMatrices interpolateVelocity(double factor) {
        if (getInterpolatableVelocity() != null && factor != 0.0) {
            return new StatusBarModelMatrices(interpolateVelocityMatrix(factor), color, bgColor, progress);
        } else {
            return this;
        }
    }

    @Override
    public double getProgress() {
        return progress;
    }

    @Override
    public void setProgress(double progress) {
        this.progress = progress;
    }

    public Color getColor() {
        return color;
    }

    public Color getBgColor() {
        return bgColor;
    }
}
