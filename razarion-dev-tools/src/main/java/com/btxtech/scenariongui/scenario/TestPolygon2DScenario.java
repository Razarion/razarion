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
public class TestPolygon2DScenario extends Scenario {
    private Polygon2D polygon1 = new Polygon2D(Arrays.asList(new DecimalPosition(255.0, 173.0), new DecimalPosition(277.0, 173.0), new DecimalPosition(277.0, 192.0), new DecimalPosition(255.0, 192.0)));

    @Override
    public void render(ExtendedGraphicsContext context) {
        context.strokePolygon(polygon1, 1, Color.RED, true);
        context.drawPosition(new DecimalPosition(228.0, 140.0), 2, Color.GREEN);
    }
}
