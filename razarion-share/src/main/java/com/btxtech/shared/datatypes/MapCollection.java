package com.btxtech.shared.datatypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beat
 * 09.08.2016.
 */
public class MapCollection<T, U> {
    private Map<T, Collection<U>> map = new HashMap<>();

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

    public Map<T, Collection<U>> getMap() {
        return map;
    }
}
