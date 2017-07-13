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
    private Index nodeIndex;
    private boolean landNode;
    private DecimalPosition nodeRelative;
    private TerrainShapeNode terrainShapeNode;
    private TerrainShapeSubNode terrainShapeSubNode;

    public PathingNodeWrapper(PathingAccess pathingAccess, boolean landNode, Index nodeIndex) {
        this.pathingAccess = pathingAccess;
        this.landNode = landNode;
        this.nodeIndex = nodeIndex;
    }

    public PathingNodeWrapper(PathingAccess pathingAccess, TerrainShapeNode terrainShapeNode, Index nodeIndex) {
        this.pathingAccess = pathingAccess;
        this.terrainShapeNode = terrainShapeNode;
        this.landNode = terrainShapeNode.isFullLand();
        this.nodeIndex = nodeIndex;
    }

    public PathingNodeWrapper(PathingAccess pathingAccess, TerrainShapeSubNode terrainShapeSubNode, Index nodeIndex, DecimalPosition nodeRelative) {
        this.pathingAccess = pathingAccess;
        this.terrainShapeSubNode = terrainShapeSubNode;
        this.nodeIndex = nodeIndex;
        this.nodeRelative = nodeRelative;
    }

    public boolean isFree() {
        if (terrainShapeNode == null && terrainShapeSubNode == null) {
            return landNode;
        } else if (terrainShapeNode != null && terrainShapeSubNode == null) {
            return terrainShapeNode.isFullLand();
        } else {
            return terrainShapeSubNode.isLand();
        }
    }

    public double getDistance(PathingNodeWrapper other) {
        return getCenter().getDistance(other.getCenter());
    }

    public DecimalPosition getCenter() {
        if (terrainShapeSubNode != null) {
            double subNodeHalf = TerrainUtil.calculateSubNodeLength(terrainShapeSubNode.getDepth()) / 2.0;
            return TerrainUtil.toAbsoluteNodeCenter(nodeIndex).add(nodeRelative).add(subNodeHalf, subNodeHalf);
        } else {
            return TerrainUtil.toAbsoluteNodeCenter(nodeIndex);
        }
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
        if (terrainShapeSubNode == null) {
            handleNode(direction, nodeIndex, northNodeHandler);
        } else {
            if (terrainShapeSubNode.getTerrainShapeSubNodes() != null) {
                handleNode(direction, nodeIndex, northNodeHandler);
            } else {
                double length = TerrainUtil.calculateSubNodeLength(terrainShapeSubNode.getDepth());
                DecimalPosition successorNodeRelative = addSubNodeLength(direction, length);
                DecimalPosition successorPosition = successorNodeRelative.add(TerrainUtil.toAbsoluteNodeCenter(nodeIndex));
                TerrainShapeSubNode terrainShapeSubNode = pathingAccess.getTerrainShapeSubNode(successorPosition);
                if (terrainShapeSubNode == null) {
                    handleNode(direction, nodeIndex, northNodeHandler);
                } else {
                    Index successorNodeIndex = TerrainUtil.toNode(successorPosition);
                    if (this.terrainShapeSubNode.getDepth() >= terrainShapeSubNode.getDepth()) {
                        northNodeHandler.accept(new PathingNodeWrapper(pathingAccess, terrainShapeSubNode, successorNodeIndex, successorPosition));
                    } else {
                        int factor = this.terrainShapeSubNode.getDepth() / terrainShapeSubNode.getDepth();
                        double successorLength = TerrainUtil.calculateSubNodeLength(terrainShapeSubNode.getDepth());
                        for (double x = 0; x < factor; x++) {
                            DecimalPosition successorSubPosition = addSubNodeLength(direction, successorLength);
                            TerrainShapeSubNode successorSubNode = pathingAccess.getTerrainShapeSubNode(successorSubPosition);
                            northNodeHandler.accept(new PathingNodeWrapper(pathingAccess, successorSubNode, successorNodeIndex, successorPosition));
                        }
                    }
                }
            }
        }
    }

    private void handleNode(Index direction, Index nodeIndex, Consumer<PathingNodeWrapper> northNodeHandler) {
        Index successorNodeIndex = nodeIndex.add(direction);
        if (!pathingAccess.isNodeInBoundary(successorNodeIndex)) {
            return;
        }
        TerrainShapeNode successorNode = pathingAccess.getTerrainShape().getTerrainShapeNode(successorNodeIndex);
        if (successorNode == null) {
            TerrainShapeTile terrainShapeTile = pathingAccess.getTerrainShape().getTerrainShapeTile(TerrainUtil.nodeToTile(successorNodeIndex));
            if (terrainShapeTile == null || terrainShapeTile.isLand()) {
                northNodeHandler.accept(new PathingNodeWrapper(pathingAccess, true, successorNodeIndex));
            }
        } else {
            if (!successorNode.hasSubNodes()) {
                if (successorNode.isFullLand()) {
                    northNodeHandler.accept(new PathingNodeWrapper(pathingAccess, true, successorNodeIndex));
                }
            } else {
                handleSubNodes(direction, successorNode.isFullLand(), successorNode.getTerrainShapeSubNodes(), northNodeHandler, successorNodeIndex, DecimalPosition.NULL);
            }
        }
    }

    private void handleSubNodes(Index direction, boolean isLand, TerrainShapeSubNode[] terrainShapeSubNodes, Consumer<PathingNodeWrapper> northNodeHandler, Index successorNodeIndex, DecimalPosition parentOffset) {
        TerrainShapeSubNode subTile1 = terrainShapeSubNodes[directionToSubNodeIndex(direction, false)];
        TerrainShapeSubNode subTile2 = terrainShapeSubNodes[directionToSubNodeIndex(direction, true)];
        if (subTile1 == null && subTile2 == null) {
            if (isLand) {
                northNodeHandler.accept(new PathingNodeWrapper(pathingAccess, true, successorNodeIndex));
            }
        } else if (subTile1 != null && subTile2 != null) {
            if (subTile1.getTerrainShapeSubNodes() != null && subTile1.getTerrainShapeSubNodes()[directionToSubNodeIndex(direction, true)] != null) {
                handleSubNodes(direction, subTile1.isLand(), subTile1.getTerrainShapeSubNodes(), northNodeHandler, successorNodeIndex, parentOffset);
            } else {
                if (subTile1.isLand()) {
                    northNodeHandler.accept(new PathingNodeWrapper(pathingAccess, subTile1, successorNodeIndex, setupOffset(direction, parentOffset, subTile1.getDepth())));
                }
            }
            if (subTile2.getTerrainShapeSubNodes() != null && subTile2.getTerrainShapeSubNodes()[directionToSubNodeIndex(direction, false)] != null) {
                DecimalPosition successorNodeRelative = parentOffset.add(addSubNodeTurnedLength(direction, TerrainUtil.calculateSubNodeLength(subTile2.getDepth())));
                handleSubNodes(direction, subTile2.isLand(), subTile2.getTerrainShapeSubNodes(), northNodeHandler, successorNodeIndex, successorNodeRelative);
            } else {
                if (subTile2.isLand()) {
                    DecimalPosition successorNodeRelative = parentOffset.add(addSubNodeTurnedLength(direction, TerrainUtil.calculateSubNodeLength(subTile2.getDepth())));
                    northNodeHandler.accept(new PathingNodeWrapper(pathingAccess, subTile2, successorNodeIndex, setupOffset(direction, successorNodeRelative, subTile1.getDepth())));
                }
            }
        } else {
            throw new IllegalStateException("PathingNodeWrapper.isFree() unknown state with subTile1 and subTile2");
        }
    }

    private DecimalPosition setupOffset(Index direction, DecimalPosition parentOffset, int depth) {
        if (direction.getX() > 0) {
            return parentOffset;
        } else if (direction.getX() < 0) {
            return new DecimalPosition(TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH - TerrainUtil.calculateSubNodeLength(depth), parentOffset.getY());
        } else if (direction.getY() > 0) {
            return parentOffset;
        } else if (direction.getY() < 0) {
            return new DecimalPosition(parentOffset.getX(), TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH - TerrainUtil.calculateSubNodeLength(depth));
        }
        throw new IllegalStateException("PathingNodeWrapper.setupOffset() unknown state");
    }

    private int directionToSubNodeIndex(Index direction, boolean second) {
        // bl, br, tr, tl
        if (direction.getX() != 0) {
            // East
            if (!second) {
                return 0;
            } else {
                return 3;
            }
        } else if (direction.getX() < 0) {
            // West
            if (!second) {
                return 1;
            } else {
                return 2;
            }
        } else if (direction.getY() > 0) {
            // North
            if (!second) {
                return 0;
            } else {
                return 1;
            }
        } else if (direction.getY() < 0) {
            // South
            if (!second) {
                return 2;
            } else {
                return 3;
            }
        }
        throw new IllegalStateException("PathingNodeWrapper.directionToSubNodeIndex() direction: " + direction);
    }

    private DecimalPosition addSubNodeLength(Index direction, double length) {
        DecimalPosition decimalPosition = nodeRelative;
        if (decimalPosition == null) {
            decimalPosition = DecimalPosition.NULL;
        }
        return decimalPosition.add(length * (double) direction.getX(), length * (double) direction.getY());
    }

    private DecimalPosition addSubNodeTurnedLength(Index direction, double length) {
        DecimalPosition decimalPosition = nodeRelative;
        if (decimalPosition == null) {
            decimalPosition = DecimalPosition.NULL;
        }
        return decimalPosition.add(length * (double) Math.abs(direction.getY()), length * (double) Math.abs(direction.getX()));
    }

    @Override
    public String toString() {
        if (terrainShapeNode == null && terrainShapeSubNode == null) {
            return "PathingNodeWrapper nodeIndex: " + nodeIndex;
        } else if (terrainShapeNode != null && terrainShapeSubNode == null) {
            return "PathingNodeWrapper terrainShapeNode nodeIndex: " + nodeIndex;
        } else {
            return "PathingNodeWrapper terrainShapeSubNode nodeIndex: " + nodeIndex + " nodeRelative: " + nodeRelative;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PathingNodeWrapper that = (PathingNodeWrapper) o;

        if (landNode != that.landNode) return false;
        if (nodeIndex != null ? !nodeIndex.equals(that.nodeIndex) : that.nodeIndex != null) return false;
        if (nodeRelative != null ? !nodeRelative.equals(that.nodeRelative) : that.nodeRelative != null) return false;
        if (terrainShapeNode != null ? !terrainShapeNode.equals(that.terrainShapeNode) : that.terrainShapeNode != null)
            return false;
        return terrainShapeSubNode != null ? terrainShapeSubNode.equals(that.terrainShapeSubNode) : that.terrainShapeSubNode == null;
    }

    @Override
    public int hashCode() {
        int result = nodeIndex != null ? nodeIndex.hashCode() : 0;
        result = 31 * result + (landNode ? 1 : 0);
        result = 31 * result + (nodeRelative != null ? nodeRelative.hashCode() : 0);
        result = 31 * result + (terrainShapeNode != null ? terrainShapeNode.hashCode() : 0);
        result = 31 * result + (terrainShapeSubNode != null ? terrainShapeSubNode.hashCode() : 0);
        return result;
    }

    //////////


    public Index getNodeIndex() {
        return nodeIndex;
    }

    public boolean isLandNode() {
        return landNode;
    }

    public DecimalPosition getNodeRelative() {
        return nodeRelative;
    }

    public TerrainShapeNode getTerrainShapeNode() {
        return terrainShapeNode;
    }

    public TerrainShapeSubNode getTerrainShapeSubNode() {
        return terrainShapeSubNode;
    }
}
