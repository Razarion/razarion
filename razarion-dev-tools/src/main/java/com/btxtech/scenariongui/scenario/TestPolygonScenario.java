package com.btxtech.scenariongui.scenario;

import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.shared.primitives.Polygon2I;
import javafx.scene.paint.Color;

import java.util.Arrays;

/**
 * Created by Beat
 * 19.03.2016.
 */
public class TestPolygonScenario extends Scenario {
    private Polygon2I polygon1 = new Polygon2I(Arrays.asList(new Index(-107, 239), new Index(-207, 203), new Index(-124, 120), new Index(-262, 10), new Index(-252, -110), new Index(-65, -128), new Index(-39, -238), new Index(187, -244), new Index(275, -10), new Index(261, 136), new Index(86, -17), new Index(90, 199), new Index(61, 315)));
    private Polygon2I polygon2 = new Polygon2I(Arrays.asList(new Index(72, 44), new Index(42, 88), new Index(-10, 96), new Index(-42, 69), new Index(-71, 40), new Index(-73, -9), new Index(-58, -61), new Index(6, -63), new Index(41, -88), new Index(43, -36)));
    private Polygon2I movedPolygon2;
    private Polygon2I remaining;


    @Override
    public void render(ExtendedGraphicsContext context) {
        context.strokeCurveIndex(polygon1.getCorners(), 1.0, Color.GREEN, true);
        if (movedPolygon2 != null) {
            context.strokeCurveIndex(movedPolygon2.getCorners(), 1.0, Color.BLUE, true);
        }
        if (remaining != null) {
            context.strokeCurveIndex(remaining.getCorners(), 1.0, Color.RED, true);
        }

    }

    @Override
    public boolean onMouseMove(Index position) {
        movedPolygon2 = polygon2.translate(position);
        return true;
    }

    @Override
    public boolean onMouseDown(Index position) {
        if (movedPolygon2 != null) {
            remaining = polygon1.remove(movedPolygon2);
        }
        return true;
    }
}
