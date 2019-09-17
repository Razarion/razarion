package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.DoubleHolder;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.datatypes.MapList;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Polygon2DRasterizer;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleTerrainObject;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeTerrainShapeObjectList;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeTerrainShapeObjectPosition;
import com.btxtech.shared.gameengine.planet.terrain.slope.Driveway;
import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;
import com.btxtech.shared.gameengine.planet.terrain.slope.VerticalSegment;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.shared.utils.GeometricUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 22.06.2017.
 */
public class TerrainShapeSetup {
    private Logger logger = Logger.getLogger(TerrainShapeSetup.class.getName());
    private TerrainShape terrainShape;
    private TerrainTypeService terrainTypeService;
    private TerrainShapeSubNodeFactory terrainShapeSubNodeFactory;
    private Map<Index, TerrainShapeNode> dirtyTerrainShapeNodes = new HashMap<>();

    public TerrainShapeSetup(TerrainShape terrainShape, TerrainTypeService terrainTypeService) {
        this.terrainShape = terrainShape;
        this.terrainTypeService = terrainTypeService;
        terrainShapeSubNodeFactory = new TerrainShapeSubNodeFactory();
    }

    public void processTerrainObject(List<TerrainObjectPosition> terrainObjectPositions) {
        if (terrainObjectPositions == null) {
            return;
        }
        long time = System.currentTimeMillis();
        Map<Index, MapList<Integer, TerrainObjectPosition>> renderTerrainObjects = new HashMap<>();
        for (TerrainObjectPosition objectPosition : terrainObjectPositions) {
            // Render engine
            MapList<Integer, TerrainObjectPosition> tileObjects = renderTerrainObjects.computeIfAbsent(TerrainUtil.toTile(objectPosition.getPosition()), k -> new MapList<>());
            tileObjects.put(objectPosition.getTerrainObjectId(), objectPosition);
            // Game engine
            TerrainObjectConfig terrainObjectConfig = terrainTypeService.getTerrainObjectConfig(objectPosition.getTerrainObjectId());
            Circle2D terrainObjectCircle = new Circle2D(objectPosition.getPosition(), terrainObjectConfig.getRadius() * objectPosition.getScale());
            ObstacleTerrainObject obstacleTerrainObject = new ObstacleTerrainObject(terrainObjectCircle);
            for (Index nodeIndex : GeometricUtil.rasterizeCircle(obstacleTerrainObject.getCircle(), TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH)) {
                TerrainShapeNode terrainShapeNode = terrainShape.getOrCreateTerrainShapeNode(nodeIndex);
                terrainShapeNode.addObstacle(obstacleTerrainObject);
                // Create terrain type
                Rectangle2D terrainRect = TerrainUtil.toAbsoluteNodeRectangle(nodeIndex);
                switch (terrainObjectCircle.checkInside(terrainRect)) {
                    case INSIDE:
                        terrainShapeNode.setTerrainType(TerrainType.BLOCKED);
                        terrainShapeNode.setTerrainShapeSubNodes(null);
                        dirtyTerrainShapeNodes.put(nodeIndex, terrainShapeNode);
                        break;
                    case PARTLY:
                        terrainShapeSubNodeFactory.fillTerrainObjectTerrainShapeSubNode(terrainShapeNode, terrainRect, terrainObjectCircle);
                        dirtyTerrainShapeNodes.put(nodeIndex, terrainShapeNode);
                        break;
                }
            }
        }
        fillInRenderTerrainObject(renderTerrainObjects);
        logger.severe("Generate Terrain Objects: " + (System.currentTimeMillis() - time));
    }

    public void processSlopes(List<TerrainSlopePosition> terrainSlopePositions) {
        if (terrainSlopePositions == null) {
            return;
        }
        long time = System.currentTimeMillis();
        for (TerrainSlopePosition terrainSlopePosition : terrainSlopePositions) {
            try {
                processSlope(setupSlope(terrainSlopePosition, 0), dirtyTerrainShapeNodes);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Can not handle slope with id: " + terrainSlopePosition.getId(), e);
            }
        }
        logger.severe("Generate Slopes: " + (System.currentTimeMillis() - time));
    }

    public void finish() {
        terrainShapeSubNodeFactory.concentrate(dirtyTerrainShapeNodes);
    }

    private Slope setupSlope(TerrainSlopePosition terrainSlopePosition, double groundHeight) {
        SlopeSkeletonConfig slopeSkeletonConfig = terrainTypeService.getSlopeSkeleton(terrainSlopePosition.getSlopeConfigId());
        Slope slope = new Slope(terrainSlopePosition.getId(), slopeSkeletonConfig, terrainSlopePosition.isInverted(), terrainSlopePosition.getPolygon(), groundHeight, terrainTypeService);
        setupSlopeChildren(slope, terrainSlopePosition.getChildren(), slope.getInnerGroundHeight());
        return slope;
    }

    private void setupSlopeChildren(Slope parentSlope, List<TerrainSlopePosition> childrenTerrainSlopePositions, double groundHeight) {
        if (childrenTerrainSlopePositions == null || childrenTerrainSlopePositions.isEmpty()) {
            return;
        }
        Collection<Slope> children = new ArrayList<>();
        for (TerrainSlopePosition terrainSlopePosition : childrenTerrainSlopePositions) {
            children.add(setupSlope(terrainSlopePosition, groundHeight));
        }
        parentSlope.setChildren(children);
    }

