package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;

import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Beat
 * on 11.07.2017.
 */
public class PathingNodeWrapper {
    private PathingAccess pathingAccess;
    private Index nodeIndex;
    private TerrainShapeNode terrainShapeNode;
    private TerrainShapeSubNode terrainShapeSubNode;
    private DecimalPosition subNodePosition;

    public PathingNodeWrapper(PathingAccess pathingAccess, Index nodeIndex) {
        this.pathingAccess = pathingAccess;
        this.nodeIndex = nodeIndex;
    }

    public PathingNodeWrapper(PathingAccess pathingAccess, Index nodeIndex, TerrainShapeNode terrainShapeNode) {
        this.pathingAccess = pathingAccess;
        this.nodeIndex = nodeIndex;
        this.terrainShapeNode = terrainShapeNode;
    }

    public PathingNodeWrapper(PathingAccess pathingAccess, DecimalPosition subNodePosition, TerrainShapeSubNode terrainShapeSubNode) {
        this.pathingAccess = pathingAccess;
        this.subNodePosition = subNodePosition;
        this.terrainShapeSubNode = terrainShapeSubNode;
    }

    public boolean isFree() {
        if (terrainShapeNode == null && terrainShapeSubNode == null) {
            return true;
        } else if (terrainShapeNode != null) {
            return terrainShapeNode.isFullLand() || terrainShapeNode.isFullDriveway();
        } else {
            return terrainShapeSubNode.isLand();
        }
    }

    public double getDistance(PathingNodeWrapper other) {
        return getCenter().getDistance(other.getCenter());
    }

    public DecimalPosition getCenter() {
        if (nodeIndex != null) {
            return TerrainUtil.toAbsoluteNodeCenter(nodeIndex);
        } else if (subNodePosition != null) {
            double length = TerrainUtil.calculateSubNodeLength(terrainShapeSubNode.getDepth()) / 2.0;
            return subNodePosition.add(length, length);
        } else {
            throw new IllegalStateException("PathingNodeWrapper.getCenter()");
        }
    }

    public void provideNorthSuccessors(List<Index> subNodeIndexScope, Consumer<PathingNodeWrapper> northNodeHandler) {
        provideSuccessors(new Index(0, 1), northNodeHandler, subNodeIndexScope);
    }

    public void provideEastSuccessors(List<Index> subNodeIndexScope, Consumer<PathingNodeWrapper> eastNodeHandler) {
        provideSuccessors(new Index(1, 0), eastNodeHandler, subNodeIndexScope);
    }

    public void provideSouthSuccessors(List<Index> subNodeIndexScope, Consumer<PathingNodeWrapper> southNodeHandler) {
        provideSuccessors(new Index(0, -1), southNodeHandler, subNodeIndexScope);
    }

    public void provideWestSuccessors(List<Index> subNodeIndexScope, Consumer<PathingNodeWrapper> westNodeHandler) {
        provideSuccessors(new Index(-1, 0), westNodeHandler, subNodeIndexScope);
    }

