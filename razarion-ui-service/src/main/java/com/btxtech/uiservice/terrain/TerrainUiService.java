package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Line3d;
import com.btxtech.shared.datatypes.MapCollection;
import com.btxtech.shared.datatypes.MapList;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.utils.GeometricUtil;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.renderer.ViewField;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created by Beat
 * 09.08.2015.
 */
@ApplicationScoped
public class TerrainUiService {
    private static final double HIGHEST_POINT_IN_VIEW = 20;
    private static final double LOWEST_POINT_IN_VIEW = -2;
    // private Logger logger = Logger.getLogger(TerrainUiService.class.getName());
    @Inject
    private TerrainTypeService terrainTypeService;
    @Inject
    private GameEngineControl gameEngineControl;
    @Inject
    private Instance<UiTerrainTile> uiTerrainTileInstance;
    @Inject
    private GameUiControl gameUiControl;
    private double highestPointInView; // Should be calculated
    private double lowestPointInView; // Should be calculated
    private MapList<Integer, ModelMatrices> terrainObjectConfigModelMatrices = new MapList<>();
    private MapCollection<DecimalPosition, BiConsumer<DecimalPosition, Double>> terrainZConsumers = new MapCollection<>();
    private Map<Index, UiTerrainTile> displayTerrainTiles = new HashMap<>();
    private Map<Index, UiTerrainTile> cacheTerrainTiles = new HashMap<>();
    private Map<Index, Consumer<TerrainTile>> terrainTileConsumers = new HashMap<>();
    private boolean loaded;

    public TerrainUiService() {
        highestPointInView = HIGHEST_POINT_IN_VIEW;
        lowestPointInView = LOWEST_POINT_IN_VIEW;
    }

    public void clear() {
        loaded = false;
        terrainObjectConfigModelMatrices.clear();
        terrainZConsumers.clear();
        clearTerrainTiles();
    }

    public void onEditorTerrainChanged() {
        terrainObjectConfigModelMatrices.clear();
        clearTerrainTiles();
    }

