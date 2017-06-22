package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.planet.terrain.container.PathingAccess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 28.01.2017.
 */
public class AStar {
    private static final int MAX_CLOSED_LIST_SIZE = 100000;
    private Map<Index, AStarNode> closedList = new HashMap<>();
    private AStarOpenList openList = new AStarOpenList();
    private Index startNode;
    private AStarNode destinationNode;
    private final PathingAccess pathingAccess;
    private boolean pathFound;
    private List<Index> tilePath;
    private double smallestHeuristic = Double.MAX_VALUE;
    private AStarNode bestFitNode;

    public AStar(Index startNode, Index destinationNode, PathingAccess pathingAccess) {
        this.startNode = startNode;
        this.destinationNode = new AStarNode(destinationNode);
        this.pathingAccess = pathingAccess;
        openList.add(new AStarNode(startNode));
    }

    public void expandAllNodes() {
        while (true) {
            if (openList.isEmpty()) {
                // Path hot found
                return;
            }
            AStarNode current = openList.removeFirst();
            if (current.equals(destinationNode)) {
                pathFound = true;
                return;
            } else {
                expandNode(current);
            }
        }
    }

    private void expandNode(AStarNode current) {
        handleAllSuccessorNodes(current);
        closedList.put(current.getTerrainShapeNodeIndex(), current);
        if (closedList.size() > MAX_CLOSED_LIST_SIZE) {
            throw new IllegalStateException("AStar max closed list size reached. startNode: " + startNode + " destinationTile: " + destinationNode.getTerrainShapeNodeIndex());
        }
    }

    private void handleAllSuccessorNodes(AStarNode current) {
        // North
        if (pathingAccess.hasNorthSuccessorNode(current.getTerrainShapeNodeIndex().getY())) {
            handleSuccessorNode(current, current.getTerrainShapeNodeIndex().add(0, 1));
        }
        // East
        if (pathingAccess.hasEastSuccessorNode(current.getTerrainShapeNodeIndex().getX())) {
            handleSuccessorNode(current, current.getTerrainShapeNodeIndex().add(1, 0));
        }
        // South
        if (pathingAccess.hasSouthSuccessorNode(current.getTerrainShapeNodeIndex().getY())) {
            handleSuccessorNode(current, current.getTerrainShapeNodeIndex().add(0, -1));
        }
        // West
        if (pathingAccess.hasWestSuccessorNode(current.getTerrainShapeNodeIndex().getX())) {
            handleSuccessorNode(current, current.getTerrainShapeNodeIndex().add(-1, 0));
        }
    }

    private void handleSuccessorNode(AStarNode current, Index successorTilePosition) {
        if (!pathingAccess.isTileFree(successorTilePosition)) {
            return;
        }

        if (!closedList.containsKey(successorTilePosition)) {
            double tentativeG = current.getG() + current.getTerrainShapeNodeIndex().getDistanceDouble(successorTilePosition);
            AStarNode successor = openList.get(successorTilePosition);
            if (successor == null || tentativeG < successor.getG()) {
                if (successor == null) {
                    if (successorTilePosition.equals(destinationNode.getTerrainShapeNodeIndex())) {
                        successor = destinationNode;
                    } else {
                        successor = new AStarNode(successorTilePosition);
                    }
                } else {
                    openList.remove(successorTilePosition);
                }
                successor.setPredecessor(current);
                successor.setG(tentativeG);
                double heuristic = successorTilePosition.getDistanceDouble(destinationNode.getTerrainShapeNodeIndex());
                successor.setF(tentativeG + heuristic);
                openList.add(successor);
                if (smallestHeuristic > heuristic) {
                    smallestHeuristic = heuristic;
                    bestFitNode = successor;
                }
            }
        }
    }

    public boolean isPathFound() {
        return pathFound;
    }

    public List<Index> getTilePath() {
        return tilePath;
    }

    public Index getBestFitTile() {
        return bestFitNode.getTerrainShapeNodeIndex();
    }

    public List<Index> convertPath() {
        tilePath = new ArrayList<>();
        AStarNode tempNode;
        if (pathFound) {
            tempNode = destinationNode.getPredecessor();
        } else {
            if (bestFitNode == null) {
                throw new IllegalStateException("AStarService: bestFitNode == null");
            }
            tempNode = bestFitNode.getPredecessor();
        }
        // Omit start
        while (tempNode != null && tempNode.getPredecessor() != null) {
            tilePath.add(tempNode.getTerrainShapeNodeIndex());
            tempNode = tempNode.getPredecessor();
        }
        Collections.reverse(tilePath);
        return tilePath;
    }

}
