package com.btxtech.webglemulator.razarion;

import com.btxtech.persistence.JsonProviderEmulator;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeTerrainShapeAccess;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeTerrainShape;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.function.Consumer;

/**
 * Created by Beat
 * on 29.06.2017.
 */
@Singleton
public class DevToolNativeTerrainShapeAccess implements NativeTerrainShapeAccess {
    @Inject
    private JsonProviderEmulator jsonProviderEmulator;

    @Override
    public void load(int planetId, Consumer<NativeTerrainShape> loadedCallback, Consumer<String> failCallback) {
        loadedCallback.accept(jsonProviderEmulator.nativeTerrainShapeServer(planetId));
    }
}
