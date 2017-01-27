package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.DecimalPosition;

/**
 * Created by Beat
 * 05.07.2016.
 */
public class ViewPositionConfig {
    private DecimalPosition fromPosition;
    private DecimalPosition toPosition;
    private Double speed;
    private boolean cameraLocked;

    public DecimalPosition getFromPosition() {
        return fromPosition;
    }

    public ViewPositionConfig setFromPosition(DecimalPosition fromPosition) {
        this.fromPosition = fromPosition;
        return this;
    }

    public DecimalPosition getToPosition() {
        return toPosition;
    }

    public ViewPositionConfig setToPosition(DecimalPosition toPosition) {
        this.toPosition = toPosition;
        return this;
    }

    public Double getSpeed() {
        return speed;
    }

    public ViewPositionConfig setSpeed(Double speed) {
        this.speed = speed;
        return this;
    }

    public boolean isCameraLocked() {
        return cameraLocked;
    }

    public ViewPositionConfig setCameraLocked(boolean cameraLocked) {
        this.cameraLocked = cameraLocked;
        return this;
    }
}
