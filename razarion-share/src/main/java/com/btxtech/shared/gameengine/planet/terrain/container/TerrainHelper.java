package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.utils.InterpolationUtils;

/**
 * Created by Beat
 * on 03.07.2017.
 */
public interface TerrainHelper {
    static double interpolateHeightFromGroundSkeletonConfig(DecimalPosition absolutePosition, GroundSkeletonConfig groundSkeletonConfig) {
        Index bottomLeft = TerrainUtil.toNode(absolutePosition);
        DecimalPosition offset = absolutePosition.divide(TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH).sub(new DecimalPosition(bottomLeft));

        double zBL = heightFromGroundSkeletonConfig(bottomLeft, groundSkeletonConfig);
        double zBR = heightFromGroundSkeletonConfig(bottomLeft.add(1, 0), groundSkeletonConfig);
        double zTR = heightFromGroundSkeletonConfig(bottomLeft.add(1, 1), groundSkeletonConfig);
        double zTL = heightFromGroundSkeletonConfig(bottomLeft.add(0, 1), groundSkeletonConfig);

        return InterpolationUtils.rectangleInterpolate(offset, zBL, zBR, zTR, zTL);
    }

    static double heightFromGroundSkeletonConfig(Index nodeIndex, GroundSkeletonConfig groundSkeletonConfig) {
        return groundSkeletonConfig.getHeight(nodeIndex.getX(), nodeIndex.getY());
    }

}
