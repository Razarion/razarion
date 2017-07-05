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
    public static final int DEPTH = 3;
    private Boolean land;
    private double height;
    private TerrainShapeSubNode[] terrainShapeSubNodes; // bl, br, tr, tl

    public TerrainShapeSubNode() {

    }

    public TerrainShapeSubNode(NativeTerrainShapeSubNode nativeTerrainShapeSubNode) {
        land = nativeTerrainShapeSubNode.land;
        height = nativeTerrainShapeSubNode.height;
        terrainShapeSubNodes = fromNativeTerrainShapeSubNode(nativeTerrainShapeSubNode.nativeTerrainShapeSubNodes);
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

    public TerrainShapeSubNode getTerrainShapeSubNode(DecimalPosition subNodeRelative) {
        return getTerrainShapeSubNode(subNodeRelative, terrainShapeSubNodes);
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

    public static TerrainShapeSubNode[] fromNativeTerrainShapeSubNode(NativeTerrainShapeSubNode[] nativeTerrainShapeSubNodes) {
        if (nativeTerrainShapeSubNodes == null) {
            return null;
        }
        TerrainShapeSubNode[] terrainShapeSubNodes = new TerrainShapeSubNode[nativeTerrainShapeSubNodes.length];
        for (int i = 0; i < nativeTerrainShapeSubNodes.length; i++) {
            NativeTerrainShapeSubNode nativeTerrainShapeSubNode = nativeTerrainShapeSubNodes[i];
            if (nativeTerrainShapeSubNode != null) {
                terrainShapeSubNodes[i] = new TerrainShapeSubNode(nativeTerrainShapeSubNode);
            }
        }
        return terrainShapeSubNodes;
    }

    public static TerrainShapeSubNode getTerrainShapeSubNode(DecimalPosition nodeRelative, TerrainShapeSubNode[] terrainShapeSubNodes) {
        if (terrainShapeSubNodes == null) {
            return null;
        }
        double subLength = TerrainUtil.calculateSubNodeLength(0);
        Index subNodeIndex = nodeRelative.divide(subLength).toIndex();
        if (subNodeIndex.getX() == 0 && subNodeIndex.getY() == 0) {
            return accessSubNode(terrainShapeSubNodes, 0, nodeRelative);
        } else if (subNodeIndex.getX() == 1 && subNodeIndex.getY() == 0) {
            return accessSubNode(terrainShapeSubNodes, 1, nodeRelative.sub(subLength, 0));
        } else if (subNodeIndex.getX() == 1 && subNodeIndex.getY() == 1) {
            return accessSubNode(terrainShapeSubNodes, 2, nodeRelative.sub(subLength, subLength));
        } else if (subNodeIndex.getX() == 0 && subNodeIndex.getY() == 1) {
            return accessSubNode(terrainShapeSubNodes, 2, nodeRelative.sub(0, subLength));
        } else {
            throw new IllegalArgumentException("TerrainShapeNode.getTerrainShapeSubNode() unknown index: " + subNodeIndex + " nodeRelative: " + nodeRelative);
        }
    }

    private static TerrainShapeSubNode accessSubNode(TerrainShapeSubNode[] terrainShapeSubNodes, int index, DecimalPosition subNodeRelative) {
        TerrainShapeSubNode terrainShapeSubNode = terrainShapeSubNodes[index];
        if (terrainShapeSubNode == null) {
            return null;
        }
        if (terrainShapeSubNode.getTerrainShapeSubNodes() == null) {
            return terrainShapeSubNode;
        }
        TerrainShapeSubNode subNode = terrainShapeSubNode.getTerrainShapeSubNode(subNodeRelative);
        if (subNode != null) {
            return subNode;
        } else {
            return terrainShapeSubNode;
        }
    }

}
