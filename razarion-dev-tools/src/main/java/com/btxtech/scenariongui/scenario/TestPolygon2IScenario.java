package com.btxtech.scenariongui.scenario;

import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import javafx.scene.paint.Color;

import java.util.Arrays;

/**
 * Created by Beat
 * 19.03.2016.
 */
public class TestPolygon2IScenario extends Scenario {
    private Polygon2D polygon1 = new Polygon2D(Arrays.asList(new DecimalPosition(-107, 239), new DecimalPosition(-207, 203), new DecimalPosition(-124, 120), new DecimalPosition(-262, 10), new DecimalPosition(-252, -110), new DecimalPosition(-65, -128), new DecimalPosition(-39, -238), new DecimalPosition(187, -244), new DecimalPosition(275, -10), new DecimalPosition(261, 136), new DecimalPosition(86, -17), new DecimalPosition(90, 199), new DecimalPosition(61, 315)));
    private Polygon2D polygon2 = new Polygon2D(Arrays.asList(new DecimalPosition(72, 44), new DecimalPosition(42, 88), new DecimalPosition(-10, 96), new DecimalPosition(-42, 69), new DecimalPosition(-71, 40), new DecimalPosition(-73, -9), new DecimalPosition(-58, -61), new DecimalPosition(6, -63), new DecimalPosition(41, -88), new DecimalPosition(43, -36)));
    private Polygon2D movedPolygon2;
    private Polygon2D remaining;


    @Override
    public void render(ExtendedGraphicsContext context) {
        context.strokeCurveDecimalPosition(polygon1.getCorners(), 1.0, Color.GREEN, true);
        if (movedPolygon2 != null) {
            context.strokeCurveDecimalPosition(movedPolygon2.getCorners(), 1.0, Color.BLUE, true);
        }
        if (remaining != null) {
            context.strokeCurveDecimalPosition(remaining.getCorners(), 1.0, Color.RED, true);
        }

    }

    @Override
    public boolean onMouseMove(DecimalPosition position) {
        movedPolygon2 = polygon2.translate(position);
        return true;
    }

    @Override
    public boolean onMouseDown(DecimalPosition position) {
        if (movedPolygon2 != null) {
            remaining = polygon1.remove(movedPolygon2);
        }
        return true;
    }
}
