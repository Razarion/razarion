package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.gameengine.planet.pathing.Obstacle;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.utils.GeometricUtil;
import com.btxtech.shared.utils.MathHelper;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by Beat
 * on 19.06.2017.
 */
public class PathingAccess {
    private final TerrainShapeManager terrainShape;

    public PathingAccess(TerrainShapeManager terrainShape) {
        this.terrainShape = terrainShape;
    }

    public TerrainType getTerrainType(DecimalPosition position) {
        return TerrainType.LAND; // TODO
    }

    public boolean isTerrainTypeAllowed(TerrainType terrainType, DecimalPosition position) {
        return TerrainType.isAllowed(terrainType, getTerrainType(position));
    }

    public boolean isTerrainTypeAllowed(TerrainType terrainType, DecimalPosition terrainPosition, double radius) {
        if (terrainType == null) {
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

    public Set<Obstacle> getObstacles(DecimalPosition position, double radius) {
        return Collections.emptySet(); // TODO
    }

    public boolean isInSight(DecimalPosition start, double radius, DecimalPosition target) {
        if (start.equals(target)) {
            return true;
        }
        double angel = start.getAngle(target);
        double angel1 = MathHelper.normaliseAngle(angel - MathHelper.QUARTER_RADIANT);
        double angel2 = MathHelper.normaliseAngle(angel + MathHelper.QUARTER_RADIANT);

        Line line = new Line(start, target);
        Line line1 = new Line(start.getPointWithDistance(angel1, radius), target.getPointWithDistance(angel1, radius));
        Line line2 = new Line(start.getPointWithDistance(angel2, radius), target.getPointWithDistance(angel2, radius));

        return !terrainShape.isSightBlocked(line) && !terrainShape.isSightBlocked(line1) && !terrainShape.isSightBlocked(line2);
    }

    public PathingNodeWrapper getPathingNodeWrapper(DecimalPosition terrainPosition) {
        return terrainShape.terrainImpactCallback(terrainPosition, new TerrainImpactCallback<PathingNodeWrapper>() {
            @Override
            public PathingNodeWrapper landNoTile(Index tileIndex) {
                return new PathingNodeWrapper(PathingAccess.this, TerrainUtil.toNode(terrainPosition));
            }

        });
    }

    public boolean isNodeInBoundary(Index nodeIndex) {
        Index fieldIndex = TerrainUtil.nodeToTile(nodeIndex);
        return fieldIndex.getX() >= 0 && fieldIndex.getY() >= 0 && fieldIndex.getX() < terrainShape.getTileXCount() && fieldIndex.getY() < terrainShape.getTileYCount();
    }

    public boolean isPositionInBoundary(DecimalPosition position) {
        Index fieldIndex = TerrainUtil.toTile(position);
        return fieldIndex.getX() >= 0 && fieldIndex.getY() >= 0 && fieldIndex.getX() < terrainShape.getTileXCount() && fieldIndex.getY() < terrainShape.getTileYCount();
    }

    public TerrainShapeManager getTerrainShape() {
        return terrainShape;
    }
}
