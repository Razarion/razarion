package com.btxtech.gameengine;

import com.btxtech.Abstract2dRenderer;
import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.shared.gameengine.planet.SyncItemContainerService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import javafx.scene.canvas.Canvas;

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

        terrainService.getObstacles().forEach(extendedGraphicsContext::drawObstacle);

        postRender();
    }
}
