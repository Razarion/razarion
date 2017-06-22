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

    static DecimalPosition toNodeAbsolute(Index node) {
        return new DecimalPosition(node.scale(GROUND_NODE_ABSOLUTE_LENGTH));
    }

    static DecimalPosition toNodeAbsolute(DecimalPosition node) {
        return node.divide(GROUND_NODE_ABSOLUTE_LENGTH);
    }

    static Index toNodeAbsoluteIndex(Index node) {
        return node.scale(GROUND_NODE_ABSOLUTE_LENGTH);
    }

    static Rectangle2D toAbsoluteNodeRectangle(Index tile) {
        DecimalPosition start = toNodeAbsolute(tile);
        return new Rectangle2D(start.getX(), start.getY(), GROUND_NODE_ABSOLUTE_LENGTH, GROUND_NODE_ABSOLUTE_LENGTH);
    }

    static Rectangle2D toAbsoluteNodeRectangle(Rectangle node) {
        return new Rectangle2D(toNodeAbsolute(node.getStart()), toNodeAbsolute(node.getEnd()));
    }

    static DecimalPosition toAbsoluteMiddle(Index node) {
        return toNodeAbsolute(node).add(GROUND_NODE_ABSOLUTE_LENGTH / 2.0, GROUND_NODE_ABSOLUTE_LENGTH / 2.0);
    }

    static Index nodeToTile(Index nodeIndex) {
        return nodeIndex.scaleInverse(TERRAIN_TILE_NODES_COUNT);
    }

    static int nodeToTile(int nodeIndex) {
        return nodeIndex / TERRAIN_TILE_NODES_COUNT;
    }

    static Index tileToNode(Index tileIndex) {
        return tileIndex.scale(TERRAIN_TILE_NODES_COUNT);
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
