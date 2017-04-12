package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.InterpolatedTerrainTriangle;
import com.btxtech.shared.datatypes.MapCollection;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.SurfaceType;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.PlanetActivationEvent;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleContainer;
import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;
import java.util.stream.DoubleStream;

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
    private ObstacleContainer obstacleContainer;
    @Inject
    private TerrainTileFactory terrainTileFactory;
    private PlanetConfig planetConfig;

    public void onPlanetActivation(@Observes PlanetActivationEvent planetActivationEvent) {
        this.planetConfig = planetActivationEvent.getPlanetConfig();
        MapCollection<TerrainObjectConfig, TerrainObjectPosition> terrainObjectConfigPositions = generateTerrainObjects(planetActivationEvent.getPlanetConfig());
        Collection<Slope> slopes = generatesSlopes();
        obstacleContainer.setup(planetActivationEvent.getPlanetConfig().getGroundMeshDimension(), slopes, terrainObjectConfigPositions);
    }

    public PlanetConfig getPlanetConfig() {
        return planetConfig;
    }

    private MapCollection<TerrainObjectConfig, TerrainObjectPosition> generateTerrainObjects(PlanetConfig planetConfig) {
        long time = System.currentTimeMillis();

        MapCollection<TerrainObjectConfig, TerrainObjectPosition> terrainObjectConfigPositions = new MapCollection<>();
        if (planetConfig.getTerrainObjectPositions() != null) {
            for (TerrainObjectPosition objectPosition : planetConfig.getTerrainObjectPositions()) {
                TerrainObjectConfig terrainObjectConfig = terrainTypeService.getTerrainObjectConfig(objectPosition.getTerrainObjectId());
                terrainObjectConfigPositions.put(terrainObjectConfig, objectPosition);
            }
        }

        logger.severe("Generate Terrain Objects: " + (System.currentTimeMillis() - time));
        return terrainObjectConfigPositions;
    }

    public Collection<Slope> generatesSlopes() {
        long time = System.currentTimeMillis();
        Collection<Slope> slopes = new ArrayList<>();
        if (planetConfig.getTerrainSlopePositions() != null) {
            for (TerrainSlopePosition terrainSlopePosition : planetConfig.getTerrainSlopePositions()) {
                SlopeSkeletonConfig slopeSkeletonConfig = terrainTypeService.getSlopeSkeleton(terrainSlopePosition.getSlopeConfigEntity());
                slopes.add(new Slope(terrainSlopePosition.getSlopeConfigEntity(), slopeSkeletonConfig, terrainSlopePosition.getPolygon()));
            }
        }
        logger.severe("Generate Slopes: " + (System.currentTimeMillis() - time));
        return slopes;
    }

    public TerrainTile generateTerrainTile(Index terrainTileIndex) {
        return terrainTileFactory.generateTerrainTile(terrainTileIndex);
    }

    public boolean overlap(DecimalPosition position) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public boolean overlap(DecimalPosition position, double radius) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public boolean overlap(Collection<DecimalPosition> positions, int baseItemTypeId) {
        BaseItemType baseItemType = itemTypeService.getBaseItemType(baseItemTypeId);
        for (DecimalPosition position : positions) {
            if (overlap(position, baseItemType.getPhysicalAreaConfig().getRadius())) {
                return true;
            }
        }
        return false;
    }

    public double getHighestZInRegion(DecimalPosition center, double radius) {
        DoubleStream.Builder doubleStreamBuilder = DoubleStream.builder();


        double startX = Math.floor((center.getX() - radius) / (double) TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH) * (double) TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH;
        double startY = Math.floor((center.getY() - radius) / (double) TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH) * (double) TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH;
        double endX = Math.ceil((center.getX() + radius) / (double) TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH) * (double) TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH;
        double endY = Math.ceil((center.getY() + radius) / (double) TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH) * (double) TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH;

        for (double x = startX; x < endX; x += (double) TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH) {
            for (double y = startY; y < endY; y += (double) TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH) {
                Rectangle2D rect = new Rectangle2D(x, y, (double) TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH, (double) TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH);
                if (rect.contains(center)) {
                    doubleStreamBuilder.add(faceMaxZ(x, y));
                } else {
                    DecimalPosition projection = rect.getNearestPoint(center);
                    if (projection.getDistance(center) <= radius) {
                        doubleStreamBuilder.add(faceMaxZ(x, y));
                    }
                }
            }
        }

        return doubleStreamBuilder.build().max().orElseThrow(IllegalStateException::new);
    }

    private double faceMaxZ(double x, double y) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public InterpolatedTerrainTriangle getInterpolatedTerrainTriangle(DecimalPosition absoluteXY) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    // -------------------------------------------------
    // TODO TerrainSettings getTerrainSettings();

    // TODO void addTerrainListener(TerrainListener terrainListener);

    // TODO boolean overlap(Index middlePoint, int radius, Collection<SurfaceType> allowedSurfaces, SurfaceType adjoinSurface, TerrainType builderTerrainType, Integer maxAdjoinDistance);

    public boolean hasSurfaceTypeInRegion(SurfaceType surfaceType, Rectangle absRectangle) {
        throw new UnsupportedOperationException();
    }

    // TODO SurfaceType getSurfaceType(Index tileIndex);

    // TODO SurfaceType getSurfaceTypeAbsolute(Index absoluteIndex);

    // TODO Index correctPosition(SyncItem syncItem, Index position);

    // TODO Index correctPosition(int radius, Index position);

    // TODO void createTerrainTileField(Collection<TerrainImagePosition> terrainImagePositions, Collection<SurfaceRect> surfaceRects);

    // TODO TerrainTile[][] getTerrainTileField();

    // TODO  void iteratorOverAllTerrainTiles(Rectangle tileRect, TerrainTileEvaluator terrainTileEvaluator);

}
