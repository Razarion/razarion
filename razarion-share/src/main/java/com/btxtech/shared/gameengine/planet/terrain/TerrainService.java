package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainAnalyzer;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeManager;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeAccess;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
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

    /**
     * Computes the per-node {@link com.btxtech.shared.gameengine.planet.terrain.container.TerrainType}
     * ordinals for a whole tile (row-major NODE_Y_COUNT * NODE_X_COUNT). This is the authoritative
     * passability (it accounts for blocking terrain objects and reads heights across tile borders) and
     * is requested on demand by the editor terrain-type overlay - it is NOT computed during normal tile
     * generation, so regular gameplay pays no cost.
     */
    public int[] generateTerrainTypeOrdinals(Index terrainTileIndex) {
        Index nodeBase = TerrainUtil.tileIndexToNodeIndex(terrainTileIndex);
        TerrainAnalyzer terrainAnalyzer = terrainShape.getTerrainAnalyzer();
        int[] ordinals = new int[TerrainUtil.NODE_X_COUNT * TerrainUtil.NODE_Y_COUNT];
        for (int ly = 0; ly < TerrainUtil.NODE_Y_COUNT; ly++) {
            for (int lx = 0; lx < TerrainUtil.NODE_X_COUNT; lx++) {
                ordinals[ly * TerrainUtil.NODE_X_COUNT + lx] =
                        terrainAnalyzer.getTerrainType(nodeBase.add(lx, ly)).ordinal();
            }
        }
        return ordinals;
    }

    public TerrainAnalyzer getTerrainAnalyzer() {
        return terrainShape.getTerrainAnalyzer();
    }

    public TerrainShapeManager getTerrainShape() {
        return terrainShape;
    }
}
