package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.SingleHolder;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.pathing.Obstacle;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.utils.GeometricUtil;

import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 17.06.2017.
 */
public class TerrainShape {
    private static Logger logger = Logger.getLogger(TerrainShape.class.getName());
    private GroundSkeletonConfig groundSkeletonConfig;
    private TerrainShapeTile[][] terrainShapeTiles;
    private SurfaceAccess surfaceAccess;
    private PathingAccess pathingAccess;
    private Index tileOffset;
    private int tileXCount;
    private int tileYCount;

    public TerrainShape(PlanetConfig planetConfig, TerrainTypeService terrainTypeService, List<TerrainSlopePosition> terrainSlopePositions, List<TerrainObjectPosition> terrainObjectPositions) {
        long time = System.currentTimeMillis();
        this.groundSkeletonConfig = groundSkeletonConfig;
        surfaceAccess = new SurfaceAccess(this);
        pathingAccess = new PathingAccess(this);
        tileOffset = planetConfig.getTerrainTileDimension().getStart();
        tileXCount = planetConfig.getTerrainTileDimension().width();
        tileYCount = planetConfig.getTerrainTileDimension().height();
        terrainShapeTiles = new TerrainShapeTile[tileXCount][tileYCount];
        TerrainShapeSetup terrainShapeSetup = new TerrainShapeSetup(this, terrainTypeService);
        terrainShapeSetup.processSlopes(terrainSlopePositions);
        terrainShapeSetup.processTerrainObject(terrainObjectPositions);
        logger.severe("Setup TerrainShape: " + (System.currentTimeMillis() - time));
    }

    public Index getTileOffset() {
        return tileOffset;
    }

    public int getTileXCount() {
        return tileXCount;
    }

    public int getTileYCount() {
        return tileYCount;
    }

    public TerrainShapeTile getTerrainShapeTile(Index terrainTileIndex) {
        Index fieldIndex = terrainTileIndex.sub(tileOffset);
        if (fieldIndex.getX() < 0 || fieldIndex.getY() < 0 || fieldIndex.getX() >= tileXCount || fieldIndex.getY() >= tileYCount) {
            return null;
        }
        return terrainShapeTiles[fieldIndex.getX()][fieldIndex.getY()];
    }

    public TerrainShapeNode getTerrainShapeNode(Index terrainNodeIndex) {
        Index terrainTileIndex = TerrainUtil.nodeToTile(terrainNodeIndex);
        TerrainShapeTile terrainShapeTile = getTerrainShapeTile(terrainTileIndex);
        if (terrainShapeTile == null) {
            return null;
        }
        Index nodeRelativeIndex = terrainNodeIndex.sub(TerrainUtil.tileToNode(terrainTileIndex));
        return terrainShapeTile.getTerrainShapeNode(nodeRelativeIndex);
    }

    public TerrainShapeNode getOrCreateTerrainShapeNode(Index terrainNodeIndex) {
        TerrainShapeNode terrainShapeNode = getTerrainShapeNode(terrainNodeIndex);
        if (terrainShapeNode != null) {
            return terrainShapeNode;
        }
        Index terrainTileIndex = TerrainUtil.nodeToTile(terrainNodeIndex);
        TerrainShapeTile terrainShapeTile = getTerrainShapeTile(terrainTileIndex);
        if (terrainShapeTile == null) {
            terrainShapeTile = createTerrainShapeTile(terrainTileIndex);
        }
        Index nodeRelativeIndex = terrainNodeIndex.sub(TerrainUtil.tileToNode(terrainTileIndex));
        return terrainShapeTile.createTerrainShapeNode(nodeRelativeIndex);
    }

    private TerrainShapeTile createTerrainShapeTile(Index terrainTileIndex) {
        Index fieldIndex = terrainTileIndex.sub(tileOffset);
        if (fieldIndex.getX() < 0) {
            throw new IllegalArgumentException("fieldIndex X < 0: " + fieldIndex + " for terrainTileIndex: " + terrainTileIndex);
        }
        if (fieldIndex.getY() < 0) {
            throw new IllegalArgumentException("fieldIndex Y < 0: " + fieldIndex + " for terrainTileIndex: " + terrainTileIndex);
        }
        if (fieldIndex.getX() >= tileXCount) {
            throw new IllegalArgumentException("fieldIndex X >= " + tileXCount + ": " + fieldIndex + " for terrainTileIndex: " + terrainTileIndex);
        }
        if (fieldIndex.getY() >= tileYCount) {
            throw new IllegalArgumentException("fieldIndex Y >= " + tileYCount + ": " + fieldIndex + " for terrainTileIndex: " + terrainTileIndex);
        }
        TerrainShapeTile terrainShapeTile = new TerrainShapeTile();
        terrainShapeTiles[fieldIndex.getX()][fieldIndex.getY()] = terrainShapeTile;
        return terrainShapeTile;
    }

