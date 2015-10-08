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
    private double rotateX = -Math.toRadians(0);
    private double rotateZ = -Math.toRadians(0);
    private Logger logger = Logger.getLogger(Shadowing.class.getName());
    private double zNear = 10;
    private double highestPoint = 20;
    private double lowestPoint = -1;
    private double shadowAlpha = 0.5;

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

    public double getHighestPoint() {
        return highestPoint;
    }

    public void setHighestPoint(double highestPoint) {
        this.highestPoint = highestPoint;
    }

    public double getLowestPoint() {
        return lowestPoint;
    }

    public void setLowestPoint(double lowestPoint) {
        this.lowestPoint = lowestPoint;
    }

    public Matrix4 createProjectionTransformation() {
        double top = calculateTop();
        return AbstractProjectionTransformation.makeBalancedOrthographicFrustum(top, top, zNear, calculateZFar());
    }

    public Matrix4 createViewTransformation() {
        YViewField yViewField = calculateYViewField();
        double yDistance = (yViewField.yTop + yViewField.yBottom) / 2.0;
        double lightNormal = camera.getTranslateY() + yDistance;
        double actualLightPos = lightNormal + Math.tan(rotateX) * calculateZ();

        Matrix4 lightViewMatrix = Matrix4.createXRotation(-rotateX);
        lightViewMatrix = lightViewMatrix.multiply(Matrix4.createZRotation(-rotateZ));
        return lightViewMatrix.multiply(Matrix4.createTranslation(-camera.getTranslateX(), -actualLightPos, -calculateZ()));
    }

    public Matrix4 createViewProjectionTransformation() {
        return createProjectionTransformation().multiply(createViewTransformation());
    }

    private YViewField calculateYViewField() {
        YViewField yViewField = new YViewField();
        yViewField.yBottom = Math.tan(camera.getRotateX() - normalProjectionTransformation.getFovY() / 2.0) * camera.getTranslateZ();
        yViewField.yTop = Math.tan(camera.getRotateX() + normalProjectionTransformation.getFovY() / 2.0) * camera.getTranslateZ();
        return yViewField;
    }

    public void testPrint() {
        logger.severe("calculated Z = " + calculateZ() + "; zNear = " + zNear + "; calculated ZFar = " + calculateZFar() + "; rotateX = Math.toRadians(" + Math.toDegrees(rotateX) + "); rotateZ = Math.toRadians(" + Math.toDegrees(rotateZ) + ");");
    }

    public double getShadowAlpha() {
        return shadowAlpha;
    }

    public void setShadowAlpha(double shadowAlpha) {
        this.shadowAlpha = shadowAlpha;
    }

    private double calculateTop() {
        YViewField yViewField = calculateYViewField();
        double halfSideLength = Math.abs(yViewField.yBottom - yViewField.yTop) / 2.0;
        return Math.cos(rotateX) * halfSideLength;
    }

    private double calculateZ() {
        return highestPoint + Math.abs(Math.sin(rotateX) * calculateTop()) + Math.abs(zNear / Math.cos(rotateX));
    }

    private double calculateZFar() {
        double norm = Math.abs(2.0 * Math.sin(rotateX) * calculateTop()) + highestPoint - lowestPoint;
        return zNear + Math.abs(norm / Math.cos(rotateX));
    }

    private class YViewField {
        double yBottom;
        double yTop;
    }
}
