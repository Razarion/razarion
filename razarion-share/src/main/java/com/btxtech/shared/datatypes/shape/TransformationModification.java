package com.btxtech.shared.datatypes.shape;


/**
 * Created by Beat
 * 05.08.2016.
 */
public enum TransformationModification {
    LOCATION(true),
    SCALE(true),
    ROTATIONX(false),
    ROTATIONY(false),
    ROTATIONZ(false);

    TransformationModification(boolean axisNeede) {
        this.axisNeede = axisNeede;
    }

    private boolean axisNeede;

    public boolean axisNeeded() {
        return axisNeede;
    }
}
