package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.gameengine.planet.pathing.Obstacle;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeObjectList;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeObjectPosition;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.shared.utils.GeometricUtil;
import com.btxtech.shared.utils.MathHelper;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.btxtech.shared.gameengine.planet.terrain.TerrainUtil.*;

/**
 * Created by Beat
 * on 19.06.2017.
 */
public class TerrainAnalyzer {
    private final HeightMapAccess heightMapAccess;
    private final TerrainShapeManager terrainShape;
    private final Map<Index, TerrainType> terrainTypeCache = new HashMap<>();

    public TerrainAnalyzer(HeightMapAccess heightMapAccess, TerrainShapeManager terrainShape) {
        this.heightMapAccess = heightMapAccess;
        this.terrainShape = terrainShape;
    }

    public TerrainType getTerrainType(Index terrainNodeIndex) {
        TerrainType cached = terrainTypeCache.get(terrainNodeIndex);
        if (cached != null) {
            return cached;
        }

        TerrainType terrainType = analyze(terrainNodeIndex);
        terrainTypeCache.put(terrainNodeIndex, terrainType);

        return terrainType;
    }

    private TerrainType analyze(Index terrainNodeIndex) {
        if(terrainShape != null) {
            if (isBlockedByTerrainObject(terrainNodeIndex)) {
                return TerrainType.BLOCKED;
            }
        }

        double blHeight = getHeightNodeAt(terrainNodeIndex);
        double brHeight = getHeightNodeAt(terrainNodeIndex.add(1, 0));
        double trHeight = getHeightNodeAt(terrainNodeIndex.add(1, 1));
        double tlHeight = getHeightNodeAt(terrainNodeIndex.add(0, 1));

        double avgHeight = (blHeight + brHeight + trHeight + tlHeight) / 4.0;
        if (avgHeight < WATER_LEVEL) {
            return TerrainType.WATER;
        }

        double maxHeight = CollectionUtils.getMax(blHeight, brHeight, trHeight, tlHeight);
        double minHeight = CollectionUtils.getMin(blHeight, brHeight, trHeight, tlHeight);
        if (Math.abs(maxHeight - minHeight) < 0.7) {
            return TerrainType.LAND;
        } else {
            return TerrainType.BLOCKED;
        }
    }

    private boolean isBlockedByTerrainObject(Index terrainNodeIndex) {
        DecimalPosition scanPosition = nodeIndexToMiddleTerrainPosition(terrainNodeIndex);
        Index terrainTileIndex = nodeIndexToTileIndex(terrainNodeIndex);
        TerrainShapeTile terrainShapeTile = terrainShape.getTerrainShapeTile(terrainTileIndex);
        if (terrainShapeTile == null) {
            return false;
        }
        if(terrainShapeTile.getNativeTerrainShapeObjectLists() == null) {
            return false;
        }
        for (NativeTerrainShapeObjectList nativeTerrainShapeObjectList : terrainShapeTile.getNativeTerrainShapeObjectLists()) {
            double radius = terrainShape.getTerrainTypeService().getTerrainObjectConfig(nativeTerrainShapeObjectList.terrainObjectConfigId).getRadius();
            if (radius > 0.0) {
                for (NativeTerrainShapeObjectPosition nativeTerrainShapeObjectPosition : nativeTerrainShapeObjectList.terrainShapeObjectPositions) {
                    double distance = scanPosition.getDistance(nativeTerrainShapeObjectPosition.x, nativeTerrainShapeObjectPosition.y);
                    if (distance - radius < 0) {
                        return true;
                    }
                }
            }

        }
        return false;
    }

    public double getHeightNodeAt(Index terrainNodeIndex) {
        int uint16 = getUInt16GroundHeightAt(terrainNodeIndex);
        return uint16ToHeight(uint16);
    }

    public int getUInt16GroundHeightAt(Index terrainNodeIndex) {
        Index terrainTileIndex = nodeIndexToTileIndex(terrainNodeIndex);

        int startTileNodeIndex = 0;
        int yAddition = 1;
        if (terrainShape != null) {
            yAddition = 0;
            startTileNodeIndex = TILE_NODE_SIZE * (terrainTileIndex.getY() * terrainShape.getTileXCount() + terrainTileIndex.getX());
        }

        Index offset = terrainNodeIndex.sub(terrainTileIndex.scale(NODE_X_COUNT, NODE_Y_COUNT));
        int offsetTileNodeIndex = offset.getY() * (NODE_X_COUNT + yAddition) + offset.getX();

        return heightMapAccess.getUInt16HeightAt(startTileNodeIndex + offsetTileNodeIndex);
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
            List<Index> rasterOffsets = GeometricUtil.rasterizeCircle(new Circle2D(DecimalPosition.NULL, radius), (int) TerrainUtil.NODE_SIZE);
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

    public PathingNodeWrapper getPathingNodeWrapper(Index nodeIndex) {
        return new PathingNodeWrapper(nodeIndex, getTerrainType(nodeIndex), this);
    }

    public PathingNodeWrapper getPathingNodeWrapper(DecimalPosition terrainPosition) {
        Index nodeIndex = TerrainUtil.terrainPositionToNodeIndex(terrainPosition);
        return getPathingNodeWrapper(nodeIndex);
    }

    public boolean isNodeInBoundary(Index nodeIndex) {
        if (nodeIndex.getX() < 0 || nodeIndex.getY() < 0) {
            return false;
        }
        int xBoundary = terrainShape.getTileXCount() * NODE_X_COUNT;
        int yBoundary = terrainShape.getTileYCount() * NODE_Y_COUNT;
        return nodeIndex.getX() < xBoundary && nodeIndex.getY() < yBoundary;
    }

    public TerrainShapeManager getTerrainShape() {
        return terrainShape;
    }
}
