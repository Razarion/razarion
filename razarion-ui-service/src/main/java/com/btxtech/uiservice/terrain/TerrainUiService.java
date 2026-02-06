package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.utils.GeometricUtil;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.renderer.ViewField;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by Beat
 * 09.08.2015.
 */
@Singleton
public class TerrainUiService {
    private final Map<Index, UiTerrainTile> cacheTerrainTiles = new HashMap<>();
    private final Map<Index, Consumer<TerrainTile>> terrainTileConsumers = new HashMap<>();
    // private Logger logger = Logger.getLogger(TerrainUiService.class.getName());
    private final Provider<GameEngineControl> gameEngineControl;
    private final Provider<UiTerrainTile> uiTerrainTileInstance;
    private Map<Index, UiTerrainTile> displayTerrainTiles = new HashMap<>();
    private int tileXCount;
    private int tileYCount;

    @Inject
    public TerrainUiService(Provider<UiTerrainTile> uiTerrainTileInstance, Provider<GameEngineControl> gameEngineControl) {
        this.uiTerrainTileInstance = uiTerrainTileInstance;
        this.gameEngineControl = gameEngineControl;
    }

    public void setPlanetConfig(PlanetConfig planetConfig) {
        DecimalPosition planetSize = planetConfig.getSize();
        tileXCount = TerrainUtil.terrainPositionToTileIndexCeil(planetSize).getX();
        tileYCount = TerrainUtil.terrainPositionToTileIndexCeil(planetSize).getY();
    }

    public void clear() {
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

    public boolean isTerrainFree(Collection<DecimalPosition> terrainPositions, BaseItemType baseItemType) {
        double radius = baseItemType.getPhysicalAreaConfig().getRadius();
        TerrainType terrainType = baseItemType.getPhysicalAreaConfig().getTerrainType();
        for (DecimalPosition terrainPosition : terrainPositions) {
            if (!isTerrainFree(terrainPosition, radius, terrainType)) {
                return false;
            }
        }
        return true;
    }

    public boolean isTerrainFree(DecimalPosition terrainPosition, double radius, TerrainType terrainType) {
        if (terrainType.isAreaCheck()) {
            List<Index> nodeIndices = GeometricUtil.rasterizeCircle(new Circle2D(DecimalPosition.NULL, radius), (int) TerrainUtil.NODE_SIZE);
            for (Index nodeIndex : nodeIndices) {
                DecimalPosition scanPosition = TerrainUtil.nodeIndexToTerrainPosition(nodeIndex).add(terrainPosition);
                if (!isTerrainFree(scanPosition, terrainType)) {
                    return false;
                }
            }
        } else {
            return isTerrainFree(terrainPosition, terrainType);
        }
        return true;
    }

    public boolean isTerrainFree(DecimalPosition terrainPosition, TerrainType terrainType) {
        Index terrainTile = TerrainUtil.terrainPositionToTileIndex(terrainPosition);
        UiTerrainTile uiTerrainTile = displayTerrainTiles.get(terrainTile);
        if (uiTerrainTile == null) {
            return false;
        }
        return uiTerrainTile.isTerrainTypeAllowed(terrainType, terrainPosition);
    }

    public TerrainType getTerrainType(DecimalPosition terrainPosition) {
        Index terrainTileIndex = TerrainUtil.terrainPositionToTileIndex(terrainPosition);
        UiTerrainTile uiTerrainTile = displayTerrainTiles.get(terrainTileIndex);
        if (uiTerrainTile == null) {
            uiTerrainTile = cacheTerrainTiles.get(terrainTileIndex);
        }
        if (uiTerrainTile == null) {
            return TerrainType.BLOCKED;
        }
        return uiTerrainTile.getTerrainType(terrainPosition);
    }

    public void requestTerrainTile(Index terrainTileIndex, Consumer<TerrainTile> terrainTileConsumer) {
        terrainTileConsumers.put(terrainTileIndex, terrainTileConsumer);
        gameEngineControl.get().requestTerrainTile(terrainTileIndex);
    }

    public void onTerrainTileResponse(TerrainTile terrainTile) {
        terrainTileConsumers.remove(terrainTile.getIndex()).accept(terrainTile);
    }
}
