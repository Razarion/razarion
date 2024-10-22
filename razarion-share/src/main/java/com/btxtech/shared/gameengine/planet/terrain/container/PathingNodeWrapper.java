package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.planet.pathing.AStarContext;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;

import java.util.function.Consumer;

/**
 * Created by Beat
 * on 11.07.2017.
 */
public class PathingNodeWrapper {
    private final TerrainType terrainType;
    private final Index nodeIndex;
    private final TerrainAnalyzer terrainAnalyzer;

    public PathingNodeWrapper(Index nodeIndex, TerrainType terrainType, TerrainAnalyzer terrainAnalyzer) {
        this.terrainType = terrainType;
        this.nodeIndex = nodeIndex;
        this.terrainAnalyzer = terrainAnalyzer;
    }

    public boolean isFree(TerrainType terrainType) {
        return TerrainType.isAllowed(this.terrainType, terrainType);
    }

    public double getDistance(PathingNodeWrapper other) {
        return getCenter().getDistance(other.getCenter());
    }

    public DecimalPosition getCenter() {
        return TerrainUtil.nodeIndexToTerrainPosition(nodeIndex)
                .add(TerrainUtil.NODE_SIZE / 2.0, TerrainUtil.NODE_SIZE / 2.0);
    }

    public void provideNorthSuccessors(AStarContext aStarContext, Consumer<PathingNodeWrapper> northNodeHandler) {
        provideSuccessors(aStarContext, new Index(0, 1), checkScopeAdapter(aStarContext, northNodeHandler));
    }

    public void provideEastSuccessors(AStarContext aStarContext, Consumer<PathingNodeWrapper> eastNodeHandler) {
        provideSuccessors(aStarContext, new Index(1, 0), checkScopeAdapter(aStarContext, eastNodeHandler));
    }

    public void provideSouthSuccessors(AStarContext aStarContext, Consumer<PathingNodeWrapper> southNodeHandler) {
        provideSuccessors(aStarContext, new Index(0, -1), checkScopeAdapter(aStarContext, southNodeHandler));
    }

    public void provideWestSuccessors(AStarContext aStarContext, Consumer<PathingNodeWrapper> westNodeHandler) {
        provideSuccessors(aStarContext, new Index(-1, 0), checkScopeAdapter(aStarContext, westNodeHandler));
    }

    private void provideSuccessors(AStarContext aStarContext, Index direction, Consumer<PathingNodeWrapper> northNodeHandler) {
        Index neighborNodeIndex = nodeIndex.add(direction);
        if (!terrainAnalyzer.isNodeInBoundary(neighborNodeIndex)) {
            return;
        }
        TerrainType neighborTerrainType = terrainAnalyzer.getTerrainType(neighborNodeIndex);
        if (aStarContext.isAllowed(neighborTerrainType)) {
            northNodeHandler.accept(new PathingNodeWrapper(neighborNodeIndex, neighborTerrainType, terrainAnalyzer));
        }
    }

    public Index getNodeIndex() {
        return nodeIndex;
    }

    private Consumer<PathingNodeWrapper> checkScopeAdapter(AStarContext aStarContext, Consumer<PathingNodeWrapper> northNodeHandler) {
        return pathingNodeWrapper -> {
            if (aStarContext.isStartStuck()) {
                double distance = pathingNodeWrapper.getCenter().getDistance(aStarContext.getStartPosition());
                if (distance < aStarContext.getMaxStuckDistance()) {
                    northNodeHandler.accept(pathingNodeWrapper);
                }
            }
            if (!pathingNodeWrapper.isStuck(aStarContext)) {
                northNodeHandler.accept(pathingNodeWrapper);
            }
        };
    }

    public boolean isStuck(AStarContext aStarContext) {
        if (aStarContext.hasScope()) {
            for (Index scopeNodeIndex : aStarContext.getScopeNodeIndices()) {
                Index scanNodeIndex = nodeIndex.add(scopeNodeIndex);
                if (!terrainAnalyzer.isNodeInBoundary(scanNodeIndex)) {
                    return true;
                }
                if (!aStarContext.isAllowed(terrainAnalyzer.getTerrainType(scanNodeIndex))) {
                    return true;
                }
            }
            return false;
        } else {
            return !aStarContext.isAllowed(terrainType);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PathingNodeWrapper other = (PathingNodeWrapper) o;
        return nodeIndex.equals(other.nodeIndex);
    }

    @Override
    public int hashCode() {
        return nodeIndex.hashCode();
    }

    @Override
    public String toString() {
        return "PathingNodeWrapper{" +
                "nodeIndex=" + nodeIndex + '}';
    }
}
