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
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.dto.WaterConfig;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleTerrainObject;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeObjectList;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeObjectPosition;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeVertex;
import com.btxtech.shared.gameengine.planet.terrain.slope.Driveway;
import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;
import com.btxtech.shared.gameengine.planet.terrain.slope.VerticalSegment;
import com.btxtech.shared.system.alarm.Alarm;
import com.btxtech.shared.system.alarm.AlarmService;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.shared.utils.GeometricUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 22.06.2017.
 */
public class TerrainShapeManagerSetup {
    private Logger logger = Logger.getLogger(TerrainShapeManagerSetup.class.getName());
    private TerrainShapeManager terrainShape;
    private TerrainTypeService terrainTypeService;
    private AlarmService alarmService;
    private TerrainShapeSubNodeFactory terrainShapeSubNodeFactory;
    private Map<Index, TerrainShapeNode> dirtyTerrainShapeNodes = new HashMap<>();

    public TerrainShapeManagerSetup(TerrainShapeManager terrainShape, TerrainTypeService terrainTypeService, AlarmService alarmService) {
        this.terrainShape = terrainShape;
        this.terrainTypeService = terrainTypeService;
        this.alarmService = alarmService;
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
            tileObjects.put(objectPosition.getTerrainObjectConfigId(), objectPosition);
            // Game engine
            TerrainObjectConfig terrainObjectConfig = terrainTypeService.getTerrainObjectConfig(objectPosition.getTerrainObjectConfigId());
            Circle2D terrainObjectCircle = new Circle2D(objectPosition.getPosition(), terrainObjectConfig.getRadius() * calculateScale(objectPosition.getScale()));
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

    private double calculateScale(Vertex scale) {
        if (scale == null) {
            return 1;
        }
        return Math.max(scale.getX(), scale.getY());
    }

    public void processSlopes(List<TerrainSlopePosition> terrainSlopePositions) {
        if (terrainSlopePositions == null) {
            return;
        }
        long time = System.currentTimeMillis();
        for (TerrainSlopePosition terrainSlopePosition : terrainSlopePositions) {
            try {
                processSlope(setupSlope(terrainSlopePosition, 0), null, dirtyTerrainShapeNodes);
            } catch (Exception e) {
                alarmService.riseAlarm(Alarm.Type.TERRAIN_SHAPE_FAILED_SLOPE_POSITION, terrainSlopePosition.getId());
                logger.log(Level.WARNING, "Can not handle slope with id: " + terrainSlopePosition.getId(), e);
            }
        }
        logger.severe("Generate Slopes: " + (System.currentTimeMillis() - time));
    }

    public void finish() {
        terrainShapeSubNodeFactory.concentrate(dirtyTerrainShapeNodes);
    }

    private Slope setupSlope(TerrainSlopePosition terrainSlopePosition, double groundHeight) {
        SlopeConfig slopeConfig = terrainTypeService.getSlopeConfig(terrainSlopePosition.getSlopeConfigId());
        WaterConfig waterConfig = slopeConfig.hasWaterConfigId() ? terrainTypeService.getWaterConfig(slopeConfig.getWaterConfigId()) : null;
        Slope slope = new Slope(terrainSlopePosition.getId(), slopeConfig, waterConfig, terrainSlopePosition.isInverted(), terrainSlopePosition.getPolygon(), groundHeight, terrainTypeService);
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

    private void processSlope(Slope slope, Slope parent, Map<Index, TerrainShapeNode> dirtyTerrainShapeNodes) {
        prepareVerticalSegments(slope);
        List<DecimalPosition> tmpInnerGameEnginePolygon = new ArrayList<>(slope.getInnerGameEnginePolygon().getCorners());
        Collections.reverse(tmpInnerGameEnginePolygon);
        ObstacleFactory.addObstacles(terrainShape, slope);
        SlopeContext slopeContext = new SlopeContext(slope);
        SlopeGroundConnectionContextHelper.prepareContextGroundSlopeConnection(slope.getInnerRenderEnginePolygon().getCorners(), false, slopeContext);
        SlopeGroundConnectionContextHelper.prepareContextGroundSlopeConnection(slope.getOuterRenderEnginePolygon().getCorners(), true, slopeContext);
        if (slope.hasWater()) {
            double waterLevel = terrainTypeService.getWaterConfig(slope.getSlopeConfig().getWaterConfigId()).getWaterLevel();
            // Setup TerrainType (game engine)
            if (!slope.isInverted()) {
                setupTerrainType(slope.getOuterGameEnginePolygon(), dirtyTerrainShapeNodes, TerrainType.LAND_COAST, slope.getOuterGroundHeight(), null);
                setupTerrainType(slope.getCoastDelimiterPolygonTerrainType(), dirtyTerrainShapeNodes, TerrainType.WATER_COAST, slope.getOuterGroundHeight() + waterLevel, null);
                setupTerrainType(slope.getInnerGameEnginePolygon(), dirtyTerrainShapeNodes, TerrainType.WATER, slope.getOuterGroundHeight() + waterLevel, null);
                // Setup slope ground connection (render engine)
                Polygon2DRasterizer outerRasterizer = Polygon2DRasterizer.create(slope.getOuterRenderEnginePolygon(), TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH);
                for (Index nodeIndex : outerRasterizer.getPiercedTiles()) {
                    TerrainShapeNode terrainShapeNode = terrainShape.getOrCreateTerrainShapeNode(nodeIndex);
                    terrainShapeNode.setRenderHideGround(true);
                    // Outer slope ground connection
                    List<List<DecimalPosition>> outerPiercings = slopeContext.getOuterPiercings(nodeIndex);
                    Rectangle2D terrainRect = TerrainUtil.toAbsoluteNodeRectangle(nodeIndex);
                    terrainShapeNode.addGroundSlopeConnections(
                            SlopeGroundConnectionFactory.setupSlopeGroundConnection(terrainRect, outerPiercings, slope.getOuterGroundHeight(), false, null, 0),
                            parent != null ? parent.getSlopeConfig().getGroundConfigId() : null);
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
                    terrainShapeNode.setRenderGroundId(slope.getSlopeConfig().getGroundConfigId());
                    terrainShapeNode.setInnerGroundHeight(slope.getInnerGroundHeight());
                    terrainShapeNode.setRenderInnerWaterSlopeId(slope.getSlopeConfig().getId());
                    terrainShapeNode.setRenderFullWaterLevel(slope.getOuterGroundHeight() + waterLevel);
                }
                // Setup slope inner seabed connection (render engine)
                for (Index nodeIndex : innerRasterizer.getPiercedTiles()) {
                    TerrainShapeNode terrainShapeNode = terrainShape.getOrCreateTerrainShapeNode(nodeIndex);
                    terrainShapeNode.setRenderHideGround(true);
                    List<List<DecimalPosition>> innerPiercings = slopeContext.getInnerPiercings(nodeIndex);
                    Rectangle2D terrainRect = TerrainUtil.toAbsoluteNodeRectangle(nodeIndex);
                    terrainShapeNode.addWaterSegments(SlopeGroundConnectionFactory.setupSlopeGroundConnection(terrainRect, innerPiercings, slope.getOuterGroundHeight() + waterLevel, false, null, 0), slope.getSlopeConfig().getId());
                    terrainShapeNode.addGroundSlopeConnections(SlopeGroundConnectionFactory.setupSlopeGroundConnection(terrainRect, innerPiercings, slope.getInnerGroundHeight(), false, null, 0), slope.getSlopeConfig().getGroundConfigId());
                }

            } else {
                setupTerrainType(slope.getOuterGameEnginePolygon(), dirtyTerrainShapeNodes, TerrainType.WATER_COAST, slope.getInnerGroundHeight() + waterLevel, null);
                setupTerrainType(slope.getCoastDelimiterPolygonTerrainType(), dirtyTerrainShapeNodes, TerrainType.LAND_COAST, slope.getInnerGroundHeight(), null);
                setupTerrainType(slope.getInnerGameEnginePolygon(), dirtyTerrainShapeNodes, TerrainType.LAND, slope.getInnerGroundHeight(), null);
                // Setup slope ground connection (render engine)
                Polygon2DRasterizer innerRasterizer = Polygon2DRasterizer.create(slope.getInnerRenderEnginePolygon(), TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH);
                for (Index nodeIndex : innerRasterizer.getPiercedTiles()) {
                    List<List<DecimalPosition>> innerPiercings = slopeContext.getInnerPiercings(nodeIndex);
                    if (innerPiercings != null) {
                        TerrainShapeNode terrainShapeNode = terrainShape.getOrCreateTerrainShapeNode(nodeIndex);
                        Rectangle2D terrainRect = TerrainUtil.toAbsoluteNodeRectangle(nodeIndex);
                        terrainShapeNode.addWaterSegments(SlopeGroundConnectionFactory.setupSlopeGroundConnection(terrainRect, innerPiercings, slope.getOuterGroundHeight() + waterLevel, true, null, 0), slope.getSlopeConfig().getId());
                        terrainShapeNode.addGroundSlopeConnections(SlopeGroundConnectionFactory.setupSlopeGroundConnection(terrainRect, innerPiercings, slope.getInnerGroundHeight(), false, null, 0), slope.getSlopeConfig().getGroundConfigId());
                    }
                }
                for (Index nodeIndex : innerRasterizer.getInnerTiles()) {
                    TerrainShapeNode terrainShapeNode = terrainShape.getOrCreateTerrainShapeNode(nodeIndex);
                    terrainShapeNode.setFullRenderEngineDriveway(false);
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
            for (Index nodeIndex : outerRasterizer.getPiercedTiles()) {
                TerrainShapeNode terrainShapeNode = terrainShape.getOrCreateTerrainShapeNode(nodeIndex);
                terrainShapeNode.setRenderHideGround(true);
                // Outer slope ground connection
                List<List<DecimalPosition>> outerPiercings = slopeContext.getOuterPiercings(nodeIndex);
                    Rectangle2D terrainRect = TerrainUtil.toAbsoluteNodeRectangle(nodeIndex);
                    terrainShapeNode.addGroundSlopeConnections(
                            SlopeGroundConnectionFactory.setupSlopeGroundConnection(terrainRect, outerPiercings, slope.getOuterGroundHeight(), false, null, 0),
                            parent != null ? parent.getSlopeConfig().getGroundConfigId() : null);
            }
            for (Index nodeIndex : outerRasterizer.getInnerTiles()) {
                TerrainShapeNode terrainShapeNode = terrainShape.getOrCreateTerrainShapeNode(nodeIndex);
                terrainShapeNode.setRenderHideGround(true);
            }

            Polygon2DRasterizer innerRasterizer = Polygon2DRasterizer.create(slope.getInnerRenderEnginePolygon(), TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH);
            for (Index nodeIndex : innerRasterizer.getInnerTiles()) {
                TerrainShapeNode terrainShapeNode = terrainShape.getOrCreateTerrainShapeNode(nodeIndex);
                terrainShapeNode.setRenderHideGround(false);
                terrainShapeNode.setRenderGroundId(slope.getSlopeConfig().getGroundConfigId());
                terrainShapeNode.setInnerGroundHeight(slope.getInnerGroundHeight());
                Rectangle2D terrainRect = TerrainUtil.toAbsoluteNodeRectangle(nodeIndex);
                Driveway fractalDriveway = slope.getDrivewayIfInsideOrTouching(terrainRect);
                // Inner driveway break line
                if (fractalDriveway != null) {
                    List<DecimalPosition> breakingGroundPiercing = fractalDriveway.setupPiercingLine(terrainRect, true);
                    if (breakingGroundPiercing != null) {
                        terrainShapeNode.setDrivewayBreakingLine(true);
                        terrainShapeNode.addGroundSlopeConnections(SlopeGroundConnectionFactory.setupSlopeGroundConnection(terrainRect, Collections.singletonList(breakingGroundPiercing), slope.getInnerGroundHeight(), false, null, 0), slope.getSlopeConfig().getGroundConfigId());
                        terrainShapeNode.addGroundSlopeConnections(SlopeGroundConnectionFactory.setupSlopeGroundConnection(terrainRect, Collections.singletonList(fractalDriveway.setupPiercingLine(terrainRect, false)), slope.getInnerGroundHeight(), false, fractalDriveway, slope.getOuterGroundHeight()), slope.getSlopeConfig().getGroundConfigId());
                    }
                    terrainShapeNode.setInnerGroundHeight(slope.getOuterGroundHeight()); // TODO replace with innerGroundHeight?
                }

                // TerrainNode completely in driveway
                Driveway driveway = slope.getDriveway(terrainRect.shrink(0.01).toCorners());
                if (driveway != null) {
                    terrainShapeNode.setDrivewayHeights(driveway.generateDrivewayHeights(terrainRect.toCorners()));
                    terrainShapeNode.setFullRenderEngineDriveway(true);
                    terrainShape.getOrCreateTerrainShapeNode(nodeIndex).setInnerGroundHeight(slope.getOuterGroundHeight()); // TODO replace with innerGroundHeight?
                }
            }
            for (Index nodeIndex : innerRasterizer.getPiercedTiles()) {
                TerrainShapeNode terrainShapeNode = terrainShape.getOrCreateTerrainShapeNode(nodeIndex);
                terrainShapeNode.setRenderHideGround(true);
                Rectangle2D terrainRect = TerrainUtil.toAbsoluteNodeRectangle(nodeIndex);
                List<List<DecimalPosition>> innerPiercings = slopeContext.getInnerPiercings(nodeIndex);
                Driveway fractalDriveway = slope.getDrivewayIfInsideOrTouching(terrainRect);
                if (fractalDriveway != null) {
                    // Inner slope ground connection regarding driveway break line
                    List<DecimalPosition> breakingGroundPiercing = fractalDriveway.setupPiercingLine(terrainRect, true);
                    if (breakingGroundPiercing != null) {
                        terrainShapeNode.addGroundSlopeConnections(SlopeGroundConnectionFactory.setupSlopeGroundConnectionBreakingLine(terrainRect, innerPiercings, slope.getInnerGroundHeight(), breakingGroundPiercing, false, null, 0), slope.getSlopeConfig().getGroundConfigId());
                        terrainShapeNode.addGroundSlopeConnections(SlopeGroundConnectionFactory.setupSlopeGroundConnectionBreakingLine(terrainRect, innerPiercings, slope.getInnerGroundHeight(), fractalDriveway.setupPiercingLine(terrainRect, false), false, fractalDriveway, slope.getOuterGroundHeight()), slope.getSlopeConfig().getGroundConfigId());
                    } else {
                        terrainShapeNode.addGroundSlopeConnections(SlopeGroundConnectionFactory.setupSlopeGroundConnection(terrainRect, innerPiercings, slope.getInnerGroundHeight(), false, fractalDriveway, slope.getOuterGroundHeight()), slope.getSlopeConfig().getGroundConfigId());
                    }
                } else {
                    // Inner slope ground connection
                    terrainShapeNode.addGroundSlopeConnections(SlopeGroundConnectionFactory.setupSlopeGroundConnection(terrainRect, innerPiercings, slope.getInnerGroundHeight(), false, null, 0), slope.getSlopeConfig().getGroundConfigId());
                }
            }
        }
        if (slope.getChildren() != null) {
            for (Slope childSlope : slope.getChildren()) {
                processSlope(childSlope, slope, dirtyTerrainShapeNodes);
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
                    terrainShapeNode.setGameEngineFullWaterLevel(innerHeight);
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
            fractionalSlope.setSlopeConfigId(slope.getSlopeConfig().getId());
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
                fractionalSlope.setSlopeConfigId(slope.getSlopeConfig().getId());
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


    private void fillInRenderTerrainObject(Map<Index, MapList<Integer, TerrainObjectPosition>> renderTerrainObjects) {
        renderTerrainObjects.forEach((tileIndex, terrainObjectGroup) -> {
            NativeTerrainShapeObjectList[] nativeTerrainShapeObjectLists = new NativeTerrainShapeObjectList[terrainObjectGroup.getMap().size()];
            int terrainObjectIdIndex = 0;
            for (Map.Entry<Integer, List<TerrainObjectPosition>> entry : terrainObjectGroup.getMap().entrySet()) {
                NativeTerrainShapeObjectList nativeTerrainShapeObjectList = new NativeTerrainShapeObjectList();
                nativeTerrainShapeObjectList.terrainObjectConfigId = entry.getKey();
                nativeTerrainShapeObjectList.terrainShapeObjectPositions = new NativeTerrainShapeObjectPosition[entry.getValue().size()];
                for (int positionIndex = 0; positionIndex < entry.getValue().size(); positionIndex++) {
                    TerrainObjectPosition terrainObjectPosition = entry.getValue().get(positionIndex);
                    NativeTerrainShapeObjectPosition nativeTerrainShapeObjectPosition = new NativeTerrainShapeObjectPosition();
                    nativeTerrainShapeObjectPosition.terrainObjectId = terrainObjectPosition.getId();
                    nativeTerrainShapeObjectPosition.x = terrainObjectPosition.getPosition().getX();
                    nativeTerrainShapeObjectPosition.y = terrainObjectPosition.getPosition().getY();
                    if (terrainObjectPosition.getScale() != null) {
                        nativeTerrainShapeObjectPosition.scale = new NativeVertex();
                        nativeTerrainShapeObjectPosition.scale.x = terrainObjectPosition.getScale().getX();
                        nativeTerrainShapeObjectPosition.scale.y = terrainObjectPosition.getScale().getY();
                        nativeTerrainShapeObjectPosition.scale.z = terrainObjectPosition.getScale().getZ();
                    }
                    if (terrainObjectPosition.getRotation() != null) {
                        nativeTerrainShapeObjectPosition.rotation = new NativeVertex();
                        nativeTerrainShapeObjectPosition.rotation.x = terrainObjectPosition.getRotation().getX();
                        nativeTerrainShapeObjectPosition.rotation.y = terrainObjectPosition.getRotation().getY();
                        nativeTerrainShapeObjectPosition.rotation.z = terrainObjectPosition.getRotation().getZ();
                    }
                    if (terrainObjectPosition.getOffset() != null) {
                        nativeTerrainShapeObjectPosition.offset = new NativeVertex();
                        nativeTerrainShapeObjectPosition.offset.x = terrainObjectPosition.getOffset().getX();
                        nativeTerrainShapeObjectPosition.offset.y = terrainObjectPosition.getOffset().getY();
                        nativeTerrainShapeObjectPosition.offset.z = terrainObjectPosition.getOffset().getZ();
                    }
                    nativeTerrainShapeObjectList.terrainShapeObjectPositions[positionIndex] = nativeTerrainShapeObjectPosition;
                }
                nativeTerrainShapeObjectLists[terrainObjectIdIndex++] = nativeTerrainShapeObjectList;
            }
            terrainShape.getOrCreateTerrainShapeTile(tileIndex).setNativeTerrainShapeObjectLists(nativeTerrainShapeObjectLists);
        });
    }

}
