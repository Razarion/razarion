package com.btxtech.shared;

import com.btxtech.client.renderer.model.GroundMesh;
import com.btxtech.client.renderer.model.VertexData;
import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.game.jsre.client.common.Index;

import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 02.05.2016.
 */
public class GroundSkeletonEntity {
    // private Logger logger = Logger.getLogger(GroundSkeletonEntity.class.getName());
    private double splattingDistance;
    private double bumpMapDepth;
    private double specularHardness;
    private double specularIntensity;
    private List<GroundSplattingEntry> splattings;
    private int splattingXCount;
    private int splattingYCount;
    private List<GroundHeightEntry> heights;
    private int heightXCount;
    private int heightYCount;

    public void setValues(GroundConfigEntity groundConfigEntity) {
        splattingDistance = groundConfigEntity.getSplattingDistance();
        bumpMapDepth = groundConfigEntity.getBumpMapDepth();
        specularHardness = groundConfigEntity.getSpecularHardness();
        specularIntensity = groundConfigEntity.getSpecularIntensity();
    }

    public void setValues(List<GroundSplattingEntry> splattings, List<GroundHeightEntry> heights, GroundConfigEntity groundConfigEntity) {
        this.splattings = splattings;
        this.heights = heights;
        splattingXCount = groundConfigEntity.getSplattingXCount();
        splattingYCount = groundConfigEntity.getSplattingYCount();
        heightXCount = groundConfigEntity.getHeightXCount();
        heightYCount = groundConfigEntity.getHeightYCount();
        setValues(groundConfigEntity);
    }

    public GroundMesh generateGroundMesh(int xCount, int yCount) {
        GroundMesh groundMesh = new GroundMesh();
        groundMesh.reset(TerrainSurface.MESH_NODE_EDGE_LENGTH, xCount, yCount, 0);

        double[][] splattingNodes = new double[splattingXCount][splattingYCount];
        for (GroundSplattingEntry groundSplattingEntry : splattings) {
            splattingNodes[groundSplattingEntry.getXIndex()][groundSplattingEntry.getYIndex()] = groundSplattingEntry.getSplatting();
        }

        double[][] heightNodes = new double[heightXCount][heightYCount];
        for (GroundHeightEntry groundHeightEntry : heights) {
            heightNodes[groundHeightEntry.getXIndex()][groundHeightEntry.getYIndex()] = groundHeightEntry.getHeight();
        }

        for (int x = 0; x < xCount; x++) {
            for (int y = 0; y < yCount; y++) {
                VertexData vertexData = groundMesh.getVertexDataSafe(new Index(x, y));
                vertexData.setSplatting(splattingNodes[x % splattingXCount][y % splattingYCount]);
                vertexData.addZ(heightNodes[x % heightXCount][y % heightYCount]);
            }
        }

        return groundMesh;
    }

    public double getSplattingDistance() {
        return splattingDistance;
    }

    public double getBumpMapDepth() {
        return bumpMapDepth;
    }

    public double getSpecularHardness() {
        return specularHardness;
    }

    public double getSpecularIntensity() {
        return specularIntensity;
    }
}
