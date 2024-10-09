package com.btxtech.shared.gameengine.datatypes.workerdto;

import org.dominokit.jackson.annotation.JSONMapper;

import java.util.Map;

@JSONMapper
public class IntIntMap {
    private Map<Integer, Integer> map;

    public Map<Integer, Integer> getMap() {
        return map;
    }

    public void setMap(Map<Integer, Integer> map) {
        this.map = map;
    }

    public IntIntMap map(Map<Integer, Integer> map) {
        setMap(map);
        return this;
    }
}
