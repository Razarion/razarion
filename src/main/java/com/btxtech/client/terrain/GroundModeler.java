package com.btxtech.client.terrain;


import com.btxtech.client.renderer.model.GroundMesh;
import com.btxtech.client.renderer.model.VertexData;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.shared.dto.GroundSkeleton;

/**
 * Created by Beat
 * 03.05.2016.
 */
public class GroundModeler {
    public static void sculptSkeleton(GroundConfig groundConfig) {
        FractalField heightField = FractalField.createSaveFractalField(groundConfig.getGroundSkeleton().getHeightXCount(), groundConfig.getGroundSkeleton().getHeightYCount(), groundConfig.getHeightFractalRoughness(), -groundConfig.getHeightFractalShift() / 2.0, groundConfig.getHeightFractalShift());
        double[][] heights = new double[groundConfig.getGroundSkeleton().getHeightXCount()][groundConfig.getGroundSkeleton().getHeightYCount()];
        for (int x = 0; x < groundConfig.getGroundSkeleton().getHeightXCount(); x++) {
            for (int y = 0; y < groundConfig.getGroundSkeleton().getHeightYCount(); y++) {
                heights[x][y] = heightField.getValue(x, y);
            }
        }
        groundConfig.getGroundSkeleton().setHeights(heights);
    }

    public static GroundMesh generateGroundMesh(GroundSkeleton groundSkeleton, int xCount, int yCount) {
        GroundMesh groundMesh = new GroundMesh();
        groundMesh.reset(TerrainSurface.MESH_NODE_EDGE_LENGTH, xCount, yCount, 0);

        for (int x = 0; x < xCount; x++) {
            for (int y = 0; y < yCount; y++) {
                VertexData vertexData = groundMesh.getVertexDataSafe(new Index(x, y));
                vertexData.setSplatting(groundSkeleton.getSplattings()[x % groundSkeleton.getSplattingXCount()][y % groundSkeleton.getSplattingYCount()]);
                vertexData.addZ(groundSkeleton.getHeights()[x % groundSkeleton.getHeightXCount()][y % groundSkeleton.getHeightYCount()]);
            }
        }

        return groundMesh;
    }


}
