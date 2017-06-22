package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.planet.pathing.Obstacle;

import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * on 18.06.2017.
 */
public class TerrainShapeNode {
    private double[] fullDrivewayHeights; // bl, br, tr, tl
    private TerrainShapeSubNode[][] terrainShapeSubNodes;
    private Double uniformGroundHeight;
    private List<List<Vertex>> groundSlopeConnections;
    private List<List<Vertex>> waterSegments;
    private Boolean water;
    private Collection<Obstacle> obstacles;

    public boolean isFullDriveway() {
        return fullDrivewayHeights != null;
    }

    public boolean isFullWater() {
        return water != null && water;
    }

    public boolean hasSubNodes() {
        return terrainShapeSubNodes != null;
    }

    public double getUniformGroundHeight() {
        if (uniformGroundHeight != null) {
            return uniformGroundHeight;
        } else {
            return 0;
        }
    }

    public TerrainShapeSubNode getTerrainShapeSubNode(DecimalPosition nodeRelative) {
        throw new UnsupportedOperationException();
    }

    public List<List<Vertex>> getGroundSlopeConnections() {
        return groundSlopeConnections;
    }

    public List<List<Vertex>> getWaterSegments() {
        return waterSegments;
    }

    public Collection<Obstacle> getObstacles() {
        return obstacles;
    }

    public double getDrivewayHeightBL() {
        if (fullDrivewayHeights == null) {
            throw new IllegalStateException("TerrainShapeNode.getDrivewayHeightBL() fullDrivewayHeights == null");
        }
        return fullDrivewayHeights[0];
    }

    public double getDrivewayHeightBR() {
        if (fullDrivewayHeights == null) {
            throw new IllegalStateException("TerrainShapeNode.getDrivewayHeightBR() fullDrivewayHeights == null");
        }
        return fullDrivewayHeights[1];
    }

    public double getDrivewayHeightTR() {
        if (fullDrivewayHeights == null) {
            throw new IllegalStateException("TerrainShapeNode.getDrivewayHeightTR() fullDrivewayHeights == null");
        }
        return fullDrivewayHeights[2];
    }

    public double getDrivewayHeightTL() {
        if (fullDrivewayHeights == null) {
            throw new IllegalStateException("TerrainShapeNode.getDrivewayHeightTL() fullDrivewayHeights == null");
        }
        return fullDrivewayHeights[3];
    }
}
