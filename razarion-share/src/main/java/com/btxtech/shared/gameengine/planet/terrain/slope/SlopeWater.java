package com.btxtech.shared.gameengine.planet.terrain.slope;

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

    public SlopeWater(Water water, SlopeSkeletonConfig slopeSkeletonConfig, List<Index> corners) {
        super(slopeSkeletonConfig, corners);
        this.water = water;
    }

    public void wrap(GroundMesh groundMesh) {
        super.wrap(groundMesh);

        Triangulator.calculate(getOuterLine(), new Triangulator.Listener<Vertex>() {
            @Override
            public void onTriangle(Vertex vertex1, Vertex vertex2, Vertex vertex3) {
                water.addTriangle(vertex1, vertex2, vertex3);
            }
        });
    }

    @Override
    public boolean hasWater() {
        return true;
    }

    @Override
    public double getWaterLevel() {
        return water.getLevel();
    }

    @Override
    public double getWaterGround() {
        return water.getGround();
    }
}
