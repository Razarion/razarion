package com.btxtech.scenariongui.scenario;

import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.shared.datatypes.DecimalPosition;
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
            isInSight = getTerrainService().getPathingAccess().isInSight(start, RADIUS, position);
        }
        return true;
    }
}
