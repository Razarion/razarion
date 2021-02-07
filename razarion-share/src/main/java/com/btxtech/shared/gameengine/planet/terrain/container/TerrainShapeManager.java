package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.SingleHolder;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.pathing.Obstacle;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeFractionalSlope;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeAccess;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeNode;
import com.btxtech.shared.gameengine.planet.terrain.container.json.NativeTerrainShapeTile;
import com.btxtech.shared.system.alarm.AlarmService;
import com.btxtech.shared.utils.ExceptionUtil;
import com.btxtech.shared.utils.GeometricUtil;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 17.06.2017.
 */
public class TerrainShapeManager {
    private static Logger logger = Logger.getLogger(TerrainShapeManager.class.getName());
    private TerrainShapeTile[][] terrainShapeTiles;
    private SurfaceAccess surfaceAccess;
    private PathingAccess pathingAccess;
    private DecimalPosition planetSize;
    private int tileXCount;
    private int tileYCount;
    public TerrainShapeManager() {
    }

    public TerrainShapeManager(PlanetConfig planetConfig, TerrainTypeService terrainTypeService, AlarmService alarmService, List<TerrainSlopePosition> terrainSlopePositions, List<TerrainObjectPosition> terrainObjectPositions) {
        long time = System.currentTimeMillis();
        surfaceAccess = new SurfaceAccess(this);
        pathingAccess = new PathingAccess(this);
        setupDimension(planetConfig);
        terrainShapeTiles = new TerrainShapeTile[tileXCount][tileYCount];
        TerrainShapeManagerSetup terrainShapeSetup = new TerrainShapeManagerSetup(this, terrainTypeService, alarmService);
        terrainShapeSetup.processSlopes(terrainSlopePositions);
        terrainShapeSetup.processTerrainObject(terrainObjectPositions);
        terrainShapeSetup.finish();
        logger.severe("Setup TerrainShape: " + (System.currentTimeMillis() - time) + " for planet config: " + planetConfig.getId());
    }

