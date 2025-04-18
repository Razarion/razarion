package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.DecimalPosition;
import jakarta.persistence.Embeddable;

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

    public void setFromPosition(DecimalPosition fromPosition) {
        this.fromPosition = fromPosition;
    }

    public DecimalPosition getToPosition() {
        return toPosition;
    }

    public void setToPosition(DecimalPosition toPosition) {
        this.toPosition = toPosition;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public boolean isCameraLocked() {
        return cameraLocked;
    }

    public void setCameraLocked(boolean cameraLocked) {
        this.cameraLocked = cameraLocked;
    }

    public Double getBottomWidth() {
        return bottomWidth;
    }

    public void setBottomWidth(Double bottomWidth) {
        this.bottomWidth = bottomWidth;
    }

    public ViewFieldConfig fromPosition(DecimalPosition fromPosition) {
        setFromPosition(fromPosition);
        return this;
    }

    public ViewFieldConfig toPosition(DecimalPosition toPosition) {
        setToPosition(toPosition);
        return this;
    }

    public ViewFieldConfig speed(Double speed) {
        setSpeed(speed);
        return this;
    }

    public ViewFieldConfig cameraLocked(boolean cameraLocked) {
        setCameraLocked(cameraLocked);
        return this;
    }

    public ViewFieldConfig bottomWidth(Double bottomWidth) {
        setBottomWidth(bottomWidth);
        return this;
    }
}
