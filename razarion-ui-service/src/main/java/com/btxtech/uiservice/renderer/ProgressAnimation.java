package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.shape.ModelMatrixAnimation;
import com.btxtech.shared.datatypes.shape.TimeValueSample;
import com.btxtech.shared.gameengine.datatypes.syncobject.SyncSpawnItem;
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
            double progress = (timeValueSample.getTimeStamp() - beginTimeStamp) / (endTimeStamp - beginTimeStamp);
            progressAnimationSamples.add(new ProgressAnimationSamples(progress, timeValueSample.getValue()));
        }
    }

    public boolean isItemTriggered() {
        return modelMatrixAnimation.getItemState() != null;
    }

    public ModelMatrices mix(ModelMatrices modelMatrix) {
        double progress = ((SyncSpawnItem) modelMatrix.getSyncItem()).getProgress();
        double value = calculateValue(progress);
        Matrix4 scaleMatrix = setupScaleMatrix(value);
        return modelMatrix.multiply(scaleMatrix, null);
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

    private Matrix4 setupScaleMatrix(double scale) {
        switch (modelMatrixAnimation.getAxis()) {
            case X:
                return Matrix4.createScale(scale, 1, 1);
            case Y:
                return Matrix4.createScale(1, scale, 1);
            case Z:
                return Matrix4.createScale(1, 1, scale);
            default:
                throw new IllegalArgumentException();
        }
    }
}
