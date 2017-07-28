package com.btxtech.scenariongui.scenario;

import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.utils.GeometricUtil;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * on 28.07.2017.
 */
public class FreeRandomPositionScenario extends Scenario {
    // private Polygon2D polygon2D = new Polygon2D(Arrays.asList(new DecimalPosition(81.125, 78.750), new DecimalPosition(104.625, 87.250), new DecimalPosition(119.875, 87.000), new DecimalPosition(126.625, 74.000), new DecimalPosition(127.875, 62.250), new DecimalPosition(128.375, 46.750), new DecimalPosition(125.375, 29.250), new DecimalPosition(117.125, 17.250), new DecimalPosition(78.625, 8.000), new DecimalPosition(62.375, 9.500), new DecimalPosition(48.375, 21.500), new DecimalPosition(34.625, 36.500), new DecimalPosition(28.625, 54.000), new DecimalPosition(21.125, 71.750), new DecimalPosition(21.625, 88.750), new DecimalPosition(29.875, 97.750), new DecimalPosition(41.625, 99.000), new DecimalPosition(58.375, 96.000), new DecimalPosition(67.625, 90.000)));
    private Polygon2D polygon2D = new Polygon2D(Arrays.asList(new DecimalPosition(41.500, 55.667), new DecimalPosition(51.500, 61.500), new DecimalPosition(47.333, 77.333), new DecimalPosition(45.000, 88.667), new DecimalPosition(72.833, 88.833), new DecimalPosition(90.667, 82.500), new DecimalPosition(76.500, 66.667), new DecimalPosition(66.167, 50.000), new DecimalPosition(74.000, 34.333), new DecimalPosition(92.833, 48.667), new DecimalPosition(99.500, 66.167), new DecimalPosition(111.500, 84.333), new DecimalPosition(131.500, 92.167), new DecimalPosition(149.667, 79.833), new DecimalPosition(154.167, 50.167), new DecimalPosition(142.333, 37.667), new DecimalPosition(126.833, 49.667), new DecimalPosition(120.333, 62.333), new DecimalPosition(111.833, 58.167), new DecimalPosition(112.000, 45.667), new DecimalPosition(115.833, 24.333), new DecimalPosition(126.167, 15.000), new DecimalPosition(154.667, -1.333), new DecimalPosition(147.167, -16.333), new DecimalPosition(113.167, -17.000), new DecimalPosition(97.333, -11.500), new DecimalPosition(93.333, 9.667), new DecimalPosition(92.167, 23.333), new DecimalPosition(84.833, 27.167), new DecimalPosition(79.000, 25.167), new DecimalPosition(74.833, 7.167), new DecimalPosition(72.833, -9.167), new DecimalPosition(64.833, -23.000), new DecimalPosition(42.833, -0.333), new DecimalPosition(52.333, 17.667), new DecimalPosition(60.167, 32.833), new DecimalPosition(61.833, 41.167), new DecimalPosition(39.667, 42.833), new DecimalPosition(13.000, 32.667), new DecimalPosition(5.500, 53.333), new DecimalPosition(14.833, 74.667), new DecimalPosition(36.000, 78.833), new DecimalPosition(42.333, 74.500), new DecimalPosition(20.833, 66.500), new DecimalPosition(18.500, 50.167), new DecimalPosition(25.000, 64.333)));
    private List<DecimalPosition> positions = new ArrayList<>();
    private Circle2D circle1 = new Circle2D(new DecimalPosition(73, 79), 8);

    @Override
    public void init() {
        for (int i = 0; i < 100000; i++) {
            // positions.add(GeometricUtil.findFreeRandomPosition(polygon2D, null));
            positions.add(GeometricUtil.findFreeRandomPosition(polygon2D, decimalPosition -> !circle1.inside(decimalPosition)));
        }

    }

    @Override
    public void render(ExtendedGraphicsContext extendedGraphicsContext) {
        extendedGraphicsContext.drawPositions(positions, 0.1, Color.GREEN);
        extendedGraphicsContext.strokePolygon(polygon2D, 0.3, Color.RED, false);
        extendedGraphicsContext.strokeCircle(circle1, 0.1, Color.BLUE);
    }
}
