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
    double HEIGHT_PRECISION = 0.1;
    double NODE_SIZE = 1;
    double HEIGHT_MIN = -200;
    double WATER_LEVEL = 0;
    double WALL_HEIGHT_DIFF = 0.5;
    double BOT_BOX_LENGTH = 8;
    double HEIGHT_DEFAULT = 0.5;

    static Index terrainPositionToTileIndex(DecimalPosition terrainPosition) {
        return terrainPosition.divide(NODE_X_COUNT, NODE_Y_COUNT).toIndexFloor();
    }

    static Index terrainPositionToTileIndexCeil(DecimalPosition terrainPosition) {
        return terrainPosition.divide(NODE_X_COUNT, NODE_Y_COUNT).toIndexCeil();
    }

    static Index terrainPositionToNodeIndex(DecimalPosition terrainPosition) {
        return terrainPosition.divide(NODE_SIZE, NODE_SIZE).toIndex();
    }

    static DecimalPosition nodeIndexToTerrainPosition(Index nodeIndex) {
        return new DecimalPosition(nodeIndex.scale(NODE_SIZE, NODE_SIZE));
    }

    static DecimalPosition nodeIndexToMiddleTerrainPosition(Index nodeIndex) {
        return nodeIndexToTerrainPosition(nodeIndex).add(TerrainUtil.NODE_SIZE / 2.0, TerrainUtil.NODE_SIZE / 2.0);
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
    static Index toTile(DecimalPosition absolute) {
        return absolute.divide(TERRAIN_TILE_ABSOLUTE_LENGTH).toIndexFloor();
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
}
