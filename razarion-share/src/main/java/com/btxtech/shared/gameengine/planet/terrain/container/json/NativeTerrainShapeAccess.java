package com.btxtech.shared.gameengine.planet.terrain.container.json;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Uint16ArrayEmu;

import java.util.function.Consumer;

/**
 * Created by Beat
 * on 27.06.2017.
 */
public interface NativeTerrainShapeAccess {
    /**
     * @param tileXCount how many tiles the planet is wide, and tileYCount how tall. Handed in
     *                   rather than derived, because an implementation that fetches the height map
     *                   by the tile has to know the grid before it can ask for a rectangle of it -
     *                   and the caller has just computed it from the planet config.
     */
    void load(int planetId, int tileXCount, int tileYCount, Index startTile, Consumer<NativeTerrainShape> loadedCallback, Consumer<String> failCallback);

    Uint16ArrayEmu createTileGroundHeightMap(Index terrainTileIndex);

    int getGroundHeightAt(int index);

    /**
     * Per-node relief of one tile, for the ground shader only: (NODE_X_COUNT + 1) * (NODE_Y_COUNT + 1)
     * values laid out like {@link #createTileGroundHeightMap(Index)}. High byte = sky visibility
     * (255 open, lower = occluded by the ground around it), low byte = curvature (128 flat, lower =
     * hollow, higher = crest). It looks across tile edges, which is why it is made where the whole
     * height map is - a tile on its own would draw seams. Null where nobody renders (server, tests).
     */
    default Uint16ArrayEmu createTileGroundRelief(Index terrainTileIndex) {
        return null;
    }

    /**
     * Whether a terrain tile and its eight neighbours have their real heights. Building a tile reads
     * one row into the tiles beside it and its relief looks further, so a tile is only safe to build
     * when the ring around it is there too. Always true where the whole height map is loaded before
     * the game starts.
     */
    default boolean isTileDrawable(Index terrainTileIndex) {
        return true;
    }
}
