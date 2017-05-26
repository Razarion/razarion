package com.btxtech.shared.datatypes.tracking;

import com.btxtech.shared.datatypes.DecimalPosition;

/**
 * User: beat
 * Date: 14.04.2011
 * Time: 11:45:27
 */
public class ViewFieldTracking extends DetailedTracking{
    private double z;
    private DecimalPosition bottomLeft;
    private DecimalPosition bottomRight;
    private DecimalPosition topRight;
    private DecimalPosition topLeft;

    public double getZ() {
        return z;
    }

    public ViewFieldTracking setZ(double z) {
        this.z = z;
        return this;
    }

    public DecimalPosition getBottomLeft() {
        return bottomLeft;
    }

    public ViewFieldTracking setBottomLeft(DecimalPosition bottomLeft) {
        this.bottomLeft = bottomLeft;
        return this;
    }

    public DecimalPosition getBottomRight() {
        return bottomRight;
    }

    public ViewFieldTracking setBottomRight(DecimalPosition bottomRight) {
        this.bottomRight = bottomRight;
        return this;
    }

    public DecimalPosition getTopRight() {
        return topRight;
    }

    public ViewFieldTracking setTopRight(DecimalPosition topRight) {
        this.topRight = topRight;
        return this;
    }

    public DecimalPosition getTopLeft() {
        return topLeft;
    }

    public ViewFieldTracking setTopLeft(DecimalPosition topLeft) {
        this.topLeft = topLeft;
        return this;
    }
}
