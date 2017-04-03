package com.btxtech.shared.gameengine.planet.terrain.slope;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Triangulator;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.gameengine.planet.terrain.Water;
import com.btxtech.shared.gameengine.planet.terrain.ground.GroundMesh;

import java.util.List;

/**
 * Created by Beat
 * 10.04.2016.
 */
public class SlopeWater extends Slope {
    private final Water water;

    public SlopeWater(int slopeId, Water water, SlopeSkeletonConfig slopeSkeletonConfig, List<DecimalPosition> corners) {
        super(slopeId, slopeSkeletonConfig, corners);
        this.water = water;
    }

    public void wrap(GroundMesh groundMesh) {
        super.wrap(groundMesh);

        Triangulator.calculate(getOuterLine(), water::addTriangle);
    }

    @Override
    public boolean hasWater() {
        return true;
    }

    @Override
    public double getWaterLevel() {
        return water.getLevel();
    }
}
