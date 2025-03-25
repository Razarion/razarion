package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.DecimalPosition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Beat
 * on 17.07.2018.
 */
public class ObstacleFactoryContext {
    private DecimalPosition oldInnerPolygon;
    private DecimalPosition oldOuterPolygon;
    private List<DecimalPosition> tmpInnerPolygon = new ArrayList<>();
    private List<DecimalPosition> tmpOuterPolygon = new ArrayList<>();
    private List<DecimalPosition> tmpBeginningInnerPolygon;
    private List<DecimalPosition> tmpBeginningOuterPolygon;
    private final List<List<DecimalPosition>> polygons = new ArrayList<>();
    private boolean flatDriveway;

    public void addPositions(DecimalPosition innerSlopeGameEngine, DecimalPosition outerSlopeGameEngine, boolean flatDriveway, boolean first) {
        if (!flatDriveway && this.flatDriveway) {
            tmpInnerPolygon.add(oldInnerPolygon);
            tmpOuterPolygon.add(oldOuterPolygon);
        }
        if (!flatDriveway) {
            tmpInnerPolygon.add(innerSlopeGameEngine);
            tmpOuterPolygon.add(outerSlopeGameEngine);
        }
        if (first) {
            if (flatDriveway) {
                throw new IllegalStateException("first && flatDriveway");
            }
            this.flatDriveway = true;
        } else if (flatDriveway && !this.flatDriveway) {
            tmpInnerPolygon.add(innerSlopeGameEngine);
            tmpOuterPolygon.add(outerSlopeGameEngine);
            // End detected
            if (tmpBeginningInnerPolygon == null && tmpBeginningOuterPolygon == null) {
                // Save beginning unfinished obstacle
                tmpBeginningInnerPolygon = tmpInnerPolygon;
                tmpBeginningOuterPolygon = tmpOuterPolygon;
                tmpInnerPolygon = new ArrayList<>();
                tmpOuterPolygon = new ArrayList<>();
            } else {
                completePolygon();
            }
        }
        this.flatDriveway = flatDriveway;
        oldInnerPolygon = innerSlopeGameEngine;
        oldOuterPolygon = outerSlopeGameEngine;
    }

    private void completePolygon() {
        Collections.reverse(tmpInnerPolygon);
        tmpOuterPolygon.addAll(tmpInnerPolygon);
        polygons.add(tmpOuterPolygon);
        tmpInnerPolygon = new ArrayList<>();
        tmpOuterPolygon = new ArrayList<>();
    }

    public void complete() {
        if (tmpBeginningInnerPolygon != null && tmpBeginningOuterPolygon != null) {
            tmpInnerPolygon.addAll(tmpBeginningInnerPolygon);
            tmpOuterPolygon.addAll(tmpBeginningOuterPolygon);
            completePolygon();
        }
        if (polygons.isEmpty()) {
            // If there are no driveways just make two separate polygons for inner and outer
            polygons.add(tmpOuterPolygon);
            Collections.reverse(tmpInnerPolygon);
            polygons.add(tmpInnerPolygon);
        }
    }

    public List<List<DecimalPosition>> getPolygons() {
        return polygons;
    }
}
