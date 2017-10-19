package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeTerrainShapeSubNode;

/**
 * Created by Beat
 * on 20.06.2017.
 */
public class TerrainShapeSubNode {
    private TerrainType terrainType;
    private Double height;
    private int depth;
    private TerrainShapeSubNode parent;
    private TerrainShapeSubNode[] terrainShapeSubNodes; // bl, br, tr, tl

    public TerrainShapeSubNode(TerrainShapeSubNode parent, int depth) {
        this.parent = parent;
        this.depth = depth;
    }

    public TerrainShapeSubNode(TerrainShapeSubNode parent, int depth, NativeTerrainShapeSubNode nativeTerrainShapeSubNode) {
        this.parent = parent;
        this.depth = depth;
        if (nativeTerrainShapeSubNode.terrainTypeOrdinal != null) {
            terrainType = TerrainType.values()[nativeTerrainShapeSubNode.terrainTypeOrdinal];
        }
        height = nativeTerrainShapeSubNode.height;
        terrainShapeSubNodes = fromNativeTerrainShapeSubNode(this, depth + 1, nativeTerrainShapeSubNode.nativeTerrainShapeSubNodes);
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public Double getHeight() {
        return getHeightRecursively();
    }

    @Deprecated // too complex
    private Double getHeightRecursively() {
        if (height != null) {
            return height;
        }
        if (parent != null) {
            return parent.getHeightRecursively();
        }
        return null;
    }

    public Vertex getNorm() {
        return Vertex.Z_NORM; // TODO
    }

    @Deprecated // use getTerrainType()
    public boolean isLand() {
        return terrainType == TerrainType.LAND;
    }

    public TerrainType getTerrainType() {
        return terrainType;
    }

//    @Deprecated // too complex
//     private Boolean isNotLandRecursively() {
//        if(notLand != null && notLand) {
//            return true;
//        }
//        if(parent != null) {
//            return parent.isNotLandRecursively();
//        }
//        return null;
//    }

    public void setTerrainType(TerrainType terrainType) {
        this.terrainType = terrainType;
    }

    public TerrainShapeSubNode[] getTerrainShapeSubNodes() {
        return terrainShapeSubNodes;
    }

    public void setTerrainShapeSubNodes(TerrainShapeSubNode[] terrainShapeSubNodes) {
        this.terrainShapeSubNodes = terrainShapeSubNodes;
    }

    public NativeTerrainShapeSubNode toNativeTerrainShapeSubNode() {
        NativeTerrainShapeSubNode nativeTerrainShapeSubNode = new NativeTerrainShapeSubNode();
        nativeTerrainShapeSubNode.terrainTypeOrdinal = terrainType != null ? terrainType.ordinal() : null;
        nativeTerrainShapeSubNode.height = height;
        nativeTerrainShapeSubNode.nativeTerrainShapeSubNodes = toNativeTerrainShapeSubNode(terrainShapeSubNodes);
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

    public static TerrainShapeSubNode[] fromNativeTerrainShapeSubNode(TerrainShapeSubNode parent, int depth, NativeTerrainShapeSubNode[] nativeTerrainShapeSubNodes) {
        if (nativeTerrainShapeSubNodes == null) {
            return null;
        }
        TerrainShapeSubNode[] terrainShapeSubNodes = new TerrainShapeSubNode[nativeTerrainShapeSubNodes.length];
        for (int i = 0; i < nativeTerrainShapeSubNodes.length; i++) {
            NativeTerrainShapeSubNode nativeTerrainShapeSubNode = nativeTerrainShapeSubNodes[i];
            if (nativeTerrainShapeSubNode != null) {
                terrainShapeSubNodes[i] = new TerrainShapeSubNode(parent, depth, nativeTerrainShapeSubNode);
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

    public void outerDirectionCallback(TerrainType terrainType, Index outerDirection, DecimalPosition subNodePosition, TerrainShapeNode.DirectionConsumer directionConsumer) {
        if (terrainShapeSubNodes == null) {
            if (TerrainType.isAllowed(terrainType, this.terrainType)) {
                directionConsumer.onTerrainShapeSubNode(this, subNodePosition);
            }
        } else if (outerDirection.getX() > 0) {
            // Access from west
            double length = TerrainUtil.calculateSubNodeLength(depth + 1);
            terrainShapeSubNodes[0].outerDirectionCallback(terrainType, outerDirection, subNodePosition, directionConsumer);
            terrainShapeSubNodes[3].outerDirectionCallback(terrainType, outerDirection, subNodePosition.add(0, length), directionConsumer);
        } else if (outerDirection.getX() < 0) {
            // Access from east
            double length = TerrainUtil.calculateSubNodeLength(depth + 1);
            terrainShapeSubNodes[1].outerDirectionCallback(terrainType, outerDirection, subNodePosition.add(length, 0), directionConsumer);
            terrainShapeSubNodes[2].outerDirectionCallback(terrainType, outerDirection, subNodePosition.add(length, length), directionConsumer);
        } else if (outerDirection.getY() > 0) {
            // Access from south
            double length = TerrainUtil.calculateSubNodeLength(depth + 1);
            terrainShapeSubNodes[0].outerDirectionCallback(terrainType, outerDirection, subNodePosition, directionConsumer);
            terrainShapeSubNodes[1].outerDirectionCallback(terrainType, outerDirection, subNodePosition.add(length, 0), directionConsumer);
        } else if (outerDirection.getY() < 0) {
            // Access from north
            double length = TerrainUtil.calculateSubNodeLength(depth + 1);
            terrainShapeSubNodes[2].outerDirectionCallback(terrainType, outerDirection, subNodePosition.add(length, length), directionConsumer);
            terrainShapeSubNodes[3].outerDirectionCallback(terrainType, outerDirection, subNodePosition.add(0, length), directionConsumer);
        } else {
            throw new IllegalArgumentException("TerrainShapeNode.outerDirectionCallback() outerDirection: " + outerDirection);
        }
    }

    public void outerDirectionCallback(TerrainType terrainType, DecimalPosition subNodeRelative, DecimalPosition subNodeAbsolute, int destinationDepth, Index direction, TerrainShapeNode.DirectionConsumer directionConsumer) {
        if (destinationDepth >= depth) {
            outerDirectionCallback(terrainType, direction, subNodeAbsolute, directionConsumer);
            return;
        }
        double length = TerrainUtil.calculateSubNodeLength(this.depth);
        if (subNodeRelative.getX() < length && subNodeRelative.getY() < length) {
            terrainShapeSubNodes[0].outerDirectionCallback(terrainType, subNodeRelative, subNodeAbsolute, destinationDepth, direction, directionConsumer);
        } else if (subNodeRelative.getX() >= length && subNodeRelative.getY() < length) {
            terrainShapeSubNodes[1].outerDirectionCallback(terrainType, subNodeRelative.sub(length, 0), subNodeAbsolute, destinationDepth, direction, directionConsumer);
        } else if (subNodeRelative.getX() >= length && subNodeRelative.getY() >= length) {
            terrainShapeSubNodes[2].outerDirectionCallback(terrainType, subNodeRelative.sub(length, length), subNodeAbsolute, destinationDepth, direction, directionConsumer);
        } else if (subNodeRelative.getX() < length && subNodeRelative.getY() >= length) {
            terrainShapeSubNodes[3].outerDirectionCallback(terrainType, subNodeRelative.sub(0, length), subNodeAbsolute, destinationDepth, direction, directionConsumer);
        } else {
            throw new IllegalArgumentException("TerrainShapeSubNode.outerDirectionCallback()");
        }

    }

    public void merge(TerrainShapeSubNode terrainShapeSubNode) {
        System.out.println(" FIX ME com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeSubNode at 246 ********************* " + terrainShapeSubNode);
//        Boolean notLand = isNotLandRecursively();
//        Boolean otherNotLand = terrainShapeSubNode.isNotLandRecursively();
//        if (notLand == null && otherNotLand != null) {
//            if (otherNotLand) {
//                this.notLand = true;
//            }
//        } else if (notLand != null && otherNotLand != null) {
//            if (notLand || otherNotLand) {
//                this.notLand = true;
//            } else {
//                this.notLand = null;
//            }
//        } else if (notLand != null) {
//            if (notLand) {
//                this.notLand = true;
//            } else {
//                this.notLand = null;
//            }
//        }
//
//        if (height == null && terrainShapeSubNode.height != null) {
//            height = terrainShapeSubNode.height;
//        }
//
//        if (terrainShapeSubNodes == null && terrainShapeSubNode.getTerrainShapeSubNodes() != null) {
//            terrainShapeSubNodes = terrainShapeSubNode.getTerrainShapeSubNodes();
//            for (TerrainShapeSubNode shapeSubNode : terrainShapeSubNodes) {
//                shapeSubNode.parent = this;
//            }
//        } else if (terrainShapeSubNodes != null && terrainShapeSubNode.getTerrainShapeSubNodes() != null) {
//            for (int i = 0; i < terrainShapeSubNodes.length; i++) {
//                terrainShapeSubNodes[i].merge(terrainShapeSubNode.getTerrainShapeSubNodes()[i]);
//            }
//        }
    }
}
