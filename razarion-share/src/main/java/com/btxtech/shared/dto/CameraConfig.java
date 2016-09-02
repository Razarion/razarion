package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.Index;

/**
 * Created by Beat
 * 05.07.2016.
 */
public class CameraConfig {
    private Index fromPosition;
    private Index toPosition;
    private boolean smooth;
    private boolean cameraLocked;

    public Index getFromPosition() {
        return fromPosition;
    }

    public CameraConfig setFromPosition(Index fromPosition) {
        this.fromPosition = fromPosition;
        return this;
    }

    public Index getToPosition() {
        return toPosition;
    }

    public CameraConfig setToPosition(Index toPosition) {
        this.toPosition = toPosition;
        return this;
    }

    public boolean isSmooth() {
        return smooth;
    }

    public CameraConfig setSmooth(boolean smooth) {
        this.smooth = smooth;
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
