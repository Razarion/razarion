package com.btxtech.shared.datatypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Created by Beat
 * 09.08.2016.
 */
public class MapCollection<K, C> {
    private final Map<K, Collection<C>> map = new HashMap<>();

    public void put(K key, C value) {
        Collection<C> collection = map.computeIfAbsent(key, k -> new ArrayList<>());
        collection.add(value);
    }

    public Collection<C> get(K key) {
        return map.get(key);
    }

    public Collection<C> getAll() {
        Collection<C> all = new ArrayList<>();
        for (Collection<C> list : map.values()) {
            all.addAll(list);
        }
        return all;
    }

    public Collection<C> getSave(K key) {
        Collection<C> values = get(key);
        if (values != null) {
            return values;
        } else {
            return Collections.emptyList();
        }
    }

    public Map<K, Collection<C>> getMap() {
        return map;
    }

    public Collection<C> remove(K key) {
        return map.remove(key);
    }

    public void remove(K key, C value) {
        Collection<C> collection = map.get(key);
        if (collection == null) {
            return;
        }
        collection.remove(value);
        if (collection.isEmpty()) {
            map.remove(key);
        }
    }

    /**
     * Iterates over the map collection
     * the callback should return false to stop the iteration
     *
     * @param callback callback
     */
    public void iterate(BiFunction<K, C, Boolean> callback) {
        for (Map.Entry<K, Collection<C>> entry : map.entrySet()) {
            for (C c : entry.getValue()) {
                if (!callback.apply(entry.getKey(), c)) {
                    return;
                }
            }
        }
    }

    public void clear() {
        map.clear();
    }

    public boolean containsKey(K key) {
        return map.containsKey(key);
    }
}
