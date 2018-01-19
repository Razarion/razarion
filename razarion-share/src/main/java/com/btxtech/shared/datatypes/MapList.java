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
public class MapList<K, L> {
    private final Map<K, List<L>> map = new HashMap<>();

    public void put(K key, L value) {
        List<L> collection = map.computeIfAbsent(key, k -> new ArrayList<>());
        collection.add(value);
    }

    public void putAll(K key, List<L> values) {
        List<L> list = map.get(key);
        if (list != null) {
            list.addAll(values);
        } else {
            map.put(key, values);
        }
    }

    public void putAll(MapList<K, L> other) {
        other.map.forEach((otherKey, otherList) -> {
            List<L> list = map.computeIfAbsent(otherKey, k -> new ArrayList<>());
            list.addAll(otherList);
        });
    }

    public List<L> get(K key) {
        return map.get(key);
    }

    public List<L> getAll() {
        List<L> all = new ArrayList<>();
        for (List<L> list : map.values()) {
            all.addAll(list);
        }
        return all;
    }

    public List<L> getSave(K key) {
        List<L> values = get(key);
        if (values != null) {
            return values;
        } else {
            return Collections.emptyList();
        }
    }

    public Map<K, List<L>> getMap() {
        return map;
    }

    public void remove(K key) {
        map.remove(key);
    }

    public void remove(K key, L value) {
        List<L> collection = map.get(key);
        if (collection == null) {
            return;
        }
        collection.remove(value);
        if (collection.isEmpty()) {
            map.remove(key);
        }
    }

    public Collection<K> getKeys() {
        return map.keySet();
    }

    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    /**
     * Iterates over the map collection
     * the callback should return true to stop the iteration
     *
     * @param callback callback
     */
    public void iterate(BiFunction<K, L, Boolean> callback) {
        for (Map.Entry<K, List<L>> entry : map.entrySet()) {
            for (L l : entry.getValue()) {
                if (!callback.apply(entry.getKey(), l)) {
                    return;
                }
            }
        }
    }

    public void clear() {
        map.clear();
    }
}
