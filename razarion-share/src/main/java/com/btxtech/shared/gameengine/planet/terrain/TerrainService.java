package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.InterpolatedTerrainTriangle;
import com.btxtech.shared.datatypes.Line3d;
import com.btxtech.shared.datatypes.MapCollection;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.SingleHolder;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.SurfaceType;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.PlanetActivationEvent;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleContainer;
import com.btxtech.shared.gameengine.planet.terrain.ground.GroundMesh;
import com.btxtech.shared.gameengine.planet.terrain.ground.GroundModeler;
import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;
import com.btxtech.shared.gameengine.planet.terrain.slope.SlopeWater;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.DoubleStream;

/**
 * Created by Beat
 * 13.07.2016.
 */
@Singleton
public class TerrainService {
    public static final int MESH_NODE_EDGE_LENGTH = 8;
    private Logger logger = Logger.getLogger(TerrainService.class.getName());
    @Inject
    private TerrainTypeService terrainTypeService;
    @Inject
    private ObstacleContainer obstacleContainer;
    private Water water;
    private GroundMesh groundMesh;
    private Rectangle groundMeshDimension;
    private Map<Integer, Slope> slopeMap = new HashMap<>();
    private MapCollection<TerrainObjectConfig, TerrainObjectPosition> terrainObjectConfigPositions;
    private PlanetConfig planetConfig;

    public void onPlanetActivation(@Observes PlanetActivationEvent planetActivationEvent) {
        setup(planetActivationEvent.getPlanetConfig());
        obstacleContainer.setup(planetActivationEvent.getPlanetConfig().getGroundMeshDimension(), slopeMap.values(), terrainObjectConfigPositions);
    }

    public void init(PlanetConfig planetConfig, TerrainTypeService terrainTypeService) {
        this.terrainTypeService = terrainTypeService;
        setup(planetConfig);
    }

    private void setup(PlanetConfig planetConfig) {
        logger.severe("Start setup surface");
        long time = System.currentTimeMillis();
        this.planetConfig = planetConfig;

        groundMeshDimension = planetConfig.getGroundMeshDimension();
        setupGround();

        water = new Water(planetConfig.getWaterLevel());

        setupPlateaus();

        terrainObjectConfigPositions = new MapCollection<>();
        if (planetConfig.getTerrainObjectPositions() != null) {
            for (TerrainObjectPosition objectPosition : planetConfig.getTerrainObjectPositions()) {
                TerrainObjectConfig terrainObjectConfig = terrainTypeService.getTerrainObjectConfig(objectPosition.getTerrainObjectId());
                terrainObjectConfigPositions.put(terrainObjectConfig, objectPosition);
            }
        }

        logger.severe("Setup surface took: " + (System.currentTimeMillis() - time));
    }

    public MapCollection<TerrainObjectConfig, TerrainObjectPosition> getTerrainObjectPositions() {
        return terrainObjectConfigPositions;
    }