    private void provideSuccessors(Index direction, Consumer<PathingNodeWrapper> northNodeHandler, List<Index> scope) {
        if (nodeIndex != null && terrainShapeSubNode == null) {
            Index neighborNodeIndex = nodeIndex.add(direction);
            if (!pathingAccess.isNodeInBoundary(neighborNodeIndex)) {
                return;
            }
            TerrainShapeNode neighborNode = pathingAccess.getTerrainShapeNode(neighborNodeIndex);
            if (neighborNode == null) {
                northNodeHandler.accept(new PathingNodeWrapper(pathingAccess, neighborNodeIndex));
            } else {
                neighborNode.outerDirectionCallback(direction, TerrainUtil.toNodeAbsolute(neighborNodeIndex), new TerrainShapeNode.DirectionConsumer() {
                    @Override
                    public void onTerrainShapeNode(TerrainShapeNode terrainShapeNode) {
                        northNodeHandler.accept(new PathingNodeWrapper(pathingAccess, neighborNodeIndex, terrainShapeNode));
                    }

                    @Override
                    public void onTerrainShapeSubNode(TerrainShapeSubNode terrainShapeSubNode, DecimalPosition subNodePosition) {
                        northNodeHandler.accept(new PathingNodeWrapper(pathingAccess, subNodePosition, terrainShapeSubNode));
                    }
                });
            }
        } else if (terrainShapeSubNode != null && subNodePosition != null) {
            double subNodeLength = TerrainUtil.calculateSubNodeLength(terrainShapeSubNode.getDepth());
            DecimalPosition neighborSubNodePosition;
            if (direction.getX() > 0) {
                neighborSubNodePosition = subNodePosition.add(subNodeLength, 0);
            } else if (direction.getX() < 0) {
                neighborSubNodePosition = subNodePosition.sub(TerrainUtil.MIN_SUB_NODE_LENGTH, 0);
            } else if (direction.getY() > 0) {
                neighborSubNodePosition = subNodePosition.add(0, subNodeLength);
            } else if (direction.getY() < 0) {
                neighborSubNodePosition = subNodePosition.sub(0, TerrainUtil.MIN_SUB_NODE_LENGTH);
            } else {
                throw new IllegalStateException("PathingNodeWrapper.provideSuccessors() direction: " + direction);
            }
            if (!pathingAccess.isPositionInBoundary(neighborSubNodePosition)) {
                return;
            }
            pathingAccess.getTerrainShape().terrainImpactCallback(neighborSubNodePosition, new TerrainImpactCallback<Void>() {
                @Override
                public Void landNoTile(Index tileIndex) {
                    northNodeHandler.accept(new PathingNodeWrapper(pathingAccess, TerrainUtil.toNode(neighborSubNodePosition)));
                    return null;
                }

                @Override
                public Void inTile(TerrainShapeTile terrainShapeTile, Index tileIndex) {
                    if (terrainShapeTile.isLand()) {
                        northNodeHandler.accept(new PathingNodeWrapper(pathingAccess, TerrainUtil.toNode(neighborSubNodePosition)));
                    }
                    return null;
                }

                @Override
                public Void inNode(TerrainShapeNode terrainShapeNode, Index nodeRelativeIndex, DecimalPosition tileRelative, Index tileIndex) {
                    if (terrainShapeNode.isFullLand() || terrainShapeNode.isFullDriveway()) {
                        northNodeHandler.accept(new PathingNodeWrapper(pathingAccess, TerrainUtil.tileToNode(tileIndex).add(nodeRelativeIndex), terrainShapeNode));
                    }
                    return null;
                }

                @Override
                public Void inSubNode(TerrainShapeSubNode terrainShapeSubNode, DecimalPosition nodeRelative, Index nodeRelativeIndex, DecimalPosition tileRelative, Index tileIndex) {
                    Index nodeIndex = TerrainUtil.tileToNode(tileIndex).add(nodeRelativeIndex);
                    if (PathingNodeWrapper.this.terrainShapeSubNode.getDepth() < terrainShapeSubNode.getDepth()) {
                        DecimalPosition correctedSubNodePosition = TerrainUtil.toSubNodeAbsolute(TerrainUtil.toNodeAbsolute(nodeIndex).add(nodeRelative), PathingNodeWrapper.this.terrainShapeSubNode.getDepth());
                        pathingAccess.outerDirectionCallback(correctedSubNodePosition, PathingNodeWrapper.this.terrainShapeSubNode.getDepth(), direction, new TerrainShapeNode.DirectionConsumer() {
                            @Override
                            public void onTerrainShapeNode(TerrainShapeNode terrainShapeNode) {
                                throw new IllegalStateException("PathingNodeWrapper.provideSuccessors()");
                            }

                            @Override
                            public void onTerrainShapeSubNode(TerrainShapeSubNode terrainShapeSubNode, DecimalPosition subNodePosition) {
                                if (terrainShapeSubNode.isLand()) {
                                    northNodeHandler.accept(new PathingNodeWrapper(pathingAccess, subNodePosition, terrainShapeSubNode));
                                }
                            }
                        });
                    } else {
                        if (terrainShapeSubNode.isLand()) {
                            northNodeHandler.accept(new PathingNodeWrapper(pathingAccess, TerrainUtil.toSubNodeAbsolute(neighborSubNodePosition, terrainShapeSubNode.getDepth()), terrainShapeSubNode));
                        }
                    }
                    return null;
                }
            });
        } else {
            throw new IllegalStateException("PathingNodeWrapper.provideNorthSuccessors()");
        }
    }

    public Index getNodeIndex() {
        return nodeIndex;
    }

    public TerrainShapeNode getTerrainShapeNode() {
        return terrainShapeNode;
    }

    public TerrainShapeSubNode getTerrainShapeSubNode() {
        return terrainShapeSubNode;
    }

    public DecimalPosition getSubNodePosition() {
        return subNodePosition;
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
        if (nodeIndex != null) {
            return other.nodeIndex != null && nodeIndex.equals(other.nodeIndex);
        } else {
            return other.subNodePosition != null && subNodePosition.equals(other.subNodePosition);
        }
    }

    @Override
    public int hashCode() {
        if (nodeIndex != null) {
            return nodeIndex.hashCode();
        } else {
            return subNodePosition.hashCode();
        }
    }
}
