package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.Index;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
* Created by beat
* on 10.11.2014.
*/
class AStarOpenList {
    private PriorityQueue<AStarNode> sortedList = new PriorityQueue<>();
    private Map<Index, AStarNode> map = new HashMap<>();

    public void add(AStarNode node) {
        sortedList.add(node);
        map.put(node.getTerrainShapeNodeIndex(), node);
    }

    public AStarNode removeFirst() {
        AStarNode node = sortedList.poll();
        map.remove(node.getTerrainShapeNodeIndex());
        return node;
    }

    public AStarNode get(Index index) {
        return map.get(index);
    }

    public void remove(Index index) {
        AStarNode node = map.remove(index);
        if (node != null) {
            sortedList.remove(node);
        }
    }

    public boolean isEmpty() {
        return sortedList.isEmpty();
    }
}
