package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.planet.terrain.container.PathingNodeWrapper;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * on 23.10.2017.
 */
public class AStarContext {
    private TerrainType terrainType;
    private List<Index> subNodeIndexScope;
    private final Map<PathingNodeWrapper, Collection<PathingNodeWrapper>> cache = new HashMap<>();

    public AStarContext(TerrainType terrainType, List<Index> subNodeIndexScope) {
        this.terrainType = terrainType;
        this.subNodeIndexScope = subNodeIndexScope;
    }

    public boolean isNullTerrainTypeAllowed() {
        return TerrainType.isAllowed(terrainType, null);
    }

    public boolean isAllowed(TerrainType terrainType) {
        return TerrainType.isAllowed(this.terrainType, terrainType);
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

}
