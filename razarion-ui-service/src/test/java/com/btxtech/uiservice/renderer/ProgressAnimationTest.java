package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.shape.ModelMatrixAnimation;
import com.btxtech.shared.datatypes.shape.ShapeTransform;
import com.btxtech.shared.datatypes.shape.TimeValueSample;
import com.btxtech.shared.datatypes.shape.TransformationModification;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 27.08.2016.
 */
public class ProgressAnimationTest {

    @Test
    public void testDispatch() throws Exception {
        List<TimeValueSample> timeValueSamples = new ArrayList<>();
        timeValueSamples.add(new TimeValueSample().setTimeStamp(41).setValue(0.0));
        timeValueSamples.add(new TimeValueSample().setTimeStamp(2083).setValue(0.0));
        timeValueSamples.add(new TimeValueSample().setTimeStamp(6350).setValue(10.0));
        ModelMatrixAnimation modelMatrixAnimation = new ModelMatrixAnimation();
        modelMatrixAnimation.setTimeValueSamples(timeValueSamples).setModification(TransformationModification.SCALE).setAxis(ModelMatrixAnimation.Axis.X);

        ProgressAnimation progressAnimation = new ProgressAnimation(modelMatrixAnimation);

        for (int i = 0; i <= 10; i++) {
            double progress = i * 0.1;
            ShapeTransform shapeTransform = new ShapeTransform();
            progressAnimation.dispatch(shapeTransform, progress);
            System.out.println("shapeTransform: " + progress + " " + shapeTransform);
        }

    }
}