    private void processSlope(Slope slope, Map<Index, TerrainShapeNode> dirtyTerrainShapeNodes) {
        prepareVerticalSegments(slope);
        List<DecimalPosition> tmpInnerGameEnginePolygon = new ArrayList<>(slope.getInnerGameEnginePolygon().getCorners());
        Collections.reverse(tmpInnerGameEnginePolygon);
        ObstacleFactory.addObstacles(terrainShape, slope);
        SlopeContext slopeContext = new SlopeContext(slope);
        SlopeGroundConnectorFactory.prepareContextGroundSlopeConnection(slope.getInnerRenderEnginePolygon().getCorners(), false, slopeContext);
        SlopeGroundConnectorFactory.prepareContextGroundSlopeConnection(slope.getOuterRenderEnginePolygon().getCorners(), true, slopeContext);
        if (slope.hasWater()) {
            // Setup TerrainType (game engine)
            if (!slope.isInverted()) {
                setupTerrainType(slope.getOuterGameEnginePolygon(), dirtyTerrainShapeNodes, TerrainType.LAND_COAST, slope.getOuterGroundHeight(), null);
                setupTerrainType(slope.getCoastDelimiterPolygonTerrainType(), dirtyTerrainShapeNodes, TerrainType.WATER_COAST, slope.getOuterGroundHeight() + slope.getSlopeSkeletonConfig().getWaterLevel(), null);
                setupTerrainType(slope.getInnerGameEnginePolygon(), dirtyTerrainShapeNodes, TerrainType.WATER, slope.getOuterGroundHeight() + slope.getSlopeSkeletonConfig().getWaterLevel(), null);
                // Setup slope ground connection (render engine)
                Polygon2DRasterizer outerRasterizer = Polygon2DRasterizer.create(slope.getOuterRenderEnginePolygon(), TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH);
                for (Index nodeIndex : outerRasterizer.getPiercedTiles()) {
                    List<List<DecimalPosition>> outerPiercings = slopeContext.getOuterPiercings(nodeIndex);
                    if (outerPiercings != null) {
                        TerrainShapeNode terrainShapeNode = terrainShape.getOrCreateTerrainShapeNode(nodeIndex);
                        for (List<DecimalPosition> outerPiercing : outerPiercings) {
                            terrainShapeNode.setRenderHideGround(true);
                            Rectangle2D terrainRect = TerrainUtil.toAbsoluteNodeRectangle(nodeIndex);
                            terrainShapeNode.addGroundSlopeConnections(setupSlopeGroundConnection(terrainRect, outerPiercing, slope.getOuterGroundHeight(), false, null, 0), null);
                        }
                    }
                }
                // Setup inner water
                for (Index nodeIndex : outerRasterizer.getInnerTiles()) {
                    TerrainShapeNode terrainShapeNode = terrainShape.getOrCreateTerrainShapeNode(nodeIndex);
                    terrainShapeNode.setRenderHideGround(true);
                }
                // Setup inner water seabed
                Polygon2DRasterizer innerRasterizer = Polygon2DRasterizer.create(slope.getInnerRenderEnginePolygon(), TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH);
                for (Index nodeIndex : innerRasterizer.getInnerTiles()) {
                    TerrainShapeNode terrainShapeNode = terrainShape.getOrCreateTerrainShapeNode(nodeIndex);
                    terrainShapeNode.setRenderHideGround(false);
                    terrainShapeNode.setRenderInnerSlopeId(slope.getSlopeSkeletonConfig().getId());
                    terrainShapeNode.setInnerGroundHeight(slope.getInnerGroundHeight());
                    terrainShapeNode.setRenderInnerWaterSlopeId(slope.getSlopeSkeletonConfig().getId());
                    terrainShapeNode.setFullWaterLevel(slope.getOuterGroundHeight() + slope.getSlopeSkeletonConfig().getWaterLevel());
                }
                // Setup slope inner seabed connection (render engine)
                for (Index nodeIndex : innerRasterizer.getPiercedTiles()) {
                    List<List<DecimalPosition>> innerPiercings = slopeContext.getInnerPiercings(nodeIndex);
                    if (innerPiercings != null) {
                        TerrainShapeNode terrainShapeNode = terrainShape.getOrCreateTerrainShapeNode(nodeIndex);
                        for (List<DecimalPosition> innerPiercing : innerPiercings) {
                            terrainShapeNode.setRenderHideGround(true);
                            Rectangle2D terrainRect = TerrainUtil.toAbsoluteNodeRectangle(nodeIndex);
                            terrainShapeNode.addWaterSegments(setupSlopeGroundConnection(terrainRect, innerPiercing, slope.getOuterGroundHeight() + slope.getSlopeSkeletonConfig().getWaterLevel(), false, null, 0), slope.getSlopeSkeletonConfig().getId());
                            terrainShapeNode.addGroundSlopeConnections(setupSlopeGroundConnection(terrainRect, innerPiercing, slope.getInnerGroundHeight(), false, null, 0), slope.getSlopeSkeletonConfig().getId());
                        }
                    }
                }

            } else {
                setupTerrainType(slope.getOuterGameEnginePolygon(), dirtyTerrainShapeNodes, TerrainType.WATER_COAST, slope.getInnerGroundHeight() + slope.getSlopeSkeletonConfig().getWaterLevel(), null);
                setupTerrainType(slope.getCoastDelimiterPolygonTerrainType(), dirtyTerrainShapeNodes, TerrainType.LAND_COAST, slope.getInnerGroundHeight(), null);
                setupTerrainType(slope.getInnerGameEnginePolygon(), dirtyTerrainShapeNodes, TerrainType.LAND, slope.getInnerGroundHeight(), null);
                // Setup slope ground connection (render engine)
                Polygon2DRasterizer innerRasterizer = Polygon2DRasterizer.create(slope.getInnerRenderEnginePolygon(), TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH);
                for (Index nodeIndex : innerRasterizer.getPiercedTiles()) {
                    List<List<DecimalPosition>> innerPiercings = slopeContext.getInnerPiercings(nodeIndex);
                    if (innerPiercings != null) {
                        TerrainShapeNode terrainShapeNode = terrainShape.getOrCreateTerrainShapeNode(nodeIndex);
                        for (List<DecimalPosition> innerPiercing : innerPiercings) {
                            Rectangle2D terrainRect = TerrainUtil.toAbsoluteNodeRectangle(nodeIndex);
                            terrainShapeNode.addWaterSegments(setupSlopeGroundConnection(terrainRect, innerPiercing, slope.getOuterGroundHeight() + slope.getSlopeSkeletonConfig().getWaterLevel(), true, null, 0), slope.getSlopeSkeletonConfig().getId());
                            terrainShapeNode.addGroundSlopeConnections(setupSlopeGroundConnection(terrainRect, innerPiercing, slope.getInnerGroundHeight(), false, null, 0), slope.getSlopeSkeletonConfig().getId());
                        }
                    }
                }
                for (Index nodeIndex : innerRasterizer.getInnerTiles()) {
                    TerrainShapeNode terrainShapeNode = terrainShape.getOrCreateTerrainShapeNode(nodeIndex);
                    terrainShapeNode.setFullWaterLevel(null);
                    terrainShapeNode.setInnerGroundHeight(slope.getInnerGroundHeight());
                }
            }
        } else {
            // Setup TerrainType
            DrivewayContext drivewayContext = new DrivewayContext(slope.getDrivewayGameEngineHandler(), DrivewayContext.Type.FLAT_DRIVEWAY, TerrainType.LAND, slope.getOuterGroundHeight());
            setupTerrainType(slope.getOuterGameEnginePolygon(), dirtyTerrainShapeNodes, TerrainType.BLOCKED, slope.getOuterGroundHeight(), drivewayContext);
            drivewayContext = new DrivewayContext(slope.getDrivewayGameEngineHandler(), DrivewayContext.Type.SLOPE_DRIVEWAY, TerrainType.LAND, slope.getInnerGroundHeight());
            setupTerrainType(slope.getInnerGameEnginePolygon(), dirtyTerrainShapeNodes, TerrainType.LAND, slope.getInnerGroundHeight(), drivewayContext);

            Polygon2DRasterizer outerRasterizer = Polygon2DRasterizer.create(slope.getOuterRenderEnginePolygon(), TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH);
            Set<Index> completeUnderSlope = new HashSet<>(outerRasterizer.getInnerTiles());
            for (Index nodeIndex : outerRasterizer.getPiercedTiles()) {
                // Outer slope ground connection
                List<List<DecimalPosition>> outerPiercings = slopeContext.getOuterPiercings(nodeIndex);
                for (List<DecimalPosition> outerPiercing : outerPiercings) {
                    Rectangle2D terrainRect = TerrainUtil.toAbsoluteNodeRectangle(nodeIndex);
                    terrainShape.getOrCreateTerrainShapeNode(nodeIndex).addGroundSlopeConnections(setupSlopeGroundConnection(terrainRect, outerPiercing, slope.getOuterGroundHeight(), false, null, 0), null);
                }
                // Set height
                terrainShape.getOrCreateTerrainShapeNode(nodeIndex).setInnerGroundHeight(slope.getOuterGroundHeight()); // TODO this is may wrong
            }

            Polygon2DRasterizer innerRasterizer = Polygon2DRasterizer.create(slope.getInnerRenderEnginePolygon(), TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH);
            for (Index nodeIndex : innerRasterizer.getPiercedTiles()) {
                completeUnderSlope.remove(nodeIndex);
                Rectangle2D terrainRect = TerrainUtil.toAbsoluteNodeRectangle(nodeIndex);
                TerrainShapeNode terrainShapeNode = terrainShape.getOrCreateTerrainShapeNode(nodeIndex);
                List<List<DecimalPosition>> innerPiercings = slopeContext.getInnerPiercings(nodeIndex);
                Driveway fractalDriveway = slope.getDrivewayIfInsideOrTouching(terrainRect);
                if (fractalDriveway != null) {
                    // Inner slope ground connection regarding driveway break line
                    List<DecimalPosition> breakingGroundPiercing = fractalDriveway.setupPiercingLine(terrainRect, true);
                    if (breakingGroundPiercing != null) {
                        terrainShapeNode.addGroundSlopeConnections(setupSlopeGroundConnection(terrainRect, breakingGroundPiercing, slope.getInnerGroundHeight(), false, null, 0), slope.getSlopeSkeletonConfig().getId());
                        terrainShapeNode.addGroundSlopeConnections(setupSlopeGroundConnection(terrainRect, fractalDriveway.setupPiercingLine(terrainRect, false), slope.getInnerGroundHeight(), false, fractalDriveway, slope.getOuterGroundHeight()), slope.getSlopeSkeletonConfig().getId());
                    } else {
                        if (innerPiercings != null) {
                            for (List<DecimalPosition> innerPiercing : innerPiercings) {
                                terrainShapeNode.addGroundSlopeConnections(setupSlopeGroundConnection(terrainRect, innerPiercing, slope.getInnerGroundHeight(), false, fractalDriveway, slope.getOuterGroundHeight()), slope.getSlopeSkeletonConfig().getId());
                            }
                        }
                    }
                } else {
                    // Inner slope ground connection
                    if (innerPiercings != null) {
                        for (List<DecimalPosition> innerPiercing : innerPiercings) {
                            terrainShapeNode.addGroundSlopeConnections(setupSlopeGroundConnection(terrainRect, innerPiercing, slope.getInnerGroundHeight(), false, null, 0), slope.getSlopeSkeletonConfig().getId());
                        }
                    }
                }
                // Set height
                terrainShape.getOrCreateTerrainShapeNode(nodeIndex).setInnerGroundHeight(slope.getInnerGroundHeight());
            }
            for (Index nodeIndex : innerRasterizer.getInnerTiles()) {
                completeUnderSlope.remove(nodeIndex);
                Rectangle2D terrainRect = TerrainUtil.toAbsoluteNodeRectangle(nodeIndex);
                Driveway fractalDriveway = slope.getDrivewayIfInsideOrTouching(terrainRect);
                // Inner driveway break line
                if (fractalDriveway != null) {
                    List<DecimalPosition> breakingGroundPiercing = fractalDriveway.setupPiercingLine(terrainRect, true);
                    if (breakingGroundPiercing != null) {
                        TerrainShapeNode terrainShapeNode = terrainShape.getOrCreateTerrainShapeNode(nodeIndex);
                        terrainShapeNode.setDrivewayBreakingLine(true);
                        terrainShapeNode.addGroundSlopeConnections(setupSlopeGroundConnection(terrainRect, breakingGroundPiercing, slope.getInnerGroundHeight(), false, null, 0), slope.getSlopeSkeletonConfig().getId());
                        terrainShapeNode.addGroundSlopeConnections(setupSlopeGroundConnection(terrainRect, fractalDriveway.setupPiercingLine(terrainRect, false), slope.getInnerGroundHeight(), false, fractalDriveway, slope.getOuterGroundHeight()), slope.getSlopeSkeletonConfig().getId());
                    }
                    terrainShape.getOrCreateTerrainShapeNode(nodeIndex).setInnerGroundHeight(slope.getOuterGroundHeight()); // TODO replace with innerGroundHeight?
                }

                // TerrainNode completely in driveway
                Driveway driveway = slope.getDriveway(terrainRect.shrink(0.01).toCorners());
                if (driveway != null) {
                    TerrainShapeNode terrainShapeNode = terrainShape.getOrCreateTerrainShapeNode(nodeIndex);
                    terrainShapeNode.setDrivewayHeights(driveway.generateDrivewayHeights(terrainRect.toCorners()));
                    terrainShapeNode.setFullRenderEngineDriveway(true);
                    terrainShape.getOrCreateTerrainShapeNode(nodeIndex).setInnerGroundHeight(slope.getOuterGroundHeight()); // TODO replace with innerGroundHeight?
                } else {
                    terrainShape.getOrCreateTerrainShapeNode(nodeIndex).setInnerGroundHeight(slope.getInnerGroundHeight());
                }
            }


            // Completely under slope
            for (Index nodeIndex : completeUnderSlope) {
                terrainShape.getOrCreateTerrainShapeNode(nodeIndex).setInnerGroundHeight(slope.getInnerGroundHeight()); // TODO  Used?
            }
        }
        if (slope.getChildren() != null) {
            for (Slope childSlope : slope.getChildren()) {
                processSlope(childSlope, dirtyTerrainShapeNodes);
            }
        }
    }

