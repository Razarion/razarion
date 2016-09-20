package com.btxtech.gameengine;

import com.btxtech.Abstract2dRenderer;
import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.pathing.Obstacle;
import com.btxtech.shared.gameengine.planet.pathing.PathingService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * 17.05.2016.
 */
@Singleton
public class JavaFxGameEngineRenderer extends Abstract2dRenderer {
    private static final Color UNIT_DIRECTION_COLOR = new Color(1.0, 0, 1.0, 1.0);
    private static final Color UNIT_COLOR_MOVING = new Color(0, 0, 0, 0.5);
    private static final Color UNIT_COLOR_STANDING = new Color(0, 0, 0, 0.25);
    private static final Color OBSTACLE_COLOR = new Color(0, 0, 0, 0.75);
    @Inject
    private SyncItemContainerService syncItemContainerService;
    @Inject
    private TerrainService terrainService;

    public void init(Canvas canvas, double scale) {
        super.init(canvas, scale);
    }

    public void render() {
        preRender();

        ExtendedGraphicsContext extendedGraphicsContext = createExtendedGraphicsContext();

        syncItemContainerService.iterateOverItems(true, true, null, syncItem -> {
            extendedGraphicsContext.drawUnit(syncItem, UNIT_COLOR_MOVING);
            return null;
        });

        for (Obstacle obstacle : terrainService.getObstacles()) {
            extendedGraphicsContext.drawObstacle(obstacle, OBSTACLE_COLOR);
        }

        postRender();
    }
}
