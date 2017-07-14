package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;

import java.util.function.Consumer;

/**
 * Created by Beat
 * on 11.07.2017.
 */
public class PathingNodeWrapper {
    private PathingAccess pathingAccess;
    private final TerrainShapeTile terrainShapeTile;
    private TerrainShapeNode terrainShapeNode;
    private TerrainShapeSubNode terrainShapeSubNode;
    private Index subNodeIndex;

    public PathingNodeWrapper(PathingAccess pathingAccess, TerrainShapeTile terrainShapeTile, TerrainShapeNode terrainShapeNode, TerrainShapeSubNode terrainShapeSubNode, DecimalPosition terrainPosition) {
        this.pathingAccess = pathingAccess;
        this.terrainShapeTile = terrainShapeTile;
        this.terrainShapeNode = terrainShapeNode;
        this.terrainShapeSubNode = terrainShapeSubNode;
        subNodeIndex = TerrainUtil.smallestSubNodeIndex(terrainPosition);
    }

    public boolean isFree() {
        if (terrainShapeTile == null && terrainShapeNode == null && terrainShapeSubNode == null) {
            return true;
        } else if (terrainShapeTile != null && terrainShapeNode == null && terrainShapeSubNode == null) {
            return terrainShapeTile.isLand();
        } else if (terrainShapeTile == null && terrainShapeNode != null && terrainShapeSubNode == null) {
            return terrainShapeNode.isFullLand();
        } else if (terrainShapeTile == null && terrainShapeNode == null) {
            return terrainShapeSubNode.isLand();
        } else {
            throw new IllegalStateException("PathingNodeWrapper constructor");
        }
    }

    public double getDistance(PathingNodeWrapper other) {
        return getCenter().getDistance(other.getCenter());
    }

    public DecimalPosition getCenter() {
        return TerrainUtil.smallestSubNodeCenter(subNodeIndex);
    }

    public void provideNorthSuccessors(Consumer<PathingNodeWrapper> northNodeHandler) {
        provideSuccessors(new Index(0, 1), northNodeHandler);
    }

    public void provideEastSuccessors(Consumer<PathingNodeWrapper> eastNodeHandler) {
        provideSuccessors(new Index(1, 0), eastNodeHandler);
    }

    public void provideSouthSuccessors(Consumer<PathingNodeWrapper> southNodeHandler) {
        provideSuccessors(new Index(0, -1), southNodeHandler);
    }

    public void provideWestSuccessors(Consumer<PathingNodeWrapper> westNodeHandler) {
        provideSuccessors(new Index(-1, 0), westNodeHandler);
    }

    private void provideSuccessors(Index direction, Consumer<PathingNodeWrapper> northNodeHandler) {
        Index successorSubNodeIndex = subNodeIndex.add(direction);
        if (!pathingAccess.isNodeInBoundary(TerrainUtil.smallestSubNodeToNode(successorSubNodeIndex))) {
            return;
        }
        PathingNodeWrapper successorNode = pathingAccess.getPathingNodeWrapper(TerrainUtil.smallestSubNodeCenter(successorSubNodeIndex));
        if(!successorNode.isFree()) {
            return;
        }
        northNodeHandler.accept(successorNode);
    }

    public Index getSubNodeIndex() {
        return subNodeIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PathingNodeWrapper that = (PathingNodeWrapper) o;
        return subNodeIndex.equals(that.subNodeIndex);
    }

    @Override
    public int hashCode() {
        return subNodeIndex.hashCode();
    }
}
