package com.btxtech.scenariongui.scenario;

import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.scenariongui.InstanceStringGenerator;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.utils.CollectionUtils;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 19.03.2016.
 */
public class DrawPolygonScenario extends Scenario {
    private List<List<DecimalPosition>> polygons = new ArrayList<>();

    @Override
    public void render(ExtendedGraphicsContext context) {
        for (List<DecimalPosition> polygon : polygons) {
            context.strokeCurveDecimalPosition(polygon, 0.2, Color.RED, true);
        }

    }

    @Override
    public boolean onMouseDown(DecimalPosition position) {
        List<DecimalPosition> corners;
        if (polygons.isEmpty()) {
            corners = new ArrayList<>();
            polygons.add(corners);
        } else {
            corners = CollectionUtils.getLast(polygons);
        }
        corners.add(position);
        return true;
    }

    @Override
    public void onGenerate() {
        System.out.println("------------------------------------------------------------");
        for (List<DecimalPosition> polygon : polygons) {
            System.out.println(InstanceStringGenerator.generateDecimalPositionList(polygon));
        }
    }

    @Override
    public void onCmd1() {
        System.out.println("Start new Polygon");
        polygons.add(new ArrayList<>());
    }
}
