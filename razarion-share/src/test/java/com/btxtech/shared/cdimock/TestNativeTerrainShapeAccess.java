package com.btxtech.shared.cdimock;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Uint16ArrayEmu;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeManager;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeAccess;
import com.btxtech.shared.system.alarm.AlarmService;

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
    @Inject
    private AlarmService alarmService;
    private PlanetConfig planetConfig;
    private List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
    private List<TerrainObjectPosition> terrainObjectPositions = new ArrayList<>();
    private NativeTerrainShapeAccess nativeTerrainShapeAccess;

    @Override
    public void load(int planetId, Consumer<NativeTerrainShape> loadedCallback, Consumer<String> failCallback) {
        TerrainShapeManager terrainShape;
        if (terrainSlopePositions != null && terrainObjectPositions != null) {
            terrainShape = new TerrainShapeManager(planetConfig, terrainTypeService, alarmService, terrainSlopePositions, terrainObjectPositions);
            loadedCallback.accept(terrainShape.toNativeTerrainShape());
        } else if (nativeTerrainShapeAccess != null) {
            terrainShape = new TerrainShapeManager();
            terrainShape.lazyInit(planetConfig, nativeTerrainShapeAccess, () -> {
                loadedCallback.accept(terrainShape.toNativeTerrainShape());
            }, failCallback);
        } else {
            throw new RuntimeException("++++++++++ Unexpected ++++++++++");
        }
    }

    @Override
    public Uint16ArrayEmu createGroundHeightMap(Index terrainTileIndex) {
        throw new UnsupportedOperationException();
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

            @Override
            public Uint16ArrayEmu createGroundHeightMap(Index terrainTileIndex) {
                throw new UnsupportedOperationException();
            }
        };
        terrainSlopePositions = null;
        terrainObjectPositions = null;
    }
}