    public <T> T terrainImpactCallback(DecimalPosition absolutePosition, TerrainImpactCallback<T> terrainImpactCallback) {
        Index tileIndex = TerrainUtil.toTile(absolutePosition);
        TerrainShapeTile terrainShapeTile = getTerrainShapeTile(tileIndex);
        if (terrainShapeTile == null) {
            // Land ground zero
            return terrainImpactCallback.landNoTile(tileIndex);
        }
        if (!terrainShapeTile.hasNodes()) {
            return terrainImpactCallback.inTile(terrainShapeTile, tileIndex);
        }
        DecimalPosition tileRelative = absolutePosition.sub(TerrainUtil.toTileAbsolute(tileIndex));
        Index nodeRelativeIndex = TerrainUtil.toNode(tileRelative);
        TerrainShapeNode terrainShapeNode = terrainShapeTile.getTerrainShapeNode(nodeRelativeIndex);
        if (terrainShapeNode == null) {
            return terrainImpactCallback.inTile(terrainShapeTile, tileIndex);
        }

        if (!terrainShapeNode.hasSubNodes()) {
            return terrainImpactCallback.inNode(terrainShapeNode, nodeRelativeIndex, tileRelative, tileIndex);
        }

        DecimalPosition nodeRelative = tileRelative.sub(TerrainUtil.toTileAbsolute(nodeRelativeIndex));
        TerrainShapeSubNode terrainShapeSubNode = terrainShapeNode.getTerrainShapeSubNode(nodeRelative);
        if (terrainShapeSubNode == null) {
            return terrainImpactCallback.inNode(terrainShapeNode, nodeRelativeIndex, tileRelative, tileIndex);
        }

        return terrainImpactCallback.inSubNode(terrainShapeSubNode, nodeRelative, nodeRelativeIndex, tileRelative, tileIndex);
    }

    public void terrainRegionImpactCallback(DecimalPosition absolutePosition, double radius, TerrainRegionImpactCallback terrainRegionImpactCallback) {
        terrainRegionImpactCallback(absolutePosition, radius, new SimpleControl(), terrainRegionImpactCallback);
    }

    public void terrainRegionImpactCallback(DecimalPosition absolutePosition, double radius, TerrainRegionImpactCallback.Control control, TerrainRegionImpactCallback terrainRegionImpactCallback) {
        Circle2D circle2D = new Circle2D(absolutePosition, radius);
        for (Index tileIndex : GeometricUtil.rasterizeCircle(circle2D, (int) TerrainUtil.TERRAIN_TILE_ABSOLUTE_LENGTH)) {
            TerrainShapeTile terrainShapeTile = getTerrainShapeTile(tileIndex);
            if (terrainShapeTile == null) {
                // Land ground zero
                terrainRegionImpactCallback.landNoTile(tileIndex);
                return;
            }
            if (!terrainShapeTile.hasNodes()) {
                terrainRegionImpactCallback.inTile(terrainShapeTile, tileIndex);
                return;
            }

            SingleHolder<Boolean> tileCallbackCalled = new SingleHolder<>(false);
            terrainShapeTile.iterateOverTerrainNodes((nodeRelativeIndex, terrainShapeNode, iterationControl) -> {
                Rectangle2D absoluteNodeRect = TerrainUtil.toAbsoluteNodeRectangle(nodeRelativeIndex);
                if (circle2D.intersects(absoluteNodeRect)) {
                    if (terrainShapeNode != null) {
                        // TODO if (terrainShapeNode.hasSubNodes()) {
                        // TODO    terrainShapeNode.iterateOverTerrainSubNodes();
                        // TODO} else {
                        terrainRegionImpactCallback.inNode(terrainShapeNode, nodeRelativeIndex, tileIndex);
                        if (control.isStop()) {
                            iterationControl.doStop();
                        }
                        // TODO}
                    } else {
                        if (!tileCallbackCalled.getO()) {
                            terrainRegionImpactCallback.inTile(terrainShapeTile, tileIndex);
                            tileCallbackCalled.setO(true);
                            if (control.isStop()) {
                                iterationControl.doStop();
                            }
                        }
                    }
                }
            });
        }
    }

    public TerrainShape(PlanetConfig planetConfig, TerrainTypeService terrainTypeService) {
        groundSkeletonConfig = terrainTypeService.getGroundSkeletonConfig();
    }

    public PathingAccess getPathingAccess() {
        return pathingAccess;
    }

    public SurfaceAccess getSurfaceAccess() {
        return surfaceAccess;
    }

    GroundSkeletonConfig getGroundSkeletonConfig() {
        return groundSkeletonConfig;
    }

    public boolean isSightBlocked(Line line) {
        List<Index> nodeIndices = GeometricUtil.rasterizeLine(line, TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH);
        for (Index nodeIndex : nodeIndices) {
            TerrainShapeNode terrainShapeNode = getTerrainShapeNode(nodeIndex);
            if (terrainShapeNode != null && terrainShapeNode.getObstacles() != null) {
                for (Obstacle obstacle : terrainShapeNode.getObstacles()) {
                    if (obstacle.isPiercing(line)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static class SimpleControl implements TerrainRegionImpactCallback.Control {
        private boolean stop;

        @Override
        public void doStop() {
            stop = true;
        }

        @Override
        public boolean isStop() {
            return stop;
        }
    }

}
