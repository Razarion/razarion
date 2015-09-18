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
    private double x = 20;
    private double y = 48;
    private double z = 15;
    private double rotateX = -Math.toRadians(0);
    private double rotateZ = -Math.toRadians(0);
    private Logger logger = Logger.getLogger(Shadowing.class.getName());

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

    @Deprecated
    public Matrix4 createMvpShadowBias_UNKNWON() {
        Matrix4 lightViewMatrix = Matrix4.createXRotation(-rotateX);
        lightViewMatrix = lightViewMatrix.multiply(Matrix4.createZRotation(-rotateZ));
        lightViewMatrix = lightViewMatrix.multiply(Matrix4.createTranslation(-x, -y, -z));
        return projectionTransformation.createMatrix().multiply(lightViewMatrix.multiply(modelTransformation.createMatrix()));
    }

    public Matrix4 createMvpShadowBias() {
        Matrix4 lightViewMatrix = Matrix4.createXRotation(-rotateX);
        lightViewMatrix = lightViewMatrix.multiply(Matrix4.createZRotation(-rotateZ));
        return lightViewMatrix.multiply(Matrix4.createTranslation(-x, -y, -z));
    }

    public void testPrint() {
        logger.severe("x = " + x + "; y = " + y + "; z = " + z + "; rotateX = Math.toRadians(" + Math.toDegrees(rotateX) + "); rotateZ = Math.toRadians(" + Math.toDegrees(rotateZ) + ");");
    }
}
