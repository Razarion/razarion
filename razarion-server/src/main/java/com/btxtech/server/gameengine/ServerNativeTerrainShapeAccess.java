package com.btxtech.server.gameengine;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Uint16ArrayEmu;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeAccess;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShape;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.function.Consumer;

/**
 * Created by Beat
 * on 28.06.2017.
 */
@Singleton
public class ServerNativeTerrainShapeAccess implements NativeTerrainShapeAccess {
    @Inject
    private ServerTerrainShapeService serverTerrainShapeService;

    @Override
    public void load(int planetId, Consumer<NativeTerrainShape> loadedCallback, Consumer<String> failCallback) {
        loadedCallback.accept(serverTerrainShapeService.getNativeTerrainShape(planetId));
    }

    @Override
    public Uint16ArrayEmu createGroundHeightMap(Index terrainTileIndex) {
        throw new UnsupportedOperationException();
    }
}
