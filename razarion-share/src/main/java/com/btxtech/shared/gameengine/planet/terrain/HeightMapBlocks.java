package com.btxtech.shared.gameengine.planet.terrain;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;

/**
 * The rectangles the rest of a height map is fetched in, behind a running game.
 * <p>
 * Fixed blocks of {@link #BLOCK_TILES} by {@link #BLOCK_TILES} tiles, and only those holding a tile
 * that is not flat - the flat table answers the others exactly. The client works them out from the
 * flat table, the server from the map itself, and both have to name the same rectangles: the server
 * encodes a rectangle on first request (seconds of gzip for a big one) and warms exactly these.
 * <p>
 * Blocks rather than one rectangle around the content, because on planet 117 the non-flat tiles
 * reach every edge - the outermost row and column - so that rectangle was the whole planet: 52 MB in
 * one buffer and 52 MB of tiles, while the game was already running, and a phone failed it (Array
 * buffer allocation failed, PROD 2026-10-17). A block is at most 3.3 MB decoded and is fetched one
 * after another.
 */
public final class HeightMapBlocks {
    public static final int BLOCK_TILES = 8;

    private HeightMapBlocks() {
    }

    /**
     * @param flat whether the tile with this index (y * tileXCount + x) is flat
     * @return {x, y, countX, countY} per block, row by row
     */
    public static List<int[]> contentBlocks(int tileXCount, int tileYCount, IntPredicate flat) {
        List<int[]> blocks = new ArrayList<>();
        for (int blockY = 0; blockY < tileYCount; blockY += BLOCK_TILES) {
            for (int blockX = 0; blockX < tileXCount; blockX += BLOCK_TILES) {
                int countX = Math.min(BLOCK_TILES, tileXCount - blockX);
                int countY = Math.min(BLOCK_TILES, tileYCount - blockY);
                if (hasContent(tileXCount, blockX, blockY, countX, countY, flat)) {
                    blocks.add(new int[]{blockX, blockY, countX, countY});
                }
            }
        }
        return blocks;
    }

    private static boolean hasContent(int tileXCount, int blockX, int blockY, int countX, int countY, IntPredicate flat) {
        for (int y = blockY; y < blockY + countY; y++) {
            for (int x = blockX; x < blockX + countX; x++) {
                if (!flat.test(y * tileXCount + x)) {
                    return true;
                }
            }
        }
        return false;
    }
}
