package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.planet.terrain.container.PathingAccess;
import com.btxtech.shared.gameengine.planet.terrain.container.PathingNodeWrapper;
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
    private PathingNodeWrapper destinationNode;
    private TerrainType terrainType;
    private List<Index> subNodeIndexScope;
    private PathingAccess pathingAccess;
    private PathingNodeWrapper found;
    private Set<PathingNodeWrapper> openList = new HashSet<>();
    private Set<PathingNodeWrapper> closeList = new HashSet<>();

    public DestinationFinder(PathingNodeWrapper destinationNode, TerrainType terrainType, List<Index> subNodeIndexScope, PathingAccess pathingAccess) {
        this.destinationNode = destinationNode;
        this.terrainType = terrainType;
        this.subNodeIndexScope = subNodeIndexScope;
        this.pathingAccess = pathingAccess;
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
        pathingNodeWrapper.provideNorthSuccessors(terrainType, null, this::handleSuccessor);
        pathingNodeWrapper.provideEastSuccessors(terrainType, null, this::handleSuccessor);
        pathingNodeWrapper.provideSouthSuccessors(terrainType, null, this::handleSuccessor);
        pathingNodeWrapper.provideWestSuccessors(terrainType, null, this::handleSuccessor);
    }

    private void handleSuccessor(PathingNodeWrapper successor) {
        if (closeList.contains(successor)) {
            return;
        }
        if (successor.isFree(terrainType)) {
            openList.add(successor);
            if (isFree(successor)) {
                found = successor;
            }
        }
    }

    private boolean isFree(PathingNodeWrapper pathingNodeWrapper) {
        if (pathingNodeWrapper.getTerrainShapeSubNode() != null) {
            for (Index index : subNodeIndexScope) {
                if (!pathingAccess.isTerrainTypeAllowed(terrainType, pathingNodeWrapper.getSubNodePosition().add(index.getX(), index.getY()))) {
                    return false;
                }
            }
        }
        return true;
    }

}
