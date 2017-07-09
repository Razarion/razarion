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
    private TerrainShape terrainShape;

    public SurfaceAccess(TerrainShape terrainShape) {
        this.terrainShape = terrainShape;
    }

    public double getHighestZInRegion(DecimalPosition center, double radius) {
        Vertex norm = getInterpolatedNorm(center);
        double angle = Vertex.Z_NORM.unsignedAngle(norm);
        return Math.tan(angle) * radius + getInterpolatedZ(center);
    }

    public double getInterpolatedZ(DecimalPosition absolutePosition) {
        return terrainShape.terrainImpactCallback(absolutePosition, new TerrainImpactCallback<Double>() {
            @Override
            public Double landNoTile(Index tileIndex) {
                return interpolateHeightFromGroundSkeletonConfig(absolutePosition);
            }

            @Override
            public Double inTile(TerrainShapeTile terrainShapeTile, Index tileIndex) {
                if (terrainShapeTile.isLand()) {
                    return interpolateHeightFromGroundSkeletonConfig(absolutePosition) + terrainShapeTile.getUniformGroundHeight();
                } else {
                    return terrainShapeTile.getUniformGroundHeight();
                }
            }

            @Override
            public Double inNode(TerrainShapeNode terrainShapeNode, Index nodeIndex, DecimalPosition tileRelative, Index tileIndex) {
                if (!terrainShapeNode.isFullWater() && !terrainShapeNode.isFullDriveway() && !terrainShapeNode.isHiddenUnderSlope()) {
                    return interpolateHeightFromGroundSkeletonConfig(absolutePosition) + terrainShapeNode.getUniformGroundHeight();
                } else if (terrainShapeNode.isFullWater()) {
                    return terrainShapeNode.getFullWaterLevel();
                } else if (terrainShapeNode.isFullDriveway()) {
                    return InterpolationUtils.rectangleInterpolate(absolutePosition.sub(TerrainUtil.toTileAbsolute(nodeIndex)), terrainShapeNode.getDrivewayHeightBL(), terrainShapeNode.getDrivewayHeightBR(), terrainShapeNode.getDrivewayHeightTR(), terrainShapeNode.getDrivewayHeightTL());
                } else if (terrainShapeNode.isHiddenUnderSlope()) {
                    return terrainShapeNode.getUniformGroundHeight();
                } else {
                    throw new IllegalArgumentException("SurfaceAccess.getInterpolatedZ() unknown state");
                }
            }

            @Override
            public Double inSubNode(TerrainShapeSubNode terrainShapeSubNode, DecimalPosition nodeRelative, Index nodeRelativeIndex, DecimalPosition tileRelative, Index tileIndex) {
                return terrainShapeSubNode.getHeight();
            }
        });
    }

    public Vertex getInterpolatedNorm(DecimalPosition absolutePosition) {
        return terrainShape.terrainImpactCallback(absolutePosition, new TerrainImpactCallback<Vertex>() {
            @Override
            public Vertex landNoTile(Index tileIndex) {
                return interpolateNormFromGroundSkeletonConfig(absolutePosition);
            }

            @Override
            public Vertex inTile(TerrainShapeTile terrainShapeTile, Index tileIndex) {
                if (terrainShapeTile.isLand()) {
                    return interpolateNormFromGroundSkeletonConfig(absolutePosition);
                } else {
                    return Vertex.Z_NORM;
                }
            }

            @Override
            public Vertex inNode(TerrainShapeNode terrainShapeNode, Index nodeIndex, DecimalPosition tileRelative, Index tileIndex) {
                if (!terrainShapeNode.isFullWater() && !terrainShapeNode.isFullDriveway()) {
                    return interpolateNormFromGroundSkeletonConfig(absolutePosition);
                } else if (terrainShapeNode.isFullWater()) {
                    return Vertex.Z_NORM;
                } else if (terrainShapeNode.isFullDriveway()) {
                    return interpolateNormFromGroundSkeletonConfig(absolutePosition.sub(TerrainUtil.toTileAbsolute(nodeIndex)), terrainShapeNode.getDrivewayHeightBL(), terrainShapeNode.getDrivewayHeightBR(), terrainShapeNode.getDrivewayHeightTR(), terrainShapeNode.getDrivewayHeightTL());
                } else {
                    throw new IllegalArgumentException("SurfaceAccess.getInterpolatedZ() unknown state");
                }
            }

            @Override
            public Vertex inSubNode(TerrainShapeSubNode terrainShapeSubNode, DecimalPosition nodeRelative, Index nodeRelativeIndex, DecimalPosition tileRelative, Index tileIndex) {
                return terrainShapeSubNode.getNorm();
            }
        });
    }

    public Vertex interpolateNormFromGroundSkeletonConfig(DecimalPosition absolutePosition) {
        return interpolateNormFromGroundSkeletonConfig(absolutePosition, 0, 0, 0, 0);
    }

    public Vertex interpolateNormFromGroundSkeletonConfig(DecimalPosition absolutePosition, double additionZBL, double additionZBR, double additionZTR, double additionZTL) {
        Index bottomLeft = TerrainUtil.toNode(absolutePosition);
        DecimalPosition offset = absolutePosition.divide(TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH).sub(new DecimalPosition(bottomLeft));

        Triangle2d triangle1 = new Triangle2d(new DecimalPosition(0, 0), new DecimalPosition(1, 0), new DecimalPosition(0, 1));
        double zBR = heightFromGroundSkeletonConfig(bottomLeft.add(1, 0)) + additionZBR;
        double zTL = heightFromGroundSkeletonConfig(bottomLeft.add(0, 1)) + additionZTL;
        if (triangle1.isInside(offset)) {
            double zBL = heightFromGroundSkeletonConfig(bottomLeft) + additionZBL;
            return new Vertex(zBL - zBR, zBL - zTL, TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH).normalize(1.0);
        } else {
            double zTR = heightFromGroundSkeletonConfig(bottomLeft.add(1, 1)) + additionZTR;
            return new Vertex(zBR - zTR, zTL - zTR, TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH).normalize(1.0);
        }
    }

    private double interpolateHeightFromGroundSkeletonConfig(DecimalPosition absolutePosition) {
        return TerrainHelper.interpolateHeightFromGroundSkeletonConfig(absolutePosition, terrainShape.getGroundSkeletonConfig());
    }

    private double heightFromGroundSkeletonConfig(Index nodeIndex) {
        return terrainShape.getGroundSkeletonConfig().getHeight(nodeIndex.getX(), nodeIndex.getY());
    }
}
