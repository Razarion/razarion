package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.InterpolatedTerrainTriangle;
import com.btxtech.shared.datatypes.MapCollection;
import com.btxtech.shared.datatypes.Ray3d;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.SingleHolder;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.SurfaceType;
import com.btxtech.shared.gameengine.datatypes.TerrainType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemType;
import com.btxtech.shared.gameengine.planet.PlanetActivationEvent;
import com.btxtech.shared.gameengine.planet.pathing.Obstacle;
import com.btxtech.shared.gameengine.planet.terrain.ground.GroundMesh;
import com.btxtech.shared.gameengine.planet.terrain.ground.GroundModeler;
import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;
import com.btxtech.shared.gameengine.planet.terrain.slope.SlopeWater;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 13.07.2016.
 */
@Singleton
public class TerrainService {
    public static final int MESH_NODE_EDGE_LENGTH = 64;
    private Logger logger = Logger.getLogger(TerrainService.class.getName());
    @Inject
    private TerrainTypeService terrainTypeService;
    private Water water;
    private GroundMesh groundMesh;
    private Map<Integer, Slope> slopeMap = new HashMap<>();
    private MapCollection<TerrainObjectConfig, TerrainObjectPosition> terrainObjectConfigPositions;
    private Collection<Obstacle> obstacles;

    public void onPlanetActivation(@Observes PlanetActivationEvent planetActivationEvent) {
        logger.severe("Start setup surface");
        long time = System.currentTimeMillis();
        setupGround(planetActivationEvent.getPlanetConfig().getGroundMeshDimension());

        water = new Water(planetActivationEvent.getPlanetConfig().getWaterLevel());

        slopeMap.clear();
        if (planetActivationEvent.getPlanetConfig().getTerrainSlopePositions() != null) {
            planetActivationEvent.getPlanetConfig().getTerrainSlopePositions().forEach(this::setupPlateau);
        }

        terrainObjectConfigPositions = new MapCollection<>();
        if (planetActivationEvent.getPlanetConfig().getTerrainObjectPositions() != null) {
            for (TerrainObjectPosition objectPosition : planetActivationEvent.getPlanetConfig().getTerrainObjectPositions()) {
                TerrainObjectConfig terrainObjectConfig = terrainTypeService.getTerrainObjectConfig(objectPosition.getTerrainObjectId());
                terrainObjectConfigPositions.put(terrainObjectConfig, objectPosition);
            }
        }

        setupObstacles();

        logger.severe("Setup surface took: " + (System.currentTimeMillis() - time));
    }

    private void setupObstacles() {
        obstacles = new ArrayList<>();
        for (Slope slope : slopeMap.values()) {
            obstacles.addAll(slope.generateObstacles());
        }

        // TODO setup obstacles for terrain objects (circle)
    }

    public Collection<Obstacle> getObstacles() {
        return obstacles;
    }

    public MapCollection<TerrainObjectConfig, TerrainObjectPosition> getTerrainObjectPositions() {
        return terrainObjectConfigPositions;
    }

    private void setupPlateau(TerrainSlopePosition terrainSlopePosition) {
        SlopeSkeletonConfig slopeSkeletonConfig = terrainTypeService.getSlopeSkeleton(terrainSlopePosition.getSlopeId());
        Slope slope;
        if (slopeSkeletonConfig.getType() == SlopeSkeletonConfig.Type.WATER) {
            slope = new SlopeWater(water, slopeSkeletonConfig, terrainSlopePosition.getPolygon());
        } else if (slopeSkeletonConfig.getType() == SlopeSkeletonConfig.Type.LAND) {
            slope = new Slope(slopeSkeletonConfig, terrainSlopePosition.getPolygon());
        } else {
            throw new IllegalStateException("Unknown enum type: " + slopeSkeletonConfig.getType());
        }
        slope.wrap(groundMesh);
        slope.setupGroundConnection(groundMesh);
        slopeMap.put(slopeMap.size(), slope);
    }

    private void setupGround(Rectangle groundMesh) {
        if (terrainTypeService.getGroundSkeletonConfig() != null) {
            this.groundMesh = GroundModeler.generateGroundMesh(terrainTypeService.getGroundSkeletonConfig(), groundMesh);
            this.groundMesh.setupNorms();
        }
    }

    @Deprecated
    public Slope getSlope(int id) {
        return slopeMap.get(id);
    }

    public Collection<Integer> getSlopeIds() {
        return slopeMap.keySet();
    }

    public Collection<Slope> getSlopes() {
        return slopeMap.values();
    }

    public Water getWater() {
        return water;
    }

    public GroundMesh getGroundMesh() {
        return groundMesh;
    }

    public boolean overlap(DecimalPosition position, ItemType itemType, TerrainType builderTerrainType, Integer builderMaxAdjoinDistance) {
        // Check in terrain objects
        SingleHolder<Boolean> result = new SingleHolder<>(false);
        terrainObjectConfigPositions.iterate((terrainObjectConfig, terrainObjectPosition) -> {
            if (terrainObjectPosition.getPosition().getDistance(position) < terrainObjectConfig.getRadius() + itemType.getRadius()) {
                result.setO(true);
                return true;
            } else {
                return false;
            }
        });
        if (result.getO()) {
            return true;
        }
        // Check in slopes
        for (Slope slope : slopeMap.values()) {
            if (slope.isInSlope(position, itemType.getRadius())) {
                return true;
            }
        }
        return false;
    }

    public Vertex calculatePositionOnZeroLevel(Ray3d worldPickRay) {
        // Find multiplier where the ray hits the ground (z = 0). start + m*direction -> z = 0
        double m = -worldPickRay.getStart().getZ() / worldPickRay.getDirection().getZ();
        return worldPickRay.getPoint(m);
//        logger.severe("Point On Ground: " + pointOnGround);
//        VertexData vertexData = originalGroundMesh.getVertexFromAbsoluteXY(pointOnGround.toXY());
//        if (vertexData != null) {
//            logger.severe("Ground VertexData: " + vertexData);
//        } else {
//            logger.severe("Position not on ground");
//        }
    }

    public InterpolatedTerrainTriangle getInterpolatedTerrainTriangle(DecimalPosition absoluteXY) {
        InterpolatedTerrainTriangle interpolatedTerrainTriangle = groundMesh.getInterpolatedTerrainTriangle(absoluteXY);
        if (interpolatedTerrainTriangle != null) {
            return interpolatedTerrainTriangle;
        }

        for (Slope slope : getSlopes()) {
            interpolatedTerrainTriangle = slope.getInterpolatedVertexData(absoluteXY);
            if (interpolatedTerrainTriangle != null) {
                return interpolatedTerrainTriangle;
            }
        }

        // TODO water

        throw new IllegalArgumentException("No InterpolatedTerrainTriangle at: " + absoluteXY);
    }

    public Vertex calculatePositionGroundMesh(Ray3d worldPickRay) {
        DecimalPosition zeroLevel = calculatePositionOnZeroLevel(worldPickRay).toXY();
        double height = getInterpolatedTerrainTriangle(zeroLevel).getHeight();
        return new Vertex(zeroLevel, height);
    }

    public Vertex calculatePositionGroundMesh(DecimalPosition position) {
        double height = getInterpolatedTerrainTriangle(position).getHeight();
        return new Vertex(position, height);
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
