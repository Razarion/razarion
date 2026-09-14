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
    void load(int planetId, int tileXCount, int tileYCount, Consumer<NativeTerrainShape> loadedCallback, Consumer<String> failCallback);

    Uint16ArrayEmu createTileGroundHeightMap(Index terrainTileIndex);

    int getGroundHeightAt(int index);
}
