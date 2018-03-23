package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.container.PathingNodeWrapper;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by Beat
 * on 22.03.2018.
 */
@Dependent
public class TerrainAreaFinder {
    @Inject
    private TerrainService terrainService;
    private AStarContext aStarContext;
    private double minDistance;
    private double maxDistance;
    private List<Node> openList = new ArrayList<>();
    private Set<Node> closeList = new HashSet<>();
    private Set<PathingNodeWrapper> area = new HashSet<>();

    public void start(DecimalPosition start, TerrainType terrainType, double minDistance, double maxDistance) {
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        PathingNodeWrapper pathingNodeWrapper = terrainService.getPathingAccess().getPathingNodeWrapper(start);
        openList.add(new Node(pathingNodeWrapper, 0));
        aStarContext = new AStarContext(terrainType, null);

        while (!openList.isEmpty()) {
            expand();
        }
    }

    public Set<PathingNodeWrapper> getArea() {
        return area;
    }

    public DecimalPosition getRandomPosition() {
        if (area.isEmpty()) {
            throw new IllegalStateException("area.isEmpty()");
        }
        int count = (int) (Math.random() * area.size());
        for (PathingNodeWrapper pathingNodeWrapper : area) {
            count--;
            if (count <= 0) {
                return pathingNodeWrapper.getCenter();
            }
        }
        throw new IllegalStateException("TerrainAreaFinder.getRandomPosition()");
    }

    private void expand() {
        if (openList.isEmpty()) {
            throw new IllegalStateException("TerrainAreaFinder.expand() open list is empty");
        }
        Node node = openList.remove(0);
        closeList.add(node);
        node.pathingNodeWrapper.provideNorthSuccessors(aStarContext, pathingNodeWrapper -> handleSuccessor(node, pathingNodeWrapper));
        node.pathingNodeWrapper.provideEastSuccessors(aStarContext, pathingNodeWrapper -> handleSuccessor(node, pathingNodeWrapper));
        node.pathingNodeWrapper.provideSouthSuccessors(aStarContext, pathingNodeWrapper -> handleSuccessor(node, pathingNodeWrapper));
        node.pathingNodeWrapper.provideWestSuccessors(aStarContext, pathingNodeWrapper -> handleSuccessor(node, pathingNodeWrapper));
    }

    private void handleSuccessor(Node node, PathingNodeWrapper successor) {
        double distance = node.distance + successor.getCenter().getDistance(node.pathingNodeWrapper.getCenter());
        if (distance > maxDistance) {
            return;
        }
        Node successorNode = new Node(successor, distance);
        if (closeList.contains(successorNode)) {
            return;
        }
        if (openList.contains(successorNode)) {
            return;
        }
        openList.add(successorNode);
        if (successorNode.distance >= minDistance) {
            area.add(successor);
        }
    }

    private class Node {
        PathingNodeWrapper pathingNodeWrapper;
        double distance;

        public Node(PathingNodeWrapper pathingNodeWrapper, double distance) {
            this.pathingNodeWrapper = pathingNodeWrapper;
            this.distance = distance;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Node node = (Node) o;
            return pathingNodeWrapper.equals(node.pathingNodeWrapper);
        }

        @Override
        public int hashCode() {
            return pathingNodeWrapper.hashCode();
        }
    }
}
