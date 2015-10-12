package com.btxtech.client.renderer.model;

import com.btxtech.game.jsre.common.MathHelper;
import com.btxtech.shared.primitives.Matrix4;
import com.btxtech.shared.primitives.Vertex;
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
    private double rotateY = -Math.toRadians(45);
    private Logger logger = Logger.getLogger(Shadowing.class.getName());
    private double zNear = 10;
    private double highestPoint = 120;
    private double lowestPoint = -1;
    private double shadowAlpha = 0.6;

    public double getRotateX() {
        return rotateX;
    }

    public void setRotateX(double rotateX) {
        this.rotateX = rotateX;
    }

    public double getRotateY() {
        return rotateY;
    }

    public void setRotateY(double rotateY) {
        this.rotateY = rotateY;
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
        return AbstractProjectionTransformation.makeBalancedOrthographicFrustum(calculateRight(), calculateTop(), zNear, calculateZFar());
    }

    public Matrix4 createViewTransformation() {
        double actualLightPosX = camera.getTranslateX() + Math.tan(rotateY) * calculateZ();

        double lightNormalY = camera.getTranslateY() + calculateYViewField().calculateAverage();
        double actualLightPosY = lightNormalY + Math.tan(rotateX) * calculateZ();

        Matrix4 lightViewMatrix = Matrix4.createXRotation(-rotateX);
        lightViewMatrix = lightViewMatrix.multiply(Matrix4.createYRotation(-rotateY));
        return lightViewMatrix.multiply(Matrix4.createTranslation(-actualLightPosX, -actualLightPosY, -calculateZ()));
    }

    public Matrix4 createViewProjectionTransformation() {
        return createProjectionTransformation().multiply(createViewTransformation());
    }

    private ViewField calculateYViewField() {
        ViewField yViewField = new ViewField();
        yViewField.start = Math.tan(camera.getRotateX() - normalProjectionTransformation.getFovY() / 2.0) * camera.getTranslateZ();
        yViewField.end = Math.tan(camera.getRotateX() + normalProjectionTransformation.getFovY() / 2.0) * camera.getTranslateZ();
        return yViewField;
    }

    public void testPrint() {
        logger.severe("calculateAngle() = " + Math.toDegrees(calculateAngle()));
        logger.severe("calculateRight() = " + calculateRight() + "; calculateTop() = " + calculateTop());
        logger.severe("calculated Z = " + calculateZ() + "; zNear = " + zNear + "; calculated ZFar = " + calculateZFar() + "; rotateX = " + Math.toDegrees(rotateX) + "; rotateY = " + Math.toDegrees(rotateY) + "");
    }

    public double getShadowAlpha() {
        return shadowAlpha;
    }

    public void setShadowAlpha(double shadowAlpha) {
        this.shadowAlpha = shadowAlpha;
    }

    private double calculateRight() {
        double maxY = calculateYViewField().calculateMax();
        double z = MathHelper.getPythagorasC(maxY, camera.getTranslateZ());
        double xHalfViewFiled = Math.tan(normalProjectionTransformation.calculateFovX() / 2.0) * z;
        return Math.cos(rotateY) * xHalfViewFiled;
    }

    private double calculateTop() {
        return Math.cos(rotateX) * calculateYViewField().calculateHalfDifference();
    }

    private double calculateZ() {
        double angle = calculateAngle();
        double z = Math.cos(angle) * MathHelper.getPythagorasC(calculateRight(), calculateTop());
        return z + highestPoint + Math.cos(angle) * zNear;
    }

    private double calculateZFar() {
        double angle = calculateAngle();
        double norm = Math.abs(2.0 * Math.cos(angle) * MathHelper.getPythagorasC(calculateRight(), calculateTop())) + highestPoint - lowestPoint;
        return zNear + Math.abs(norm / Math.cos(angle));
    }

    private double calculateAngle() {
        Vertex planeNorm = Matrix4.createXRotation(rotateX).multiply(Matrix4.createYRotation(rotateY)).multiply(new Vertex(0, 0, 1), 1);
        return planeNorm.unsignedAngle(new Vertex(0, 0, 1));
    }

    private class ViewField {
        double start;
        double end;

        public double calculateAverage() {
            return (end + start) / 2.0;
        }

        public double calculateHalfDifference() {
            return Math.abs(start - end) / 2.0;
        }

        public double calculateMax() {
            return Math.max(Math.abs(start), Math.abs(end));
        }

    }
}
