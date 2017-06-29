package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeTerrainShape;

import java.util.function.Consumer;

/**
 * Created by Beat
 * on 27.06.2017.
 */
public interface TerrainShapeAccess {
    void load(int planetId, Consumer<NativeTerrainShape> loadedCallback, Consumer<String> failCallback);
}
