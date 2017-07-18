package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.planet.terrain.container.PathingNodeWrapper;

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
    private Map<PathingNodeWrapper, AStarNode> closedList = new HashMap<>();
    private AStarOpenList openList = new AStarOpenList();
    private PathingNodeWrapper startNode;
    private AStarNode destinationNode;
    private boolean pathFound;
    private List<PathingNodeWrapper> tilePath;
    private double smallestHeuristic = Double.MAX_VALUE;
    private AStarNode bestFitNode;
    private List<Index> subNodeIndexScope;

    public AStar(PathingNodeWrapper startNode, PathingNodeWrapper destinationNode, List<Index> subNodeIndexScope) {
        this.startNode = startNode;
        this.destinationNode = new AStarNode(destinationNode);
        this.subNodeIndexScope = subNodeIndexScope;
        openList.add(new AStarNode(startNode));
    }

    public void expandAllNodes() {
        while (true) {
            if (openList.isEmpty()) {
                // Path not found
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
        closedList.put(current.getPathingNodeWrapper(), current);
        if (closedList.size() > MAX_CLOSED_LIST_SIZE) {
            throw new IllegalStateException("AStar max closed list size reached. startNode: " + startNode + " destinationTile: " + destinationNode.getPathingNodeWrapper());
        }
    }

    private void handleAllSuccessorNodes(AStarNode current) {
        // North
        current.getPathingNodeWrapper().provideNorthSuccessors(subNodeIndexScope, northSuccessor -> handleSuccessorNode(current, northSuccessor));
        // East
        current.getPathingNodeWrapper().provideEastSuccessors(subNodeIndexScope, eastSuccessor -> handleSuccessorNode(current, eastSuccessor));
        // South
        current.getPathingNodeWrapper().provideSouthSuccessors(subNodeIndexScope, southSuccessor -> handleSuccessorNode(current, southSuccessor));
        // West
        current.getPathingNodeWrapper().provideWestSuccessors(subNodeIndexScope, westSuccessor -> handleSuccessorNode(current, westSuccessor));
    }

    private void handleSuccessorNode(AStarNode current, PathingNodeWrapper successorTilePosition) {
        if (!closedList.containsKey(successorTilePosition)) {
            double tentativeG = current.getG() + current.getPathingNodeWrapper().getDistance(successorTilePosition);
            AStarNode successor = openList.get(successorTilePosition);
            if (successor == null || tentativeG < successor.getG()) {
                if (successor == null) {
                    if (successorTilePosition.equals(destinationNode.getPathingNodeWrapper())) {
                        successor = destinationNode;
                    } else {
                        successor = new AStarNode(successorTilePosition);
                    }
                } else {
                    openList.remove(successorTilePosition);
                }
                successor.setPredecessor(current);
                successor.setG(tentativeG);
                double heuristic = successorTilePosition.getDistance(destinationNode.getPathingNodeWrapper());
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

    public List<PathingNodeWrapper> getTilePath() {
        return tilePath;
    }

    public PathingNodeWrapper getBestFitTile() {
        return bestFitNode.getPathingNodeWrapper();
    }

    public List<PathingNodeWrapper> convertPath() {
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
            tilePath.add(tempNode.getPathingNodeWrapper());
            tempNode = tempNode.getPredecessor();
        }
        Collections.reverse(tilePath);
        return tilePath;
    }

}
