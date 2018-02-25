package com.btxtech.gameengine;

import com.btxtech.Abstract2dRenderer;
import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.projectile.Projectile;
import com.btxtech.shared.gameengine.planet.projectile.ProjectileService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeNode;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeSubNode;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeTile;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import javax.inject.Inject;
import java.lang.reflect.Field;

/**
 * Created by Beat
 * 17.05.2016.
 */
public class JavaFxGameEngineRenderer extends Abstract2dRenderer {
    private static final DecimalPosition FROM = new DecimalPosition(-100, -100);
    private static final double LENGTH = 200;
    @Inject
    private SyncItemContainerService syncItemContainerService;
    @Inject
    private TerrainService terrainService;
    @Inject
    private ProjectileService projectileService;

    public void init(Canvas canvas, double scale) {
        super.init(canvas, scale);
    }

    public void render() {
        preRender();

        ExtendedGraphicsContext extendedGraphicsContext = createExtendedGraphicsContext();
        renderFree(extendedGraphicsContext);
        // Obstacles
        TerrainShape terrainShape;
        try {
            Field field = TerrainService.class.getDeclaredField("terrainShape");
            field.setAccessible(true);
            terrainShape = (TerrainShape) field.get(terrainService);
            field.setAccessible(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        TerrainShapeTile[][] terrainShapeTiles;
        try {
            Field field = TerrainShape.class.getDeclaredField("terrainShapeTiles");
            field.setAccessible(true);
            terrainShapeTiles = (TerrainShapeTile[][]) field.get(terrainShape);
            field.setAccessible(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        doRenderShape(extendedGraphicsContext, terrainShape, terrainShapeTiles);
        for (int x = terrainShape.getTileOffset().getX(); x < terrainShape.getTileOffset().getX() + terrainShape.getTileYCount(); x++) {
            for (int y = terrainShape.getTileOffset().getY(); y < terrainShape.getTileOffset().getY() + terrainShape.getTileYCount(); y++) {
                Index index = new Index(x, y);
                TerrainShapeTile terrainShapeTile = terrainShape.getTerrainShapeTile(index);
                if (terrainShapeTile != null) {
                    terrainShapeTile.iterateOverTerrainNodes((nodeRelativeIndex, terrainShapeNode, iterationControl) -> {
                        if (terrainShapeNode != null && terrainShapeNode.getObstacles() != null) {
                            terrainShapeNode.getObstacles().forEach(obstacle -> extendedGraphicsContext.drawObstacle(obstacle, Color.BLACK, Color.BLACK));
                        }
                    });
                }
            }
        }
        // Items
        syncItemContainerService.iterateOverItems(true, true, null, syncItem -> {
            try {
                extendedGraphicsContext.drawUnit(syncItem);
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return null;
        });
        // Projectiles
        for (Projectile projectile : projectileService.getProjectiles()) {
            extendedGraphicsContext.strokeVertex(projectile.getPosition(), Color.RED, 0.3);
        }

        postRender();
    }

    private void renderFree(ExtendedGraphicsContext egc) {
        for (double x = FROM.getX(); x < FROM.getX() + LENGTH; x++) {
            for (double y = FROM.getY(); y < FROM.getY() + LENGTH; y++) {
                DecimalPosition samplePosition = new DecimalPosition(x + 0.5, y + 0.5);
                // double z = terrainService.getSurfaceAccess().getInterpolatedZ(samplePosition);
                // boolean free = terrainService.getPathingAccess().isTerrainFree(samplePosition);
                // double v = InterpolationUtils.interpolate(0.0, 1.0, min, max, z);
                // egc.getGc().setFill(new Color(v, v, v, 1));
                // egc.getGc().fillRect(x, y, 1, 1);
//                if (free) {
//                    egc.getGc().setFill(Color.GREEN);
//                } else {
//                    egc.getGc().setFill(Color.RED);
//                }
//                egc.getGc().fillRect(x, y, 0.6, 0.6);
            }
        }
    }

    private void doRenderShape(ExtendedGraphicsContext extendedGraphicsContext, TerrainShape terrainShape, TerrainShapeTile[][] terrainShapeTiles) {
        for (int x = 0; x < terrainShape.getTileXCount(); x++) {
            for (int y = 0; y < terrainShape.getTileYCount(); y++) {
                TerrainShapeTile terrainShapeTile = terrainShapeTiles[x][y];
                if (terrainShapeTile != null) {
                    displayTerrainShapeTile(extendedGraphicsContext, new Index(x, y).add(terrainShape.getTileOffset()), terrainShapeTile);
                }
            }
        }
    }

    private void displayTerrainShapeTile(ExtendedGraphicsContext extendedGraphicsContext, Index tileIndex, TerrainShapeTile terrainShapeTile) {
        extendedGraphicsContext.getGc().setLineWidth(0.1);
        extendedGraphicsContext.getGc().setStroke(Color.DARKGREEN);
        DecimalPosition absolute = TerrainUtil.toTileAbsolute(tileIndex);
        extendedGraphicsContext.getGc().strokeRect(absolute.getX(), absolute.getY(), TerrainUtil.TERRAIN_TILE_ABSOLUTE_LENGTH, TerrainUtil.TERRAIN_TILE_ABSOLUTE_LENGTH);
        displayNodes(extendedGraphicsContext, absolute, terrainShapeTile);

    }

    private void displayNodes(ExtendedGraphicsContext egc, DecimalPosition absoluteTile, TerrainShapeTile terrainShapeTile) {
        if (!terrainShapeTile.hasNodes()) {
            return;
        }
        terrainShapeTile.iterateOverTerrainNodes((nodeRelativeIndex, terrainShapeNode, iterationControl) -> {
            if (terrainShapeNode == null) {
                return;
            }
            displayNode(egc, absoluteTile, nodeRelativeIndex, terrainShapeNode);
        });

    }

    private void displayNode(ExtendedGraphicsContext egc, DecimalPosition absoluteTile, Index nodeRelativeIndex, TerrainShapeNode terrainShapeNode) {
        DecimalPosition absolute = TerrainUtil.toNodeAbsolute(nodeRelativeIndex).add(absoluteTile);
        egc.getGc().setLineWidth(0.1);
        egc.getGc().setStroke(Color.BLACK);
        egc.getGc().strokeRect(absolute.getX(), absolute.getY(), TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH, TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH);
        displaySubNodes(egc, 0, absolute, terrainShapeNode.getTerrainShapeSubNodes());
    }

    private void displaySubNodes(ExtendedGraphicsContext egc, int depth, DecimalPosition absolute, TerrainShapeSubNode[] terrainShapeSubNodes) {
        if (terrainShapeSubNodes == null) {
            return;
        }
        double subLength = TerrainUtil.calculateSubNodeLength(depth);
        TerrainShapeSubNode bottomLeft = terrainShapeSubNodes[0];
        if (bottomLeft != null) {
            displaySubNode(egc, depth, absolute, bottomLeft);
        }
        TerrainShapeSubNode bottomRight = terrainShapeSubNodes[1];
        if (bottomRight != null) {
            displaySubNode(egc, depth, absolute.add(subLength, 0), bottomRight);
        }
        TerrainShapeSubNode topRight = terrainShapeSubNodes[2];
        if (topRight != null) {
            displaySubNode(egc, depth, absolute.add(subLength, subLength), topRight);
        }
        TerrainShapeSubNode topLeft = terrainShapeSubNodes[3];
        if (topLeft != null) {
            displaySubNode(egc, depth, absolute.add(0, subLength), topLeft);
        }
    }

    private void displaySubNode(ExtendedGraphicsContext egc, int depth, DecimalPosition absolute, TerrainShapeSubNode terrainShapeSubNode) {
        double subLength = TerrainUtil.calculateSubNodeLength(depth);
        if (terrainShapeSubNode.getTerrainShapeSubNodes() == null) {
            TerrainType terrainType = terrainShapeSubNode.getTerrainType();
            if (terrainType == null) {
                terrainType = TerrainType.getNullTerrainType();
            }
            switch (terrainType) {
                case LAND:
                    egc.getGc().setFill(Color.GREEN);
                    break;
                case WATER:
                    egc.getGc().setFill(Color.BLUE);
                    break;
                case LAND_COAST:
                    egc.getGc().setFill(Color.LIGHTGREEN);
                    break;
                case WATER_COAST:
                    egc.getGc().setFill(Color.SANDYBROWN);
                    break;
                case BLOCKED:
                    egc.getGc().setFill(Color.RED);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown terrainType: " + terrainShapeSubNode.getTerrainType());
            }
            egc.getGc().fillRect(absolute.getX(), absolute.getY(), subLength, subLength);
        }
        egc.getGc().setStroke(Color.BLUEVIOLET);
        egc.getGc().setLineWidth(0.1);
        egc.getGc().strokeRect(absolute.getX(), absolute.getY(), subLength, subLength);
        displaySubNodes(egc, depth + 1, absolute, terrainShapeSubNode.getTerrainShapeSubNodes());
    }

}
