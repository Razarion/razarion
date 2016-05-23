package com.btxtech.gui.scenario;

import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.client.common.Index;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * 17.07.2015.
 */
public class TriangulationManualScenario extends Scenario {
    private List<DecimalPosition> positions = new ArrayList<>(Arrays.asList(new DecimalPosition(113, 280), new DecimalPosition(279, 146), new DecimalPosition(440, 286)));

    @Override
    public void setup() {
        triangulation();
    }

    private void triangulation() {
//        getCanvas().getChildren().clear();
//        addPolygon(positions);
//        Triangulator triangulator = new Triangulator();
//        List<Triangle2d> triangle2ds = triangulator.calculate(new Polygon2d(positions));
//        for (Triangle2d triangle2d : triangle2ds) {
//            addTriangle(triangle2d, Color.TRANSPARENT, createRandomColor(0.3));
//        }
    }

    @Override
    public void onMouseDown(Index position) {
        positions.add(new DecimalPosition(position));
        triangulation();
    }
}
