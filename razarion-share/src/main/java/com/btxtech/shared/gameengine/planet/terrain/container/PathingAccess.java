package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.gameengine.planet.pathing.Obstacle;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.utils.GeometricUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

    public TerrainType getTerrainType(DecimalPosition position) {
        return terrainShape.terrainImpactCallback(position, new TerrainImpactCallback<TerrainType>() {
            @Override
            public TerrainType landNoTile(Index tileIndex) {
                return TerrainType.LAND;
            }

            @Override
            public TerrainType inTile(TerrainShapeTile terrainShapeTile, Index tileIndex) {
                return terrainShapeTile.getTerrainType();
            }

            @Override
            public TerrainType inNode(TerrainShapeNode terrainShapeNode, Index nodeRelativeIndex, DecimalPosition tileRelative, Index tileIndex) {
                return terrainShapeNode.getTerrainType();
            }

            @Override
            public TerrainType inSubNode(TerrainShapeSubNode terrainShapeSubNode, TerrainShapeNode terrainShapeNode, DecimalPosition nodeRelative, Index nodeRelativeIndex, DecimalPosition tileRelative, Index tileIndex) {
                return terrainShapeSubNode.getTerrainType();
            }
        });
    }

    public boolean isTerrainTypeAllowed(TerrainType terrainType, DecimalPosition position) {
        return TerrainType.isAllowed(terrainType, getTerrainType(position));
    }

    @Deprecated // Use isTerrainTypeAllowed
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
                return !terrainShapeNode.isFullWater() && !terrainShapeNode.getDoNotRenderGround();
            }

            @Override
            public Boolean inSubNode(TerrainShapeSubNode terrainShapeSubNode, TerrainShapeNode terrainShapeNode, DecimalPosition nodeRelative, Index nodeRelativeIndex, DecimalPosition tileRelative, Index tileIndex) {
                return terrainShapeSubNode.isLand();
            }
        });
    }

    public boolean isTerrainTypeAllowed(TerrainType terrainType, DecimalPosition terrainPosition, double radius) {
        if(terrainType == null) {
            throw new NullPointerException("PathingAccess.isTerrainTypeAllowed() terrainType==null");
        }
        if (terrainType.isAreaCheck()) {
            List<Index> subNodeIndices = GeometricUtil.rasterizeCircle(new Circle2D(DecimalPosition.NULL, radius), (int) TerrainUtil.MIN_SUB_NODE_LENGTH);
            for (Index subNodeIndex : subNodeIndices) {
                DecimalPosition scanPosition = TerrainUtil.smallestSubNodeCenter(subNodeIndex).add(terrainPosition);
                if (!isTerrainTypeAllowed(terrainType, scanPosition)) {
                    return false;
                }
            }
            return true;
        } else {
            return isTerrainTypeAllowed(terrainType, terrainPosition);
        }
    }

    public Collection<Obstacle> getObstacles(DecimalPosition position, double radius) {
        Collection<Obstacle> obstacles = new ArrayList<>();
        terrainShape.terrainNodesInCircleCallback(position, radius, terrainShapeNode -> {
            if (terrainShapeNode.getObstacles() != null) {
                obstacles.addAll(terrainShapeNode.getObstacles());
            }
            return true;
        });
        return obstacles;
    }

    public Collection<Obstacle> getObstacles(SyncPhysicalMovable syncPhysicalMovable) {
        return getObstacles(syncPhysicalMovable.getPosition2d(), syncPhysicalMovable.getRadius());
    }

    public boolean isInSight(SyncPhysicalArea syncPhysicalArea, DecimalPosition target) {
        if (syncPhysicalArea.getPosition2d().equals(target)) {
            return true;
        }
        double angel = syncPhysicalArea.getPosition2d().getAngle(target);
        // double angel1 = MathHelper.normaliseAngle(angel - MathHelper.QUARTER_RADIANT);
        // double angel2 = MathHelper.normaliseAngle(angel + MathHelper.QUARTER_RADIANT);

        Line line = new Line(syncPhysicalArea.getPosition2d(), target);
        // Line line1 = new Line(syncPhysicalArea.getPosition2d().getPointWithDistance(angel1, syncPhysicalArea.getRadius()), target.getPointWithDistance(angel1, syncPhysicalArea.getRadius()));
        // Line line2 = new Line(syncPhysicalArea.getPosition2d().getPointWithDistance(angel2, syncPhysicalArea.getRadius()), target.getPointWithDistance(angel2, syncPhysicalArea.getRadius()));

        return !terrainShape.isSightBlocked(line) /*&& !terrainShape.isSightBlocked(line1) && !terrainShape.isSightBlocked(line2)*/;
    }

    public PathingNodeWrapper getPathingNodeWrapper(DecimalPosition terrainPosition) {
        return terrainShape.terrainImpactCallback(terrainPosition, new TerrainImpactCallback<PathingNodeWrapper>() {
            @Override
            public PathingNodeWrapper landNoTile(Index tileIndex) {
                return new PathingNodeWrapper(PathingAccess.this, TerrainUtil.toNode(terrainPosition));
            }

            @Override
            public PathingNodeWrapper inTile(TerrainShapeTile terrainShapeTile, Index tileIndex) {
                if (terrainShapeTile.isLand()) {
                    return new PathingNodeWrapper(PathingAccess.this, TerrainUtil.toNode(terrainPosition));
                } else {
                    return null;
                }
            }

            @Override
            public PathingNodeWrapper inNode(TerrainShapeNode terrainShapeNode, Index nodeRelativeIndex, DecimalPosition tileRelative, Index tileIndex) {
                return new PathingNodeWrapper(PathingAccess.this, TerrainUtil.toNode(terrainPosition), terrainShapeNode);
            }

            @Override
            public PathingNodeWrapper inSubNode(TerrainShapeSubNode terrainShapeSubNode, TerrainShapeNode terrainShapeNode, DecimalPosition nodeRelative, Index nodeRelativeIndex, DecimalPosition tileRelative, Index tileIndex) {
                return new PathingNodeWrapper(PathingAccess.this, TerrainUtil.toSubNodeAbsolute(terrainPosition, terrainShapeSubNode.getDepth()), terrainShapeSubNode);
            }
        });
    }

    public boolean isNodeInBoundary(Index nodeIndex) {
        Index fieldIndex = TerrainUtil.nodeToTile(nodeIndex).sub(terrainShape.getTileOffset());
        return fieldIndex.getX() >= 0 && fieldIndex.getY() >= 0 && fieldIndex.getX() < terrainShape.getTileXCount() && fieldIndex.getY() < terrainShape.getTileYCount();
    }

    public boolean isPositionInBoundary(DecimalPosition position) {
        Index fieldIndex = TerrainUtil.toTile(position).sub(terrainShape.getTileOffset());
        return fieldIndex.getX() >= 0 && fieldIndex.getY() >= 0 && fieldIndex.getX() < terrainShape.getTileXCount() && fieldIndex.getY() < terrainShape.getTileYCount();
    }

    public TerrainShapeNode getTerrainShapeNode(Index nodeIndex) {
        return terrainShape.getTerrainShapeNode(nodeIndex);
    }

    public TerrainShape getTerrainShape() {
        return terrainShape;
    }

    public void outerDirectionCallback(TerrainType terrainType, DecimalPosition subNodePosition, int destinationDepth, Index direction, TerrainShapeNode.DirectionConsumer directionConsumer) {
        Index nodeIndex = TerrainUtil.toNode(subNodePosition);
        TerrainShapeNode terrainShapeNode = getTerrainShapeNode(nodeIndex);

        double length = TerrainUtil.calculateSubNodeLength(0);
        DecimalPosition nodeRelative = subNodePosition.sub(TerrainUtil.toNodeAbsolute(nodeIndex));
        TerrainShapeSubNode[] terrainShapeSubNodes = terrainShapeNode.getTerrainShapeSubNodes();
        if (nodeRelative.getX() < length && nodeRelative.getY() < length) {
            terrainShapeSubNodes[0].outerDirectionCallback(terrainType, nodeRelative, subNodePosition, destinationDepth, direction, directionConsumer);
        } else if (nodeRelative.getX() >= length && nodeRelative.getY() < length) {
            terrainShapeSubNodes[1].outerDirectionCallback(terrainType, nodeRelative.sub(length, 0), subNodePosition, destinationDepth, direction, directionConsumer);
        } else if (nodeRelative.getX() >= length && nodeRelative.getY() >= length) {
            terrainShapeSubNodes[2].outerDirectionCallback(terrainType, nodeRelative.sub(length, length), subNodePosition, destinationDepth, direction, directionConsumer);
        } else if (nodeRelative.getX() < length && nodeRelative.getY() >= length) {
            terrainShapeSubNodes[3].outerDirectionCallback(terrainType, nodeRelative.sub(0, length), subNodePosition, destinationDepth, direction, directionConsumer);
        } else {
            throw new IllegalArgumentException("PathingAccess.outerDirectionCallback()");
        }
    }
}
