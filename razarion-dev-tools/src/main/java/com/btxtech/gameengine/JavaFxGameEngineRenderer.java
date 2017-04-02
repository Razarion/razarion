package com.btxtech.gameengine;

import com.btxtech.Abstract2dRenderer;
import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.pathing.Obstacle;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleContainer;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleContainerNode;
import com.btxtech.shared.gameengine.planet.projectile.Projectile;
import com.btxtech.shared.gameengine.planet.projectile.ProjectileService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import javax.inject.Inject;

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
    private ObstacleContainer obstacleContainer;
    @Inject
    private ProjectileService projectileService;

    public void init(Canvas canvas, double scale) {
        super.init(canvas, scale);
    }

    public void render() {
        preRender();

        ExtendedGraphicsContext extendedGraphicsContext = createExtendedGraphicsContext();
        // Obstacles
        for (int x = 0; x < obstacleContainer.getXCount(); x++) {
            for (int y = 0; y < obstacleContainer.getYCount(); y++) {
                Index index = new Index(x, y);
                ObstacleContainerNode obstacleContainerNode = obstacleContainer.getObstacleContainerNode(index);
                if (obstacleContainerNode != null && obstacleContainerNode.getObstacles() != null) {
                    for (Obstacle obstacle : obstacleContainerNode.getObstacles()) {
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
