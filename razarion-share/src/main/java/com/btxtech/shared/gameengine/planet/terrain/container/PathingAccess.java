package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.gameengine.planet.pathing.Obstacle;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;

import java.util.Collection;

/**
 * Created by Beat
 * on 19.06.2017.
 */
public class PathingAccess {
    private class IterationControl implements TerrainRegionImpactCallback.Control {
        private boolean notLane;

        private void setNotLand() {
            notLane = true;
        }

        @Override
        public boolean isStop() {
            return notLane;
        }

        public boolean isNotLane() {
            return notLane;
        }
    }

    private TerrainShape terrainShape;

    public PathingAccess(TerrainShape terrainShape) {
        this.terrainShape = terrainShape;
    }

    public boolean isTerrainFree(DecimalPosition position) {
        return terrainShape.terrainImpactCallback(position, new TerrainImpactCallback<Boolean>() {
            @Override
            public Boolean landNoTile(Index tileIndex) {
                return true;
            }

            @Override
            public Boolean inTile(TerrainShapeTile terrainShapeTile, Index tileIndex) {
                return terrainShapeTile.isLand();
            }

            @Override
            public Boolean inNode(TerrainShapeNode terrainShapeNode, Index nodeRelativeIndex, DecimalPosition tileRelative, Index tileIndex) {
                return !terrainShapeNode.isFullWater();
            }

            @Override
            public Boolean inSubNode(TerrainShapeSubNode terrainShapeSubNode, DecimalPosition nodeRelative, Index nodeRelativeIndex, DecimalPosition tileRelative, Index tileIndex) {
                return terrainShapeSubNode.isLand();
            }
        });
    }

    public boolean isTerrainFree(DecimalPosition position, double radius) {
        IterationControl iterationControl = new IterationControl();
        terrainShape.terrainRegionImpactCallback(position, radius, iterationControl, new TerrainRegionImpactCallback() {

            @Override
            public void inTile(TerrainShapeTile terrainShapeTile, Index tileIndex) {
                if (!terrainShapeTile.isLand()) {
                    iterationControl.setNotLand();
                }
            }

            @Override
            public void inNode(TerrainShapeNode terrainShapeNode, Index nodeRelativeIndex, Index tileIndex) {
                if (terrainShapeNode.isFullWater()) {
                    iterationControl.setNotLand();
                }
            }
        });
        return !iterationControl.isNotLane();
    }

    public boolean isTileFree(Index nodeIndex) {
        TerrainShapeNode terrainShapeNode = terrainShape.getTerrainShapeNode(nodeIndex);
        return terrainShapeNode == null || !terrainShapeNode.isFullWater();
    }

    public boolean hasNorthSuccessorNode(int currentNodePositionY) {
        int tileY = TerrainUtil.nodeToTile(currentNodePositionY + 1);
        return tileY - terrainShape.getTileOffset().getY() < terrainShape.getTileYCount() - 1;
    }

    public boolean hasEastSuccessorNode(int currentNodePositionX) {
        int tileX = TerrainUtil.nodeToTile(currentNodePositionX + 1);
        return tileX - terrainShape.getTileOffset().getX() < terrainShape.getTileXCount() - 1;
    }

    public boolean hasSouthSuccessorNode(int currentNodePositionY) {
        int tileY = TerrainUtil.nodeToTile(currentNodePositionY - 1);
        return tileY >= 0;
    }

    public boolean hasWestSuccessorNode(int currentNodePositionX) {
        int tileX = TerrainUtil.nodeToTile(currentNodePositionX - 1);
        return tileX >= 0;
    }

    // --------------------------------------

    public Collection<Obstacle> getObstacles(SyncPhysicalMovable syncPhysicalMovable) {
        throw new UnsupportedOperationException();
    }

    public boolean isInSight(SyncPhysicalArea syncPhysicalArea, DecimalPosition target) {
        throw new UnsupportedOperationException();
    }
}
