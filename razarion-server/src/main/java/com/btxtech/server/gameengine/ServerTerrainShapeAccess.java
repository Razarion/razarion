package com.btxtech.server.gameengine;

import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeAccess;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeTerrainShape;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.function.Consumer;

/**
 * Created by Beat
 * on 28.06.2017.
 */
@Singleton
public class ServerTerrainShapeAccess implements TerrainShapeAccess {
    @Inject
    private TerrainShapeService terrainShapeService;

    @Override
    public void load(int planetId, Consumer<NativeTerrainShape> loadedCallback, Consumer<String> failCallback) {
        loadedCallback.accept(terrainShapeService.getNativeTerrainShape(planetId));
    }
}