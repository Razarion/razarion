package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.gameengine.planet.terrain.container.PathingNodeWrapper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beat
 * on 27.09.2017.
 */
public class SuccessorNodeCache {
    private final Map<PathingNodeWrapper, Collection<PathingNodeWrapper>> cache = new HashMap<>();

    public Collection<PathingNodeWrapper> get(PathingNodeWrapper key) {
        synchronized (cache) {
            return cache.get(key);
        }
    }

    public void put(PathingNodeWrapper key, Collection<PathingNodeWrapper> toBeCached) {
        synchronized (cache) {
            cache.put(key, toBeCached);
        }
    }
}
