package com.btxtech.uiservice.storyboard;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.dto.AnimatedMeshConfig;
import com.btxtech.shared.utils.MathHelper;

/**
 * Created by Beat
 * 12.07.2016.
 */
public class AnimatedMesh {
    private final int id;
    private final AnimatedMeshConfig animatedMeshConfig;
    private long timeStamp = System.currentTimeMillis();

    public AnimatedMesh(int id, AnimatedMeshConfig animatedMeshConfig) {
        this.id = id;
        this.animatedMeshConfig = animatedMeshConfig;
    }

    public int getId() {
        return id;
    }

    public AnimatedMeshConfig getAnimatedMeshConfig() {
        return animatedMeshConfig;
    }

    public Matrix4 calculateModelMatrix() {
        int duration = (int) (System.currentTimeMillis() - timeStamp);
        double factor = duration / animatedMeshConfig.getDuration();
        factor = MathHelper.clamp01(factor);
        double scale = animatedMeshConfig.getScaleFrom() + (animatedMeshConfig.getScaleTo() - animatedMeshConfig.getScaleFrom()) * factor;
        Matrix4 modelMatrix = Matrix4.createTranslation(animatedMeshConfig.getPosition().getX(), animatedMeshConfig.getPosition().getY(), animatedMeshConfig.getPosition().getZ());
        return modelMatrix.multiply(Matrix4.createScale(scale, scale, scale));
    }
}
