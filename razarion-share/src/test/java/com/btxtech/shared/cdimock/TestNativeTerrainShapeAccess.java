package com.btxtech.shared.cdimock;

import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeAccess;

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
    private NativeTerrainShapeAccess nativeTerrainShapeAccess;

    @Override
    public void load(int planetId, Consumer<NativeTerrainShape> loadedCallback, Consumer<String> failCallback) {
        TerrainShape terrainShape;
        if (terrainSlopePositions != null && terrainObjectPositions != null) {
            terrainShape = new TerrainShape(planetConfig, terrainTypeService, terrainSlopePositions, terrainObjectPositions);
            loadedCallback.accept(terrainShape.toNativeTerrainShape());
        } else if (nativeTerrainShapeAccess != null) {
            terrainShape = new TerrainShape();
            terrainShape.lazyInit(planetConfig, terrainTypeService, nativeTerrainShapeAccess, () -> {
                loadedCallback.accept(terrainShape.toNativeTerrainShape());
            }, failCallback);
        } else {
            throw new RuntimeException("++++++++++ Unexpected ++++++++++");
        }
    }

    public void setPlanetConfig(PlanetConfig planetConfig) {
        this.planetConfig = planetConfig;
    }

    public void setTerrainSlopeAndObjectPositions(List<TerrainSlopePosition> terrainSlopePositions, List<TerrainObjectPosition> terrainObjectPositions) {
        this.terrainSlopePositions = terrainSlopePositions;
        if (this.terrainSlopePositions == null) {
            this.terrainSlopePositions = new ArrayList<>();
        }
        this.terrainObjectPositions = terrainObjectPositions;
        if (this.terrainObjectPositions == null) {
            this.terrainObjectPositions = new ArrayList<>();
        }
        nativeTerrainShapeAccess = null;
    }

    public void setNativeTerrainShapeAccess(NativeTerrainShape nativeTerrainShape) {
        this.nativeTerrainShapeAccess = new NativeTerrainShapeAccess() {
            @Override
            public void load(int planetId, Consumer<NativeTerrainShape> loadedCallback, Consumer<String> failCallback) {
                loadedCallback.accept(nativeTerrainShape);
            }
        };
        terrainSlopePositions = null;
        terrainObjectPositions = null;
    }
}
