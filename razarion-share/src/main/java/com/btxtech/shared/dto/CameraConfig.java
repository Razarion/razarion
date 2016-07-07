package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.Index;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Created by Beat
 * 05.07.2016.
 */
@Portable
public class CameraConfig {
    private Index fromPosition;
    private Index toPosition;
    private boolean smooth;
    private boolean cameraLocked;

    public Index getFromPosition() {
        return fromPosition;
    }

    public void setFromPosition(Index fromPosition) {
        this.fromPosition = fromPosition;
    }

    public Index getToPosition() {
        return toPosition;
    }

    public void setToPosition(Index toPosition) {
        this.toPosition = toPosition;
    }

    public boolean isSmooth() {
        return smooth;
    }

    public void setSmooth(boolean smooth) {
        this.smooth = smooth;
    }

    public boolean isCameraLocked() {
        return cameraLocked;
    }

    public void setCameraLocked(boolean cameraLocked) {
        this.cameraLocked = cameraLocked;
    }
}