    // Rename setup game engine / render engine
    private void setupTerrainType(Polygon2D terrainRegion, Map<Index, TerrainShapeNode> dirtyTerrainShapeNodes, TerrainType innerTerrainType, double innerHeight, DrivewayContext drivewayContext) {
        Polygon2DRasterizer polygon2DRasterizer = Polygon2DRasterizer.create(terrainRegion, TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH);
        if (innerTerrainType != null) {
            for (Index nodeIndex : polygon2DRasterizer.getInnerTiles()) {
                if (drivewayContext != null) {
                    Rectangle2D terrainRect = TerrainUtil.toAbsoluteNodeRectangle(nodeIndex);
                    switch (drivewayContext.checkInside(terrainRect)) {
                        case INSIDE:
                            TerrainShapeNode terrainShapeNode = terrainShape.getOrCreateTerrainShapeNode(nodeIndex);
                            terrainShapeNode.setTerrainType(drivewayContext.getInnerTerrainType());
                            terrainShapeNode.setGameEngineHeight(drivewayContext.getHeight());
                            if (drivewayContext.getType() == DrivewayContext.Type.SLOPE_DRIVEWAY) {
                                terrainShapeNode.setDrivewayHeights(drivewayContext.getDrivewayHeights(terrainRect));
                                terrainShapeNode.setFullGameEngineDriveway(true);
                            }
                            continue;
                        case PARTLY:
                            handlePartly(terrainRegion, dirtyTerrainShapeNodes, innerTerrainType, innerHeight, drivewayContext, nodeIndex, terrainRect);
                            continue;
                    }
                }

                TerrainShapeNode terrainShapeNode = terrainShape.getOrCreateTerrainShapeNode(nodeIndex);
                if (!terrainShapeNode.hasSubNodes()) {
                    terrainShapeNode.setTerrainType(innerTerrainType);
                    terrainShapeNode.setGameEngineHeight(innerHeight);
                }
            }
            for (Index nodeIndex : polygon2DRasterizer.getPiercedTiles()) {
                Rectangle2D terrainRect = TerrainUtil.toAbsoluteNodeRectangle(nodeIndex);
                if (drivewayContext != null) {
                    switch (drivewayContext.checkInside(terrainRect)) {
                        case INSIDE:
                            TerrainShapeNode terrainShapeNode = terrainShape.getOrCreateTerrainShapeNode(nodeIndex);
                            terrainShapeNode.setTerrainType(drivewayContext.getInnerTerrainType());
                            terrainShapeNode.setGameEngineHeight(drivewayContext.getHeight());
                            if (drivewayContext.getType() == DrivewayContext.Type.SLOPE_DRIVEWAY) {
                                terrainShapeNode.setDrivewayHeights(drivewayContext.getDrivewayHeights(terrainRect));
                                terrainShapeNode.setFullGameEngineDriveway(true);
                            }
                            continue;
                        case PARTLY:
                            handlePartly(terrainRegion, dirtyTerrainShapeNodes, innerTerrainType, innerHeight, drivewayContext, nodeIndex, terrainRect);
                            continue;
                    }
                }
                handlePartly(terrainRegion, dirtyTerrainShapeNodes, innerTerrainType, innerHeight, drivewayContext, nodeIndex, terrainRect);
            }
        }
    }

