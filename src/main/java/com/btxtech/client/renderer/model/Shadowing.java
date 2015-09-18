package com.btxtech.client.renderer.model;

import com.btxtech.shared.primitives.Matrix4;
import org.jboss.errai.databinding.client.api.Bindable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 14.09.2015.
 */
@Singleton
@Bindable
public class Shadowing {
    @Inject
    private ViewTransformation viewTransformation;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private ModelTransformation modelTransformation;
    private double x = -8;
    private double y = 49;
    private double z = 15;
    private double rotateX = -Math.toRadians(45);
    private double rotateZ = -Math.toRadians(90);
    // private Logger logger = Logger.getLogger(Shadowing.class.getName());

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double getRotateX() {
        return rotateX;
    }

    public void setRotateX(double rotateX) {
        this.rotateX = rotateX;
    }

    public double getRotateZ() {
        return rotateZ;
    }

    public void setRotateZ(double rotateZ) {
        this.rotateZ = rotateZ;
    }

    public void setupViewTransformation() {
        viewTransformation.setTranslateX(-x);
        viewTransformation.setTranslateY(-y);
        viewTransformation.setTranslateZ(-z);
        viewTransformation.setRotateX(-rotateX);
        viewTransformation.setRotateZ(-rotateZ);
    }

    public Matrix4 createMvpShadowBias() {
        Matrix4 lightViewMatrix = Matrix4.createXRotation(-rotateX);
        lightViewMatrix = lightViewMatrix.multiply(Matrix4.createZRotation(-rotateZ));
        lightViewMatrix = lightViewMatrix.multiply(Matrix4.createTranslation(-x, -y, -z));
        return projectionTransformation.createMatrix().multiply(lightViewMatrix.multiply(modelTransformation.createMatrix()));
    }

}
