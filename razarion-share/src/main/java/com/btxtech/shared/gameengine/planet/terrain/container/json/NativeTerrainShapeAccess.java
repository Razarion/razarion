package com.btxtech.shared.gameengine.planet.terrain.container.json;

import java.util.function.Consumer;

/**
 * Created by Beat
 * on 27.06.2017.
 */
public interface NativeTerrainShapeAccess {
    void load(int planetId, Consumer<NativeTerrainShape> loadedCallback, Consumer<String> failCallback);
}
