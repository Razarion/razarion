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
            extendedGraphicsContext.drawUnit(syncItem);
            return null;
        });

        for (Obstacle obstacle : terrainService.getObstacles()) {
            extendedGraphicsContext.drawObstacle(obstacle);
        }

        postRender();
    }
}
