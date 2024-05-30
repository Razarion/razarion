package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.MapCollection;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.utils.GeometricUtil;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.renderer.ViewField;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.Collection;
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
    // private Logger logger = Logger.getLogger(TerrainUiService.class.getName());
    @Inject
    private GameEngineControl gameEngineControl;
    @Inject
    private Instance<UiTerrainTile> uiTerrainTileInstance;
    private final MapCollection<DecimalPosition, BiConsumer<DecimalPosition, Double>> terrainZConsumers = new MapCollection<>();
    private Map<Index, UiTerrainTile> displayTerrainTiles = new HashMap<>();
    private final Map<Index, UiTerrainTile> cacheTerrainTiles = new HashMap<>();
    private final Map<Index, Consumer<TerrainTile>> terrainTileConsumers = new HashMap<>();
    private int tileXCount;
    private int tileYCount;

    public void setPlanetConfig(PlanetConfig planetConfig) {
        DecimalPosition planetSize = planetConfig.getSize();
        tileXCount = TerrainUtil.toTileCeil(planetSize).getX();
        tileYCount = TerrainUtil.toTileCeil(planetSize).getY();
    }

    public void clear() {
        terrainZConsumers.clear();
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

    public void onViewChanged(ViewField viewField, Rectangle2D viewFieldAabb) {
        Collection<Index> display = GeometricUtil.rasterizeTerrainViewField(viewFieldAabb, viewField.toPolygon());

        Map<Index, UiTerrainTile> newDisplayTerrainTiles = new HashMap<>();
        for (Index index : display) {
            if (index.getX() < 0 || index.getY() < 0 || index.getX() >= tileXCount || index.getY() >= tileYCount) {
                continue;
            }

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
            newUiTerrainTile.init(index);
            newUiTerrainTile.setActive(true);
            newDisplayTerrainTiles.put(index, newUiTerrainTile);
        }
        for (Map.Entry<Index, UiTerrainTile> hidden : displayTerrainTiles.entrySet()) {
            hidden.getValue().setActive(false);
            cacheTerrainTiles.put(hidden.getKey(), hidden.getValue());
        }
        displayTerrainTiles = newDisplayTerrainTiles;
    }

    public double calculateLandWaterProportion() {
        double value = 0;
        int count = 0;
        for (UiTerrainTile uiTerrainTile : displayTerrainTiles.values()) {
            if (uiTerrainTile.getTerrainTile() != null) {
                // TODO value += uiTerrainTile.getTerrainTile().getLandWaterProportion();
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
            return isTerrainFreeInDisplay(terrainPosition, terrainType);
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
        terrainTileConsumers.remove(terrainTile.getIndex()).accept(terrainTile);
    }

    public Collection<UiTerrainTile> getDisplayTerrainTiles() {
        return displayTerrainTiles.values();
    }
}