    public void setupPlateaus() {
        slopeMap.clear();
        if (planetConfig.getTerrainSlopePositions() != null) {
            planetConfig.getTerrainSlopePositions().forEach(this::setupPlateau);
        }
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

    public void setupGround() {
        if (terrainTypeService.getGroundSkeletonConfig() != null) {
            this.groundMesh = GroundModeler.generateGroundMesh(terrainTypeService.getGroundSkeletonConfig(), groundMeshDimension);
            this.groundMesh.setupNorms();
        }
    }

    public Slope getSlope(int id) {
        return slopeMap.get(id);
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

    public boolean overlap(DecimalPosition position) {
        // Check in terrain objects
        SingleHolder<Boolean> result = new SingleHolder<>(false);
        terrainObjectConfigPositions.iterate((terrainObjectConfig, terrainObjectPosition) -> {
            if (terrainObjectPosition.getPosition().getDistance(position) < terrainObjectConfig.getRadius()) {
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
            if (slope.isInSlope(position)) {
                return true;
            }
        }
        return false;
    }

    public boolean overlap(DecimalPosition position, double radius) {
        // Check in terrain objects
        SingleHolder<Boolean> result = new SingleHolder<>(false);
        terrainObjectConfigPositions.iterate((terrainObjectConfig, terrainObjectPosition) -> {
            if (terrainObjectPosition.getPosition().getDistance(position) < terrainObjectConfig.getRadius() + radius) {
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
            if (slope.isInSlope(position, radius)) {
                return true;
            }
        }
        return false;
    }

    public boolean overlap(Collection<DecimalPosition> positions, BaseItemType baseItemType) {
        for (DecimalPosition position : positions) {
            if (overlap(position, baseItemType.getPhysicalAreaConfig().getRadius())) {
                return true;
            }
        }
        return false;
    }

    public double getHighestZInRegion(DecimalPosition center, double radius) {
        DoubleStream.Builder doubleStreamBuilder = DoubleStream.builder();


        double startX = Math.floor((center.getX() - radius) / (double) MESH_NODE_EDGE_LENGTH) * (double) MESH_NODE_EDGE_LENGTH;
        double startY = Math.floor((center.getY() - radius) / (double) MESH_NODE_EDGE_LENGTH) * (double) MESH_NODE_EDGE_LENGTH;
        double endX = Math.ceil((center.getX() + radius) / (double) MESH_NODE_EDGE_LENGTH) * (double) MESH_NODE_EDGE_LENGTH;
        double endY = Math.ceil((center.getY() + radius) / (double) MESH_NODE_EDGE_LENGTH) * (double) MESH_NODE_EDGE_LENGTH;

        for (double x = startX; x < endX; x += (double) MESH_NODE_EDGE_LENGTH) {
            for (double y = startY; y < endY; y += (double) MESH_NODE_EDGE_LENGTH) {
                Rectangle2D rect = new Rectangle2D(x, y, (double) MESH_NODE_EDGE_LENGTH, (double) MESH_NODE_EDGE_LENGTH);
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


//        double startX = Math.floor((center.getX() - radius) / (double) MESH_NODE_EDGE_LENGTH) * (double) MESH_NODE_EDGE_LENGTH;
//        double startY = Math.floor((center.getY() - radius) / (double) MESH_NODE_EDGE_LENGTH) * (double) MESH_NODE_EDGE_LENGTH;
//        double endX = Math.ceil((center.getX() + radius) / (double) MESH_NODE_EDGE_LENGTH) * (double) MESH_NODE_EDGE_LENGTH;
//        double endY = Math.ceil((center.getY() + radius) / (double) MESH_NODE_EDGE_LENGTH) * (double) MESH_NODE_EDGE_LENGTH;
//
//        int countX = (int) ((endX - startX) / (double) MESH_NODE_EDGE_LENGTH);
//        int countY = (int) ((endY - startY) / (double) MESH_NODE_EDGE_LENGTH);
//
//        double maxZ = Double.MIN_VALUE;
//
//        for (double x = startX; x < endX; x += (double) MESH_NODE_EDGE_LENGTH) {
//            for (double y = startY; y < endY; y += (double) MESH_NODE_EDGE_LENGTH) {
//                if (countX < 2 || countY < 2) {
//                    maxZ = Math.max(faceMaxZ(x, y), maxZ);
//                } else {
//                    double correctedX = x;
//                    if (startX + (endX - startX) / 2.0 > x) {
//                        correctedX += (double) MESH_NODE_EDGE_LENGTH;
//                    }
//                    double correctedY = y;
//                    if (startY + (endY - startY) / 2.0 > y) {
//                        correctedY += (double) MESH_NODE_EDGE_LENGTH;
//                    }
//                    double distance = center.getDistance(new DecimalPosition(correctedX, correctedY));
//                    if (distance <= radius) {
//                        maxZ = Math.max(faceMaxZ(x, y), maxZ);
//                    }
//                }
//            }
//        }
//        return maxZ;
    }

    private double faceMaxZ(double x, double y) {
        return groundMesh.faceMaxZ(new DecimalPosition(x, y));
    }

    public InterpolatedTerrainTriangle getInterpolatedTerrainTriangle(DecimalPosition absoluteXY) {
        // Ground
        InterpolatedTerrainTriangle interpolatedTerrainTriangle = groundMesh.getInterpolatedTerrainTriangle(absoluteXY);
        if (interpolatedTerrainTriangle != null) {
            return interpolatedTerrainTriangle;
        }
        // Slope
        for (Slope slope : getSlopes()) {
            interpolatedTerrainTriangle = slope.getInterpolatedVertexData(absoluteXY);
            if (interpolatedTerrainTriangle != null) {
                return interpolatedTerrainTriangle;
            }
        }
        // Water
        interpolatedTerrainTriangle = water.getInterpolatedVertexData(absoluteXY);
        if (interpolatedTerrainTriangle != null) {
            return interpolatedTerrainTriangle;
        }
        // Nothing found
        throw new NoInterpolatedTerrainTriangleException(absoluteXY);
    }

    public Vertex calculatePositionGroundMesh(Line3d worldPickRay) {
        // Water
        InterpolatedTerrainTriangle interpolatedTerrainTriangle = water.getInterpolatedVertexData(worldPickRay);
        if (interpolatedTerrainTriangle != null) {
            return interpolatedTerrainTriangle.crossPoint(worldPickRay);
        }
        // Ground
        DecimalPosition groundXY = groundMesh.calculatePositionOnHeightLevel(worldPickRay).toXY();
        interpolatedTerrainTriangle = groundMesh.getInterpolatedTerrainTriangle(groundXY);
        if (interpolatedTerrainTriangle != null) {
            return interpolatedTerrainTriangle.crossPoint(worldPickRay);
        }
        // Slope
        for (Slope slope : getSlopes()) {
            Vertex slopePosition = slope.calculatePositionOnSlope(worldPickRay);
            if (slopePosition != null) {
                return slopePosition;
            }
        }
        // Nothing found
        throw new NoInterpolatedTerrainTriangleException(worldPickRay);
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
