package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.DecimalPosition;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;

/**
 * Created by Beat
 * 05.07.2016.
 */
@Embeddable
public class ViewFieldConfig {
    private DecimalPosition fromPosition;
    private DecimalPosition toPosition;
    private Double speed;
    private boolean cameraLocked;
    private Double bottomWidth;

    public DecimalPosition getFromPosition() {
        return fromPosition;
    }

    public ViewFieldConfig setFromPosition(DecimalPosition fromPosition) {
        this.fromPosition = fromPosition;
        return this;
    }

    public DecimalPosition getToPosition() {
        return toPosition;
    }

    public ViewFieldConfig setToPosition(DecimalPosition toPosition) {
        this.toPosition = toPosition;
        return this;
    }

    public Double getSpeed() {
        return speed;
    }

    public ViewFieldConfig setSpeed(Double speed) {
        this.speed = speed;
        return this;
    }

    public boolean isCameraLocked() {
        return cameraLocked;
    }

    public ViewFieldConfig setCameraLocked(boolean cameraLocked) {
        this.cameraLocked = cameraLocked;
        return this;
    }

    public Double getBottomWidth() {
        return bottomWidth;
    }

    public ViewFieldConfig setBottomWidth(Double bottomWidth) {
        this.bottomWidth = bottomWidth;
        return this;
    }
}
