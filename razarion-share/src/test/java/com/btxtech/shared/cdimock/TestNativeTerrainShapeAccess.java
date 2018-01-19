package com.btxtech.shared.cdimock;

import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeTerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeTerrainShapeAccess;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Beat
 * on 21.08.2017.
 */
@Singleton
public class TestNativeTerrainShapeAccess implements NativeTerrainShapeAccess {
    @Inject
    private TerrainTypeService terrainTypeService;
    private PlanetConfig planetConfig;
    private List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
    private List<TerrainObjectPosition> terrainObjectPositions = new ArrayList<>();

    @Override
    public void load(int planetId, Consumer<NativeTerrainShape> loadedCallback, Consumer<String> failCallback) {
        TerrainShape terrainShape = new TerrainShape(planetConfig, terrainTypeService, terrainSlopePositions, terrainObjectPositions);
        loadedCallback.accept(terrainShape.toNativeTerrainShape());
    }

    public void setPlanetConfig(PlanetConfig planetConfig) {
        this.planetConfig = planetConfig;
    }

    public void setTerrainSlopePositions(List<TerrainSlopePosition> terrainSlopePositions) {
        this.terrainSlopePositions = terrainSlopePositions;
    }

    public void setTerrainObjectPositions(List<TerrainObjectPosition> terrainObjectPositions) {
        this.terrainObjectPositions = terrainObjectPositions;
    }
}
