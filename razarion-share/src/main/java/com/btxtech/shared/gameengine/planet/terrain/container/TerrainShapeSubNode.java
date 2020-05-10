package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.planet.pathing.AStarContext;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeSubNode;

/**
 * Created by Beat
 * on 20.06.2017.
 */
public class TerrainShapeSubNode {
    // private Logger logger = Logger.getLogger(TerrainShapeSubNode.class.getName());
    private TerrainType terrainType;
    private Double height;
    private int depth;
    private TerrainShapeSubNode[] terrainShapeSubNodes; // bl, br, tr, tl
    private double[] drivewayHeights; // bl, br, tr, tl

    public TerrainShapeSubNode(int depth) {
        this.depth = depth;
    }

    public TerrainShapeSubNode(int depth, NativeTerrainShapeSubNode nativeTerrainShapeSubNode) {
        this.depth = depth;
        terrainType = TerrainType.fromOrdinal(nativeTerrainShapeSubNode.terrainTypeOrdinal);
        height = nativeTerrainShapeSubNode.height;
        terrainShapeSubNodes = fromNativeTerrainShapeSubNode(depth + 1, nativeTerrainShapeSubNode.nativeTerrainShapeSubNodes);
        drivewayHeights = nativeTerrainShapeSubNode.drivewayHeights;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public void setDefaultHeightIfNull(Double renderEngineHeight) {
        if (height != null) {
            return;
        }
        if (renderEngineHeight != null) {
            height = renderEngineHeight;
        } else {
            height = TerrainShapeNode.DEFAULT_HEIGHT;
        }
    }

    public Double getHeight() {
        return height;
    }

    public double getHeightSafe() {
        if (height != null) {
            return height;
        } else {
            return 0;
        }
    }

    public TerrainType getTerrainType() {
        return terrainType;
    }

    public void setTerrainType(TerrainType terrainType) {
        this.terrainType = terrainType;
    }

    public TerrainShapeSubNode[] getTerrainShapeSubNodes() {
        return terrainShapeSubNodes;
    }

    public TerrainShapeSubNode getChildSubNodeBL() {
        if (terrainShapeSubNodes == null) {
            throw new IllegalStateException("TerrainShapeSubNode.getChildSubNodeBL() terrainShapeSubNodes == null");
        }
        return terrainShapeSubNodes[0];
    }

    public TerrainShapeSubNode getChildSubNodeBR() {
        if (terrainShapeSubNodes == null) {
            throw new IllegalStateException("TerrainShapeSubNode.getChildSubNodeBR() terrainShapeSubNodes == null");
        }
        return terrainShapeSubNodes[1];
    }

    public TerrainShapeSubNode getChildSubNodeTR() {
        if (terrainShapeSubNodes == null) {
            throw new IllegalStateException("TerrainShapeSubNode.getChildSubNodeTR() terrainShapeSubNodes == null");
        }
        return terrainShapeSubNodes[2];
    }

    public TerrainShapeSubNode getChildSubNodeTL() {
        if (terrainShapeSubNodes == null) {
            throw new IllegalStateException("TerrainShapeSubNode.getChildSubNodeTL() terrainShapeSubNodes == null");
        }
        return terrainShapeSubNodes[3];
    }

    public void setTerrainShapeSubNodes(TerrainShapeSubNode[] terrainShapeSubNodes) {
        this.terrainShapeSubNodes = terrainShapeSubNodes;
    }

    public NativeTerrainShapeSubNode toNativeTerrainShapeSubNode() {
        NativeTerrainShapeSubNode nativeTerrainShapeSubNode = new NativeTerrainShapeSubNode();
        nativeTerrainShapeSubNode.terrainTypeOrdinal = TerrainType.toOrdinal(terrainType);
        nativeTerrainShapeSubNode.height = height;
        nativeTerrainShapeSubNode.nativeTerrainShapeSubNodes = toNativeTerrainShapeSubNode(terrainShapeSubNodes);
        nativeTerrainShapeSubNode.drivewayHeights = drivewayHeights;
        return nativeTerrainShapeSubNode;
    }

    public TerrainShapeSubNode getTerrainShapeSubNode(int depth, DecimalPosition subNodeRelative) {
        return getTerrainShapeSubNode(depth, subNodeRelative, terrainShapeSubNodes);
    }

    public void iterateOverTerrainSubNodes(TerrainShapeNode.TerrainShapeSubNodeConsumer terrainShapeSubNodeConsumer, DecimalPosition relativeOffsetParent, int depthParent) {
        int depth = depthParent + 1;
        double subNodeLength = TerrainUtil.calculateSubNodeLength(0);
        for (int i = 0; i < terrainShapeSubNodes.length; i++) {
            DecimalPosition relativeOffset = TerrainShapeSubNode.numberToSubNodeIndex(i).multiply(subNodeLength).add(relativeOffsetParent);
            TerrainShapeSubNode terrainShapeSubNode = terrainShapeSubNodes[i];
            if (terrainShapeSubNode.getTerrainShapeSubNodes() != null) {
                terrainShapeSubNode.iterateOverTerrainSubNodes(terrainShapeSubNodeConsumer, relativeOffset, depth + 1);
            } else {
                terrainShapeSubNodeConsumer.onTerrainShapeSubNode(this, relativeOffset, depth);
            }
        }
    }

    public int getDepth() {
        return depth;
    }

    public static NativeTerrainShapeSubNode[] toNativeTerrainShapeSubNode(TerrainShapeSubNode[] terrainShapeSubNodes) {
        if (terrainShapeSubNodes == null) {
            return null;
        }
        NativeTerrainShapeSubNode[] nativeTerrainShapeSubNodes = new NativeTerrainShapeSubNode[terrainShapeSubNodes.length];
        for (int i = 0; i < terrainShapeSubNodes.length; i++) {
            TerrainShapeSubNode terrainShapeSubNode = terrainShapeSubNodes[i];
            if (terrainShapeSubNode != null) {
                nativeTerrainShapeSubNodes[i] = terrainShapeSubNode.toNativeTerrainShapeSubNode();
            }
        }
        return nativeTerrainShapeSubNodes;
    }

    public static TerrainShapeSubNode[] fromNativeTerrainShapeSubNode(int depth, NativeTerrainShapeSubNode[] nativeTerrainShapeSubNodes) {
        if (nativeTerrainShapeSubNodes == null) {
            return null;
        }
        TerrainShapeSubNode[] terrainShapeSubNodes = new TerrainShapeSubNode[nativeTerrainShapeSubNodes.length];
        for (int i = 0; i < nativeTerrainShapeSubNodes.length; i++) {
            NativeTerrainShapeSubNode nativeTerrainShapeSubNode = nativeTerrainShapeSubNodes[i];
            if (nativeTerrainShapeSubNode != null) {
                terrainShapeSubNodes[i] = new TerrainShapeSubNode(depth, nativeTerrainShapeSubNode);
            }
        }
        return terrainShapeSubNodes;
    }

    public static TerrainShapeSubNode getTerrainShapeSubNode(int depth, DecimalPosition nodeRelative, TerrainShapeSubNode[] terrainShapeSubNodes) {
        if (terrainShapeSubNodes == null) {
            return null;
        }
        double subLength = TerrainUtil.calculateSubNodeLength(depth);
        Index subNodeIndex = nodeRelative.divide(subLength).toIndex();
        if (subNodeIndex.getX() == 0 && subNodeIndex.getY() == 0) {
            return accessSubNode(depth + 1, terrainShapeSubNodes, 0, nodeRelative);
        } else if (subNodeIndex.getX() == 1 && subNodeIndex.getY() == 0) {
            return accessSubNode(depth + 1, terrainShapeSubNodes, 1, nodeRelative.sub(subLength, 0));
        } else if (subNodeIndex.getX() == 1 && subNodeIndex.getY() == 1) {
            return accessSubNode(depth + 1, terrainShapeSubNodes, 2, nodeRelative.sub(subLength, subLength));
        } else if (subNodeIndex.getX() == 0 && subNodeIndex.getY() == 1) {
            return accessSubNode(depth + 1, terrainShapeSubNodes, 3, nodeRelative.sub(0, subLength));
        } else {
            throw new IllegalArgumentException("TerrainShapeNode.outerDirectionCallback() unknown index: " + subNodeIndex + " nodeRelative: " + nodeRelative);
        }
    }

    private static TerrainShapeSubNode accessSubNode(int depth, TerrainShapeSubNode[] terrainShapeSubNodes, int index, DecimalPosition subNodeRelative) {
        TerrainShapeSubNode terrainShapeSubNode = terrainShapeSubNodes[index];
        if (terrainShapeSubNode == null) {
            return null;
        }
        if (terrainShapeSubNode.getTerrainShapeSubNodes() == null) {
            return terrainShapeSubNode;
        }
        TerrainShapeSubNode subNode = terrainShapeSubNode.getTerrainShapeSubNode(depth, subNodeRelative);
        if (subNode != null) {
            return subNode;
        } else {
            return terrainShapeSubNode;
        }
    }

    public static Index numberToSubNodeIndex(int number) {
        switch (number) {
            case 0:
                return new Index(0, 0);
            case 1:
                return new Index(1, 0);
            case 2:
                return new Index(1, 1);
            case 3:
                return new Index(0, 1);
        }
        throw new IllegalArgumentException("TerrainShapeSubNode.numberToSubNodeIndex(): " + number);
    }

    public void outerDirectionCallback(AStarContext aStarContext, Index outerDirection, DecimalPosition subNodePosition, TerrainShapeNode.DirectionConsumer directionConsumer) {
        if (terrainShapeSubNodes == null) {
            if (aStarContext.isAllowed(terrainType)) {
                directionConsumer.onTerrainShapeSubNode(this, subNodePosition);
            }
        } else if (outerDirection.getX() > 0) {
            // Access from west
            double length = TerrainUtil.calculateSubNodeLength(depth + 1);
            getChildSubNodeBL().outerDirectionCallback(aStarContext, outerDirection, subNodePosition, directionConsumer);
            getChildSubNodeTL().outerDirectionCallback(aStarContext, outerDirection, subNodePosition.add(0, length), directionConsumer);
        } else if (outerDirection.getX() < 0) {
            // Access from east
            double length = TerrainUtil.calculateSubNodeLength(depth + 1);
            getChildSubNodeBR().outerDirectionCallback(aStarContext, outerDirection, subNodePosition.add(length, 0), directionConsumer);
            getChildSubNodeTR().outerDirectionCallback(aStarContext, outerDirection, subNodePosition.add(length, length), directionConsumer);
        } else if (outerDirection.getY() > 0) {
            // Access from south
            double length = TerrainUtil.calculateSubNodeLength(depth + 1);
            getChildSubNodeBL().outerDirectionCallback(aStarContext, outerDirection, subNodePosition, directionConsumer);
            getChildSubNodeBR().outerDirectionCallback(aStarContext, outerDirection, subNodePosition.add(length, 0), directionConsumer);
        } else if (outerDirection.getY() < 0) {
            // Access from north
            double length = TerrainUtil.calculateSubNodeLength(depth + 1);
            getChildSubNodeTR().outerDirectionCallback(aStarContext, outerDirection, subNodePosition.add(length, length), directionConsumer);
            getChildSubNodeTL().outerDirectionCallback(aStarContext, outerDirection, subNodePosition.add(0, length), directionConsumer);
        } else {
            throw new IllegalArgumentException("TerrainShapeNode.outerDirectionCallback() outerDirection: " + outerDirection);
        }
    }

    public void outerDirectionCallback(AStarContext aStarContext, DecimalPosition subNodeRelative, DecimalPosition subNodeAbsolute, int destinationDepth, Index direction, TerrainShapeNode.DirectionConsumer directionConsumer) {
        if (destinationDepth <= depth) {
            outerDirectionCallback(aStarContext, direction, subNodeAbsolute, directionConsumer);
            return;
        }
        double length = TerrainUtil.calculateSubNodeLength(destinationDepth);
        if (subNodeRelative.getX() < length && subNodeRelative.getY() < length) {
            getChildSubNodeBL().outerDirectionCallback(aStarContext, subNodeRelative, subNodeAbsolute, destinationDepth, direction, directionConsumer);
        } else if (subNodeRelative.getX() >= length && subNodeRelative.getY() < length) {
            getChildSubNodeBR().outerDirectionCallback(aStarContext, subNodeRelative.sub(length, 0), subNodeAbsolute, destinationDepth, direction, directionConsumer);
        } else if (subNodeRelative.getX() >= length && subNodeRelative.getY() >= length) {
            getChildSubNodeTR().outerDirectionCallback(aStarContext, subNodeRelative.sub(length, length), subNodeAbsolute, destinationDepth, direction, directionConsumer);
        } else if (subNodeRelative.getX() < length && subNodeRelative.getY() >= length) {
            getChildSubNodeTL().outerDirectionCallback(aStarContext, subNodeRelative.sub(0, length), subNodeAbsolute, destinationDepth, direction, directionConsumer);
        } else {
            throw new IllegalArgumentException("TerrainShapeSubNode.outerDirectionCallback()");
        }
    }

    public void setDrivewayHeights(double[] drivewayHeights) {
        this.drivewayHeights = drivewayHeights;
    }

    public double[] getDrivewayHeights() {
        return drivewayHeights;
    }

    public boolean isDriveway() {
        return drivewayHeights != null;
    }

    public double getDrivewayHeightBL() {
        if (drivewayHeights == null) {
            throw new IllegalStateException("TerrainShapeSubNode.getDrivewayHeightBL() drivewayHeights == null");
        }
        return drivewayHeights[0];
    }

    public double getDrivewayHeightBR() {
        if (drivewayHeights == null) {
            throw new IllegalStateException("TerrainShapeSubNode.getDrivewayHeightBR() drivewayHeights == null");
        }
        return drivewayHeights[1];
    }

    public double getDrivewayHeightTR() {
        if (drivewayHeights == null) {
            throw new IllegalStateException("TerrainShapeSubNode.getDrivewayHeightTR() drivewayHeights == null");
        }
        return drivewayHeights[2];
    }

    public double getDrivewayHeightTL() {
        if (drivewayHeights == null) {
            throw new IllegalStateException("TerrainShapeSubNode.getDrivewayHeightTL() drivewayHeights == null");
        }
        return drivewayHeights[3];
    }
}
