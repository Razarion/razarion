package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.SurfaceType;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeTerrainShapeAccess;
import com.btxtech.shared.gameengine.planet.terrain.container.PathingAccess;
import com.btxtech.shared.gameengine.planet.terrain.container.SurfaceAccess;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShape;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 13.07.2016.
 */
@Singleton
public class TerrainService {
    private Logger logger = Logger.getLogger(TerrainService.class.getName());
    @Inject
    private TerrainTypeService terrainTypeService;
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private TerrainTileFactory terrainTileFactory;
    @Inject
    private NativeTerrainShapeAccess nativeTerrainShapeAccess;
    private TerrainShape terrainShape;
    private PlanetConfig planetConfig;

    public void setup(PlanetConfig planetConfig, Runnable finishCallback, Consumer<String> failCallback) {
        this.planetConfig = planetConfig;
        setup(finishCallback, failCallback);
    }

    public void clean() {
        terrainShape = null;
    }

    public void setup(Runnable finishCallback, Consumer<String> failCallback) {
        terrainShape = new TerrainShape();
        terrainShape.lazyInit(planetConfig, terrainTypeService, nativeTerrainShapeAccess, finishCallback, failCallback);
    }

    public PlanetConfig getPlanetConfig() {
        return planetConfig;
    }

    public TerrainTile generateTerrainTile(Index terrainTileIndex) {
        return terrainTileFactory.generateTerrainTile(terrainTileIndex, terrainShape);
    }

    public boolean hasSurfaceTypeInRegion(SurfaceType surfaceType, Rectangle absRectangle) {
        throw new UnsupportedOperationException();
    }

    public PathingAccess getPathingAccess() {
        return terrainShape.getPathingAccess();
    }

    public SurfaceAccess getSurfaceAccess() {
        return terrainShape.getSurfaceAccess();
    }
}
