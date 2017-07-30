package com.btxtech.scenariongui.scenario;

import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.scenariongui.InstanceStringGenerator;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.utils.PolygonUtil;
import javafx.scene.paint.Color;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * 19.03.2016.
 */
public class TestPolygon2DScenario extends Scenario {
    List<DecimalPosition> tooSmall = Arrays.asList(new DecimalPosition(38.375, 12.750), new DecimalPosition(62.875, 14.000), new DecimalPosition(78.625, 32.000));
    List<DecimalPosition> nonIntersectingPolygon = Arrays.asList(new DecimalPosition(38.375, 12.750), new DecimalPosition(62.875, 14.000), new DecimalPosition(78.625, 32.000), new DecimalPosition(76.875, 59.250), new DecimalPosition(56.375, 72.500), new DecimalPosition(31.875, 75.750), new DecimalPosition(13.875, 64.250), new DecimalPosition(6.875, 47.250), new DecimalPosition(12.125, 27.750), new DecimalPosition(21.125, 16.500));
    List<DecimalPosition> selfIntersecting1 = Arrays.asList(new DecimalPosition(27.125, 54.750), new DecimalPosition(15.125, 30.250), new DecimalPosition(23.875, 18.250), new DecimalPosition(46.875, 15.750), new DecimalPosition(52.875, 26.250), new DecimalPosition(48.875, 52.500), new DecimalPosition(78.625, 52.250), new DecimalPosition(33.375, 36.750), new DecimalPosition(68.625, 77.250), new DecimalPosition(34.375, 75.250));
    List<DecimalPosition> selfIntersecting2 = Arrays.asList(new DecimalPosition(34.875, 11.250), new DecimalPosition(71.875, 19.750), new DecimalPosition(85.125, 13.750), new DecimalPosition(69.125, 12.750), new DecimalPosition(64.875, 30.250), new DecimalPosition(74.625, 34.500), new DecimalPosition(90.875, 44.250), new DecimalPosition(93.625, 34.500), new DecimalPosition(77.375, 43.750), new DecimalPosition(64.875, 64.250), new DecimalPosition(51.875, 79.250), new DecimalPosition(70.625, 80.750), new DecimalPosition(81.375, 67.750), new DecimalPosition(85.625, 59.000), new DecimalPosition(72.875, 53.250), new DecimalPosition(54.125, 52.000), new DecimalPosition(37.375, 51.250), new DecimalPosition(21.875, 56.000), new DecimalPosition(14.375, 71.000), new DecimalPosition(28.875, 77.750), new DecimalPosition(38.625, 70.000), new DecimalPosition(44.125, 57.500), new DecimalPosition(45.625, 42.250), new DecimalPosition(29.375, 36.500), new DecimalPosition(15.875, 33.000), new DecimalPosition(8.625, 25.750), new DecimalPosition(10.375, 20.500), new DecimalPosition(13.625, 13.000), new DecimalPosition(17.125, 9.000));

    @Override
    public void render(ExtendedGraphicsContext context) {
        context.strokePolygon(selfIntersecting2, 0.3, Color.RED, true);
        List<DecimalPosition> polygon = PolygonUtil.removeSelfIntersectingCorners(selfIntersecting2);
        context.strokePolygon(polygon, 0.1, Color.GREEN, true);
        System.out.println(InstanceStringGenerator.generateDecimalPositionList(polygon));
    }
}
