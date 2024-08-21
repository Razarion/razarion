package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Rectangle2D;

/**
 * Created by Beat
 * 28.03.2017.
 */
public interface TerrainUtil {
    // See: Angular BabylonTerrainTileImpl
    int NODE_X_COUNT = 160;
    int NODE_Y_COUNT = 160;
    int TILE_NODE_SIZE = NODE_X_COUNT * NODE_Y_COUNT;
    double NODE_X_DISTANCE = 1;
    double NODE_Y_DISTANCE = 1;
    double HEIGHT_PRECISION = 0.1;
    double HEIGHT_MIN = -200;
    double WATER_LEVEL = 0;
    double HEIGHT_DEFAULT = 0.5;

    static Index terrainPositionToTileIndex(DecimalPosition terrainPosition) {
        return terrainPosition.divide(NODE_X_COUNT, NODE_Y_COUNT).toIndexFloor();
    }

    static Index terrainPositionToNodeIndex(DecimalPosition terrainPosition) {
        return terrainPosition.divide(NODE_X_DISTANCE, NODE_Y_DISTANCE).toIndex();
    }

    static DecimalPosition nodeIndexToTerrainPosition(Index nodeIndex) {
        return new DecimalPosition(nodeIndex.scale(NODE_X_DISTANCE, NODE_Y_DISTANCE));
    }

    static DecimalPosition nodeIndexToMiddleTerrainPosition(Index nodeIndex) {
        return nodeIndexToTerrainPosition(nodeIndex).add(TerrainUtil.NODE_X_DISTANCE / 2.0, TerrainUtil.NODE_Y_DISTANCE / 2.0);
    }

    static Index nodeIndexToTileIndex(Index nodeIndex) {
        return nodeIndex.scaleInverseXY(NODE_X_COUNT, NODE_Y_COUNT);
    }

    static Index tileIndexToNodeIndex(Index nodeIndex) {
        return nodeIndex.scale(NODE_X_COUNT, NODE_Y_COUNT);
    }

    // See: Angular Code BabylonTerrainTileImpl.uint16ToHeight
    static double uint16ToHeight(int uint16) {
        return uint16 * HEIGHT_PRECISION + HEIGHT_MIN;
    }

    // See: Angular Code BabylonTerrainTileImpl.heightToUnit16
    static int heightToUnit16(double height) {
        double value = (height - HEIGHT_MIN) / HEIGHT_PRECISION;
        return (int) (Math.round(value * 10) / 10);
    }

    @Deprecated
    int TERRAIN_NODE_ABSOLUTE_LENGTH = 8;
    @Deprecated
    int TERRAIN_TILE_NODES_COUNT = 20;
    @Deprecated
    double TERRAIN_TILE_ABSOLUTE_LENGTH = TERRAIN_NODE_ABSOLUTE_LENGTH * TERRAIN_TILE_NODES_COUNT;
    @Deprecated
    int MAX_SUB_NODE_DEPTH = 2;
    @Deprecated
    double MIN_SUB_NODE_LENGTH = calculateSubNodeLength(MAX_SUB_NODE_DEPTH);
    @Deprecated
    int TOTAL_MIN_SUB_NODE_COUNT = (int) (TERRAIN_NODE_ABSOLUTE_LENGTH / MIN_SUB_NODE_LENGTH);

    @Deprecated
    static Index toTile(DecimalPosition absolute) {
        return absolute.divide(TERRAIN_TILE_ABSOLUTE_LENGTH).toIndexFloor();
    }

    @Deprecated
    static Index toTileCeil(DecimalPosition absolute) {
        return absolute.divide(TERRAIN_TILE_ABSOLUTE_LENGTH).toIndexCeil();
    }

    @Deprecated
    static DecimalPosition toTileAbsolute(Index tile) {
        return new DecimalPosition(tile.scale(TERRAIN_TILE_ABSOLUTE_LENGTH));
    }

    @Deprecated
    static Rectangle2D toAbsoluteTileRectangle(Index tile) {
        DecimalPosition start = toTileAbsolute(tile);
        return new Rectangle2D(start.getX(), start.getY(), TERRAIN_TILE_ABSOLUTE_LENGTH, TERRAIN_TILE_ABSOLUTE_LENGTH);
    }

    @Deprecated
    static Index toNode(DecimalPosition absolute) {
        return absolute.divide(TERRAIN_NODE_ABSOLUTE_LENGTH).toIndexFloor();
    }

    @Deprecated
    static DecimalPosition toNodeAbsolute(Index node) {
        return new DecimalPosition(node.scale(TERRAIN_NODE_ABSOLUTE_LENGTH));
    }

    @Deprecated
    static DecimalPosition toNodeAbsolute(DecimalPosition node) {
        return node.divide(TERRAIN_NODE_ABSOLUTE_LENGTH);
    }

    @Deprecated
    static Rectangle2D toAbsoluteNodeRectangle(Index node) {
        DecimalPosition start = toNodeAbsolute(node);
        return new Rectangle2D(start.getX(), start.getY(), TERRAIN_NODE_ABSOLUTE_LENGTH, TERRAIN_NODE_ABSOLUTE_LENGTH);
    }

    @Deprecated
    static DecimalPosition toAbsoluteNodeCenter(Index node) {
        return toNodeAbsolute(node).add(toAbsoluteNodeCenter());
    }

    @Deprecated
    static DecimalPosition toAbsoluteNodeCenter() {
        return new DecimalPosition(TERRAIN_NODE_ABSOLUTE_LENGTH / 2.0, TERRAIN_NODE_ABSOLUTE_LENGTH / 2.0);
    }

    @Deprecated
    static Index nodeToTile(Index nodeIndex) {
        return nodeIndex.divide(TERRAIN_TILE_NODES_COUNT).toIndexFloor();
    }

    /**
     * Absolute length of a sub node. Dept 0 is topmost
     *
     * @param depth current dept start with 0
     * @return absolute length
     */
    @Deprecated
    static double calculateSubNodeLength(int depth) {
        return TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH / (double) (1 << depth + 1);
    }

    @Deprecated
    static DecimalPosition smallestSubNodeAbsolute(Index subNodeIndex) {
        return new DecimalPosition(subNodeIndex);
    }

    @Deprecated
    static DecimalPosition smallestSubNodeCenter(Index subNodeIndex) {
        return smallestSubNodeAbsolute(subNodeIndex).add(0.5, 0.5);
    }
}