    private void handlePartly(Polygon2D terrainRegion, Map<Index, TerrainShapeNode> dirtyTerrainShapeNodes, TerrainType innerTerrainType, double innerHeight, DrivewayContext drivewayContext, Index nodeIndex, Rectangle2D terrainRect) {
        TerrainShapeNode terrainShapeNode = terrainShape.getOrCreateTerrainShapeNode(nodeIndex);
        dirtyTerrainShapeNodes.putIfAbsent(nodeIndex, terrainShapeNode);
        terrainShapeSubNodeFactory.fillSlopeTerrainShapeSubNode(terrainShapeNode, terrainRect, terrainRegion, innerTerrainType, innerHeight, drivewayContext);
    }

    private void prepareVerticalSegments(Slope slope) {
        // This can may be used for performance bust. After Bewusstseinsveränderung

        // Find start
        int start = -1;
        for (int i = 0; i < slope.getVerticalSegments().size(); i++) {
            Index currentTileIndex = TerrainUtil.toTile(CollectionUtils.getCorrectedElement(i, slope.getVerticalSegments()).getInner());
            Index predecessorTileIndex = TerrainUtil.toTile(CollectionUtils.getCorrectedElement(i - 1, slope.getVerticalSegments()).getInner());
            if (!currentTileIndex.equals(predecessorTileIndex)) {
                start = i;
                break;
            }
        }
        if (start < 0) {
            // Slope is completely inside a tile
            FractionalSlope fractionalSlope = new FractionalSlope();
            fractionalSlope.setSlopeSkeletonConfigId(slope.getSlopeSkeletonConfig().getId());
            if (slope.isInverted()) {
                fractionalSlope.setGroundHeight(slope.getInnerGroundHeight());
            } else {
                fractionalSlope.setGroundHeight(slope.getOuterGroundHeight());
            }
            fractionalSlope.setInverted(slope.isInverted());
            List<FractionalSlopeSegment> fractionalSlopeSegments = new ArrayList<>();
            fractionalSlopeSegments.add(FractionalSlopeSegment.fromVerticalSegment(CollectionUtils.getCorrectedElement(-1, slope.getVerticalSegments()))); // Norm tangent extra. Used in light calculation
            slope.getVerticalSegments().forEach(verticalSegment -> fractionalSlopeSegments.add(FractionalSlopeSegment.fromVerticalSegment(verticalSegment)));
            fractionalSlopeSegments.add(FractionalSlopeSegment.fromVerticalSegment(CollectionUtils.getCorrectedElement(0, slope.getVerticalSegments()))); // Connect end to start
            fractionalSlopeSegments.add(FractionalSlopeSegment.fromVerticalSegment(CollectionUtils.getCorrectedElement(1, slope.getVerticalSegments()))); // Norm tangent extra. Used in light calculation
            fractionalSlope.setFractionalSlopeSegments(fractionalSlopeSegments);
            terrainShape.getOrCreateTerrainShapeTile(TerrainUtil.toTile(slope.getVerticalSegments().get(0).getInner())).addFractionalSlope(fractionalSlope);
        } else {
            // Slope is in multiply tiles
            for (int i = 0; i < slope.getVerticalSegments().size(); i++) {
                DoubleHolder<FractionalSlope, Integer> holder = findFractionalSlope(slope, slope.getVerticalSegments(), start + i);
                Index currentTileIndex = TerrainUtil.toTile(CollectionUtils.getCorrectedElement(start + i, slope.getVerticalSegments()).getInner());
                terrainShape.getOrCreateTerrainShapeTile(currentTileIndex).addFractionalSlope(holder.getO1());
                i = holder.getO2() - start;
            }
        }
    }

