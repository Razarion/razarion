package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.InterpolatedTerrainTriangle;
import com.btxtech.shared.datatypes.Line3d;
import com.btxtech.shared.datatypes.MapCollection;
import com.btxtech.shared.datatypes.MapList;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.SingleHolder;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.dto.SlopeNode;
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
import com.btxtech.shared.gameengine.planet.terrain.slope.VerticalSegment;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.shared.utils.MathHelper;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
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
    private Instance<TerrainTileContext> terrainTileContextInstance;
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

    public TerrainTile generateTerrainTile(Index terrainTileIndex) {
        long time = System.currentTimeMillis();
        TerrainTileContext terrainTileContext = terrainTileContextInstance.get();
        terrainTileContext.init(terrainTileIndex, terrainTypeService.getGroundSkeletonConfig());
        insertGroundPart(terrainTileContext);
        insertSlopePart(terrainTileContext);
        TerrainTile terrainTile = terrainTileContext.complete();
        logger.severe("generateTerrainTile: " + (System.currentTimeMillis() - time));
        return terrainTile;
    }

    private void iterateOverTerrainNodes(Index terrainTileIndex, Consumer<Index> nodeCallback) {
        int xNodeStart = terrainTileIndex.getX() * TerrainUtil.TERRAIN_TILE_NODES_COUNT;
        int xNodeEnd = terrainTileIndex.getX() * TerrainUtil.TERRAIN_TILE_NODES_COUNT + TerrainUtil.TERRAIN_TILE_NODES_COUNT;
        int yNodeStart = terrainTileIndex.getY() * TerrainUtil.TERRAIN_TILE_NODES_COUNT;
        int yNodeEnd = terrainTileIndex.getY() * TerrainUtil.TERRAIN_TILE_NODES_COUNT + TerrainUtil.TERRAIN_TILE_NODES_COUNT;
        for (int xNode = xNodeStart; xNode < xNodeEnd; xNode++) {
            for (int yNode = yNodeStart; yNode < yNodeEnd; yNode++) {
                Index index = new Index(xNode, yNode);
                nodeCallback.accept(index);
            }
        }
    }

    private void insertGroundPart(TerrainTileContext terrainTileContext) {
        terrainTileContext.initGround();

        GroundSkeletonConfig groundSkeletonConfig = terrainTypeService.getGroundSkeletonConfig();

        final SingleHolder<Integer> rectangleIndex = new SingleHolder<>(0);
        iterateOverTerrainNodes(terrainTileContext.getTerrainTileIndex(), nodeIndex -> {
            if (obstacleContainer.isSlope(nodeIndex)) {
                return;
            }
            double slopeHeight = obstacleContainer.getInsideSlopeHeight(nodeIndex);
            insertTerrainRectangle(nodeIndex.getX(), nodeIndex.getY(), rectangleIndex.getO(), groundSkeletonConfig, slopeHeight, terrainTileContext);
            rectangleIndex.setO(rectangleIndex.getO() + 1);
        });
        terrainTileContext.setGroundVertexCount(rectangleIndex.getO() * 2 * 3); // Per rectangle are two triangles with 3 corners
    }

    private void insertTerrainRectangle(int xNode, int yNode, int rectangleIndex, GroundSkeletonConfig groundSkeletonConfig, double slopeHeight, TerrainTileContext terrainTileContext) {
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


        double splattingBL = terrainTileContext.getSplatting(xNode, yNode);
        double splattingBR = terrainTileContext.getSplatting(rightXNode, yNode);
        double splattingTR = terrainTileContext.getSplatting(rightXNode, topYNode);
        double splattingTL = terrainTileContext.getSplatting(xNode, topYNode);

        terrainTileContext.setSplatting(xNode, yNode, splattingBL, splattingBR, splattingTR, splattingTL);

        // Triangle 1
        int triangleIndex = rectangleIndex * 2;
        int triangleCornerIndex = triangleIndex * 3;
        terrainTileContext.insertTriangleCorner(vertexBL, normBL, tangentBL, splattingBL, triangleCornerIndex);
        terrainTileContext.insertTriangleCorner(vertexBR, normBR, tangentBR, splattingBR, triangleCornerIndex + 1);
        terrainTileContext.insertTriangleCorner(vertexTL, normTL, tangentTL, splattingTL, triangleCornerIndex + 2);
        // Triangle 2
        triangleIndex = rectangleIndex * 2 + 1;
        triangleCornerIndex = triangleIndex * 3;
        terrainTileContext.insertTriangleCorner(vertexBR, normBR, tangentBR, splattingBR, triangleCornerIndex);
        terrainTileContext.insertTriangleCorner(vertexTR, normTR, tangentTR, splattingTR, triangleCornerIndex + 1);
        terrainTileContext.insertTriangleCorner(vertexTL, normTL, tangentTL, splattingTL, triangleCornerIndex + 2);
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

    private void insertSlopePart(TerrainTileContext terrainTileContext) {
        MapList<Slope, List<VerticalSegment>> connectedSlopeSegments = new MapList<>();
        Rectangle2D absoluteTerrainTileRect = TerrainUtil.toAbsoluteTileRectangle(terrainTileContext.getTerrainTileIndex());

        // Find connecting VerticalSegment
        iterateOverTerrainNodes(terrainTileContext.getTerrainTileIndex(), nodeIndex -> {
            List<VerticalSegment> nodeSegments = obstacleContainer.getVerticalSegments(nodeIndex);
            if (nodeSegments == null) {
                return;
            }
            findConnectingSegments(connectedSlopeSegments, absoluteTerrainTileRect, nodeSegments);
        });
        for (Map.Entry<Slope, List<List<VerticalSegment>>> entry : connectedSlopeSegments.getMap().entrySet()) {
            generateSlopeTerrainTile(terrainTileContext, entry.getKey(), entry.getValue());
        }
    }

    private void findConnectingSegments(MapList<Slope, List<VerticalSegment>> connectedSlopeSegments, Rectangle2D absoluteTerrainTileRect, List<VerticalSegment> nodeSegments) {
        // Do performance optimization here
        for (VerticalSegment nodeSegment : nodeSegments) {
            Slope slope = nodeSegment.getSlope();
            List<List<VerticalSegment>> existingSegments = connectedSlopeSegments.get(slope);
            if (existingSegments != null) {
                boolean found = false;
                for (List<VerticalSegment> existingSegment : existingSegments) {
                    for (VerticalSegment existing : existingSegment) {
                        if (nodeSegment == existing) {
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        break;
                    }
                }
                if (found) {
                    continue;
                }
            }
            List<VerticalSegment> connected = followSlopeSegments(nodeSegment, absoluteTerrainTileRect);
            connectedSlopeSegments.put(slope, connected);
        }
    }

    private List<VerticalSegment> followSlopeSegments(VerticalSegment current, Rectangle2D absoluteTerrainTileRect) {
        VerticalSegment predecessor = current.getPredecessor();
        VerticalSegment successor = current.getSuccessor();
        if (predecessor == current) {
            throw new UnsupportedOperationException("TerrainService.followSlopeSegments() 1 I don't know what to do...");
        }
        if (successor == current) {
            throw new UnsupportedOperationException("TerrainService.followSlopeSegments() 2 I don't know what to do...");
        }
        VerticalSegment start = current;
        // find start
        while (absoluteTerrainTileRect.contains(predecessor.getInner())) {
            start = predecessor;
            predecessor = predecessor.getPredecessor();
            if (predecessor == current) {
                start = current;
                break;
            }
        }
        VerticalSegment end = current;
        // find end
        while (absoluteTerrainTileRect.contains(successor.getInner())) {
            end = successor;
            successor = successor.getSuccessor();
            if (successor == current) {
                end = current;
                break;
            }
        }
        // Iterator from start to end
        List<VerticalSegment> connectedVerticalSegment = new ArrayList<>();
        VerticalSegment verticalSegment = start;
        connectedVerticalSegment.add(verticalSegment);
        do {
            verticalSegment = verticalSegment.getSuccessor();
            connectedVerticalSegment.add(verticalSegment);
        } while (verticalSegment != end);
        return connectedVerticalSegment;
    }

    private void generateSlopeTerrainTile(TerrainTileContext terrainTileContext, Slope slope, List<List<VerticalSegment>> connectedSegments) {
        SlopeSkeletonConfig slopeSkeletonConfig = slope.getSlopeSkeletonConfig();
        for (List<VerticalSegment> connectedSegment : connectedSegments) {
            TerrainSlopeTileContext terrainSlopeTileContext = terrainTileContext.createTerrainSlopeTileContext(slope, connectedSegment.size(), slopeSkeletonConfig.getRows());
            int vertexColumn = 0;
            for (VerticalSegment verticalSegment : connectedSegment) {
                Matrix4 transformationMatrix = verticalSegment.getTransformation();
                for (int row = 0; row < slopeSkeletonConfig.getRows(); row++) {
                    SlopeNode slopeNode = slopeSkeletonConfig.getSlopeNodes()[verticalSegment.getIndex() % slopeSkeletonConfig.getSegments()][row];
                    Vertex transformedPoint = transformationMatrix.multiply(slopeNode.getPosition(), 1.0);
                    terrainSlopeTileContext.addVertex(vertexColumn, row, transformedPoint, setupSlopeFactor(slopeNode), terrainTileContext.interpolateSplattin(transformedPoint.toXY()));
                }
                vertexColumn++;
            }
            terrainSlopeTileContext.triangulation();
        }
    }

    private static double setupSlopeFactor(SlopeNode slopeNode) {
        if (MathHelper.compareWithPrecision(1.0, slopeNode.getSlopeFactor())) {
            return 1;
        } else if (MathHelper.compareWithPrecision(0.0, slopeNode.getSlopeFactor())) {
            return 0;
        }
        // Why -shapeTemplateEntry.getNormShift() and not + is unclear
        // return (float) MathHelper.clamp(slopeSkeletonEntry.getSlopeFactor() - slopeSkeletonEntry.getNormShift(), 0.0, 1.0);
        return slopeNode.getSlopeFactor();
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
        SlopeSkeletonConfig slopeSkeletonConfig = terrainTypeService.getSlopeSkeleton(terrainSlopePosition.getSlopeConfigEntity());
        Slope slope;
        if (slopeSkeletonConfig.getType() == SlopeSkeletonConfig.Type.WATER) {
            slope = new SlopeWater(terrainSlopePosition.getSlopeConfigEntity(), water, slopeSkeletonConfig, terrainSlopePosition.getPolygon());
        } else if (slopeSkeletonConfig.getType() == SlopeSkeletonConfig.Type.LAND) {
            slope = new Slope(terrainSlopePosition.getSlopeConfigEntity(), slopeSkeletonConfig, terrainSlopePosition.getPolygon());
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
