package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Rectangle2D;

/**
 * Created by Beat
 * 28.03.2017.
 */
public interface TerrainUtil {
    int GROUND_NODE_ABSOLUTE_LENGTH = 8;
    int TERRAIN_TILE_NODES_COUNT = 20;
    int TERRAIN_TILE_TOTAL_NODES_COUNT = TERRAIN_TILE_NODES_COUNT * TERRAIN_TILE_NODES_COUNT;
    double TERRAIN_TILE_ABSOLUTE_LENGTH = GROUND_NODE_ABSOLUTE_LENGTH * TERRAIN_TILE_NODES_COUNT;

    static Index toTile(DecimalPosition absolute) {
        return absolute.divide(TERRAIN_TILE_ABSOLUTE_LENGTH).toIndexFloor();
    }

    static DecimalPosition toTileAbsolute(Index tile) {
        return new DecimalPosition(tile.scale(TERRAIN_TILE_ABSOLUTE_LENGTH));
    }

    static Rectangle2D toAbsoluteTileRectangle(Index tile) {
        DecimalPosition start = toTileAbsolute(tile);
        return new Rectangle2D(start.getX(), start.getY(), TERRAIN_TILE_ABSOLUTE_LENGTH, TERRAIN_TILE_ABSOLUTE_LENGTH);
    }

    static Index toNode(DecimalPosition absolute) {
        return absolute.divide(GROUND_NODE_ABSOLUTE_LENGTH).toIndexFloor();
    }

    static DecimalPosition toNodeAbsolute(Index tile) {
        return new DecimalPosition(tile.scale(GROUND_NODE_ABSOLUTE_LENGTH));
    }

    static Rectangle2D toAbsoluteNodeRectangle(Index tile) {
        DecimalPosition start = toNodeAbsolute(tile);
        return new Rectangle2D(start.getX(), start.getY(), GROUND_NODE_ABSOLUTE_LENGTH, GROUND_NODE_ABSOLUTE_LENGTH);
    }

    static Rectangle2D toAbsoluteNodeRectangle(Rectangle node) {
        return new Rectangle2D(toNodeAbsolute(node.getStart()), toNodeAbsolute(node.getEnd()));
    }

    static int filedToArrayNodeIndex(Index index) {
        return index.getX() + index.getY() * TERRAIN_TILE_NODES_COUNT;
    }

    static Index arrayToFiledNodeIndex(int index) {
        return new Index(index % TERRAIN_TILE_NODES_COUNT, index / TERRAIN_TILE_NODES_COUNT);
    }

    static int toNodeIndex(int tileIndex) {
        return tileIndex * TerrainUtil.TERRAIN_TILE_NODES_COUNT;
    }
}