    private DoubleHolder<FractionalSlope, Integer> findFractionalSlope(Slope slope, List<VerticalSegment> verticalSegments, int startIndex) {
        VerticalSegment predecessor = CollectionUtils.getCorrectedElement(startIndex - 1, verticalSegments);
        VerticalSegment start = CollectionUtils.getCorrectedElement(startIndex, verticalSegments);
        Rectangle2D absoluteTerrainTileRect = TerrainUtil.toAbsoluteTileRectangle(start.getInner());

        // find start
        double totalDistance = start.getInner().getDistance(predecessor.getInner());
        Collection<DecimalPosition> crossPoints = absoluteTerrainTileRect.getCrossPointsLine(new Line(start.getInner(), predecessor.getInner()));
        if (crossPoints.size() == 2) {
            Iterator<DecimalPosition> iterator = crossPoints.iterator();
            if (!iterator.next().equalsDelta(iterator.next(), 0.001)) {
                throw new IllegalStateException("Exactly one cross point expected in end. Delta too big: " + crossPoints.size());
            }
        } else if (crossPoints.size() != 1) {
            throw new IllegalStateException("Exactly one cross point expected in start finding: " + crossPoints.size());
        }
        double innerDistance = CollectionUtils.getFirst(crossPoints).getDistance(start.getInner());
        List<FractionalSlopeSegment> fractionalSlopeSegments = new ArrayList<>();
        if (innerDistance * 2.0 > totalDistance) {
            // Norm tangent extra. Used in light calculation
            fractionalSlopeSegments.add(FractionalSlopeSegment.fromVerticalSegment(CollectionUtils.getCorrectedElement(startIndex - 2, verticalSegments)));
            fractionalSlopeSegments.add(FractionalSlopeSegment.fromVerticalSegment(CollectionUtils.getCorrectedElement(startIndex - 1, verticalSegments)));
        } else {
            // Norm tangent extra. Used in light calculation
            fractionalSlopeSegments.add(FractionalSlopeSegment.fromVerticalSegment(CollectionUtils.getCorrectedElement(startIndex - 1, verticalSegments)));
        }

        // insert
        for (int i = 0; i < verticalSegments.size(); i++) {
            VerticalSegment current = CollectionUtils.getCorrectedElement(i + startIndex, verticalSegments);
            fractionalSlopeSegments.add(FractionalSlopeSegment.fromVerticalSegment(current));
            // Check for end
            VerticalSegment successor = CollectionUtils.getCorrectedElement(i + startIndex + 1, verticalSegments);
            if (!TerrainUtil.toTile(current.getInner()).equals(TerrainUtil.toTile(successor.getInner()))) {
                totalDistance = current.getInner().getDistance(successor.getInner());
                crossPoints = absoluteTerrainTileRect.getCrossPointsLine(new Line(current.getInner(), successor.getInner()));
                if (crossPoints.size() == 2) {
                    Iterator<DecimalPosition> iterator = crossPoints.iterator();
                    if (!iterator.next().equalsDelta(iterator.next(), 0.001)) {
                        throw new IllegalStateException("Exactly one cross point expected in end. Delta too big: " + crossPoints.size());
                    }
                } else if (crossPoints.size() != 1) {
                    throw new IllegalStateException("Exactly one cross point expected in end: " + crossPoints.size());
                }
                innerDistance = CollectionUtils.getFirst(crossPoints).getDistance(current.getInner());
                if (innerDistance * 2.0 > totalDistance) {
                    fractionalSlopeSegments.add(FractionalSlopeSegment.fromVerticalSegment(successor));
                    // Norm tangent extra. Used in light calculation
                    fractionalSlopeSegments.add(FractionalSlopeSegment.fromVerticalSegment(CollectionUtils.getCorrectedElement(i + startIndex + 2, verticalSegments)));
                } else {
                    // Norm tangent extra. Used in light calculation
                    fractionalSlopeSegments.add(FractionalSlopeSegment.fromVerticalSegment(successor));
                }
                FractionalSlope fractionalSlope = new FractionalSlope();
                fractionalSlope.setSlopeSkeletonConfigId(slope.getSlopeSkeletonConfig().getId());
                if (slope.isInverted()) {
                    fractionalSlope.setGroundHeight(slope.getInnerGroundHeight());
                } else {
                    fractionalSlope.setGroundHeight(slope.getOuterGroundHeight());
                }
                fractionalSlope.setInverted(slope.isInverted());
                fractionalSlope.setFractionalSlopeSegments(fractionalSlopeSegments);
                return new DoubleHolder<>(fractionalSlope, i + startIndex);
            }
        }

        throw new IllegalStateException("TerrainShapeSetup.findFractionalSlope() end not found");
    }

