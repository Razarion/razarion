package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Line3d;
import com.btxtech.shared.datatypes.MapCollection;
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
import com.btxtech.shared.dto.WaterConfig;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.utils.GeometricUtil;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.control.GameUiControlInitEvent;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.nativejs.NativeMatrixFactory;
import com.btxtech.uiservice.renderer.RenderServiceInitEvent;
import com.btxtech.uiservice.renderer.ViewField;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
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
@ApplicationScoped
public class TerrainUiService {
    private static final double HIGHEST_POINT_IN_VIEW = 20;
    private static final double LOWEST_POINT_IN_VIEW = -2;
    private Logger logger = Logger.getLogger(TerrainUiService.class.getName());
    @Inject
    private TerrainTypeService terrainTypeService;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private GameEngineControl gameEngineControl;
    @Inject
    private NativeMatrixFactory nativeMatrixFactory;
    @Inject
    private Instance<UiTerrainTile> uiTerrainTileInstance;
    private double highestPointInView; // Should be calculated
    private double lowestPointInView; // Should be calculated
    private MapCollection<TerrainObjectConfig, ModelMatrices> terrainObjectConfigModelMatrices;
    private Map<Integer, SlopeUi> slopeUis = new HashMap<>();
    private GroundUi groundUi;
    private WaterUi waterUi;
    private List<TerrainObjectPosition> terrainObjectPositions;
    private MapCollection<DecimalPosition, BiConsumer<DecimalPosition, Double>> terrainZConsumers = new MapCollection<>();
    private Consumer<Vertex> worldPickRayConsumer;
    private Consumer<Vertex> worldPickRayConsumerQueued;
    private Line3d worldPickRayQueued;
    private MapCollection<DecimalPosition, Consumer<Boolean>> overlapConsumers = new MapCollection<>();
    private Map<Integer, Consumer<Boolean>> overlapTypeConsumers = new HashMap<>();
    private Map<Index, UiTerrainTile> displayTerrainTiles = new HashMap<>();
    private Map<Index, UiTerrainTile> cacheTerrainTiles = new HashMap<>();
    private Map<Index, Consumer<TerrainTile>> terrainTileConsumers = new HashMap<>();

    public TerrainUiService() {
        highestPointInView = HIGHEST_POINT_IN_VIEW;
        lowestPointInView = LOWEST_POINT_IN_VIEW;
    }

    public void onGameUiControlInitEvent(@Observes GameUiControlInitEvent gameUiControlInitEvent) {
        slopeUis.clear();
        for (TerrainSlopePosition terrainSlopePosition : gameUiControlInitEvent.getGameUiControlConfig().getGameEngineConfig().getPlanetConfig().getTerrainSlopePositions()) {
            int id = terrainSlopePosition.getSlopeConfigEntity();
            slopeUis.put(id, new SlopeUi(id, terrainTypeService.getSlopeSkeleton(id), gameUiControlInitEvent.getGameUiControlConfig().getGameEngineConfig().getPlanetConfig().getWaterLevel(), gameUiControlInitEvent.getGameUiControlConfig().getGameEngineConfig().getGroundSkeletonConfig()));
        }
        groundUi = new GroundUi(gameUiControlInitEvent.getGameUiControlConfig().getGameEngineConfig().getGroundSkeletonConfig());
        waterUi = new WaterUi(gameUiControlInitEvent.getGameUiControlConfig().getVisualConfig().getWaterConfig());
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
                        terrainObjectConfigModelMatrices.put(terrainTypeService.getTerrainObjectConfig(terrainObjectPosition.getTerrainObjectId()), ModelMatrices.create4TerrainObject(position.getX(), position.getY(), z, terrainObjectPosition.getScale(), terrainObjectPosition.getRotationZ(), nativeMatrixFactory));
                    } else {
                        logger.warning("TerrainUiService: Can not place TerrainObjectPosition with id: " + terrainObjectPosition.getId());
                    }
                });
            } catch (Throwable t) {
                exceptionHandler.handleException("Placing terrain object failed", t);
            }
        }
    }

    public void onViewChanged(ViewField viewField, Rectangle2D absAabbRect) {
        Collection<Index> display = GeometricUtil.rasterizeTerrainViewField(absAabbRect, viewField.toPolygon());

        Map<Index, UiTerrainTile> newDisplayTerrainTiles = new HashMap<>();
        for (Index index : display) {
            UiTerrainTile uiTerrainTile = displayTerrainTiles.remove(index);
            if (uiTerrainTile != null) {
                newDisplayTerrainTiles.put(index, uiTerrainTile);
                continue;
            }

            UiTerrainTile cachedUiTerrainTile = cacheTerrainTiles.remove(index);
            if (cachedUiTerrainTile != null) {
                cachedUiTerrainTile.setActive(true);
                newDisplayTerrainTiles.put(index, cachedUiTerrainTile);
                continue;
            }

            UiTerrainTile newUiTerrainTile = uiTerrainTileInstance.get();
            newUiTerrainTile.init(index, terrainTypeService.getGroundSkeletonConfig());
            newUiTerrainTile.setActive(true);
            newDisplayTerrainTiles.put(index, newUiTerrainTile);
        }
        for (Map.Entry<Index, UiTerrainTile> entry : displayTerrainTiles.entrySet()) {
            entry.getValue().setActive(false);
            cacheTerrainTiles.put(entry.getKey(), entry.getValue());
        }
        displayTerrainTiles = newDisplayTerrainTiles;
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
        int uuid = (int) (Math.random() * Integer.MAX_VALUE);
        overlapTypeConsumers.put(uuid, callback);
        gameEngineControl.askOverlapType(uuid, positions, baseItemType.getId());
    }

    public void calculateMousePositionGroundMesh(Line3d worldPickRay, Consumer<Vertex> positionConsumer) {
        if (worldPickRayConsumer == null) {
            worldPickRayConsumer = positionConsumer;
            gameEngineControl.askTerrainPosition(worldPickRay);
        } else {
            worldPickRayQueued = worldPickRay;
            worldPickRayConsumerQueued = positionConsumer;
        }
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

    public void onTerrainPositionPickRayAnswer(Vertex terrainPosition) {
        worldPickRayConsumer.accept(terrainPosition);
        handleWorldPickRayQueued();
    }

    public void onTerrainPositionPickRayAnswerFail() {
        handleWorldPickRayQueued();
    }

    private void handleWorldPickRayQueued() {
        if (worldPickRayConsumerQueued != null) {
            worldPickRayConsumer = worldPickRayConsumerQueued;
            gameEngineControl.askTerrainPosition(worldPickRayQueued);
            worldPickRayQueued = null;
            worldPickRayConsumerQueued = null;
        } else {
            worldPickRayConsumer = null;
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

    public void enableEditMode(WaterConfig waterConfig) {
        waterUi.setWaterConfig(waterConfig);
    }

    public void requestTerrainTile(Index terrainTileIndex, Consumer<TerrainTile> terrainTileConsumer) {
        terrainTileConsumers.put(terrainTileIndex, terrainTileConsumer);
        gameEngineControl.requestTerrainTile(terrainTileIndex);
    }

    public void onTerrainTileResponse(TerrainTile terrainTile) {
        terrainTileConsumers.get(new Index(terrainTile.getIndexX(), terrainTile.getIndexY())).accept(terrainTile);
    }
}
