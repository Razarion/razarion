package com.btxtech.uiservice.cdimock;

import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeAccess;

import javax.enterprise.context.ApplicationScoped;
import java.util.function.Consumer;

/**
 * Created by Beat
 * on 09.11.2017.
 */
@ApplicationScoped
public class TestNativeTerrainShapeAccess implements NativeTerrainShapeAccess {
    @Override
    public void load(int planetId, Consumer<NativeTerrainShape> loadedCallback, Consumer<String> failCallback) {
        throw new UnsupportedOperationException();
    }
}
