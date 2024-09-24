package com.btxtech.uiservice.cdimock;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Uint16ArrayEmu;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeAccess;

import javax.inject.Singleton;
import java.util.function.Consumer;

/**
 * Created by Beat
 * on 09.11.2017.
 */
@Singleton
public class TestNativeTerrainShapeAccess implements NativeTerrainShapeAccess {
    @Override
    public void load(int planetId, Consumer<NativeTerrainShape> loadedCallback, Consumer<String> failCallback) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Uint16ArrayEmu createTileGroundHeightMap(Index terrainTileIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getGroundHeightAt(int index) {
        throw new UnsupportedOperationException();
    }
}
