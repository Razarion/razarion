package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.gameengine.planet.terrain.container.PathingNodeWrapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 28.01.2017.
 */
public class AStar {
    private static final Logger LOGGER = Logger.getLogger(AStar.class.getName());
    private static final int MAX_CLOSED_LIST_SIZE = 100000;
    private final Map<PathingNodeWrapper, AStarNode> closedList = new HashMap<>();
    private final AStarOpenList openList = new AStarOpenList();
    private final PathingNodeWrapper startNode;
    private final AStarNode destinationNode;
    private final AStarContext aStarContext;
    private boolean pathFound;
    private double smallestHeuristic = Double.MAX_VALUE;
    private AStarNode bestFitNode;

    public AStar(PathingNodeWrapper startNode, PathingNodeWrapper destinationNode, AStarContext aStarContext) {
        this.startNode = startNode;
        this.destinationNode = new AStarNode(destinationNode);
        this.aStarContext = aStarContext;
        openList.add(new AStarNode(startNode));
    }

    public void expandAllNodes() {
        while (true) {
            if (openList.isEmpty()) {
                // Path not found
                return;
            }
            // Destination likely unreachable for this unit's radius (a smaller unit might
            // fit through gaps this one cannot). Stop expanding and let the caller fall
            // back to bestFitNode via PathingService's orbit-fix branch.
            if (closedList.size() > MAX_CLOSED_LIST_SIZE) {
                LOGGER.log(Level.WARNING, "AStar closed list limit reached, falling back to bestFitNode."
                        + " destination=" + aStarContext.getDestination()
                        + " startNode=" + startNode
                        + " destinationTile=" + destinationNode.getPathingNodeWrapper()
                        + " bestFitNode=" + (bestFitNode != null ? bestFitNode.getPathingNodeWrapper() : "null"));
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
    }

    private void handleAllSuccessorNodes(AStarNode current) {
//   TODO     Collection<PathingNodeWrapper> cached = aStarContext.getFromCache(current.getPathingNodeWrapper());
//        if (cached != null) {
//            cached.forEach(successor -> handleSuccessorNode(current, successor));
//            return;
//        }
//        Collection<PathingNodeWrapper> toBeCached = new ArrayList<>();
        // North
        current.getPathingNodeWrapper().provideNorthSuccessors(aStarContext, northSuccessor -> {
            // TODO toBeCached.add(northSuccessor);
            handleSuccessorNode(current, northSuccessor);
        });
        // East
        current.getPathingNodeWrapper().provideEastSuccessors(aStarContext, eastSuccessor -> {
            // TODO toBeCached.add(eastSuccessor);
            handleSuccessorNode(current, eastSuccessor);
        });
        // South
        current.getPathingNodeWrapper().provideSouthSuccessors(aStarContext, southSuccessor -> {
            // TODO toBeCached.add(southSuccessor);
            handleSuccessorNode(current, southSuccessor);
        });
        // West
        current.getPathingNodeWrapper().provideWestSuccessors(aStarContext, westSuccessor -> {
            // TODO toBeCached.add(westSuccessor);
            handleSuccessorNode(current, westSuccessor);
        });
        // TODO aStarContext.putToCache(current.getPathingNodeWrapper(), toBeCached);
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

    public List<PathingNodeWrapper> convertPath() {
        List<PathingNodeWrapper> tilePath = new ArrayList<>();
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

    public int getCloseListSize() {
        return closedList.size();
    }

    public boolean isPathFound() {
        return pathFound;
    }

    /**
     * The reachable node closest to the destination — equals destinationNode when
     * pathFound, otherwise the best A* candidate before openList was exhausted.
     */
    public PathingNodeWrapper getReachedNode() {
        if (pathFound) {
            return destinationNode.getPathingNodeWrapper();
        }
        if (bestFitNode != null) {
            return bestFitNode.getPathingNodeWrapper();
        }
        return startNode;
    }

    public Collection<PathingNodeWrapper> getClosedListNodes() {
        return closedList.keySet();
    }

    public PathingNodeWrapper getStartNode() {
        return startNode;
    }

    public PathingNodeWrapper getDestinationNode() {
        return destinationNode.getPathingNodeWrapper();
    }
}
