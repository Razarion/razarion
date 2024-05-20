package com.btxtech.shared.gameengine.planet.terrain.container.json;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Uint16ArrayEmu;

import java.util.function.Consumer;

/**
 * Created by Beat
 * on 27.06.2017.
 */
public interface NativeTerrainShapeAccess {
    void load(int planetId, Consumer<NativeTerrainShape> loadedCallback, Consumer<String> failCallback);

    Uint16ArrayEmu createGroundHeightMap(Index terrainTileIndex);
}
