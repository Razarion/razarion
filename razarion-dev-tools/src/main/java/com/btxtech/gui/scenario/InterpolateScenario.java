package com.btxtech.gui.scenario;

import com.btxtech.game.jsre.client.common.Index;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * 23.12.2015.
 */
public class InterpolateScenario extends Scenario {
    private static final double BOTTOM = -8.0;
    private final List<Double> SLOP_INDEX = Arrays.asList(-80.0, -70.0, -60.0, -50.0, -40.0, -30.0, -20.0, -10.0, -0.0);

    @Override
    public void setup() {
        displayGrid();

        List<Index> form = new ArrayList<>();
        for (int i = 0; i < SLOP_INDEX.size(); i++) {
            System.out.println(i + ": " + SLOP_INDEX.get(i));
            form.add(new Index(i * 10, SLOP_INDEX.get(i).intValue()));
        }

        addCurve(form, Color.BLUE);

//        final List<Double> slopeForm = new ArrayList<>();
//        slopeForm.add(BOTTOM);
//        slopeForm.addAll(SLOP_INDEX);
//        slopeForm.add(0.0);
//
//        MathHelper2.interpolate(distance, slopeForm);


    }
}
