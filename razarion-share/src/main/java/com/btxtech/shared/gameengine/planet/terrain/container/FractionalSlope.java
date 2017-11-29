package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeFractionalSlope;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeFractionalSlopeSegment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * on 19.06.2017.
 */
public class FractionalSlope {
    private int slopeSkeletonConfigId;
    private double groundHeight;
    private boolean inverted;
    private List<FractionalSlopeSegment> fractionalSlopeSegments;

    public FractionalSlope() {
    }

    public FractionalSlope(NativeFractionalSlope nativeFractionalSlope) {
        slopeSkeletonConfigId = nativeFractionalSlope.slopeSkeletonConfigId;
        groundHeight = nativeFractionalSlope.groundHeight;
        inverted = nativeFractionalSlope.inverted;
        fractionalSlopeSegments = new ArrayList<>();
        for (NativeFractionalSlopeSegment fractionalSlopeSegment : nativeFractionalSlope.fractionalSlopeSegments) {
            FractionalSlopeSegment slopeSegment = new FractionalSlopeSegment(fractionalSlopeSegment);
            fractionalSlopeSegments.add(slopeSegment);
        }
    }

    public int getSlopeSkeletonConfigId() {
        return slopeSkeletonConfigId;
    }

    public double getGroundHeight() {
        return groundHeight;
    }

    public boolean isInverted() {
        return inverted;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    public List<FractionalSlopeSegment> getFractionalSlopeSegments() {
        return fractionalSlopeSegments;
    }

    public void setSlopeSkeletonConfigId(int slopeSkeletonConfigId) {
        this.slopeSkeletonConfigId = slopeSkeletonConfigId;
    }

    public void setGroundHeight(double groundHeight) {
        this.groundHeight = groundHeight;
    }

    public void setFractionalSlopeSegments(List<FractionalSlopeSegment> fractionalSlopeSegments) {
        this.fractionalSlopeSegments = fractionalSlopeSegments;
    }

    public NativeFractionalSlope toNativeFractionalSlope() {
        NativeFractionalSlope nativeFractionalSlope = new NativeFractionalSlope();
        nativeFractionalSlope.slopeSkeletonConfigId = slopeSkeletonConfigId;
        nativeFractionalSlope.groundHeight = groundHeight;
        nativeFractionalSlope.inverted = inverted;
        if(fractionalSlopeSegments != null) {
            nativeFractionalSlope.fractionalSlopeSegments = fractionalSlopeSegments.stream().map(FractionalSlopeSegment::toNativeFractionalSlopeSegment).toArray(NativeFractionalSlopeSegment[]::new);
        }
        return nativeFractionalSlope;
    }
}
