package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.shape.AnimationTrigger;
import com.btxtech.shared.datatypes.shape.ModelMatrixAnimation;
import com.btxtech.shared.datatypes.shape.ShapeTransform;
import com.btxtech.shared.datatypes.shape.TimeValueSample;
import com.btxtech.shared.utils.InterpolationUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 30.07.2016.
 */
public class ProgressAnimation {
    private ModelMatrixAnimation modelMatrixAnimation;
    private List<ProgressAnimationSamples> progressAnimationSamples;

    public ProgressAnimation(ModelMatrixAnimation modelMatrixAnimation) {
        this.modelMatrixAnimation = modelMatrixAnimation;
        progressAnimationSamples = new ArrayList<>();
        long beginTimeStamp = modelMatrixAnimation.firstTimeStamp();
        long endTimeStamp = modelMatrixAnimation.lastTimeStamp();
        for (TimeValueSample timeValueSample : modelMatrixAnimation.getTimeValueSamples()) {
            double progress = (double) (timeValueSample.getTimeStamp() - beginTimeStamp) / (double) (endTimeStamp - beginTimeStamp);
            progressAnimationSamples.add(new ProgressAnimationSamples(progress, timeValueSample.getValue()));
        }
    }

    public void dispatch(ShapeTransform shapeTransform, double progress) {
        double value = calculateValue(progress);
        setupMatrix(value, shapeTransform);
    }

    public AnimationTrigger getAnimationTrigger() {
        return modelMatrixAnimation.getAnimationTrigger();
    }

    private double calculateValue(double progress) {
        for (int i = 0; i < progressAnimationSamples.size() + 1; i++) {
            ProgressAnimationSamples last = progressAnimationSamples.get(i);
            ProgressAnimationSamples next = progressAnimationSamples.get(i + 1);
            if (last.getProgress() <= progress && next.getProgress() >= progress) {
                return InterpolationUtils.interpolate(last.getValue(), next.getValue(), last.getProgress(), next.getProgress(), progress);
            }
        }
        throw new IllegalStateException();
    }

    private void setupMatrix(double value, ShapeTransform shapeTransform) {
        switch (modelMatrixAnimation.getModification()) {
            case LOCATION:
                dispatchLocation(value, shapeTransform);
                break;
            case SCALE:
                dispatchScale(value, shapeTransform);
                break;
            default:
                throw new IllegalArgumentException("Unknown TransformationModification: " + modelMatrixAnimation.getModification());
        }
    }

    private void dispatchLocation(double translation, ShapeTransform shapeTransform) {
        switch (modelMatrixAnimation.getAxis()) {
            case X:
                shapeTransform.setTranslateX(translation);
                break;
            case Y:
                shapeTransform.setTranslateY(translation);
                break;
            case Z:
                shapeTransform.setTranslateZ(translation);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private void dispatchScale(double scale, ShapeTransform shapeTransform) {
        switch (modelMatrixAnimation.getAxis()) {
            case X:
                shapeTransform.setScaleX(scale);
                break;
            case Y:
                shapeTransform.setScaleY(scale);
                break;
            case Z:
                shapeTransform.setScaleZ(scale);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }
}