    private void clearTerrainTiles() {
        for (UiTerrainTile uiTerrainTile : displayTerrainTiles.values()) {
            uiTerrainTile.dispose();
        }
        displayTerrainTiles.clear();
        for (UiTerrainTile uiTerrainTile : cacheTerrainTiles.values()) {
            uiTerrainTile.dispose();
        }
        cacheTerrainTiles.clear();
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
            newUiTerrainTile.init(index, terrainTypeService.getGroundConfig(gameUiControl.getPlanetConfig().getPlanetId()));
            newUiTerrainTile.setActive(true);
            newDisplayTerrainTiles.put(index, newUiTerrainTile);
        }
        for (Map.Entry<Index, UiTerrainTile> entry : displayTerrainTiles.entrySet()) {
            entry.getValue().setActive(false);
            cacheTerrainTiles.put(entry.getKey(), entry.getValue());
        }
        displayTerrainTiles = newDisplayTerrainTiles;
        // Terrain objects
        terrainObjectConfigModelMatrices.clear();
        displayTerrainTiles.values().forEach(uiTerrainTile -> {
            MapList<Integer, ModelMatrices> terrainObjectModelMatrices = uiTerrainTile.getTerrainObjectModelMatrices();
            if (terrainObjectModelMatrices != null) {
                terrainObjectConfigModelMatrices.putAll(terrainObjectModelMatrices);
            }
        });
    }

    public void onTerrainObjectModelMatrices(MapList<Integer, ModelMatrices> terrainObjects) {
        terrainObjectConfigModelMatrices.putAll(terrainObjects);
    }

    public double getHighestPointInView() {
        return highestPointInView;
    }

    public double getLowestPointInView() {
        return lowestPointInView;
    }

    public List<ModelMatrices> provideTerrainObjectModelMatrices(int terrainObjectConfigId) {
        List<ModelMatrices> modelMatrices = terrainObjectConfigModelMatrices.get(terrainObjectConfigId);
        if (modelMatrices != null) {
            return modelMatrices;
        } else {
            return Collections.emptyList();
        }
    }

    public double calculateLandWaterProportion() {
        double value = 0;
        int count = 0;
        for (UiTerrainTile uiTerrainTile : displayTerrainTiles.values()) {
            if (uiTerrainTile.getTerrainTile() != null) {
                value += uiTerrainTile.getTerrainTile().getLandWaterProportion();
                count++;
            }
        }
        if (count != 0) {
            return value / (double) count;
        } else {
            return 0;
        }
    }

    public boolean isAtLeaseOneTerrainFreeInDisplay(DecimalPosition terrainPosition, Set<TerrainType> terrainTypes) {
        Index terrainTile = TerrainUtil.toTile(terrainPosition);
        UiTerrainTile uiTerrainTile = displayTerrainTiles.get(terrainTile);
        if (uiTerrainTile == null) {
            throw new IllegalStateException("TerrainUiService.isAtLeaseOneTerrainFreeInDisplay(DecimalPosition) UiTerrainTile not loaded: " + terrainTile);
        }

        return uiTerrainTile.isAtLeaseOneTerrainFree(terrainPosition, terrainTypes);
    }

    public boolean isTerrainFreeInDisplay(Collection<DecimalPosition> terrainPositions, BaseItemType baseItemType) {
        for (DecimalPosition terrainPosition : terrainPositions) {
            TerrainType terrainType = baseItemType.getPhysicalAreaConfig().getTerrainType();
            if (!isTerrainFreeInDisplay(terrainPosition, baseItemType.getPhysicalAreaConfig().getRadius(), terrainType)) {
                return false;
            }
        }
        return true;
    }

    public boolean isTerrainFreeInDisplay(DecimalPosition terrainPosition, double radius, TerrainType terrainType) {
        if (terrainType.isAreaCheck()) {
            List<Index> subNodeIndices = GeometricUtil.rasterizeCircle(new Circle2D(DecimalPosition.NULL, radius), (int) TerrainUtil.MIN_SUB_NODE_LENGTH);
            for (Index subNodeIndex : subNodeIndices) {
                DecimalPosition scanPosition = TerrainUtil.smallestSubNodeCenter(subNodeIndex).add(terrainPosition);
                if (!isTerrainFreeInDisplay(scanPosition, terrainType)) {
                    return false;
                }
            }
        } else {
            if (!isTerrainFreeInDisplay(terrainPosition, terrainType)) {
                return false;
            }
        }
        return true;
    }

    public boolean isTerrainFreeInDisplay(DecimalPosition terrainPosition, TerrainType terrainType) {
        Index terrainTile = TerrainUtil.toTile(terrainPosition);
        UiTerrainTile uiTerrainTile = displayTerrainTiles.get(terrainTile);
        if (uiTerrainTile == null) {
            throw new IllegalStateException("TerrainUiService.isTerrainFreeInDisplay(Collection<DecimalPosition>, BaseItemType) UiTerrainTile not loaded: " + terrainTile);
        }
        return uiTerrainTile.isTerrainTypeAllowed(terrainType, terrainPosition);
    }

    public Vertex calculateMousePositionGroundMesh(Line3d worldPickRay) {
        DecimalPosition groundPosition = worldPickRay.calculatePositionOnHeightLevel(0).toXY();
        double z = calculateMousePositionGroundMesh(groundPosition);
        return worldPickRay.calculatePositionOnHeightLevel(z);
    }

    public double calculateMousePositionGroundMesh(DecimalPosition groundPosition) {
        Index terrainTile = TerrainUtil.toTile(groundPosition);
        UiTerrainTile uiTerrainTile = displayTerrainTiles.get(terrainTile);
        if (uiTerrainTile == null) {
            throw new IllegalStateException("TerrainUiService.calculateMousePositionGroundMesh(DecimalPosition) UiTerrainTile not loaded: " + terrainTile);
        }
        return uiTerrainTile.interpolateDisplayHeight(groundPosition);
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

    public void requestTerrainTile(Index terrainTileIndex, Consumer<TerrainTile> terrainTileConsumer) {
        terrainTileConsumers.put(terrainTileIndex, terrainTileConsumer);
        gameEngineControl.requestTerrainTile(terrainTileIndex);
    }

    public void onTerrainTileResponse(TerrainTile terrainTile) {
        terrainTileConsumers.remove(new Index(terrainTile.getIndexX(), terrainTile.getIndexY())).accept(terrainTile);
    }

    public void enableEditMode(GroundConfig groundConfig) {
        // TODO terrainTypeService.setGroundConfigs(groundConfig);
        // TODO displayTerrainTiles.values().forEach(uiTerrainTile -> uiTerrainTile.setGroundSkeletonConfig(groundConfig));
        // TODO  cacheTerrainTiles.values().forEach(uiTerrainTile -> uiTerrainTile.setGroundSkeletonConfig(groundConfig));
    }

    public void enableEditMode(SlopeConfig slopeConfig) {
        terrainTypeService.overrideSlopeSkeletonConfig(slopeConfig);
        // TODO displayTerrainTiles.values().forEach(uiTerrainTile -> uiTerrainTile.setSlopeSkeletonConfig(slopeConfig));
        cacheTerrainTiles.values().forEach(uiTerrainTile -> uiTerrainTile.setSlopeSkeletonConfig(slopeConfig));
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded() {
        loaded = true;
    }
}
