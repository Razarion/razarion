package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.terrain.BabylonDecal;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeAccess;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeTile;
import com.btxtech.shared.system.alarm.AlarmService;
import com.btxtech.shared.utils.ExceptionUtil;
import com.btxtech.shared.utils.GeometricUtil;

import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 17.06.2017.
 */
public class TerrainShapeManager {
    private final static Logger logger = Logger.getLogger(TerrainShapeManager.class.getName());
    private final NativeTerrainShapeAccess nativeTerrainShapeAccess;
    private final TerrainTypeService terrainTypeService;
    private TerrainShapeTile[][] terrainShapeTiles;
    private PathingAccess pathingAccess;
    private DecimalPosition planetSize;
    private int tileXCount;
    private int tileYCount;

    public TerrainShapeManager(TerrainTypeService terrainTypeService, NativeTerrainShapeAccess nativeTerrainShapeAccess) {
        this.terrainTypeService = terrainTypeService;
        this.nativeTerrainShapeAccess = nativeTerrainShapeAccess;
    }

    public TerrainShapeManager(PlanetConfig planetConfig,
                               TerrainTypeService terrainTypeService,
                               AlarmService alarmService,
                               List<TerrainObjectPosition> terrainObjectPositions,
                               List<BabylonDecal> babylonDecals) {
        this.terrainTypeService = terrainTypeService;
        long time = System.currentTimeMillis();
        this.nativeTerrainShapeAccess = null;
        pathingAccess = new PathingAccess(this);
        setupDimension(planetConfig);
        terrainShapeTiles = new TerrainShapeTile[tileXCount][tileYCount];
        TerrainShapeManagerSetup terrainShapeSetup = new TerrainShapeManagerSetup(this, terrainTypeService, alarmService);
        terrainShapeSetup.processTerrainObject(terrainObjectPositions);
        terrainShapeSetup.processBotDecals(babylonDecals);
        logger.severe("Setup TerrainShape: " + (System.currentTimeMillis() - time) + " for planet config: " + planetConfig.getId());
    }

    public void lazyInit(PlanetConfig planetConfig, Runnable finishCallback, Consumer<String> failCallback) {
        pathingAccess = new PathingAccess(this);
        setupDimension(planetConfig);
        nativeTerrainShapeAccess.load(planetConfig.getId(), nativeTerrainShape -> {
            try {
                // long time = System.currentTimeMillis();
                terrainShapeTiles = new TerrainShapeTile[tileXCount][tileYCount];
                for (int x = 0; x < tileXCount; x++) {
                    for (int y = 0; y < tileYCount; y++) {
                        NativeTerrainShapeTile nativeTerrainShapeTile = nativeTerrainShape.nativeTerrainShapeTiles[x][y];
                        if (nativeTerrainShapeTile != null) {
                            TerrainShapeTile terrainShapeTile = new TerrainShapeTile();
                            terrainShapeTiles[x][y] = terrainShapeTile;
                            terrainShapeTile.setNativeTerrainShapeObjectLists(nativeTerrainShapeTile.nativeTerrainShapeObjectLists);
                            terrainShapeTile.setNativeBabylonDecals(nativeTerrainShapeTile.nativeBabylonDecals);
                        }
                    }
                }
                // logger.severe("Setup TerrainShape Net: " + (System.currentTimeMillis() - time));
                finishCallback.run();
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "NativeTerrainShapeAccess load callback failed", t);
                failCallback.accept(ExceptionUtil.setupStackTrace("NativeTerrainShapeAccess load callback failed", t));
            }
        }, failCallback);
    }

    private void setupDimension(PlanetConfig planetConfig) {
        DecimalPosition planetSize = planetConfig.getSize();
        tileXCount = TerrainUtil.toTileCeil(planetSize).getX();
        tileYCount = TerrainUtil.toTileCeil(planetSize).getY();
    }

    public NativeTerrainShape toNativeTerrainShape() {
        NativeTerrainShape nativeTerrainShape = new NativeTerrainShape();
        nativeTerrainShape.nativeTerrainShapeTiles = new NativeTerrainShapeTile[tileXCount][tileYCount];
        for (int x = 0; x < tileXCount; x++) {
            for (int y = 0; y < tileYCount; y++) {
                TerrainShapeTile terrainShapeTile = terrainShapeTiles[x][y];
                if (terrainShapeTile != null) {
                    nativeTerrainShape.nativeTerrainShapeTiles[x][y] = terrainShapeTile.toNativeTerrainShapeTile();
                }
            }
        }
        return nativeTerrainShape;
    }

    public int getTileXCount() {
        return tileXCount;
    }

    public int getTileYCount() {
        return tileYCount;
    }

    public TerrainShapeTile getTerrainShapeTile(Index terrainTileIndex) {
        if (terrainTileIndex.getX() < 0 || terrainTileIndex.getY() < 0 || terrainTileIndex.getX() >= tileXCount || terrainTileIndex.getY() >= tileYCount) {
            return null;
        }
        return terrainShapeTiles[terrainTileIndex.getX()][terrainTileIndex.getY()];
    }

    public TerrainShapeTile getOrCreateTerrainShapeTile(Index terrainTileIndex) {
        TerrainShapeTile terrainShapeTile = getTerrainShapeTile(terrainTileIndex);
        if (terrainShapeTile == null) {
            terrainShapeTile = createTerrainShapeTile(terrainTileIndex);
        }
        return terrainShapeTile;
    }

    private TerrainShapeTile createTerrainShapeTile(Index terrainTileIndex) {
        if (terrainTileIndex.getX() < 0) {
            throw new IllegalArgumentException("terrainTileIndex X < 0: " + terrainTileIndex);
        }
        if (terrainTileIndex.getY() < 0) {
            throw new IllegalArgumentException("terrainTileIndex Y < 0: " + terrainTileIndex);
        }
        if (terrainTileIndex.getX() >= tileXCount) {
            throw new IllegalArgumentException("terrainTileIndex X >= " + tileXCount + ": " + terrainTileIndex);
        }
        if (terrainTileIndex.getY() >= tileYCount) {
            throw new IllegalArgumentException("terrainTileIndex Y >= " + tileYCount + ": " + terrainTileIndex);
        }
        TerrainShapeTile terrainShapeTile = new TerrainShapeTile();
        terrainShapeTiles[terrainTileIndex.getX()][terrainTileIndex.getY()] = terrainShapeTile;
        return terrainShapeTile;
    }

    public PathingAccess getPathingAccess() {
        return pathingAccess;
    }

    public boolean isSightBlocked(Line line) {
        return GeometricUtil.rasterizeLine(line, (int) TerrainUtil.NODE_X_DISTANCE)
                .stream()
                .map(nodeIndex -> pathingAccess.getTerrainType(nodeIndex))
                .anyMatch(terrainType -> terrainType != TerrainType.LAND);
    }

    public TerrainTypeService getTerrainTypeService() {
        return terrainTypeService;
    }

    public NativeTerrainShapeAccess getNativeTerrainShapeAccess() {
        return nativeTerrainShapeAccess;
    }
}
