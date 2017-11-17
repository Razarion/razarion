package com.btxtech.scenariongui.scenario;

import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.model.DevToolHelper;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import javafx.scene.paint.Color;

/**
 * Created by Beat
 * 29.01.2017.
 */
public class PathingAccessInSightScenario extends AbstractTerrainScenario {
    private static final double RADIUS = 2;
    private DecimalPosition start;
    private DecimalPosition destination;
    private boolean isInSight;

    @Override
    public void render(ExtendedGraphicsContext extendedGraphicsContext) {
        renderFree(extendedGraphicsContext);

        if (start != null && destination != null) {
            if (isInSight) {
                extendedGraphicsContext.getGc().setStroke(Color.GREEN);
            } else {
                extendedGraphicsContext.getGc().setStroke(Color.RED);
            }
            extendedGraphicsContext.getGc().setLineWidth(RADIUS * 2.0);
            extendedGraphicsContext.getGc().strokeLine(start.getX(), start.getY(), destination.getX(), destination.getY());
        }

        drawObstacle(extendedGraphicsContext);
    }

    @Override
    public boolean onMouseDown(DecimalPosition position) {
        start = position;
        return true;
    }

    @Override
    public boolean onMouseMove(DecimalPosition position) {
        if (start != null) {
            destination = position;
            SyncPhysicalArea syncPhysicalArea = DevToolHelper.generateSyncPhysicalArea(start, RADIUS);
            isInSight = getTerrainService().getPathingAccess().isInSight(syncPhysicalArea.getPosition2d(), position);
        }
        return true;
    }
}
