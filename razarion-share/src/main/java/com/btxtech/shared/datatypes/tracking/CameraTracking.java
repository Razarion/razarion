package com.btxtech.shared.datatypes.tracking;

import com.btxtech.shared.datatypes.DecimalPosition;

/**
 * User: beat
 * Date: 14.04.2011
 * Time: 11:45:27
 */
public class CameraTracking extends DetailedTracking {
    private DecimalPosition position;
    private double fovY;

    public DecimalPosition getPosition() {
        return position;
    }

    public CameraTracking setPosition(DecimalPosition position) {
        this.position = position;
        return this;
    }

    public double getFovY() {
        return fovY;
    }

    public CameraTracking setFovY(double fovY) {
        this.fovY = fovY;
        return this;
    }
}
