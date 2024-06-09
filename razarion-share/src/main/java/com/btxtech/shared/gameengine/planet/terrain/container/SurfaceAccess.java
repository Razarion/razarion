package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Triangle2d;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.utils.InterpolationUtils;

/**
 * Created by Beat
 * on 19.06.2017.
 */
@Deprecated
public class SurfaceAccess {
    // final private static Logger LOGGER = Logger.getLogger(SurfaceAccess.class.getName());
    private TerrainShapeManager terrainShape;

    public SurfaceAccess(TerrainShapeManager terrainShape) {
        this.terrainShape = terrainShape;
    }

    @Deprecated
    public double getHighestZInRegion(DecimalPosition center, double radius) {
        Vertex norm = getInterpolatedNorm(center);
        double angle = Vertex.Z_NORM.unsignedAngle(norm);
        return Math.tan(angle) * radius + getInterpolatedZ(center);
    }

    @Deprecated
    public double getInterpolatedZ(DecimalPosition absolutePosition) {
        Double z = terrainShape.terrainImpactCallback(absolutePosition, new TerrainImpactCallback<Double>() {
            @Override
            public Double landNoTile(Index tileIndex) {
                return interpolateHeightFromGroundSkeletonConfig(absolutePosition);
            }

        });
        if (z == null) {
            throw new IllegalArgumentException("SurfaceAccess.getInterpolatedZ() TerrainShapeNode at: " + absolutePosition + "  has no z-position");
        }
        return z;
    }

    @Deprecated
    public Vertex getInterpolatedNorm(DecimalPosition absolutePosition) {
        return terrainShape.terrainImpactCallback(absolutePosition, new TerrainImpactCallback<Vertex>() {
            @Override
            public Vertex landNoTile(Index tileIndex) {
                return interpolateNormFromGroundSkeletonConfig(absolutePosition);
            }
        });
    }

    @Deprecated
    private Vertex interpolateNormFromGroundSkeletonConfig(DecimalPosition absolutePosition) {
        return Vertex.Z_NORM;
    }

    private double interpolateHeightFromGroundSkeletonConfig(DecimalPosition absolutePosition) {
        // Ground skeleton is not respected
        return 0;
    }

}
