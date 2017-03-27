package com.btxtech.shared.datatypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Created by Beat
 * 09.08.2016.
 */
public class MapList<T, U> {
    private final Map<T, List<U>> map = new HashMap<>();

    public void put(T key, U value) {
        List<U> collection = map.computeIfAbsent(key, k -> new ArrayList<>());
        collection.add(value);
    }

    public List<U> get(T key) {
        return map.get(key);
    }

    public List<U> getAll() {
        List<U> all = new ArrayList<>();
        for (List<U> list : map.values()) {
            all.addAll(list);
        }
        return all;
    }

    public List<U> getSave(T key) {
        List<U> values = get(key);
        if (values != null) {
            return values;
        } else {
            return Collections.emptyList();
        }
    }

    public Map<T, List<U>> getMap() {
        return map;
    }

    public void remove(T key) {
        map.remove(key);
    }

    public void remove(T key, U value) {
        List<U> collection = map.get(key);
        if (collection == null) {
            return;
        }
        collection.remove(value);
        if (collection.isEmpty()) {
            map.remove(key);
        }
    }

    public Collection<T> getKeys() {
        return map.keySet();
    }

    /**
     * Iterates over the map collection
     * the callback should return true to stop the iteration
     *
     * @param callback callback
     */
    public void iterate(BiFunction<T, U, Boolean> callback) {
        for (Map.Entry<T, List<U>> entry : map.entrySet()) {
            for (U u : entry.getValue()) {
                if(!callback.apply(entry.getKey(), u)) {
                    return;
                }
            }
        }
    }

    public void clear() {
        map.clear();;
    }
}
