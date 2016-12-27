package com.btxtech.scenariongui.scenario;

import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle2D;
import javafx.scene.paint.Color;

/**
 * Created by Beat
 * 27.12.2016.
 */
public class Rectangle2DScenario extends Scenario {
    private Rectangle2D movingRect = new Rectangle2D(0, 0, 20, 30);
    private Rectangle2D fixRect = new Rectangle2D(0, 0, 50, 50);
    private Rectangle2D cross;


    @Override
    public void render(ExtendedGraphicsContext extendedGraphicsContext) {
        extendedGraphicsContext.fillRectangle(fixRect, 0.1, Color.BLUE);
        extendedGraphicsContext.fillRectangle(movingRect, 0.1, Color.GREEN);
        if (cross != null) {
            extendedGraphicsContext.fillRectangle(cross, 0.1, Color.RED);
        }
    }

    public boolean onMouseMove(DecimalPosition position) {
        movingRect = new Rectangle2D(position.getX(), position.getY(), movingRect.width(), movingRect.height());

        cross = fixRect.calculateCrossSection(movingRect);

        System.out.println("cover ratio: " + fixRect.coverRatio(movingRect));

        return true;
    }

}
