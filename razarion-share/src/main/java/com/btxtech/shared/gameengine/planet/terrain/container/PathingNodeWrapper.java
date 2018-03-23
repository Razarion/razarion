package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.gameengine.planet.pathing.AStarContext;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;

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

    public boolean isFree(TerrainType terrainType) {
        if (terrainShapeNode == null && terrainShapeSubNode == null) {
            return terrainType == TerrainType.LAND;
        } else if (terrainShapeNode != null) {
            return TerrainType.isAllowed(terrainType, terrainShapeNode.getTerrainType());
        } else {
            return TerrainType.isAllowed(terrainType, terrainShapeSubNode.getTerrainType());
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

    public Rectangle2D getRectangle() {
        if (nodeIndex != null) {
            return TerrainUtil.toAbsoluteNodeRectangle(nodeIndex);
        } else if (subNodePosition != null) {
            double length = TerrainUtil.calculateSubNodeLength(terrainShapeSubNode.getDepth());
            return new Rectangle2D(subNodePosition.getX(), subNodePosition.getY(), length, length);
        } else {
            throw new IllegalStateException("PathingNodeWrapper.getRectangle()");
        }
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
        if (nodeIndex != null && terrainShapeSubNode == null) {
            Index neighborNodeIndex = nodeIndex.add(direction);
            if (!pathingAccess.isNodeInBoundary(neighborNodeIndex)) {
                return;
            }
            TerrainShapeNode neighborNode = pathingAccess.getTerrainShapeNode(neighborNodeIndex);
            if (neighborNode == null) {
                if (aStarContext.isNullTerrainTypeAllowed()) {
                    northNodeHandler.accept(new PathingNodeWrapper(pathingAccess, neighborNodeIndex));
                }
            } else {
                neighborNode.outerDirectionCallback(aStarContext, direction, TerrainUtil.toNodeAbsolute(neighborNodeIndex), new TerrainShapeNode.DirectionConsumer() {
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
                    if (aStarContext.isNullTerrainTypeAllowed()) {
                        northNodeHandler.accept(new PathingNodeWrapper(pathingAccess, TerrainUtil.toNode(neighborSubNodePosition)));
                    }
                    return null;
                }

                @Override
                public Void inTile(TerrainShapeTile terrainShapeTile, Index tileIndex) {
                    if (aStarContext.isAllowed(terrainShapeTile.getTerrainType())) {
                        northNodeHandler.accept(new PathingNodeWrapper(pathingAccess, TerrainUtil.toNode(neighborSubNodePosition)));
                    }
                    return null;
                }

                @Override
                public Void inNode(TerrainShapeNode terrainShapeNode, Index nodeRelativeIndex, DecimalPosition tileRelative, Index tileIndex) {
                    if (aStarContext.isAllowed(terrainShapeNode.getTerrainType())) {
                        northNodeHandler.accept(new PathingNodeWrapper(pathingAccess, TerrainUtil.tileToNode(tileIndex).add(nodeRelativeIndex), terrainShapeNode));
                    }
                    return null;
                }

                @Override
                public Void inSubNode(TerrainShapeSubNode terrainShapeSubNode, TerrainShapeNode terrainShapeNode, DecimalPosition nodeRelative, Index nodeRelativeIndex, DecimalPosition tileRelative, Index tileIndex) {
                    Index nodeIndex = TerrainUtil.tileToNode(tileIndex).add(nodeRelativeIndex);
                    if (PathingNodeWrapper.this.terrainShapeSubNode.getDepth() < terrainShapeSubNode.getDepth()) {
                        DecimalPosition correctedSubNodePosition = TerrainUtil.toSubNodeAbsolute(TerrainUtil.toNodeAbsolute(nodeIndex).add(nodeRelative), PathingNodeWrapper.this.terrainShapeSubNode.getDepth());
                        pathingAccess.outerDirectionCallback(aStarContext, correctedSubNodePosition, PathingNodeWrapper.this.terrainShapeSubNode.getDepth(), direction, new TerrainShapeNode.DirectionConsumer() {
                            @Override
                            public void onTerrainShapeNode(TerrainShapeNode terrainShapeNode) {
                                throw new IllegalStateException("PathingNodeWrapper.provideSuccessors()");
                            }

                            @Override
                            public void onTerrainShapeSubNode(TerrainShapeSubNode terrainShapeSubNode, DecimalPosition subNodePosition) {
                                northNodeHandler.accept(new PathingNodeWrapper(pathingAccess, subNodePosition, terrainShapeSubNode));
                            }
                        });
                    } else {
                        if (aStarContext.isAllowed(terrainShapeSubNode.getTerrainType())) {
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

    private Consumer<PathingNodeWrapper> checkScopeAdapter(AStarContext aStarContext, Consumer<PathingNodeWrapper> northNodeHandler) {
        return pathingNodeWrapper -> {
            if (aStarContext.isStartSuck()) {
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
        if (aStarContext.hasSubNodeIndexScope()) {
            if (getTerrainShapeSubNode() != null) {
                for (Index index : aStarContext.getSubNodeIndexScope()) {
                    DecimalPosition scanPosition = getCenter().add(TerrainUtil.smallestSubNodeCenter(index));
                    if (!aStarContext.isAllowed(pathingAccess.getTerrainType(scanPosition))) {
                        return true;
                    }
                }
                return false;
            } else {
                return false;
            }
        } else {
            return false;
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

    @Override
    public String toString() {
        return "PathingNodeWrapper{" +
                "nodeIndex=" + nodeIndex +
                ", subNodePosition=" + subNodePosition +
                '}';
    }
}
