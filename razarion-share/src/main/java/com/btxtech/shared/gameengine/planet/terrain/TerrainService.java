package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.MapCollection;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Triangle2d;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.SurfaceType;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.PlanetActivationEvent;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleContainer;
import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

    public double getHighestZInRegion(DecimalPosition center, double radius) {
        List<Index> indices = obstacleContainer.absoluteCircleToNodes(new Circle2D(center, radius));
        DoubleStream.Builder doubleStreamBuilder = DoubleStream.builder();
        for (Index nodeIndex : indices) {
            doubleStreamBuilder.add(faceMaxZ(nodeIndex));
        }
        return doubleStreamBuilder.build().max().orElseThrow(IllegalStateException::new);
    }


    public double faceMaxZ(Index nodeIndex) {
        return Math.max(Math.max(getGroundZ(nodeIndex), getGroundZ(nodeIndex.add(1, 0))), Math.max(getGroundZ(nodeIndex.add(1, 1)), getGroundZ(nodeIndex.add(0, 1))));
    }

    public double getGroundZ(Index nodeIndex) {
        return terrainTypeService.getGroundSkeletonConfig().getHeight(nodeIndex.getX(), nodeIndex.getY());
    }


    public double getInterpolatedZ(DecimalPosition absolutePosition) {
        Index bottomLeft = TerrainUtil.toNode(absolutePosition);
        DecimalPosition offset = absolutePosition.divide(TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH).sub(new DecimalPosition(bottomLeft));

        Triangle2d triangle1 = new Triangle2d(new DecimalPosition(0, 0), new DecimalPosition(1, 0), new DecimalPosition(0, 1));
        double zBR = getGroundZ(bottomLeft.add(1, 0));
        double zTL = getGroundZ(bottomLeft.add(0, 1));
        if (triangle1.isInside(offset)) {
            Vertex weight = triangle1.interpolate(offset);
            double zBL = getGroundZ(bottomLeft);
            return weight.getX() * zBL + weight.getY() * zBR + weight.getZ() * zTL;
        } else {
            Triangle2d triangle2 = new Triangle2d(new DecimalPosition(1, 0), new DecimalPosition(1, 1), new DecimalPosition(0, 1));
            Vertex weight = triangle2.interpolate(offset);
            double zTR = getGroundZ(bottomLeft.add(1, 1));
            return weight.getX() * zBR + weight.getY() * zTR + weight.getZ() * zTL;
        }
    }

    public Vertex getNorm(DecimalPosition absolutePosition) {
        Index bottomLeft = TerrainUtil.toNode(absolutePosition);
        DecimalPosition offset = absolutePosition.divide(TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH).sub(new DecimalPosition(bottomLeft));

        Triangle2d triangle1 = new Triangle2d(new DecimalPosition(0, 0), new DecimalPosition(1, 0), new DecimalPosition(0, 1));
        double zBR = getGroundZ(bottomLeft.add(1, 0));
        double zTL = getGroundZ(bottomLeft.add(0, 1));
        if (triangle1.isInside(offset)) {
            double zBL = getGroundZ(bottomLeft);
            return new Vertex(zBL - zBR, zBL - zTL, TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH).normalize(1.0);
        } else {
            double zTR = getGroundZ(bottomLeft.add(1, 1));
            return new Vertex(zBR - zTR, zTL - zTR, TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH).normalize(1.0);
        }
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
