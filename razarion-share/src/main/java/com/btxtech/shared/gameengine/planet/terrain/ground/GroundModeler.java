package com.btxtech.shared.gameengine.planet.terrain.ground;


import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;

/**
 * Created by Beat
 * 03.05.2016.
 */
@Deprecated
public class GroundModeler {
    @Deprecated
    public static GroundMesh generateGroundMesh(GroundSkeletonConfig groundSkeletonConfig, Rectangle groundMeshDimension) {
        GroundMesh groundMesh = new GroundMesh();
        groundMesh.reset(TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH, groundMeshDimension, 0);

        for (int x = groundMeshDimension.startX(); x < groundMeshDimension.endX(); x++) {
            for (int y = groundMeshDimension.startY(); y < groundMeshDimension.endY(); y++) {
                VertexData vertexData = groundMesh.getVertexDataSafe(new Index(x, y));
                vertexData.setSplatting(groundSkeletonConfig.getSplattings()[x % groundSkeletonConfig.getSplattingXCount()][y % groundSkeletonConfig.getSplattingYCount()]);
                vertexData.addZ(groundSkeletonConfig.getHeights()[x % groundSkeletonConfig.getHeightXCount()][y % groundSkeletonConfig.getHeightYCount()]);
            }
        }

        return groundMesh;
    }


}
