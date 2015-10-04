package com.btxtech.client.renderer.model;

import com.btxtech.shared.primitives.Matrix4;
import org.jboss.errai.databinding.client.api.Bindable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 14.09.2015.
 */
@Singleton
@Bindable
public class Shadowing {
    public static final int TEXTURE_SIZE = 1024;
    @Inject
    private Camera camera;
    @Inject
    @Normal
    private ProjectionTransformation normalProjectionTransformation;
    private double z = 200;
    private double rotateX = -Math.toRadians(0);
    private double rotateZ = -Math.toRadians(0);
    private Logger logger = Logger.getLogger(Shadowing.class.getName());
    private double zNear = 150;
    private double zFar = 201;

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

    public double getZNear() {
        return zNear;
    }

    public void setZNear(double zNear) {
        this.zNear = zNear;
    }

    public double getZFar() {
        return zFar;
    }

    public void setZFar(double zFar) {
        this.zFar = zFar;
    }

    public Matrix4 createProjectionTransformation() {
        YViewField yViewField = calculateYViewField();
        double halfSideLength = Math.abs(yViewField.yDistance1 - yViewField.yDistance2) / 2.0;
        return AbstractProjectionTransformation.makeBalancedOrthographicFrustum(halfSideLength, halfSideLength, zNear, zFar);
    }

    public Matrix4 createModelViewProjectionTransformation() {
        return createProjectionTransformation().multiply(createModelViewTransformation());
    }

    public Matrix4 createModelViewTransformation() {
        Matrix4 lightViewMatrix = Matrix4.createXRotation(-rotateX);
        lightViewMatrix = lightViewMatrix.multiply(Matrix4.createZRotation(-rotateZ));
        YViewField yViewField = calculateYViewField();
        double yDistance = (yViewField.yDistance2 + yViewField.yDistance1) / 2.0;
        double y = camera.getTranslateY() + yDistance;
        return lightViewMatrix.multiply(Matrix4.createTranslation(-camera.getTranslateX(), -y, -z));
    }

    private YViewField calculateYViewField() {
        YViewField yViewField = new YViewField();
        yViewField.yDistance1 = Math.tan(camera.getRotateX() - normalProjectionTransformation.getFovY() / 2.0) * camera.getTranslateZ();
        yViewField.yDistance2 = Math.tan(camera.getRotateX() + normalProjectionTransformation.getFovY() / 2.0) * camera.getTranslateZ();
        return yViewField;
    }

    public void testPrint() {
        YViewField yViewField = calculateYViewField();
        double sideLength = Math.abs(yViewField.yDistance1 - yViewField.yDistance2);
        logger.severe("yViewField: " + (yViewField.yDistance1 + z) + ":" + (yViewField.yDistance2 + z) + " sideLength = " + sideLength);
        logger.severe("z = " + z + "; zNear = " + zNear + "; zFar = " + zFar + "; rotateX = Math.toRadians(" + Math.toDegrees(rotateX) + "); rotateZ = Math.toRadians(" + Math.toDegrees(rotateZ) + ");");
    }

    private class YViewField {
        double yDistance1;
        double yDistance2;
    }
}
