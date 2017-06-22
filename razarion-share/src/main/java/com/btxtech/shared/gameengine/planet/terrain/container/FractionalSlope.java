package com.btxtech.shared.gameengine.planet.terrain.container;

import java.util.List;

/**
 * Created by Beat
 * on 19.06.2017.
 */
public class FractionalSlope {
    private int slopeSkeletonConfigId;
    private double groundHeight;
    private List<FractionalSlopeSegment> fractionalSlopeSegments;

    public int getSlopeSkeletonConfigId() {
        return slopeSkeletonConfigId;
    }

    public double getGroundHeight() {
        return groundHeight;
    }

    public List<FractionalSlopeSegment> getFractionalSlopeSegments() {
        return fractionalSlopeSegments;
    }
}
