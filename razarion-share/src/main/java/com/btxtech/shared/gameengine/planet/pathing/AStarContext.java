package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.planet.terrain.container.PathingAccess;
import com.btxtech.shared.gameengine.planet.terrain.container.PathingNodeWrapper;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * on 23.10.2017.
 */
public class AStarContext {
    private DecimalPosition targetPosition;
    private double range;
    private TerrainType terrainType;
    private Set<TerrainType> skippableTerrainType;
    private List<Index> subNodeIndexScope;
    private final Map<PathingNodeWrapper, Collection<PathingNodeWrapper>> cache = new HashMap<>();

    public AStarContext(DecimalPosition targetPosition, double range, TerrainType terrainType, Set<TerrainType> skippableTerrainType, List<Index> subNodeIndexScope) {
        this.targetPosition = targetPosition;
        this.range = range;
        this.terrainType = terrainType;
        this.skippableTerrainType = skippableTerrainType;
        this.subNodeIndexScope = subNodeIndexScope;
    }

    public boolean isNullTerrainTypeAllowed(DecimalPosition position) {
        if (TerrainType.isAllowed(terrainType, null)) {
            return true;
        }
        if (skippableTerrainType == null) {
            return false;
        }
        if (!isInSkippableRange(position)) {
            return false;
        }
        return skippableTerrainType.contains(TerrainType.getNullTerrainType());
    }

    public boolean isAllowed(TerrainType terrainType, DecimalPosition position) {
        if (TerrainType.isAllowed(this.terrainType, terrainType)) {
            return true;
        }
        if (skippableTerrainType == null) {
            return false;
        }
        if (!isInSkippableRange(position)) {
            return false;
        }
        return skippableTerrainType.contains(terrainType);
    }

    public boolean isSkippable(TerrainType terrainType, DecimalPosition position) {
        return skippableTerrainType != null && isInSkippableRange(position) && skippableTerrainType.contains(terrainType);
    }

    public List<DecimalPosition> stripSkippable(List<DecimalPosition> positions, PathingAccess pathingAccess) {
        if (skippableTerrainType == null) {
            return positions;
        }
        return positions.stream().filter(position -> {
            if(isInSkippableRange(position)) {
                TerrainType terrainType = pathingAccess.getTerrainType(position);
                return TerrainType.isAllowed(this.terrainType, terrainType) || !skippableTerrainType.contains(terrainType);
            } else {
                return true;
            }
        }).collect(Collectors.toList());
    }

    public List<Index> getSubNodeIndexScope() {
        return subNodeIndexScope;
    }

    public boolean hasSubNodeIndexScope() {
        return subNodeIndexScope != null;
    }

    public Collection<PathingNodeWrapper> getFromCache(PathingNodeWrapper key) {
        synchronized (cache) {
            return cache.get(key);
        }
    }

    public void putToCache(PathingNodeWrapper key, Collection<PathingNodeWrapper> toBeCached) {
        synchronized (cache) {
            cache.put(key, toBeCached);
        }
    }

    private boolean isInSkippableRange(DecimalPosition position) {
        return position.getDistance(targetPosition) < range;
    }

}
