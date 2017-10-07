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
    int TERRAIN_NODE_ABSOLUTE_LENGTH = 8;
    int TERRAIN_TILE_NODES_COUNT = 20;
    int TERRAIN_TILE_TOTAL_NODES_COUNT = TERRAIN_TILE_NODES_COUNT * TERRAIN_TILE_NODES_COUNT;
    double TERRAIN_TILE_ABSOLUTE_LENGTH = TERRAIN_NODE_ABSOLUTE_LENGTH * TERRAIN_TILE_NODES_COUNT;
    int MAX_SUB_NODE_DEPTH = 2;
    double MIN_SUB_NODE_LENGTH = calculateSubNodeLength(MAX_SUB_NODE_DEPTH);
    int TOTAL_MIN_SUB_NODE_COUNT = (int) (TERRAIN_NODE_ABSOLUTE_LENGTH / MIN_SUB_NODE_LENGTH);

    static Index toTile(DecimalPosition absolute) {
        return absolute.divide(TERRAIN_TILE_ABSOLUTE_LENGTH).toIndexFloor();
    }

    static DecimalPosition toTileAbsolute(Index tile) {
        return new DecimalPosition(tile.scale(TERRAIN_TILE_ABSOLUTE_LENGTH));
    }

    static Rectangle2D toTileAbsolute(Rectangle tile) {
        return new Rectangle2D(toTileAbsolute(tile.getStart()), toTileAbsolute(tile.getEnd()));
    }

    static Rectangle2D toAbsoluteTileRectangle(Index tile) {
        DecimalPosition start = toTileAbsolute(tile);
        return new Rectangle2D(start.getX(), start.getY(), TERRAIN_TILE_ABSOLUTE_LENGTH, TERRAIN_TILE_ABSOLUTE_LENGTH);
    }

    static Rectangle2D toAbsoluteTileRectangle(DecimalPosition absolute) {
        Index tileIndex = toTile(absolute);
        return toAbsoluteTileRectangle(tileIndex);
    }

    static Index toNode(DecimalPosition absolute) {
        return absolute.divide(TERRAIN_NODE_ABSOLUTE_LENGTH).toIndexFloor();
    }

    static DecimalPosition toNodeAbsolute(Index node) {
        return new DecimalPosition(node.scale(TERRAIN_NODE_ABSOLUTE_LENGTH));
    }

    static DecimalPosition toNodeAbsolute(DecimalPosition node) {
        return node.divide(TERRAIN_NODE_ABSOLUTE_LENGTH);
    }

    static Index toNodeAbsoluteIndex(Index node) {
        return node.scale(TERRAIN_NODE_ABSOLUTE_LENGTH);
    }

    static Rectangle2D toAbsoluteNodeRectangle(Index node) {
        DecimalPosition start = toNodeAbsolute(node);
        return new Rectangle2D(start.getX(), start.getY(), TERRAIN_NODE_ABSOLUTE_LENGTH, TERRAIN_NODE_ABSOLUTE_LENGTH);
    }

    static Rectangle2D toAbsoluteNodeRectangle(Rectangle node) {
        return new Rectangle2D(toNodeAbsolute(node.getStart()), toNodeAbsolute(node.getEnd()));
    }

    static DecimalPosition toAbsoluteNodeCenter(Index node) {
        return toNodeAbsolute(node).add(TERRAIN_NODE_ABSOLUTE_LENGTH / 2.0, TERRAIN_NODE_ABSOLUTE_LENGTH / 2.0);
    }

    static Index nodeToTile(Index nodeIndex) {
        return nodeIndex.divide(TERRAIN_TILE_NODES_COUNT).toIndexFloor();
    }

    static int nodeToTile(int nodeIndex) {
        return (int) Math.floor((double) nodeIndex / (double) TERRAIN_TILE_NODES_COUNT);
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

    /**
     * Absolute length of a sub node. Dept 0 is topmost
     *
     * @param depth current dept start with 0
     * @return absolute length
     */
    static double calculateSubNodeLength(int depth) {
        return TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH / (double) (1 << depth + 1);
    }

    /**
     * The index of the smallest possible sub node.
     * At the moment, the smallest sub node is 1, therefore just the integer value of the position is used
     *
     * @param terrainAbsolute absolute terrain position
     * @return sub node index
     */
    static Index smallestSubNodeIndex(DecimalPosition terrainAbsolute) {
        return terrainAbsolute.toIndexFloor();
    }

    static DecimalPosition smallestSubNodeAbsolute(Index subNodeIndex) {
        return new DecimalPosition(subNodeIndex);
    }

    static DecimalPosition smallestSubNodeCenter(Index subNodeIndex) {
        return smallestSubNodeAbsolute(subNodeIndex).add(0.5, 0.5);
    }

    static Index smallestSubNodeToNode(Index subNodeIndex) {
        return subNodeIndex.scaleInverse(TERRAIN_NODE_ABSOLUTE_LENGTH);
    }

    static DecimalPosition toSubNodeAbsolute(DecimalPosition terrainAbsolute, int depth) {
        double length = calculateSubNodeLength(depth);
        return terrainAbsolute.divide(length).toIndexFloor().multiply(length);
    }
}
