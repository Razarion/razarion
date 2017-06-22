package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.MapCollection;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.SurfaceType;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleContainer;
import com.btxtech.shared.gameengine.planet.terrain.container.PathingAccess;
import com.btxtech.shared.gameengine.planet.terrain.container.SurfaceAccess;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
    @Deprecated
    private ObstacleContainer obstacleContainer;
    @Inject
    private TerrainTileFactory terrainTileFactory;
    private TerrainShape terrainShape;
    private PlanetConfig planetConfig;

    public void setup(PlanetConfig planetConfig) {
        this.planetConfig = planetConfig;
        setup(planetConfig.getTerrainSlopePositions(), planetConfig.getTerrainObjectPositions());
    }

    public void clean() {
        obstacleContainer.clear();
    }

    public void override4Editor(List<TerrainSlopePosition> terrainSlopePositions, List<TerrainObjectPosition> terrainObjectPositions) {
        setup(terrainSlopePositions, terrainObjectPositions);
    }

    private void setup(List<TerrainSlopePosition> terrainSlopePositions, List<TerrainObjectPosition> terrainObjectPositions) {
        MapCollection<TerrainObjectConfig, TerrainObjectPosition> terrainObjectConfigPositions = generateTerrainObjects(terrainObjectPositions);
        Collection<Slope> slopes = generatesSlopes(terrainSlopePositions);
        obstacleContainer.setup(planetConfig.getTerrainTileDimension(), slopes, terrainObjectConfigPositions);
        terrainShape = new TerrainShape(planetConfig, terrainTypeService);
    }

    public PlanetConfig getPlanetConfig() {
        return planetConfig;
    }

    private MapCollection<TerrainObjectConfig, TerrainObjectPosition> generateTerrainObjects(List<TerrainObjectPosition> terrainObjectPositions) {
        long time = System.currentTimeMillis();

        MapCollection<TerrainObjectConfig, TerrainObjectPosition> terrainObjectConfigPositions = new MapCollection<>();
        if (terrainObjectPositions != null) {
            for (TerrainObjectPosition objectPosition : terrainObjectPositions) {
                TerrainObjectConfig terrainObjectConfig = terrainTypeService.getTerrainObjectConfig(objectPosition.getTerrainObjectId());
                terrainObjectConfigPositions.put(terrainObjectConfig, objectPosition);
            }
        }

        logger.severe("Generate Terrain Objects: " + (System.currentTimeMillis() - time));
        return terrainObjectConfigPositions;
    }

    public Collection<Slope> generatesSlopes(List<TerrainSlopePosition> terrainSlopePositions) {
        long time = System.currentTimeMillis();
        Collection<Slope> slopes = new ArrayList<>();
        if (terrainSlopePositions != null) {
            for (TerrainSlopePosition terrainSlopePosition : terrainSlopePositions) {
                slopes.add(setupSlope(terrainSlopePosition));
            }
        }
        logger.severe("Generate Slopes: " + (System.currentTimeMillis() - time));
        return slopes;
    }

    private Slope setupSlope(TerrainSlopePosition terrainSlopePosition) {
        SlopeSkeletonConfig slopeSkeletonConfig = terrainTypeService.getSlopeSkeleton(terrainSlopePosition.getSlopeConfigId());
        Slope slope = new Slope(terrainSlopePosition.getId(), slopeSkeletonConfig, terrainSlopePosition.getPolygon());
        setupSlopeChildren(slope, terrainSlopePosition.getChildren());
        return slope;
    }

    private void setupSlopeChildren(Slope slope, List<TerrainSlopePosition> terrainSlopePositions) {
        if (terrainSlopePositions == null || terrainSlopePositions.isEmpty()) {
            return;
        }
        Collection<Slope> children = new ArrayList<>();
        for (TerrainSlopePosition terrainSlopePosition : terrainSlopePositions) {
            children.add(setupSlope(terrainSlopePosition));
        }
        slope.setChildren(children);
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
