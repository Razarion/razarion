package com.btxtech.client.renderer.model;

import com.btxtech.shared.primitives.Matrix4;
import com.btxtech.shared.primitives.Vertex;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * 14.09.2015.
 */
@Singleton
public class Shadowing {
    @Inject
    private ViewTransformation viewTransformation;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private ModelTransformation modelTransformation;
    private Vertex worldLightPos = new Vertex(-8, 49, 15);
    private double rotateX = -Math.toRadians(45);
    private double rotateZ = -Math.toRadians(90);


    public Vertex getWorldLightPos() {
        return worldLightPos;
    }

    public double getRotateX() {
        return rotateX;
    }

    public double getRotateZ() {
        return rotateZ;
    }

    public void setupViewTransformation() {
        viewTransformation.setTranslateX(-worldLightPos.getX());
        viewTransformation.setTranslateY(-worldLightPos.getY());
        viewTransformation.setTranslateZ(-worldLightPos.getZ());
        viewTransformation.setRotateX(-rotateX);
        viewTransformation.setRotateZ(-rotateZ);
    }

    public Matrix4 createMvpShadowBias() {
        Matrix4 lightViewMatrix = Matrix4.createXRotation(-rotateX);
        lightViewMatrix = lightViewMatrix.multiply(Matrix4.createZRotation(-rotateZ));
        lightViewMatrix = lightViewMatrix.multiply(Matrix4.createTranslation(-worldLightPos.getX(), -worldLightPos.getY(), -worldLightPos.getZ()));
        return projectionTransformation.createMatrix().multiply(lightViewMatrix.multiply(modelTransformation.createMatrix()));
    }

}
