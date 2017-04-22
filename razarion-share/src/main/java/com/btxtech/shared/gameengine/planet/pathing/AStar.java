package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.Index;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 28.01.2017.
 */
@Dependent
public class AStar {
    private static final int MAX_CLOSED_LIST_SIZE = 100000;
    @Inject
    private ObstacleContainer obstacleContainer;
    private Map<Index, AStarNode> closedList = new HashMap<>();
    private AStarOpenList openList = new AStarOpenList();
    private Index startTile;
    private AStarNode destinationNode;
    private boolean pathFound;
    private List<Index> tilePath;
    private double smallestHeuristic = Double.MAX_VALUE;
    private AStarNode bestFitNode;

    public void init(Index startTile, Index destinationTile) {
        this.startTile = startTile;
        destinationNode = new AStarNode(destinationTile);
        openList.add(new AStarNode(startTile));
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
        closedList.put(current.getTileIndex(), current);
        if (closedList.size() > MAX_CLOSED_LIST_SIZE) {
            throw new IllegalStateException("AStar max closed list size reached. startTile: " + startTile + " destinationTile: " + destinationNode.getTileIndex());
        }
    }

    private void handleAllSuccessorNodes(AStarNode current) {
        // North
        if (obstacleContainer.hasNorthSuccessorNode(current.getTileIndex().getY())) {
            handleSuccessorNode(current, current.getTileIndex().add(0, 1));
        }
        // East
        if (obstacleContainer.hasEastSuccessorNode(current.getTileIndex().getX())) {
            handleSuccessorNode(current, current.getTileIndex().add(1, 0));
        }
        // South
        if (obstacleContainer.hasSouthSuccessorNode(current.getTileIndex().getY())) {
            handleSuccessorNode(current, current.getTileIndex().add(0, -1));
        }
        // West
        if (obstacleContainer.hasWestSuccessorNode(current.getTileIndex().getX())) {
            handleSuccessorNode(current, current.getTileIndex().add(-1, 0));
        }
    }

    private void handleSuccessorNode(AStarNode current, Index successorTilePosition) {
        if (!obstacleContainer.isFree(successorTilePosition)) {
            return;
        }

        if (!closedList.containsKey(successorTilePosition)) {
            double tentativeG = current.getG() + current.getTileIndex().getDistanceDouble(successorTilePosition);
            AStarNode successor = openList.get(successorTilePosition);
            if (successor == null || tentativeG < successor.getG()) {
                if (successor == null) {
                    if (successorTilePosition.equals(destinationNode.getTileIndex())) {
                        successor = destinationNode;
                    } else {
                        successor = new AStarNode(successorTilePosition);
                    }
                } else {
                    openList.remove(successorTilePosition);
                }
                successor.setPredecessor(current);
                successor.setG(tentativeG);
                double heuristic = successorTilePosition.getDistanceDouble(destinationNode.getTileIndex());
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
        return bestFitNode.getTileIndex();
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
            tilePath.add(tempNode.getTileIndex());
            tempNode = tempNode.getPredecessor();
        }
        Collections.reverse(tilePath);
        return tilePath;
    }

}
