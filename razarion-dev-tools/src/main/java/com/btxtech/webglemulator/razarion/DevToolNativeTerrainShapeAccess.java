package com.btxtech.webglemulator.razarion;

import com.btxtech.persistence.JsonProviderEmulator;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeTerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeTerrainShapeAccess;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Beat
 * on 29.06.2017.
 */
@Singleton
public class DevToolNativeTerrainShapeAccess implements NativeTerrainShapeAccess {
    @Inject
    private JsonProviderEmulator jsonProviderEmulator;
    @Inject
    private TerrainTypeService terrainTypeService;
    private List<TerrainSlopePosition> terrainSlopePositions;
    private PlanetConfig planetConfig;

    @Override
    public void load(int planetId, Consumer<NativeTerrainShape> loadedCallback, Consumer<String> failCallback) {
        if (terrainSlopePositions != null && planetConfig != null) {
            TerrainShape terrainShape = new TerrainShape(planetConfig, terrainTypeService, terrainSlopePositions, null);
            loadedCallback.accept(terrainShape.toNativeTerrainShape());
        } else {
            loadedCallback.accept(jsonProviderEmulator.nativeTerrainShapeServer(planetId));
        }
    }

    public void setTerrainSlopePositions(List<TerrainSlopePosition> terrainSlopePositions) {
        this.terrainSlopePositions = terrainSlopePositions;
    }

    public void setPlanetConfig(PlanetConfig planetConfig) {
        this.planetConfig = planetConfig;
    }
}
