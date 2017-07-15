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
    private Boolean land;
    private double height;
    private int depth;
    private TerrainShapeSubNode[] terrainShapeSubNodes; // bl, br, tr, tl

    public TerrainShapeSubNode(int depth) {
        this.depth = depth;
    }

    public TerrainShapeSubNode(int depth, NativeTerrainShapeSubNode nativeTerrainShapeSubNode) {
        this.depth = depth;
        land = nativeTerrainShapeSubNode.land;
        height = nativeTerrainShapeSubNode.height;
        terrainShapeSubNodes = fromNativeTerrainShapeSubNode(depth + 1, nativeTerrainShapeSubNode.nativeTerrainShapeSubNodes);
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getHeight() {
        return height;
    }

    public Vertex getNorm() {
        return Vertex.Z_NORM; // TODO
    }

    public boolean isLand() {
        return land != null && land;
    }

    public void setLand() {
        land = true;
    }

    public TerrainShapeSubNode[] getTerrainShapeSubNodes() {
        return terrainShapeSubNodes;
    }

    public void setTerrainShapeSubNodes(TerrainShapeSubNode[] terrainShapeSubNodes) {
        this.terrainShapeSubNodes = terrainShapeSubNodes;
    }

    public NativeTerrainShapeSubNode toNativeTerrainShapeSubNode() {
        NativeTerrainShapeSubNode nativeTerrainShapeSubNode = new NativeTerrainShapeSubNode();
        nativeTerrainShapeSubNode.land = land;
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
            throw new IllegalArgumentException("TerrainShapeNode.getTerrainShapeSubNode() unknown index: " + subNodeIndex + " nodeRelative: " + nodeRelative);
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
}