    private List<Vertex> setupSlopeGroundConnection(Rectangle2D absoluteRect, List<DecimalPosition> piercingLine, double groundHeight, boolean water, Driveway driveway, double drivewayBaseHeight) {
        if (water) {
            piercingLine = new ArrayList<>(piercingLine);
            Collections.reverse(piercingLine);
        }
        List<Vertex> polygon = new ArrayList<>();

        RectanglePiercing startRectanglePiercing;
        RectanglePiercing endRectanglePiercing;
        if (piercingLine.size() == 2) {
            // This is a left out node
            Line crossLine = new Line(piercingLine.get(0), piercingLine.get(1));
            Collection<DecimalPosition> crossPoints = absoluteRect.getCrossPointsLine(crossLine);
            if (crossPoints.size() == 1) {
                // Goes exactly through the corner -> return. This is may wrong
                return null;
            } else if (crossPoints.size() != 2) {
                throw new IllegalStateException("Exactly two cross points expected: " + crossPoints.size());
            }
            DecimalPosition start = DecimalPosition.getNearestPoint(piercingLine.get(0), crossPoints);
            startRectanglePiercing = getRectanglePiercing(absoluteRect, start);
            DecimalPosition end = DecimalPosition.getFurthestPoint(piercingLine.get(0), crossPoints);
            endRectanglePiercing = getRectanglePiercing(absoluteRect, end);
        } else {
            Line startLine = new Line(piercingLine.get(0), piercingLine.get(1));
            startRectanglePiercing = getRectanglePiercing(absoluteRect, startLine, piercingLine.get(0));

            Line endLine = new Line(piercingLine.get(piercingLine.size() - 2), piercingLine.get(piercingLine.size() - 1));
            endRectanglePiercing = getRectanglePiercing(absoluteRect, endLine, piercingLine.get(piercingLine.size() - 1));
        }

        addOnlyXyUnique(polygon, toVertexSlope(startRectanglePiercing.getCross(), driveway, drivewayBaseHeight, groundHeight));
        Side side = startRectanglePiercing.getSide();
        if (startRectanglePiercing.getSide() == endRectanglePiercing.getSide()) {
            if (!startRectanglePiercing.getSide().isBefore(startRectanglePiercing.getCross(), endRectanglePiercing.getCross())) {
                addOnlyXyUnique(polygon, toVertexGround(getSuccessorCorner(absoluteRect, side), driveway, drivewayBaseHeight, groundHeight, water));
                side = side.getSuccessor();
            }
        }

        while (side != endRectanglePiercing.side) {
            addOnlyXyUnique(polygon, toVertexGround(getSuccessorCorner(absoluteRect, side), driveway, drivewayBaseHeight, groundHeight, water));
            side = side.getSuccessor();
        }
        addOnlyXyUnique(polygon, toVertexSlope(endRectanglePiercing.getCross(), driveway, drivewayBaseHeight, groundHeight));

        for (int i = piercingLine.size() - 2; i > 0; i--) {
            addOnlyXyUnique(polygon, toVertexSlope(piercingLine.get(i), driveway, drivewayBaseHeight, groundHeight));
        }

        if (polygon.size() < 3) {
            return null;
        }
        return polygon;
    }

