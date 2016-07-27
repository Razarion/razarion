package com.btxtech.shared.gameengine.planet.terrain.ground;


import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;

/**
 * Created by Beat
 * 03.05.2016.
 */
public class GroundModeler {
    public static GroundMesh generateGroundMesh(GroundSkeletonConfig groundSkeletonConfig, int xCount, int yCount) {
        GroundMesh groundMesh = new GroundMesh();
        groundMesh.reset(TerrainService.MESH_NODE_EDGE_LENGTH, xCount, yCount, 0);

        for (int x = 0; x < xCount; x++) {
            for (int y = 0; y < yCount; y++) {
                VertexData vertexData = groundMesh.getVertexDataSafe(new Index(x, y));
                vertexData.setSplatting(groundSkeletonConfig.getSplattings()[x % groundSkeletonConfig.getSplattingXCount()][y % groundSkeletonConfig.getSplattingYCount()]);
                vertexData.addZ(groundSkeletonConfig.getHeights()[x % groundSkeletonConfig.getHeightXCount()][y % groundSkeletonConfig.getHeightYCount()]);
            }
        }

        return groundMesh;
    }


}
