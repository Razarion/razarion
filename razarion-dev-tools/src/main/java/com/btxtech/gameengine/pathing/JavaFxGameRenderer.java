package com.btxtech.gameengine.pathing;

import com.btxtech.Abstract2dRenderer;
import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.shared.gameengine.pathing.Obstacle;
import com.btxtech.shared.gameengine.pathing.Pathing;
import com.btxtech.shared.gameengine.pathing.Unit;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

/**
 * Created by Beat
 * 17.05.2016.
 */
public class JavaFxGameRenderer extends Abstract2dRenderer {
    private static final Color UNIT_DIRECTION_COLOR = new Color(1.0, 0, 1.0, 1.0);
    private static final Color UNIT_COLOR_MOVING = new Color(0, 0, 0, 0.5);
    private static final Color UNIT_COLOR_STANDING = new Color(0, 0, 0, 0.25);
    private static final Color OBSTACLE_COLOR = new Color(0, 0, 0, 0.75);

    public JavaFxGameRenderer(Canvas canvas, double scale) {
        super(canvas, scale);
    }

    public void render(Pathing pathing) {
        preRender();

        ExtendedGraphicsContext extendedGraphicsContext = createExtendedGraphicsContext();
        for (Unit unit : pathing.getUnits()) {
            extendedGraphicsContext.drawUnit(unit, UNIT_COLOR_MOVING, UNIT_COLOR_STANDING, UNIT_DIRECTION_COLOR);
        }
        for (Obstacle obstacle : pathing.getObstacles()) {
            extendedGraphicsContext.drawObstacle(obstacle, OBSTACLE_COLOR);
        }

        postRender();
    }
}