    private void addOnlyXyUnique(List<Vertex> list, Vertex vertex) {
        if (list.isEmpty()) {
            list.add(vertex);
            return;
        }
        DecimalPosition decimalPosition = vertex.toXY();
        for (Vertex existing : list) {
            if (existing.toXY().equals(decimalPosition)) {
                return;
            }
        }
        list.add(vertex);
    }

    private Vertex toVertexGround(DecimalPosition position, Driveway driveway, double drivewayBaseHeight, double groundHeight, boolean water) {
        if (water) {
            return new Vertex(position, groundHeight);
        } else {
            Index nodeTile = TerrainUtil.toNode(position);
            double height;
            if (driveway != null) {
                height = driveway.getInterpolateDrivewayHeight(position) + drivewayBaseHeight;
            } else {
                height = groundHeight + terrainTypeService.getGroundSkeletonConfig().getHeight(nodeTile.getX(), nodeTile.getY());
            }
            return new Vertex(position, height);
        }
    }

    private Vertex toVertexSlope(DecimalPosition position, Driveway driveway, double drivewayBaseHeight, double groundHeight) {
        double height;
        if (driveway != null) {
            height = driveway.getInterpolateDrivewayHeight(position) + drivewayBaseHeight;
        } else {
            height = groundHeight;
        }
        return new Vertex(position, height);
    }


    private RectanglePiercing getRectanglePiercing(Rectangle2D rectangle, DecimalPosition crossPoint) {
        if (rectangle.lineW().isPointInLineInclusive(crossPoint)) {
            return new RectanglePiercing(crossPoint, Side.WEST);
        }
        if (rectangle.lineS().isPointInLineInclusive(crossPoint)) {
            return new RectanglePiercing(crossPoint, Side.SOUTH);
        }
        if (rectangle.lineE().isPointInLineInclusive(crossPoint)) {
            return new RectanglePiercing(crossPoint, Side.EAST);
        }
        if (rectangle.lineN().isPointInLineInclusive(crossPoint)) {
            return new RectanglePiercing(crossPoint, Side.NORTH);
        }
        throw new IllegalArgumentException("getRectanglePiercing should not happen 2");
    }

