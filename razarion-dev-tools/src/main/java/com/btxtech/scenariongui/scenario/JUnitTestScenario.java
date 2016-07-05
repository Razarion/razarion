package com.btxtech.scenariongui.scenario;

import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.scenariongui.InstanceStringGenerator;
import com.btxtech.shared.datatypes.Polygon2I;
import javafx.scene.paint.Color;

import java.util.Arrays;

/**
 * Created by Beat
 * 19.03.2016.
 */
public class JUnitTestScenario extends Scenario {
    @Override
    public void render(ExtendedGraphicsContext context) {

        Polygon2I polygon1 = new Polygon2I(Arrays.asList(new Index(-79, 7), new Index(-25, -74), new Index(50, -72), new Index(82, -42), new Index(46, 83), new Index(-73, 83)));
        context.strokeCurveIndex(polygon1.getCorners(), 1.0, Color.BLUE, true);
        Polygon2I polygon2 = new Polygon2I(Arrays.asList(new Index(116, 127), new Index(31, 40), new Index(103, -11), new Index(202, -14), new Index(231, 53), new Index(200, 129)));
        context.strokeCurveIndex(polygon2.getCorners(), 1.0, Color.GREEN, true);

        context.strokeCurveIndex(polygon2.remove(polygon1).getCorners(), 1.0, Color.RED, true);

        System.out.println(InstanceStringGenerator.generateIndexList(polygon2.remove(polygon1).getCorners()));

    }
}
