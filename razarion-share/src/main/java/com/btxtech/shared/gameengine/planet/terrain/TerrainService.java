package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.InterpolatedTerrainTriangle;
import com.btxtech.shared.datatypes.Line3d;
import com.btxtech.shared.datatypes.MapCollection;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.SingleHolder;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.dto.VertexList;
import com.btxtech.shared.gameengine.ItemTypeService;
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
import com.btxtech.shared.system.JsInteropObjectFactory;
import com.btxtech.shared.utils.CollectionUtils;

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
    private Logger logger = Logger.getLogger(TerrainService.class.getName());
    @Inject
    private TerrainTypeService terrainTypeService;
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private JsInteropObjectFactory jsInteropObjectFactory;
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

    public TerrainTile generateTerrainTile(Index terrainTileIndex) {
        TerrainTile terrainTile = jsInteropObjectFactory.generateTerrainTile();
        terrainTile.init(terrainTileIndex.getX(), terrainTileIndex.getY());

        int verticesCount = (int) (Math.pow(TerrainUtil.TERRAIN_TILE_NODES_COUNT, 2) * 6);
        terrainTile.initGroundArrays(verticesCount * Vertex.getComponentsPerVertex(), verticesCount);

        GroundSkeletonConfig groundSkeletonConfig = terrainTypeService.getGroundSkeletonConfig();

        int rectangleIndex = 0;
        int xNodeStart = terrainTileIndex.getX() * TerrainUtil.TERRAIN_TILE_NODES_COUNT;
        int xNodeEnd = terrainTileIndex.getX() * TerrainUtil.TERRAIN_TILE_NODES_COUNT + TerrainUtil.TERRAIN_TILE_NODES_COUNT;
        int yNodeStart = terrainTileIndex.getY() * TerrainUtil.TERRAIN_TILE_NODES_COUNT;
        int yNodeEnd = terrainTileIndex.getY() * TerrainUtil.TERRAIN_TILE_NODES_COUNT + TerrainUtil.TERRAIN_TILE_NODES_COUNT;
        for (int xNode = xNodeStart; xNode < xNodeEnd; xNode++) {
            for (int yNode = yNodeStart; yNode < yNodeEnd; yNode++) {
                Index index = new Index(xNode, yNode);
                if (obstacleContainer.isSlope(index)) {
                    continue;
                }
                double slopeHeight = obstacleContainer.getInsideSlopeHeight(index);
                insertTerrainRectangle(xNode, yNode, rectangleIndex, groundSkeletonConfig, slopeHeight, terrainTile);
                rectangleIndex++;
            }
        }
        terrainTile.setGroundVertexCount(rectangleIndex * 2 * 3); // Per rectangle are two triangles with 3 corners
        return terrainTile;
    }

    private void insertTerrainRectangle(int xNode, int yNode, int rectangleIndex, GroundSkeletonConfig groundSkeletonConfig, double slopeHeight, TerrainTile terrainTile) {
        int rightXNode = xNode + 1;
        int topYNode = yNode + 1;

        double zBL = slopeHeight + groundSkeletonConfig.getHeights()[CollectionUtils.getCorrectedIndex(xNode, groundSkeletonConfig.getHeightXCount())][CollectionUtils.getCorrectedIndexInvert(yNode, groundSkeletonConfig.getHeightYCount())];
        double zBR = slopeHeight + groundSkeletonConfig.getHeights()[CollectionUtils.getCorrectedIndex(rightXNode, groundSkeletonConfig.getHeightXCount())][CollectionUtils.getCorrectedIndexInvert(yNode, groundSkeletonConfig.getHeightYCount())];
        double zTR = slopeHeight + groundSkeletonConfig.getHeights()[CollectionUtils.getCorrectedIndex(rightXNode, groundSkeletonConfig.getHeightXCount())][CollectionUtils.getCorrectedIndexInvert(topYNode, groundSkeletonConfig.getHeightYCount())];
        double zTL = slopeHeight + groundSkeletonConfig.getHeights()[CollectionUtils.getCorrectedIndex(xNode, groundSkeletonConfig.getHeightXCount())][CollectionUtils.getCorrectedIndexInvert(topYNode, groundSkeletonConfig.getHeightYCount())];
        double absoluteX = xNode * TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH;
        double absoluteY = yNode * TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH;
        Vertex vertexBL = new Vertex(absoluteX, absoluteY, zBL);
        Vertex vertexBR = new Vertex(absoluteX + TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH, absoluteY, zBR);
        Vertex vertexTR = new Vertex(absoluteX + TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH, absoluteY + TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH, zTR);
        Vertex vertexTL = new Vertex(absoluteX, absoluteY + TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH, zTL);

        Vertex normBL = setupNorm(xNode, yNode, groundSkeletonConfig);
        Vertex normBR = setupNorm(rightXNode, yNode, groundSkeletonConfig);
        Vertex normTR = setupNorm(rightXNode, topYNode, groundSkeletonConfig);
        Vertex normTL = setupNorm(xNode, topYNode, groundSkeletonConfig);

        Vertex tangentBL = setupTangent(xNode, yNode, groundSkeletonConfig);
        Vertex tangentBR = setupTangent(rightXNode, yNode, groundSkeletonConfig);
        Vertex tangentTR = setupTangent(rightXNode, topYNode, groundSkeletonConfig);
        Vertex tangentTL = setupTangent(xNode, topYNode, groundSkeletonConfig);

        double splattingBL = groundSkeletonConfig.getSplattings()[CollectionUtils.getCorrectedIndex(xNode, groundSkeletonConfig.getSplattingXCount())][CollectionUtils.getCorrectedIndexInvert(yNode, groundSkeletonConfig.getSplattingYCount())];
        double splattingBR = groundSkeletonConfig.getSplattings()[CollectionUtils.getCorrectedIndex(rightXNode, groundSkeletonConfig.getSplattingXCount())][CollectionUtils.getCorrectedIndexInvert(yNode, groundSkeletonConfig.getSplattingYCount())];
        double splattingTR = groundSkeletonConfig.getSplattings()[CollectionUtils.getCorrectedIndex(rightXNode, groundSkeletonConfig.getSplattingXCount())][CollectionUtils.getCorrectedIndexInvert(topYNode, groundSkeletonConfig.getSplattingYCount())];
        double splattingTL = groundSkeletonConfig.getSplattings()[CollectionUtils.getCorrectedIndex(xNode, groundSkeletonConfig.getSplattingXCount())][CollectionUtils.getCorrectedIndexInvert(topYNode, groundSkeletonConfig.getSplattingYCount())];

        // Triangle 1
        int triangleIndex = rectangleIndex * 2;
        int triangleCornerIndex = triangleIndex * 3;
        insertTriangleCorner(vertexBL, normBL, tangentBL, splattingBL, triangleCornerIndex, terrainTile);
        insertTriangleCorner(vertexBR, normBR, tangentBR, splattingBR, triangleCornerIndex + 1, terrainTile);
        insertTriangleCorner(vertexTL, normTL, tangentTL, splattingTL, triangleCornerIndex + 2, terrainTile);
        // Triangle 2
        triangleIndex = rectangleIndex * 2 + 1;
        triangleCornerIndex = triangleIndex * 3;
        insertTriangleCorner(vertexBR, normBR, tangentBR, splattingBR, triangleCornerIndex, terrainTile);
        insertTriangleCorner(vertexTR, normTR, tangentTR, splattingTR, triangleCornerIndex + 1, terrainTile);
        insertTriangleCorner(vertexTL, normTL, tangentTL, splattingTL, triangleCornerIndex + 2, terrainTile);
    }

    private Vertex setupNorm(int x, int y, GroundSkeletonConfig groundSkeletonConfig) {
        int xEast = x + 1;
        int xWest = x - 1 < 0 ? groundSkeletonConfig.getHeightXCount() - 1 : x - 1;
        int yNorth = y + 1;
        int ySouth = y - 1 < 0 ? groundSkeletonConfig.getHeightYCount() - 1 : y - 1;

        double zNorth = groundSkeletonConfig.getHeights()[CollectionUtils.getCorrectedIndex(x, groundSkeletonConfig.getHeightXCount())][CollectionUtils.getCorrectedIndexInvert(yNorth, groundSkeletonConfig.getHeightYCount())];
        double zEast = groundSkeletonConfig.getHeights()[CollectionUtils.getCorrectedIndex(xEast, groundSkeletonConfig.getHeightXCount())][CollectionUtils.getCorrectedIndexInvert(y, groundSkeletonConfig.getHeightYCount())];
        double zSouth = groundSkeletonConfig.getHeights()[CollectionUtils.getCorrectedIndex(x, groundSkeletonConfig.getHeightXCount())][CollectionUtils.getCorrectedIndexInvert(ySouth, groundSkeletonConfig.getHeightYCount())];
        double zWest = groundSkeletonConfig.getHeights()[CollectionUtils.getCorrectedIndex(xWest, groundSkeletonConfig.getHeightXCount())][CollectionUtils.getCorrectedIndexInvert(y, groundSkeletonConfig.getHeightYCount())];
        return new Vertex(zWest - zEast, zSouth - zNorth, 2 * TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH).normalize(1.0);
    }

    private Vertex setupTangent(int x, int y, GroundSkeletonConfig groundSkeletonConfig) {
        int xEast = x + 1;
        int xWest = x - 1 < 0 ? groundSkeletonConfig.getHeightXCount() - 1 : x - 1;

        double zEast = groundSkeletonConfig.getHeights()[CollectionUtils.getCorrectedIndex(xEast, groundSkeletonConfig.getHeightXCount())][CollectionUtils.getCorrectedIndexInvert(y, groundSkeletonConfig.getHeightYCount())];
        double zWest = groundSkeletonConfig.getHeights()[CollectionUtils.getCorrectedIndex(xWest, groundSkeletonConfig.getHeightXCount())][CollectionUtils.getCorrectedIndexInvert(y, groundSkeletonConfig.getHeightYCount())];

        return new Vertex(TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH * 2.0, 0, zEast - zWest).normalize(1.0);
    }

    private void insertTriangleCorner(Vertex vertex, Vertex norm, Vertex tangent, double splatting, int triangleCornerIndex, TerrainTile terrainTile) {
        terrainTile.setGroundTriangleCorner(triangleCornerIndex, vertex.getX(), vertex.getY(), vertex.getZ(), norm.getX(), norm.getY(), norm.getZ(), tangent.getX(), tangent.getY(), tangent.getZ(), splatting);
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


//        double startX = Math.floor((center.getX() - radius) / (double) GROUND_NODE_ABSOLUTE_LENGTH) * (double) GROUND_NODE_ABSOLUTE_LENGTH;
//        double startY = Math.floor((center.getY() - radius) / (double) GROUND_NODE_ABSOLUTE_LENGTH) * (double) GROUND_NODE_ABSOLUTE_LENGTH;
//        double endX = Math.ceil((center.getX() + radius) / (double) GROUND_NODE_ABSOLUTE_LENGTH) * (double) GROUND_NODE_ABSOLUTE_LENGTH;
//        double endY = Math.ceil((center.getY() + radius) / (double) GROUND_NODE_ABSOLUTE_LENGTH) * (double) GROUND_NODE_ABSOLUTE_LENGTH;
//
//        int countX = (int) ((endX - startX) / (double) GROUND_NODE_ABSOLUTE_LENGTH);
//        int countY = (int) ((endY - startY) / (double) GROUND_NODE_ABSOLUTE_LENGTH);
//
//        double maxZ = Double.MIN_VALUE;
//
//        for (double x = startX; x < endX; x += (double) GROUND_NODE_ABSOLUTE_LENGTH) {
//            for (double y = startY; y < endY; y += (double) GROUND_NODE_ABSOLUTE_LENGTH) {
//                if (countX < 2 || countY < 2) {
//                    maxZ = Math.max(faceMaxZ(x, y), maxZ);
//                } else {
//                    double correctedX = x;
//                    if (startX + (endX - startX) / 2.0 > x) {
//                        correctedX += (double) GROUND_NODE_ABSOLUTE_LENGTH;
//                    }
//                    double correctedY = y;
//                    if (startY + (endY - startY) / 2.0 > y) {
//                        correctedY += (double) GROUND_NODE_ABSOLUTE_LENGTH;
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

    public void overrideSlopeSkeletonConfig(SlopeSkeletonConfig slopeSkeletonConfig) {
        for (Slope slope : slopeMap.values()) {
            if (slope.getSlopeSkeletonConfig().getId() == slopeSkeletonConfig.getId()) {
                slope.updateSlopeSkeleton(slopeSkeletonConfig);
            }
        }
    }

    public VertexList createGroundVertexList() {
        VertexList vertexList;
        if (groundMesh != null) {
            vertexList = groundMesh.provideVertexList();
        } else {
            vertexList = new VertexList();
        }
        for (Slope slope : slopeMap.values()) {
            if (!slope.hasWater()) {
                vertexList.append(slope.getGroundPlateauConnector().getTopMesh().provideVertexList());
                vertexList.append(slope.getGroundPlateauConnector().getInnerConnectionVertexList());
            }
            vertexList.append(slope.getGroundPlateauConnector().getOuterConnectionVertexList());
        }
        return vertexList;
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
