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
                switch (terrainShapeTile.getTerrainType()) {
                    case LAND:
                        return interpolateHeightFromGroundSkeletonConfig(absolutePosition) + terrainShapeTile.getUniformGroundHeight();
                    case WATER:
                        return terrainShapeTile.getUniformGroundHeight();
                    default:
                        throw new IllegalArgumentException("SurfaceAccess.getInterpolatedZ() for TerrainShapeTile at: " + absolutePosition + " TerrainType: " + terrainShapeTile.getTerrainType());
                }
            }

            @Override
            public Double inNode(TerrainShapeNode terrainShapeNode, Index nodeRelativeIndex, DecimalPosition tileRelative, Index tileIndex) {
                if (terrainShapeNode.getTerrainType() == null) {
                    return interpolateHeightFromGroundSkeletonConfig(absolutePosition) + terrainShapeNode.getGameEngineHeight();
                }
                switch (terrainShapeNode.getTerrainType()) {
                    case LAND:
                        if (terrainShapeNode.isFullGameEngineDriveway()) {
                            DecimalPosition relative = TerrainUtil.toNodeAbsolute(absolutePosition.sub(TerrainUtil.toNodeAbsolute(nodeRelativeIndex).add(TerrainUtil.toTileAbsolute(tileIndex))));
                            return InterpolationUtils.rectangleInterpolate(relative, terrainShapeNode.getDrivewayHeightBL(), terrainShapeNode.getDrivewayHeightBR(), terrainShapeNode.getDrivewayHeightTR(), terrainShapeNode.getDrivewayHeightTL());
                        } else {
                            return interpolateHeightFromGroundSkeletonConfig(absolutePosition) + terrainShapeNode.getGameEngineHeight();
                        }
                    case WATER:
                        return terrainShapeNode.getFullWaterLevel();
                    case LAND_COAST:
                        return interpolateHeightFromGroundSkeletonConfig(absolutePosition) + terrainShapeNode.getGameEngineHeight();
                    case WATER_COAST:
                        return interpolateHeightFromGroundSkeletonConfig(absolutePosition) + terrainShapeNode.getGameEngineHeight();
                    case BLOCKED:
                        return interpolateHeightFromGroundSkeletonConfig(absolutePosition) + terrainShapeNode.getGameEngineHeight();
                }
                throw new IllegalArgumentException("SurfaceAccess.getInterpolatedZ() TerrainShapeNode at: " + absolutePosition + " TerrainType: " + terrainShapeNode.getTerrainType());
            }

            @Override
            public Double inSubNode(TerrainShapeSubNode terrainShapeSubNode, TerrainShapeNode terrainShapeNode, DecimalPosition nodeRelative, Index nodeRelativeIndex, DecimalPosition tileRelative, Index tileIndex) {
                if (terrainShapeSubNode.getTerrainType() == null) {
                    return interpolateHeightFromGroundSkeletonConfig(absolutePosition) + terrainShapeSubNode.getHeight();
                }
                switch (terrainShapeSubNode.getTerrainType()) {
                    case LAND:
                        if (terrainShapeSubNode.isDriveway()) {
                            DecimalPosition relative = absolutePosition.sub(tileRelative.add(TerrainUtil.toTileAbsolute(tileIndex)));
                            DecimalPosition normalizedRelative = relative.divide(TerrainUtil.calculateSubNodeLength(terrainShapeSubNode.getDepth()));
                            return InterpolationUtils.rectangleInterpolate(normalizedRelative, terrainShapeSubNode.getDrivewayHeightBL(), terrainShapeSubNode.getDrivewayHeightBR(), terrainShapeSubNode.getDrivewayHeightTR(), terrainShapeSubNode.getDrivewayHeightTL());
                        } else {
                            double height = terrainShapeSubNode.getHeight() != null ? terrainShapeSubNode.getHeight() : 0;
                            return interpolateHeightFromGroundSkeletonConfig(absolutePosition) + height;
                        }
                    case WATER:
                        return terrainShapeSubNode.getHeight();
                    case LAND_COAST:
                        return interpolateHeightFromGroundSkeletonConfig(absolutePosition) + terrainShapeSubNode.getHeight();
                    case WATER_COAST:
                        return interpolateHeightFromGroundSkeletonConfig(absolutePosition) + terrainShapeSubNode.getHeight();
                    case BLOCKED:
                        return interpolateHeightFromGroundSkeletonConfig(absolutePosition) + terrainShapeSubNode.getHeight();
                }
                throw new IllegalArgumentException("SurfaceAccess.getInterpolatedZ() TerrainShapeSubNode at: " + absolutePosition + " TerrainType: " + terrainShapeNode.getTerrainType());
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
                return interpolateNormFromGroundSkeletonConfig(absolutePosition);
            }

            @Override
            public Vertex inNode(TerrainShapeNode terrainShapeNode, Index nodeIndex, DecimalPosition tileRelative, Index tileIndex) {
                if (terrainShapeNode.isFullGameEngineDriveway()) {
                    DecimalPosition relative = absolutePosition.sub(TerrainUtil.toTileAbsolute(nodeIndex)).sub(tileRelative);
                    return interpolateNormFromGroundSkeletonConfig(relative, TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH, terrainShapeNode.getDrivewayHeightBL(), terrainShapeNode.getDrivewayHeightBR(), terrainShapeNode.getDrivewayHeightTR(), terrainShapeNode.getDrivewayHeightTL());
                } else {
                    return interpolateNormFromGroundSkeletonConfig(absolutePosition);
                }
            }

            @Override
            public Vertex inSubNode(TerrainShapeSubNode terrainShapeSubNode, TerrainShapeNode terrainShapeNode, DecimalPosition nodeRelative, Index nodeRelativeIndex, DecimalPosition tileRelative, Index tileIndex) {
                if (terrainShapeSubNode.isDriveway()) {
                    DecimalPosition relative = absolutePosition.sub(TerrainUtil.toTileAbsolute(nodeRelativeIndex)).sub(tileRelative).sub(nodeRelative);
                    return interpolateNormFromGroundSkeletonConfig(relative, TerrainUtil.calculateSubNodeLength(terrainShapeSubNode.getDepth()), terrainShapeSubNode.getDrivewayHeightBL(), terrainShapeSubNode.getDrivewayHeightBR(), terrainShapeSubNode.getDrivewayHeightTR(), terrainShapeSubNode.getDrivewayHeightTL());
                } else {
                    return interpolateNormFromGroundSkeletonConfig(absolutePosition);
                }
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
            return new Vertex(zBR - zTR, zTL - zTR, length).normalize(1.0);
        }
    }

    private double interpolateHeightFromGroundSkeletonConfig(DecimalPosition absolutePosition) {
        return 0;
    }

}
