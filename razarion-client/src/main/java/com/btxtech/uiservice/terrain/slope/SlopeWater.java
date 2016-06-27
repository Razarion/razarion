package com.btxtech.uiservice.terrain.slope;

import com.btxtech.uiservice.terrain.GroundMesh;
import com.btxtech.uiservice.terrain.Water;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.shared.dto.SlopeSkeleton;
import com.btxtech.shared.primitives.Triangulator;
import com.btxtech.shared.primitives.Vertex;

import java.util.List;

/**
 * Created by Beat
 * 10.04.2016.
 */
public class SlopeWater extends Slope {
    private final Water water;

    public SlopeWater(Water water, SlopeSkeleton slopeSkeleton, List<Index> corners) {
        super(slopeSkeleton, corners);
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