    private RectanglePiercing getRectanglePiercing(Rectangle2D rectangle, Line line, DecimalPosition reference) {
        int crossPoints = rectangle.getCrossPointsLine(line).size();
        if (crossPoints == 0) {
            throw new IllegalArgumentException("getRectanglePiercing should not happen 1");
        }
        boolean ambiguous = crossPoints > 1;

        double minDistance = Double.MAX_VALUE;
        DecimalPosition bestFitCrossPoint = null;
        Side bestFitSide = null;
        DecimalPosition crossPoint = rectangle.lineW().getCrossInclusive(line);
        if (crossPoint != null) {
            if (ambiguous) {
                double distance = crossPoint.getDistance(reference);
                if (distance < minDistance) {
                    minDistance = distance;
                    bestFitCrossPoint = crossPoint;
                    bestFitSide = Side.WEST;
                }
            } else {
                return new RectanglePiercing(crossPoint, Side.WEST);
            }
        }
        crossPoint = rectangle.lineS().getCrossInclusive(line);
        if (crossPoint != null) {
            if (ambiguous) {
                double distance = crossPoint.getDistance(reference);
                if (distance < minDistance) {
                    minDistance = distance;
                    bestFitCrossPoint = crossPoint;
                    bestFitSide = Side.SOUTH;
                }
            } else {
                return new RectanglePiercing(crossPoint, Side.SOUTH);
            }
        }
        crossPoint = rectangle.lineE().getCrossInclusive(line);
        if (crossPoint != null) {
            if (ambiguous) {
                double distance = crossPoint.getDistance(reference);
                if (distance < minDistance) {
                    minDistance = distance;
                    bestFitCrossPoint = crossPoint;
                    bestFitSide = Side.EAST;
                }
            } else {
                return new RectanglePiercing(crossPoint, Side.EAST);
            }
        }
        crossPoint = rectangle.lineN().getCrossInclusive(line);
        if (crossPoint != null) {
            if (ambiguous) {
                double distance = crossPoint.getDistance(reference);
                if (distance < minDistance) {
                    bestFitCrossPoint = crossPoint;
                    bestFitSide = Side.NORTH;
                }
            } else {
                return new RectanglePiercing(crossPoint, Side.NORTH);
            }
        }
        if (ambiguous) {
            return new RectanglePiercing(bestFitCrossPoint, bestFitSide);
        } else {
            throw new IllegalArgumentException("getRectanglePiercing should not happen 2");
        }
    }

    public static class RectanglePiercing {

        private DecimalPosition cross;
        private Side side;

        public RectanglePiercing(DecimalPosition cross, Side side) {
            this.cross = cross;
            this.side = side;
        }

        public DecimalPosition getCross() {
            return cross;
        }

        public Side getSide() {
            return side;
        }
    }


    public enum Side {
        NORTH {
            @Override
            boolean isBefore(DecimalPosition position1, DecimalPosition position2) {
                return position1.getX() > position2.getX();
            }
        },
        WEST {
            @Override
            boolean isBefore(DecimalPosition position1, DecimalPosition position2) {
                return position1.getY() > position2.getY();
            }
        },
        SOUTH {
            @Override
            boolean isBefore(DecimalPosition position1, DecimalPosition position2) {
                return position1.getX() < position2.getX();
            }
        },
        EAST {
            @Override
            boolean isBefore(DecimalPosition position1, DecimalPosition position2) {
                return position1.getY() < position2.getY();
            }
        };

        Side getSuccessor() {
            switch (this) {
                case NORTH:
                    return WEST;
                case WEST:
                    return SOUTH;
                case SOUTH:
                    return EAST;
                case EAST:
                    return NORTH;
                default:
                    throw new IllegalArgumentException("Side don't know how to handle: " + this);
            }
        }

        abstract boolean isBefore(DecimalPosition position1, DecimalPosition position2);
    }

    public DecimalPosition getSuccessorCorner(Rectangle2D rectangle, Side side) {
        switch (side) {
            case NORTH:
                return rectangle.cornerTopLeft();
            case WEST:
                return rectangle.cornerBottomLeft();
            case SOUTH:
                return rectangle.cornerBottomRight();
            case EAST:
                return rectangle.cornerTopRight();
            default:
                throw new IllegalArgumentException("getCorner: don't know how to handle side: " + side);
        }
    }

    private void fillInRenderTerrainObject(Map<Index, MapList<Integer, TerrainObjectPosition>> renderTerrainObjects) {
        renderTerrainObjects.forEach((tileIndex, terrainObjectGroup) -> {
            NativeTerrainShapeObjectList[] nativeTerrainShapeObjectLists = new NativeTerrainShapeObjectList[terrainObjectGroup.getMap().size()];
            int terrainObjectIdIndex = 0;
            for (Map.Entry<Integer, List<TerrainObjectPosition>> entry : terrainObjectGroup.getMap().entrySet()) {
                NativeTerrainShapeObjectList nativeTerrainShapeObjectList = new NativeTerrainShapeObjectList();
                nativeTerrainShapeObjectList.terrainObjectId = entry.getKey();
                nativeTerrainShapeObjectList.positions = new NativeTerrainShapeObjectPosition[entry.getValue().size()];
                for (int positionIndex = 0; positionIndex < entry.getValue().size(); positionIndex++) {
                    TerrainObjectPosition terrainObjectPosition = entry.getValue().get(positionIndex);
                    NativeTerrainShapeObjectPosition nativeTerrainShapeObjectPosition = new NativeTerrainShapeObjectPosition();
                    nativeTerrainShapeObjectPosition.x = terrainObjectPosition.getPosition().getX();
                    nativeTerrainShapeObjectPosition.y = terrainObjectPosition.getPosition().getY();
                    nativeTerrainShapeObjectPosition.scale = terrainObjectPosition.getScale();
                    nativeTerrainShapeObjectPosition.rotationZ = terrainObjectPosition.getRotationZ();
                    nativeTerrainShapeObjectList.positions[positionIndex] = nativeTerrainShapeObjectPosition;
                }
                nativeTerrainShapeObjectLists[terrainObjectIdIndex++] = nativeTerrainShapeObjectList;
            }
            terrainShape.getOrCreateTerrainShapeTile(tileIndex).setNativeTerrainShapeObjectLists(nativeTerrainShapeObjectLists);
        });
    }

}
