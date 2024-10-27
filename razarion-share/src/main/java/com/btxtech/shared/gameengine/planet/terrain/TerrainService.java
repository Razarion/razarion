package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainAnalyzer;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeManager;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeAccess;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.function.Consumer;

/**
 * Created by Beat
 * 13.07.2016.
 */
@Singleton
public class TerrainService {
    // private Logger logger = Logger.getLogger(TerrainService.class.getName());
    private final TerrainTypeService terrainTypeService;
    private final TerrainTileFactory terrainTileFactory;
    private final Provider<NativeTerrainShapeAccess> nativeTerrainShapeAccess;
    private TerrainShapeManager terrainShape;
    private PlanetConfig planetConfig;

    @Inject
    public TerrainService(Provider<NativeTerrainShapeAccess> nativeTerrainShapeAccess, TerrainTileFactory terrainTileFactory, TerrainTypeService terrainTypeService) {
        this.nativeTerrainShapeAccess = nativeTerrainShapeAccess;
        this.terrainTileFactory = terrainTileFactory;
        this.terrainTypeService = terrainTypeService;
    }

    public void setup(PlanetConfig planetConfig, Runnable finishCallback, Consumer<String> failCallback) {
        this.planetConfig = planetConfig;
        setup(finishCallback, failCallback);
    }

    public void clean() {
        terrainShape = null;
    }

    private void setup(Runnable finishCallback, Consumer<String> failCallback) {
        terrainShape = new TerrainShapeManager(terrainTypeService, nativeTerrainShapeAccess.get());
        terrainShape.lazyInit(planetConfig, finishCallback, failCallback);
    }

    public PlanetConfig getPlanetConfig() {
        return planetConfig;
    }

    public TerrainTile generateTerrainTile(Index terrainTileIndex) {
        return terrainTileFactory.generateTerrainTile(terrainTileIndex, terrainShape, planetConfig);
    }

    public TerrainAnalyzer getPathingAccess() {
        return terrainShape.getTerrainAnalyzer();
    }

    public TerrainShapeManager getTerrainShape() {
        return terrainShape;
    }
}
