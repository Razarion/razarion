package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line3d;
import com.btxtech.shared.datatypes.MapCollection;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.terrain.GroundUi;
import com.btxtech.shared.datatypes.terrain.SlopeUi;
import com.btxtech.shared.datatypes.terrain.WaterUi;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.utils.MathHelper;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.control.GameUiControlInitEvent;
import com.btxtech.uiservice.renderer.RenderServiceInitEvent;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 09.08.2015.
 */
@Singleton
public class TerrainUiService {
    private Logger logger = Logger.getLogger(TerrainUiService.class.getName());
    @Inject
    private TerrainTypeService terrainTypeService;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private GameEngineControl gameEngineControl;
    private static final double HIGHEST_POINT_IN_VIEW = 20;
    private static final double LOWEST_POINT_IN_VIEW = -2;
    private double highestPointInView; // Should be calculated
    private double lowestPointInView; // Should be calculated
    private MapCollection<TerrainObjectConfig, ModelMatrices> terrainObjectConfigModelMatrices;
    private Map<Integer, SlopeUi> slopeUis = new HashMap<>();
    private GroundUi groundUi;
    private WaterUi waterUi;
    private List<TerrainObjectPosition> terrainObjectPositions;
    private MapCollection<DecimalPosition, BiConsumer<DecimalPosition, Double>> terrainZConsumers = new MapCollection<>();
    private MapCollection<Line3d, Consumer<Vertex>> worldPickRayConsumers = new MapCollection<>();
    private MapCollection<DecimalPosition, Consumer<Boolean>> overlapConsumers = new MapCollection<>();
    private Map<Integer, Consumer<Boolean>> overlapTypeConsumers = new HashMap<>();

    public TerrainUiService() {
        highestPointInView = HIGHEST_POINT_IN_VIEW;
        lowestPointInView = LOWEST_POINT_IN_VIEW;
    }

    public void onGameUiControlInitEvent(@Observes GameUiControlInitEvent gameUiControlInitEvent) {
        slopeUis.clear();
        for (TerrainSlopePosition terrainSlopePosition : gameUiControlInitEvent.getGameUiControlConfig().getGameEngineConfig().getPlanetConfig().getTerrainSlopePositions()) {
            int id = terrainSlopePosition.getSlopeId();
            slopeUis.put(id, new SlopeUi(id, terrainTypeService.getSlopeSkeleton(id), gameUiControlInitEvent.getGameUiControlConfig().getGameEngineConfig().getPlanetConfig().getWaterLevel(), gameUiControlInitEvent.getGameUiControlConfig().getGameEngineConfig().getGroundSkeletonConfig()));
        }
        groundUi = new GroundUi(gameUiControlInitEvent.getGameUiControlConfig().getGameEngineConfig().getGroundSkeletonConfig());
        waterUi = new WaterUi(gameUiControlInitEvent.getGameUiControlConfig().getVisualConfig());
        terrainObjectPositions = gameUiControlInitEvent.getGameUiControlConfig().getGameEngineConfig().getPlanetConfig().getTerrainObjectPositions();
    }

    public void setBuffers(GroundUi groundUi, Collection<SlopeUi> slopeUis, WaterUi waterUi) {
        this.groundUi.setBuffers(groundUi);
        for (SlopeUi slopeUi : slopeUis) {
            this.slopeUis.get(slopeUi.getId()).setBuffers(slopeUi);
        }
        this.waterUi.setBuffers(waterUi);
    }

    public void onRenderServiceInitEvent(@Observes RenderServiceInitEvent renderServiceInitEvent) {
        terrainObjectConfigModelMatrices = new MapCollection<>();
        for (TerrainObjectPosition terrainObjectPosition : terrainObjectPositions) {
            try {
                getTerrainZ(terrainObjectPosition.getPosition(), (position, z) -> {
                    if (z != null) {
                        Matrix4 model = terrainObjectPosition.createModelMatrix(z);
                        terrainObjectConfigModelMatrices.put(terrainTypeService.getTerrainObjectConfig(terrainObjectPosition.getTerrainObjectId()), new ModelMatrices(model));
                    } else {
                        logger.warning("TerrainUiService: Can not place TerrainObjectPosition with id: " + terrainObjectPosition.getId());
                    }
                });
            } catch (Throwable t) {
                exceptionHandler.handleException("Placing terrain object failed", t);
            }
        }
    }

    public double getHighestPointInView() {
        return highestPointInView;
    }

    public double getLowestPointInView() {
        return lowestPointInView;
    }

    public GroundUi getGroundUi() {
        return groundUi;
    }

    public WaterUi getWaterUi() {
        return waterUi;
    }

