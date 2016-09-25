package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;

/**
 * Created by Beat
 * 05.07.2016.
 */
public class CameraConfig {
    private DecimalPosition fromPosition;
    private DecimalPosition toPosition;
    private Double speed;
    private boolean cameraLocked;

    public DecimalPosition getFromPosition() {
        return fromPosition;
    }

    public CameraConfig setFromPosition(DecimalPosition fromPosition) {
        this.fromPosition = fromPosition;
        return this;
    }

    public DecimalPosition getToPosition() {
        return toPosition;
    }

    public CameraConfig setToPosition(DecimalPosition toPosition) {
        this.toPosition = toPosition;
        return this;
    }

    public Double getSpeed() {
        return speed;
    }

    public CameraConfig setSpeed(Double speed) {
        this.speed = speed;
        return this;
    }

    public boolean isCameraLocked() {
        return cameraLocked;
    }

    public CameraConfig setCameraLocked(boolean cameraLocked) {
        this.cameraLocked = cameraLocked;
        return this;
    }
}
