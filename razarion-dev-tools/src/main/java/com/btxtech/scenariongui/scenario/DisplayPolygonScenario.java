package com.btxtech.scenariongui.scenario;

import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.shared.datatypes.DecimalPosition;
import javafx.scene.paint.Color;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * 19.03.2016.
 */
public class DisplayPolygonScenario extends Scenario {
    List<DecimalPosition> polygon = Arrays.asList(new DecimalPosition(547.6775828528592, 976.0), new DecimalPosition(544.0, 976.0), new DecimalPosition(544.0, 968.030815751593), new DecimalPosition(544.0644624272879, 968.1705034162113), new DecimalPosition(545.2447081060316, 970.7280515799412), new DecimalPosition(546.4249537847752, 973.285599743671), new DecimalPosition(547.6051994635188, 975.8431479074009));
    // List<DecimalPosition> polygonInner = Arrays.asList(new DecimalPosition(544.0644624272879, 968.1705034162113), new DecimalPosition(545.2447081060316, 970.7280515799412), new DecimalPosition(546.4249537847752, 973.285599743671), new DecimalPosition(547.6051994635188, 975.8431479074009));
    List<DecimalPosition> triangle1 = Arrays.asList(new DecimalPosition(547.6051994635188, 975.8431479074009), new DecimalPosition(547.6775828528592, 976.0), new DecimalPosition(544.0, 976.0));
    List<DecimalPosition> triangle2 = Arrays.asList(new DecimalPosition(547.6051994635188, 975.8431479074009), new DecimalPosition(544.0, 976.0), new DecimalPosition(544.0, 968.030815751593));


    @Override
    public void render(ExtendedGraphicsContext context) {
        context.strokeCurveDecimalPosition(polygon, 0.2, Color.RED, true);
        // context.strokeCurveDecimalPosition(polygonInner, 0.2, Color.GREEN, true);
        context.fillPolygon(triangle1, Color.BLACK);
        context.fillPolygon(triangle2, Color.LIGHTBLUE);

    }
}
