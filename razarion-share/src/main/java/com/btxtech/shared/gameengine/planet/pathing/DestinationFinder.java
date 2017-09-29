package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.planet.terrain.container.PathingAccess;
import com.btxtech.shared.gameengine.planet.terrain.container.PathingNodeWrapper;
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
    private List<Index> subNodeIndexScope;
    private PathingAccess pathingAccess;
    private PathingNodeWrapper found;
    private Set<PathingNodeWrapper> openList = new HashSet<>();
    private Set<PathingNodeWrapper> closeList = new HashSet<>();

    public DestinationFinder(PathingNodeWrapper destinationNode, List<Index> subNodeIndexScope, PathingAccess pathingAccess) {
        this.destinationNode = destinationNode;
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
        pathingNodeWrapper.provideNorthSuccessors(null, this::handleSuccessor);
        pathingNodeWrapper.provideEastSuccessors(null, this::handleSuccessor);
        pathingNodeWrapper.provideSouthSuccessors(null, this::handleSuccessor);
        pathingNodeWrapper.provideWestSuccessors(null, this::handleSuccessor);
    }

    private void handleSuccessor(PathingNodeWrapper successor) {
        if (closeList.contains(successor)) {
            return;
        }
        if (successor.isFree()) {
            openList.add(successor);
            if (isFree(successor)) {
                found = successor;
            }
        }
    }

    private boolean isFree(PathingNodeWrapper pathingNodeWrapper) {
        if (pathingNodeWrapper.getTerrainShapeSubNode() != null) {
            for (Index index : subNodeIndexScope) {
                if (!pathingAccess.isTerrainFree(pathingNodeWrapper.getSubNodePosition().add(index.getX(), index.getY()))) {
                    return false;
                }
            }
        }
        return true;
    }

}
