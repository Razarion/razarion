package com.btxtech.gameengine;

import com.btxtech.Abstract2dRenderer;
import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.pathing.Obstacle;
import com.btxtech.shared.gameengine.planet.projectile.Projectile;
import com.btxtech.shared.gameengine.planet.projectile.ProjectileService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeNode;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeTile;
import com.btxtech.shared.utils.InterpolationUtils;
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
}
