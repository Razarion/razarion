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
public class SurfaceAccess {
    // final private static Logger LOGGER = Logger.getLogger(SurfaceAccess.class.getName());
    private TerrainShapeManager terrainShape;

    public SurfaceAccess(TerrainShapeManager terrainShape) {
        this.terrainShape = terrainShape;
    }

    public double getHighestZInRegion(DecimalPosition center, double radius) {
        Vertex norm = getInterpolatedNorm(center);
        double angle = Vertex.Z_NORM.unsignedAngle(norm);
        return Math.tan(angle) * radius + getInterpolatedZ(center);
    }

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

    public Vertex getInterpolatedNorm(DecimalPosition absolutePosition) {
        return terrainShape.terrainImpactCallback(absolutePosition, new TerrainImpactCallback<Vertex>() {
            @Override
            public Vertex landNoTile(Index tileIndex) {
                return interpolateNormFromGroundSkeletonConfig(absolutePosition);
            }
        });
    }

    private Vertex interpolateNormFromGroundSkeletonConfig(DecimalPosition absolutePosition) {
        // Ground skeleton is not respected
        // return interpolateNormFromGroundSkeletonConfig(absolutePosition, 0, 0, 0, 0);
        return Vertex.Z_NORM;
    }

    /**
     * @param relative position with 0..1, 0..1
     * @param length   the length of the quadratic side
     * @param zBL      z bottom left
     * @param zBR      z bottom right
     * @param zTR      z top right
     * @param zTL      z tol left
     * @return the norm vector
     */
    private Vertex interpolateNormFromGroundSkeletonConfig(DecimalPosition relative, double length, double zBL, double zBR, double zTR, double zTL) {
        // Ground skeleton is not respected
        Triangle2d triangle1 = new Triangle2d(new DecimalPosition(0, 0), new DecimalPosition(length, 0), new DecimalPosition(0, length));
        if (triangle1.isInside(relative)) {
            return new Vertex(zBL - zBR, zBL - zTL, length).normalize(1.0);
        } else {
            return new Vertex(zTL - zTR, zBR - zTR, length).normalize(1.0);
        }
    }

    private double interpolateHeightFromGroundSkeletonConfig(DecimalPosition absolutePosition) {
        // Ground skeleton is not respected
        return 0;
    }

}
