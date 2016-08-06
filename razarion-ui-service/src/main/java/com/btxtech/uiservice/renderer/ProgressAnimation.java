package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.shape.ModelMatrixAnimation;
import com.btxtech.shared.datatypes.shape.ShapeTransformTRS;
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
        List<TimeValueSample> timeValueSamples = modelMatrixAnimation.getTimeValueSamples();
        long beginTimeStamp = timeValueSamples.get(0).getTimeStamp();
        long endTimeStamp = timeValueSamples.get(timeValueSamples.size() - 1).getTimeStamp();
        for (TimeValueSample timeValueSample : modelMatrixAnimation.getTimeValueSamples()) {
            double progress = (double)(timeValueSample.getTimeStamp() - beginTimeStamp) / (double)(endTimeStamp - beginTimeStamp);
            progressAnimationSamples.add(new ProgressAnimationSamples(progress, timeValueSample.getValue()));
        }
    }

    public boolean isItemTriggered() {
        return modelMatrixAnimation.getItemState() != null;
    }

    public void dispatch(ShapeTransformTRS shapeTransform, double progress) {
        double value = calculateValue(progress);
        setupMatrix(value, shapeTransform);
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

    private void setupMatrix(double value, ShapeTransformTRS shapeTransform) {
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

    private void dispatchLocation(double translation, ShapeTransformTRS shapeTransform) {
        switch (modelMatrixAnimation.getAxis()) {
            case X:
                shapeTransform.setXTranslate(translation);
                break;
            case Y:
                shapeTransform.setYTranslate(translation);
                break;
            case Z:
                shapeTransform.setZTranslate(translation);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private void dispatchScale(double scale, ShapeTransformTRS shapeTransform) {
        switch (modelMatrixAnimation.getAxis()) {
            case X:
                shapeTransform.setXScale(scale);
                break;
            case Y:
                shapeTransform.setYScale(scale);
                break;
            case Z:
                shapeTransform.setZScale(scale);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }
}
