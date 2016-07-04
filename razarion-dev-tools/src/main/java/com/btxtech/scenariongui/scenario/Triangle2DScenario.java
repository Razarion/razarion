package com.btxtech.scenariongui.scenario;

import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.shared.primitives.Triangle2d;
import com.btxtech.shared.primitives.Vertex;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 19.03.2016.
 */
public class Triangle2DScenario extends Scenario {
    private List<DecimalPosition> corners = new ArrayList<>();
    private Triangle2d triangle2d;

    @Override
    public void render(ExtendedGraphicsContext context) {
        context.strokeCurveDecimalPosition(corners, 1.0, Color.RED, true);
    }

    @Override
    public boolean onMouseMove(Index position) {
        if (triangle2d != null) {
            Vertex interpolation = triangle2d.interpolate(new DecimalPosition(position));
            System.out.println(interpolation);
            System.out.println((interpolation.getX() + interpolation.getY() + interpolation.getZ()));
        }
        return true;
    }

    @Override
    public boolean onMouseDown(Index position) {
        if (corners.size() < 3) {
            corners.add(new DecimalPosition(position));
        }
        if (corners.size() == 3) {
            triangle2d = new Triangle2d(corners.get(0), corners.get(1), corners.get(2));
        }
        return true;
    }
}
