package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.planet.terrain.container.PathingNodeWrapper;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainAnalyzer;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.utils.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Beat
 * on 28.09.2017.
 */
public class DestinationFinder {
    private final DecimalPosition destination;
    private final PathingNodeWrapper destinationNode;
    private final TerrainType terrainType;
    private final List<Index> subNodeIndexScope;
    private final TerrainAnalyzer pathingAccess;
    private PathingNodeWrapper found;
    private final AStarContext aStarContext;
    private final Set<PathingNodeWrapper> openList = new HashSet<>();
    private final Set<PathingNodeWrapper> closeList = new HashSet<>();

    public DestinationFinder(DecimalPosition destination, PathingNodeWrapper destinationNode, TerrainType terrainType, List<Index> subNodeIndexScope, TerrainAnalyzer pathingAccess) {
        this.destination = destination;
        this.destinationNode = destinationNode;
        this.terrainType = terrainType;
        this.subNodeIndexScope = subNodeIndexScope;
        this.pathingAccess = pathingAccess;
        aStarContext = new AStarContext(terrainType, null);
    }

    public PathingNodeWrapper find() {
        if (isFree(destinationNode)) {
            return destinationNode;
        }
        openList.add(destinationNode);
        while (found == null) {
            expand();
        }
        if (found == null) {
            throw new IllegalStateException("DestinationFinder.find() nothing found");
        }
        return found;
    }

    private void expand() {
        if (openList.isEmpty()) {
            throw new IllegalStateException("DestinationFinder.expand() open list is empty");
        }
        PathingNodeWrapper pathingNodeWrapper = CollectionUtils.getFirst(openList);
        openList.remove(pathingNodeWrapper);
        closeList.add(pathingNodeWrapper);
        pathingNodeWrapper.provideNorthSuccessors(aStarContext, this::handleSuccessor);
        pathingNodeWrapper.provideEastSuccessors(aStarContext, this::handleSuccessor);
        pathingNodeWrapper.provideSouthSuccessors(aStarContext, this::handleSuccessor);
        pathingNodeWrapper.provideWestSuccessors(aStarContext, this::handleSuccessor);
    }

    private void handleSuccessor(PathingNodeWrapper successor) {
        if (closeList.contains(successor)) {
            return;
        }
        if (successor.isFree(terrainType)) {
            openList.add(successor);
            if (isFree(successor)) {
                if (found == null) {
                    found = successor;
                } else if (successor.getCenter().getDistance(destination) < found.getCenter().getDistance(destination)) {
                    found = successor;
                }
            }
        }
    }

    private boolean isFree(PathingNodeWrapper pathingNodeWrapper) {
        for (Index index : subNodeIndexScope) {
            if (!pathingAccess.isTerrainTypeAllowed(terrainType, pathingNodeWrapper.getNodeIndex().add(index))) {
                return false;
            }
        }
        return true;
    }

}
