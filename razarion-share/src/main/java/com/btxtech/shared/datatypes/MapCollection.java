package com.btxtech.shared.datatypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

    public Collection<U> getSave(T key) {
        Collection<U> values = get(key);
        if(values != null) {
            return values;
        } else {
            return Collections.emptyList();
        }
    }

    public Map<T, Collection<U>> getMap() {
        return map;
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

}
