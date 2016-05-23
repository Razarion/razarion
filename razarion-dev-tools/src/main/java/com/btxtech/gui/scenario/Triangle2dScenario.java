package com.btxtech.gui.scenario;

import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.shared.primitives.Triangle2d;

/**
 * Created by Beat
 * 16.07.2015.
 */
public class Triangle2dScenario extends Scenario {
    private Triangle2d triangle = new Triangle2d(new DecimalPosition(10, 10), new DecimalPosition(200, 20), new DecimalPosition(160, 150));

    @Override
    public void setup() {
        addTriangle(triangle);
    }

    @Override
    public void onMouseDown(Index position) {
        if (triangle.isInside(new DecimalPosition(position))) {
            getCanvas().setStyle("-fx-background-color: #ee7b7d;");
        } else {
            getCanvas().setStyle("-fx-background-color: lightgreen;");
        }
    }
}
