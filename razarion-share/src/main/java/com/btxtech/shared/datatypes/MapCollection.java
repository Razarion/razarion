package com.btxtech.shared.datatypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Created by Beat
 * 09.08.2016.
 */
public class MapCollection<T, U> {
    private final Map<T, Collection<U>> map = new HashMap<>();

    public void put(T key, U value) {
        Collection<U> collection = map.get(key);
        if (collection == null) {
            collection = new ArrayList<>();
            map.put(key, collection);
        }
        collection.add(value);
    }

    public Collection<U> get(T key) {
        return map.get(key);
    }

    public Collection<U> getAll() {
        Collection<U> all = new ArrayList<>();
        for (Collection<U> list : map.values()) {
            all.addAll(list);
        }
        return all;
    }

    public Collection<U> getSave(T key) {
        Collection<U> values = get(key);
        if (values != null) {
            return values;
        } else {
            return Collections.emptyList();
        }
    }

    public Map<T, Collection<U>> getMap() {
        return map;
    }

    public void remove(T key) {
        map.remove(key);
    }

    public void remove(T key, U value) {
        Collection<U> collection = map.get(key);
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
    public void iterate(BiFunction<T, U, Boolean> callback) {
        for (Map.Entry<T, Collection<U>> entry : map.entrySet()) {
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
