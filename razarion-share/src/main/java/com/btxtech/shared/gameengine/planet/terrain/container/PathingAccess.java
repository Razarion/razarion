package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.gameengine.planet.pathing.Obstacle;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.utils.MathHelper;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Beat
 * on 19.06.2017.
 */
public class PathingAccess {

    private class IterationControl implements TerrainRegionImpactCallback.Control {
        private boolean notLane;

        @Override
        public void doStop() {
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
                return !terrainShapeNode.isFullWater() && !terrainShapeNode.isHiddenUnderSlope();
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
                    iterationControl.doStop();
                }
            }

            @Override
            public void inNode(TerrainShapeNode terrainShapeNode, Index nodeRelativeIndex, Index tileIndex) {
                if (terrainShapeNode.isFullWater()) {
                    iterationControl.doStop();
                }
            }
        });
        return !iterationControl.isNotLane();
    }

    public Collection<Obstacle> getObstacles(SyncPhysicalMovable syncPhysicalMovable) {
        Collection<Obstacle> obstacles = new ArrayList<>();
        terrainShape.terrainRegionImpactCallback(syncPhysicalMovable.getPosition2d(), syncPhysicalMovable.getRadius(), new TerrainRegionImpactCallback() {
            @Override
            public void inNode(TerrainShapeNode terrainShapeNode, Index nodeRelativeIndex, Index tileIndex) {
                if (terrainShapeNode.getObstacles() != null) {
                    obstacles.addAll(terrainShapeNode.getObstacles());
                }
            }
        });
        return obstacles;
    }

    public boolean isInSight(SyncPhysicalArea syncPhysicalArea, DecimalPosition target) {
        if (syncPhysicalArea.getPosition2d().equals(target)) {
            return true;
        }
        double angel = syncPhysicalArea.getPosition2d().getAngle(target);
        double angel1 = MathHelper.normaliseAngle(angel - MathHelper.QUARTER_RADIANT);
        double angel2 = MathHelper.normaliseAngle(angel + MathHelper.QUARTER_RADIANT);

        Line line = new Line(syncPhysicalArea.getPosition2d(), target);
        Line line1 = new Line(syncPhysicalArea.getPosition2d().getPointWithDistance(angel1, syncPhysicalArea.getRadius()), target.getPointWithDistance(angel1, syncPhysicalArea.getRadius()));
        Line line2 = new Line(syncPhysicalArea.getPosition2d().getPointWithDistance(angel2, syncPhysicalArea.getRadius()), target.getPointWithDistance(angel2, syncPhysicalArea.getRadius()));

        return !terrainShape.isSightBlocked(line) && !terrainShape.isSightBlocked(line1) && !terrainShape.isSightBlocked(line2);
    }

    public PathingNodeWrapper getPathingNodeWrapper(DecimalPosition terrainPosition) {
        return terrainShape.terrainImpactCallback(terrainPosition, new TerrainImpactCallback<PathingNodeWrapper>() {
            @Override
            public PathingNodeWrapper landNoTile(Index tileIndex) {
                return new PathingNodeWrapper(PathingAccess.this, true, TerrainUtil.toNode(terrainPosition));
            }

            @Override
            public PathingNodeWrapper inTile(TerrainShapeTile terrainShapeTile, Index tileIndex) {
                return new PathingNodeWrapper(PathingAccess.this, terrainShapeTile.isLand(), TerrainUtil.toNode(terrainPosition));
            }

            @Override
            public PathingNodeWrapper inNode(TerrainShapeNode terrainShapeNode, Index nodeRelativeIndex, DecimalPosition tileRelative, Index tileIndex) {
                return new PathingNodeWrapper(PathingAccess.this, terrainShapeNode, nodeRelativeIndex.add(TerrainUtil.tileToNode(tileIndex)));
            }

            @Override
            public PathingNodeWrapper inSubNode(TerrainShapeSubNode terrainShapeSubNode, DecimalPosition nodeRelative, Index nodeRelativeIndex, DecimalPosition tileRelative, Index tileIndex) {
                return new PathingNodeWrapper(PathingAccess.this, terrainShapeSubNode, nodeRelativeIndex.add(TerrainUtil.tileToNode(tileIndex)), nodeRelative);
            }
        });
    }

    public boolean isNodeInBoundary(Index nodeIndex) {
        Index fieldIndex = TerrainUtil.nodeToTile(nodeIndex).sub(terrainShape.getTileOffset());
        return fieldIndex.getX() >= 0 && fieldIndex.getY() >= 0 && fieldIndex.getX() < terrainShape.getTileXCount() && fieldIndex.getY() < terrainShape.getTileYCount();
    }

    public TerrainShape getTerrainShape() {
        return terrainShape;
    }

    public TerrainShapeSubNode getTerrainShapeSubNode(DecimalPosition terrainPosition) {
        return terrainShape.terrainImpactCallback(terrainPosition, new TerrainImpactCallback<TerrainShapeSubNode>() {
            @Override
            public TerrainShapeSubNode landNoTile(Index tileIndex) {
                return null;
            }

            @Override
            public TerrainShapeSubNode inTile(TerrainShapeTile terrainShapeTile, Index tileIndex) {
                return null;
            }

            @Override
            public TerrainShapeSubNode inNode(TerrainShapeNode terrainShapeNode, Index nodeRelativeIndex, DecimalPosition tileRelative, Index tileIndex) {
                return null;
            }

            @Override
            public TerrainShapeSubNode inSubNode(TerrainShapeSubNode terrainShapeSubNode, DecimalPosition nodeRelative, Index nodeRelativeIndex, DecimalPosition tileRelative, Index tileIndex) {
                return terrainShapeSubNode;
            }
        });
    }
}