    public List<ModelMatrices> provideTerrainObjectModelMatrices(TerrainObjectConfig terrainObjectConfig) {
        Collection<ModelMatrices> modelMatrices = terrainObjectConfigModelMatrices.get(terrainObjectConfig);
        if (modelMatrices != null) {
            return new ArrayList<>(modelMatrices);
        } else {
            return Collections.emptyList();
        }
    }

    public double calculateLandWaterProportion(Rectangle2D viewField) {
        if (waterUi.isValid()) {
            return 1.0 - waterUi.getAabb().coverRatio(viewField);
        } else {
            return 0;
        }
    }

    public Collection<SlopeUi> getSlopes() {
        return slopeUis.values();
    }

    public MapCollection<TerrainObjectConfig, TerrainObjectPosition> getTerrainObjectPositions() {
        throw new UnsupportedOperationException("FIXME: The required data is in the worker now");
        // return terrainService.getTerrainObjectPositions();
    }

    public void overlap(DecimalPosition position, Consumer<Boolean> callback) {
        boolean contains = overlapConsumers.containsKey(position);
        overlapConsumers.put(position, callback);
        if (contains) {
            return;
        }
        gameEngineControl.askOverlap(position);
    }

    public void overlap(Collection<DecimalPosition> positions, BaseItemType baseItemType, Consumer<Boolean> callback) {
        int uuid = MathHelper.generateSimpleUuid();
        overlapTypeConsumers.put(uuid, callback);
        gameEngineControl.askOverlapType(uuid, positions, baseItemType.getId());
    }

    public void calculatePositionGroundMesh(Line3d worldPickRay, Consumer<Vertex> positionConsumer) {
        boolean contains = worldPickRayConsumers.containsKey(worldPickRay);
        worldPickRayConsumers.put(worldPickRay, positionConsumer);
        if (contains) {
            return;
        }
        gameEngineControl.askTerrainPosition(worldPickRay);
    }

    public void getTerrainZ(DecimalPosition position, BiConsumer<DecimalPosition, Double> callback) {
        boolean contains = terrainZConsumers.containsKey(position);
        terrainZConsumers.put(position, callback);
        if (contains) {
            return;
        }
        gameEngineControl.askTerrainZ(position);
    }

    public void getTerrainPosition(DecimalPosition position, Consumer<Vertex> callback) {
        getTerrainZ(position, (position1, z) -> callback.accept(new Vertex(position, z)));
    }

    public void onTerrainZAnswer(DecimalPosition position, double z) {
        Collection<BiConsumer<DecimalPosition, Double>> consumers = terrainZConsumers.remove(position);
        for (BiConsumer<DecimalPosition, Double> consumer : consumers) {
            consumer.accept(position, z);
        }
    }

    public void onTerrainZAnswerFail(DecimalPosition position) {
        Collection<BiConsumer<DecimalPosition, Double>> consumers = terrainZConsumers.remove(position);
        for (BiConsumer<DecimalPosition, Double> consumer : consumers) {
            consumer.accept(position, null);
        }
    }

    public void onTerrainPositionPickRayAnswer(Line3d worldPickRay, Vertex terrainPosition) {
        Collection<Consumer<Vertex>> consumers = worldPickRayConsumers.remove(worldPickRay);
        for (Consumer<Vertex> consumer : consumers) {
            consumer.accept(terrainPosition);
        }
    }

    public void onTerrainPositionPickRayAnswerFail(Line3d worldPickRay) {
        Collection<Consumer<Vertex>> consumers = worldPickRayConsumers.remove(worldPickRay);
        for (Consumer<Vertex> consumer : consumers) {
            consumer.accept(null);
        }
    }

    public void onOverlapAnswer(DecimalPosition position, boolean overlap) {
        Collection<Consumer<Boolean>> consumers = overlapConsumers.remove(position);
        for (Consumer<Boolean> consumer : consumers) {
            consumer.accept(overlap);
        }
    }

    public void onOverlapTypeAnswer(int uuid, boolean overlaps) {
        overlapTypeConsumers.remove(uuid).accept(overlaps);
    }

    public void enableEditMode(GroundSkeletonConfig groundSkeletonConfig) {
        groundUi.setGroundSkeletonConfig(groundSkeletonConfig);
        slopeUis.values().forEach(slopeUi -> slopeUi.setGroundSkeletonConfig(groundSkeletonConfig));
    }

    public void enableEditMode(SlopeSkeletonConfig slopeSkeletonConfig) {
        slopeUis.values().stream().filter(slopeUi -> slopeUi.getId() == slopeSkeletonConfig.getId()).forEach(slopeUi -> slopeUi.setSlopeSkeletonConfig(slopeSkeletonConfig));
    }
}
