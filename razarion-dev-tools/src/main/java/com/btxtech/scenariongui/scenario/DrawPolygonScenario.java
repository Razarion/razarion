package com.btxtech.scenariongui.scenario;

import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.scenariongui.InstanceStringGenerator;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 19.03.2016.
 */
public class DrawPolygonScenario extends Scenario {
    List<List<Index>> polygons = new ArrayList<>();

    @Override
    public void render(ExtendedGraphicsContext context) {
        for (List<Index> polygon : polygons) {
            context.strokeCurveIndex(polygon, 1.0, Color.RED, true);
        }

    }

    @Override
    public boolean onMouseDown(Index position) {
        List<Index> corners;
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
        for (List<Index> polygon : polygons) {
            System.out.println(InstanceStringGenerator.generateIndexList(polygon));
        }
    }

    @Override
    public void onCmd1() {
        System.out.println("Start new Polygon");
        polygons.add(new ArrayList<Index>());
    }
}
