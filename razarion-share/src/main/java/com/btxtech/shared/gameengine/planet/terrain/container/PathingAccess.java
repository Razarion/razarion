package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.gameengine.planet.pathing.Obstacle;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.shared.utils.GeometricUtil;
import com.btxtech.shared.utils.MathHelper;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.btxtech.shared.gameengine.planet.terrain.TerrainUtil.*;

/**
 * Created by Beat
 * on 19.06.2017.
 */
public class PathingAccess {
    public static double HEIGHT_PRECISION = 0.1;
    public static double HEIGHT_MIN = -200;
    public static double WATER_LEVEL = 0;
    private final TerrainShapeManager terrainShape;

    public PathingAccess(TerrainShapeManager terrainShape) {
        this.terrainShape = terrainShape;
    }

    public TerrainType getTerrainType(Index position) {
        double blHeight = getHeightNodeAt(position);
        double brHeight = getHeightNodeAt(position.add(1, 0));
        double trHeight = getHeightNodeAt(position.add(1, 1));
        double tlHeight = getHeightNodeAt(position.add(0, 1));

        double avgHeight = (blHeight + brHeight + trHeight + tlHeight) / 4.0;
        if (avgHeight < WATER_LEVEL) {
            return TerrainType.WATER;
        }

        double maxHeight = CollectionUtils.getMax(blHeight, brHeight, trHeight, tlHeight);
        double minHeight = CollectionUtils.getMin(blHeight, brHeight, trHeight, tlHeight);
        if (Math.abs(maxHeight - minHeight) < 1) {
            return TerrainType.LAND;
        } else {
            return TerrainType.BLOCKED;
        }
    }

    public double getHeightNodeAt(Index terrainNodeIndex) {
        int uint16 = getUInt16GroundHeightAt(terrainNodeIndex);
        return uint16ToHeight(uint16);
    }

    public int getUInt16GroundHeightAt(Index terrainNodeIndex) {
        Index terrainTileIndex = terrainNodeIndex.scaleInverseXY(NODE_X_COUNT, NODE_Y_COUNT);

        int totalTileNodes = (NODE_X_COUNT + 1) * (NODE_Y_COUNT + 1);
        int startTileNodeIndex = totalTileNodes * (terrainTileIndex.getY() * terrainShape.getTileXCount() + terrainTileIndex.getX());

        Index offset = terrainNodeIndex.sub(terrainTileIndex.scale(NODE_X_COUNT, NODE_Y_COUNT));
        int offsetTileNodeIndex = offset.getY() * (NODE_X_COUNT + 1) + offset.getX();

        return terrainShape.getNativeTerrainShapeAccess().getGroundHeightAt(startTileNodeIndex + offsetTileNodeIndex);
    }

    public boolean isTerrainTypeAllowed(TerrainType terrainType, Index nodeIndex) {
        return TerrainType.isAllowed(terrainType, getTerrainType(nodeIndex));
    }

    public boolean isTerrainTypeAllowed(TerrainType terrainType, DecimalPosition terrainPosition, double radius) {
        if (terrainType == null) {
            throw new NullPointerException("PathingAccess.isTerrainTypeAllowed() terrainType==null");
        }
        Index nodeIndex = terrainPositionToNodeIndex(terrainPosition);
        if (terrainType.isAreaCheck()) {
            List<Index> rasterOffsets = GeometricUtil.rasterizeCircle(new Circle2D(DecimalPosition.NULL, radius), (int) TerrainUtil.NODE_X_DISTANCE);
            for (Index rasterOffset : rasterOffsets) {
                if (!isTerrainTypeAllowed(terrainType, nodeIndex.add(rasterOffset))) {
                    return false;
                }
            }
            return true;
        } else {
            return isTerrainTypeAllowed(terrainType, nodeIndex);
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
        Index nodeIndex = TerrainUtil.terrainPositionToNodeIndex(terrainPosition);
        return new PathingNodeWrapper(nodeIndex, getTerrainType(nodeIndex), this);
    }

    public boolean isNodeInBoundary(Index nodeIndex) {
        if(nodeIndex.getX() < 0 || nodeIndex.getY() < 0) {
            return false;
        }
        int xBoundary = terrainShape.getTileXCount() * NODE_X_COUNT;
        int yBoundary = terrainShape.getTileYCount() * NODE_Y_COUNT;
        return nodeIndex.getX() < xBoundary && nodeIndex.getY() < yBoundary;
    }

    public TerrainShapeManager getTerrainShape() {
        return terrainShape;
    }

    // See: Angular Code BabylonTerrainTileImpl.uint16ToHeight
    public static double uint16ToHeight(int uint16) {
        return uint16 * HEIGHT_PRECISION + HEIGHT_MIN;
    }

    // See: Angular Code BabylonTerrainTileImpl.heightToUnit16
    public static int heightToUnit16(double height) {
        double value = (height - HEIGHT_MIN) / HEIGHT_PRECISION;
        return (int) (Math.round(value * 10) / 10);
    }
}