    public void lazyInit(PlanetConfig planetConfig, TerrainTypeService terrainTypeService, NativeTerrainShapeAccess nativeTerrainShapeAccess, Runnable finishCallback, Consumer<String> failCallback) {
        surfaceAccess = new SurfaceAccess(this);
        pathingAccess = new PathingAccess(this);
        setupDimension(planetConfig);
        nativeTerrainShapeAccess.load(planetConfig.getId(), nativeTerrainShape -> {
            try {
                // long time = System.currentTimeMillis();
                terrainShapeTiles = new TerrainShapeTile[tileXCount][tileYCount];
                for (int x = 0; x < tileXCount; x++) {
                    for (int y = 0; y < tileYCount; y++) {
                        NativeTerrainShapeTile nativeTerrainShapeTile = nativeTerrainShape.nativeTerrainShapeTiles[x][y];
                        if (nativeTerrainShapeTile != null) {
                            TerrainShapeTile terrainShapeTile = new TerrainShapeTile();
                            terrainShapeTiles[x][y] = terrainShapeTile;
                            terrainShapeTile.setRenderFullWaterLevel(nativeTerrainShapeTile.fullWaterLevel);
                            terrainShapeTile.setUniformGroundHeight(nativeTerrainShapeTile.uniformGroundHeight);
                            if (nativeTerrainShapeTile.fractionalSlopes != null) {
                                for (NativeFractionalSlope nativeFractionalSlope : nativeTerrainShapeTile.fractionalSlopes) {
                                    terrainShapeTile.addFractionalSlope(new FractionalSlope(nativeFractionalSlope));
                                }
                            }
                            if (nativeTerrainShapeTile.nativeTerrainShapeNodes != null) {
                                TerrainShapeNode[][] terrainShapeNodes = new TerrainShapeNode[TerrainUtil.TERRAIN_TILE_NODES_COUNT][TerrainUtil.TERRAIN_TILE_NODES_COUNT];
                                for (int nodeX = 0; nodeX < TerrainUtil.TERRAIN_TILE_NODES_COUNT; nodeX++) {
                                    for (int nodeY = 0; nodeY < TerrainUtil.TERRAIN_TILE_NODES_COUNT; nodeY++) {
                                        NativeTerrainShapeNode nativeTerrainShapeNode = nativeTerrainShapeTile.nativeTerrainShapeNodes[nodeX][nodeY];
                                        if (nativeTerrainShapeNode != null) {
                                            terrainShapeNodes[nodeX][nodeY] = new TerrainShapeNode(nativeTerrainShapeNode);
                                        }
                                    }
                                }
                                terrainShapeTile.setTerrainShapeNodes(terrainShapeNodes);
                            }
                            terrainShapeTile.setNativeTerrainShapeObjectLists(nativeTerrainShapeTile.nativeTerrainShapeObjectLists);
                        }
                    }
                }
                // logger.severe("Setup TerrainShape Net: " + (System.currentTimeMillis() - time));
                finishCallback.run();
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "NativeTerrainShapeAccess load callback failed", t);
                failCallback.accept(ExceptionUtil.setupStackTrace("NativeTerrainShapeAccess load callback failed", t));
            }
        }, failCallback);
    }

    private void setupDimension(PlanetConfig planetConfig) {
        planetSize = planetConfig.getSize();
        tileXCount = TerrainUtil.toTileCeil(planetSize).getX();
        tileYCount = TerrainUtil.toTileCeil(planetSize).getY();
    }

    public NativeTerrainShape toNativeTerrainShape() {
        NativeTerrainShape nativeTerrainShape = new NativeTerrainShape();
        nativeTerrainShape.nativeTerrainShapeTiles = new NativeTerrainShapeTile[tileXCount][tileYCount];
        for (int x = 0; x < tileXCount; x++) {
            for (int y = 0; y < tileYCount; y++) {
                TerrainShapeTile terrainShapeTile = terrainShapeTiles[x][y];
                if (terrainShapeTile != null) {
                    nativeTerrainShape.nativeTerrainShapeTiles[x][y] = terrainShapeTile.toNativeTerrainShapeTile();
                }
            }
        }
        return nativeTerrainShape;
    }

    public int getTileXCount() {
        return tileXCount;
    }

    public int getTileYCount() {
        return tileYCount;
    }

    public TerrainShapeTile getTerrainShapeTile(Index terrainTileIndex) {
        if (terrainTileIndex.getX() < 0 || terrainTileIndex.getY() < 0 || terrainTileIndex.getX() >= tileXCount || terrainTileIndex.getY() >= tileYCount) {
            return null;
        }
        return terrainShapeTiles[terrainTileIndex.getX()][terrainTileIndex.getY()];
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

    public TerrainShapeTile getOrCreateTerrainShapeTile(Index terrainTileIndex) {
        TerrainShapeTile terrainShapeTile = getTerrainShapeTile(terrainTileIndex);
        if (terrainShapeTile == null) {
            terrainShapeTile = createTerrainShapeTile(terrainTileIndex);
        }
        return terrainShapeTile;
    }

    public TerrainShapeNode getOrCreateTerrainShapeNode(Index terrainNodeIndex) {
        TerrainShapeNode terrainShapeNode = getTerrainShapeNode(terrainNodeIndex);
        if (terrainShapeNode != null) {
            return terrainShapeNode;
        }
        Index terrainTileIndex = TerrainUtil.nodeToTile(terrainNodeIndex);
        TerrainShapeTile terrainShapeTile = getOrCreateTerrainShapeTile(terrainTileIndex);
        Index nodeRelativeIndex = terrainNodeIndex.sub(TerrainUtil.tileToNode(terrainTileIndex));
        return terrainShapeTile.createTerrainShapeNode(nodeRelativeIndex);
    }

    private TerrainShapeTile createTerrainShapeTile(Index terrainTileIndex) {
        if (terrainTileIndex.getX() < 0) {
            throw new IllegalArgumentException("terrainTileIndex X < 0: " + terrainTileIndex);
        }
        if (terrainTileIndex.getY() < 0) {
            throw new IllegalArgumentException("terrainTileIndex Y < 0: " + terrainTileIndex);
        }
        if (terrainTileIndex.getX() >= tileXCount) {
            throw new IllegalArgumentException("terrainTileIndex X >= " + tileXCount + ": " + terrainTileIndex);
        }
        if (terrainTileIndex.getY() >= tileYCount) {
            throw new IllegalArgumentException("terrainTileIndex Y >= " + tileYCount + ": " + terrainTileIndex);
        }
        TerrainShapeTile terrainShapeTile = new TerrainShapeTile();
        terrainShapeTiles[terrainTileIndex.getX()][terrainTileIndex.getY()] = terrainShapeTile;
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

        DecimalPosition nodeRelative = tileRelative.sub(TerrainUtil.toNodeAbsolute(nodeRelativeIndex));
        TerrainShapeSubNode terrainShapeSubNode = terrainShapeNode.getTerrainShapeSubNode(nodeRelative);
        if (terrainShapeSubNode != null) {
            return terrainImpactCallback.inSubNode(terrainShapeSubNode, terrainShapeNode, nodeRelative, nodeRelativeIndex, tileRelative, tileIndex);
        } else {
            return terrainImpactCallback.inNode(terrainShapeNode, nodeRelativeIndex, tileRelative, tileIndex);
        }
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

            Index bottomLeftNodeIndex = TerrainUtil.tileToNode(tileIndex);
            terrainShapeTile.iterateOverTerrainNodes((nodeRelativeIndex, terrainShapeNode, iterationControl) -> {
                Index absoluteNodeIndex = nodeRelativeIndex.add(bottomLeftNodeIndex);
                Rectangle2D absoluteNodeRect = TerrainUtil.toAbsoluteNodeRectangle(absoluteNodeIndex);
                if (circle2D.intersects(absoluteNodeRect)) {
                    if (terrainShapeNode != null) {
                        if (terrainShapeNode.hasSubNodes()) {
                            terrainShapeNode.iterateOverTerrainSubNodes(new TerrainShapeNode.TerrainShapeSubNodeConsumer() {
                                @Override
                                public void onTerrainShapeSubNode(TerrainShapeSubNode terrainShapeSubNode, DecimalPosition relativeOffset, int depth) {
                                    DecimalPosition absoluteSubNode = absoluteNodeRect.getStart().add(relativeOffset);
                                    double subNodeLength = TerrainUtil.calculateSubNodeLength(depth);
                                    Rectangle2D absoluteSubNodeRect = new Rectangle2D(absoluteSubNode.getX(), absoluteSubNode.getY(), subNodeLength, subNodeLength);
                                    if (circle2D.intersects(absoluteSubNodeRect)) {
                                        terrainRegionImpactCallback.inSubNode(terrainShapeSubNode);
                                    }
                                }
                            });
                        } else {
                            terrainRegionImpactCallback.inNode(terrainShapeNode, nodeRelativeIndex, tileIndex);
                            if (control.isStop()) {
                                iterationControl.doStop();
                            }
                        }
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

    /**
     * Get all nodes touching the circle
     *
     * @param absolutePosition center
     * @param radius           radius
     * @param nodeCallback     callback. Return false to stop iteration
     */

    public void terrainNodesInCircleCallback(DecimalPosition absolutePosition, double radius, Function<TerrainShapeNode, Boolean> nodeCallback) {
        List<Index> nodeIndices = GeometricUtil.rasterizeCircle(new Circle2D(absolutePosition, radius), TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH);
        for (Index nodeIndex : nodeIndices) {
            TerrainShapeNode terrainShapeNode = getTerrainShapeNode(nodeIndex);
            if (terrainShapeNode != null) {
                if (!nodeCallback.apply(terrainShapeNode)) {
                    return;
                }
            }
        }
    }


    public PathingAccess getPathingAccess() {
        return pathingAccess;
    }

    public SurfaceAccess getSurfaceAccess() {
        return surfaceAccess;
    }

    public boolean isSightBlocked(Line line) {
        List<Index> nodeIndices = GeometricUtil.rasterizeLine(line, TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH);
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

    public Rectangle2D getPlayGround() {
        return new Rectangle2D(DecimalPosition.NULL, planetSize);
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
