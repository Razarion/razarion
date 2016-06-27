package com.btxtech.uiservice.terrain;


import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.shared.dto.GroundSkeleton;

/**
 * Created by Beat
 * 03.05.2016.
 */
public class GroundModeler {
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
