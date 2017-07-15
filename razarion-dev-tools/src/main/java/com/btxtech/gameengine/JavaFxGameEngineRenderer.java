package com.btxtech.gameengine;

import com.btxtech.Abstract2dRenderer;
import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.pathing.Obstacle;
import com.btxtech.shared.gameengine.planet.projectile.Projectile;
import com.btxtech.shared.gameengine.planet.projectile.ProjectileService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeNode;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import javax.inject.Inject;
import java.lang.reflect.Field;

/**
 * Created by Beat
 * 17.05.2016.
 */
public class JavaFxGameEngineRenderer extends Abstract2dRenderer {
    @Inject
    private SyncItemContainerService syncItemContainerService;
    @Inject
    private TerrainService terrainService;
    @Inject
    private ProjectileService projectileService;
    private TerrainShape terrainShape;

    public void init(Canvas canvas, double scale) {
        try {
            Field field = TerrainService.class.getDeclaredField("terrainShape");
            field.setAccessible(true);
            terrainShape = (TerrainShape) field.get(terrainService);
            field.setAccessible(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        super.init(canvas, scale);

    }

    public void render() {
        preRender();

        ExtendedGraphicsContext extendedGraphicsContext = createExtendedGraphicsContext();
        // Obstacles
        for (int x = terrainShape.getTileOffset().getX(); x < terrainShape.getTileOffset().getX() + terrainShape.getTileYCount(); x++) {
            for (int y = terrainShape.getTileOffset().getY(); y < terrainShape.getTileOffset().getY() + terrainShape.getTileYCount(); y++) {
                Index index = new Index(x, y);
                TerrainShapeNode terrainShapeNode = terrainShape.getTerrainShapeNode(index);
                if (terrainShapeNode != null && terrainShapeNode.getObstacles() != null) {
                    for (Obstacle obstacle : terrainShapeNode.getObstacles()) {
                        extendedGraphicsContext.drawObstacle(obstacle, Color.BLACK, Color.BLACK);
                    }
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
}
