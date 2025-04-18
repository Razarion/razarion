package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.gameengine.planet.terrain.container.PathingNodeWrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Created by beat
 * on 10.11.2014.
 */
class AStarOpenList {
    private final PriorityQueue<AStarNode> sortedList = new PriorityQueue<>();
    private final Map<PathingNodeWrapper, AStarNode> map = new HashMap<>();

    public void add(AStarNode node) {
        sortedList.add(node);
        map.put(node.getPathingNodeWrapper(), node);
    }

    public AStarNode removeFirst() {
        AStarNode node = sortedList.poll();
        map.remove(node.getPathingNodeWrapper());
        return node;
    }

    public AStarNode get(PathingNodeWrapper pathingNodeWrapper) {
        return map.get(pathingNodeWrapper);
    }

    public void remove(PathingNodeWrapper pathingNodeWrapper) {
        AStarNode node = map.remove(pathingNodeWrapper);
        if (node != null) {
            sortedList.remove(node);
        }
    }

    public boolean isEmpty() {
        return sortedList.isEmpty();
    }
}
