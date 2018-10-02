package com.btxtech.scenariongui.scenario;

import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * 19.03.2016.
 */
public class Geometry2DScenario extends Scenario {
    private List<DecimalPosition> corners = new ArrayList<>(Arrays.asList(new DecimalPosition(2820.0, 1412.0), new DecimalPosition(2820.0, 1404.0), new DecimalPosition(2812.0, 1404.0), new DecimalPosition(2812.0, 1396.0), new DecimalPosition(2806.5373645885584, 1394.1546571785616)));
    private DecimalPosition unitPosition = new DecimalPosition(2828.085260480847, 1409.0394034705078);

    @Override
    public void render(ExtendedGraphicsContext context) {
        context.strokeCurveDecimalPosition(corners, 0.1, Color.GREEN, true);
        context.strokeCircle(new Circle2D(unitPosition, 2.0), 0.1, Color.RED);


        DecimalPosition v = new DecimalPosition(-2.0569548945915104 , -1.4208928747854386);
        DecimalPosition pv1 = new DecimalPosition(-2.468345873509807, -1.705071449742535);
        DecimalPosition pv2 = new DecimalPosition(-2.8797368524281115, -1.9892500246996192);
        context.strokeRay(unitPosition, v, 0.1, Color.RED);
        context.strokeRay(unitPosition, pv1, 0.1, Color.BLUE);
        context.strokeRay(unitPosition, pv2, 0.1, Color.VIOLET);
        System.out.println("v: " + v.magnitude());
        System.out.println("pv1: " + pv1.magnitude());
        System.out.println("pv2: " + pv2.magnitude());
    }

    @Override
    public boolean onMouseMove(DecimalPosition position) {
//        if (triangle2d != null) {
//            Vertex interpolation = triangle2d.interpolate(new DecimalPosition(position));
//            System.out.println(interpolation);
//            System.out.println((interpolation.getX() + interpolation.getY() + interpolation.getZ()));
//        }
        return true;
    }

    @Override
    public boolean onMouseDown(DecimalPosition position) {
//        if (corners.size() < 3) {
//            corners.add(new DecimalPosition(position));
//        }
//        if (corners.size() == 3) {
//            triangle2d = new Triangle2d(corners.get(0), corners.get(1), corners.get(2));
//        }
        return true;
    }
}
