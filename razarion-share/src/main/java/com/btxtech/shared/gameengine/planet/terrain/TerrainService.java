package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
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
import java.util.logging.Logger;

/**
 * Created by Beat
 * 13.07.2016.
 */
@Singleton
public class TerrainService {
    private final Logger logger = Logger.getLogger(TerrainService.class.getName());
    private final TerrainTypeService terrainTypeService;
    private final TerrainTileFactory terrainTileFactory;
    private final Provider<NativeTerrainShapeAccess> nativeTerrainShapeAccess;
    private TerrainShapeManager terrainShape;
    private PlanetConfig planetConfig;
    /** Where the player will be looking, or null. Decides which tiles must be there to start. */
    private DecimalPosition startPosition;
    /** False while the game runs on part of the height map - see onHeightMapPartial. */
    private boolean heightMapComplete = true;
    private Runnable heightMapCompleteListener;
    /** Counts the completions, so a cache built on heights can tell that it is out of date. */
    private int heightMapGeneration;

    @Inject
    public TerrainService(Provider<NativeTerrainShapeAccess> nativeTerrainShapeAccess, TerrainTileFactory terrainTileFactory, TerrainTypeService terrainTypeService) {
        this.nativeTerrainShapeAccess = nativeTerrainShapeAccess;
        this.terrainTileFactory = terrainTileFactory;
        this.terrainTypeService = terrainTypeService;
    }

    public void setup(PlanetConfig planetConfig, DecimalPosition startPosition, Runnable finishCallback, Consumer<String> failCallback) {
        this.planetConfig = planetConfig;
        this.startPosition = startPosition;
        setup(finishCallback, failCallback);
    }

    public void clean() {
        terrainShape = null;
    }

    /**
     * Called by a height map loader that let the game start on the player's corner of the map and
     * fetches the rest behind it. Until {@link #onHeightMapComplete} the far tiles answer stand-in
     * heights - see TeaVMNativeTerrainShapeAccess.
     */
    public void onHeightMapPartial() {
        heightMapComplete = false;
    }

    /**
     * The rest of the height map is in. Everything that was worked out from stand-in heights while it
     * was missing and then kept is worked out again: the terrain type per node here, and whatever the
     * listener holds (the pathing grid, the heights of resources and boxes the client was given).
     */
    public void onHeightMapComplete() {
        heightMapComplete = true;
        heightMapGeneration++;
        if (terrainShape != null && terrainShape.getTerrainAnalyzer() != null) {
            terrainShape.getTerrainAnalyzer().clearTerrainTypeCache();
        }
        if (heightMapCompleteListener != null) {
            heightMapCompleteListener.run();
        }
    }

    public int getHeightMapGeneration() {
        return heightMapGeneration;
    }

    public boolean isHeightMapComplete() {
        return heightMapComplete;
    }

    public void setHeightMapCompleteListener(Runnable heightMapCompleteListener) {
        this.heightMapCompleteListener = heightMapCompleteListener;
    }

    /**
     * Whether the tile may be built for the renderer now. A tile built from stand-in heights would be
     * drawn the wrong shape and kept that way, so while the map is partial the caller waits.
     */
    public boolean isTileDrawable(Index terrainTileIndex) {
        return heightMapComplete || nativeTerrainShapeAccess.get().isTileDrawable(terrainTileIndex);
    }

    private void setup(Runnable finishCallback, Consumer<String> failCallback) {
        heightMapComplete = true;
        terrainShape = new TerrainShapeManager(terrainTypeService, nativeTerrainShapeAccess.get());
        terrainShape.lazyInit(planetConfig, startPosition, finishCallback, failCallback);
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
        long time = System.currentTimeMillis();
        int[] ordinals = terrainShape.getTerrainAnalyzer().generateTerrainTypeOrdinals(terrainTileIndex);
        logger.info("generateTerrainTypeOrdinals " + terrainTileIndex + ": " + (System.currentTimeMillis() - time) + "ms");
        return ordinals;
    }

    public TerrainAnalyzer getTerrainAnalyzer() {
        return terrainShape.getTerrainAnalyzer();
    }

    public TerrainShapeManager getTerrainShape() {
        return terrainShape;
    }
}
