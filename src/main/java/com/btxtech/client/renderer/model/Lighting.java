package com.btxtech.client.renderer.model;

import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.game.jsre.common.MathHelper;
import com.btxtech.shared.primitives.Matrix4;
import com.btxtech.shared.primitives.Vertex;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 23.06.2015.
 */
@Singleton
public class Lighting {
    @Inject
    private Camera camera;
    @Inject
    private ProjectionTransformation normalProjectionTransformation;
    @Inject
    private TerrainSurface terrainSurface;
    private double zNear = 10;
    private double shadowAlpha = 0.2;
    private Logger logger = Logger.getLogger(Lighting.class.getName());
    private double rotateX = Math.toRadians(0);
    private double rotateY = -Math.toRadians(0);
    private double ambientIntensity;
    private double diffuseIntensity;

    public Lighting() {
        setGame();
    }

    /**
     * Return the point on the surface pointing to the sun
     *
     * @return direction normalized
     */
    public Vertex getLightDirection() {
        return createRotationMatrix().multiply(new Vertex(0, 0, -1), 1.0);
    }

    public void setGame() {
        diffuseIntensity = 0.6;
        ambientIntensity = 0.4;
    }

    public double getAmbientIntensity() {
        return ambientIntensity;
    }

    public void setAmbientIntensity(double ambientIntensity) {
        this.ambientIntensity = ambientIntensity;
    }

    public double getDiffuseIntensity() {
        return diffuseIntensity;
    }

    public void setDiffuseIntensity(double diffuseIntensity) {
        this.diffuseIntensity = diffuseIntensity;
    }

    public double getShadowAlpha() {
        return shadowAlpha;
    }

    public void setShadowAlpha(double shadowAlpha) {
        this.shadowAlpha = shadowAlpha;
    }

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

    public Matrix4 createViewTransformation() {
        double actualLightPosX = camera.getTranslateX() + Math.tan(rotateY) * calculateZ();

        double lightNormalY = camera.getTranslateY() + calculateYViewField().calculateAverage();
        double actualLightPosY = lightNormalY + Math.tan(rotateX) * calculateZ();

        return createNegatedRotationMatrix().multiply(Matrix4.createTranslation(-actualLightPosX, -actualLightPosY, -calculateZ()));
    }

    private Matrix4 createNegatedRotationMatrix() {
        Matrix4 rotationMatrix = Matrix4.createXRotation(-rotateX);
        return rotationMatrix.multiply(Matrix4.createYRotation(-rotateY));
    }

    private Matrix4 createRotationMatrix() {
        Matrix4 rotationMatrix = Matrix4.createXRotation(rotateX);
        return rotationMatrix.multiply(Matrix4.createYRotation(rotateY));
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

    private double calculateRight() {
        // TODO hier
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
        return z + terrainSurface.getHighestPointInView() + Math.cos(angle) * zNear;
    }

    private double calculateZFar() {
        double angle = calculateAngle();
        double norm = Math.abs(2.0 * Math.cos(angle) * MathHelper.getPythagorasC(calculateRight(), calculateTop())) + terrainSurface.getHighestPointInView() - terrainSurface.getLowestPointInView();
        return zNear + Math.abs(norm / Math.cos(angle));
    }

    private double calculateAngle() {
        Vertex planeNorm = Matrix4.createXRotation(rotateX).multiply(Matrix4.createYRotation(rotateY)).multiply(new Vertex(0, 0, 1), 1);
        return planeNorm.unsignedAngle(new Vertex(0, 0, 1));
    }
    public double getZNear() {
        return zNear;
    }

    public void setZNear(double zNear) {
        this.zNear = zNear;
    }

    public Matrix4 createProjectionTransformation() {
        return ProjectionTransformation.makeBalancedOrthographicFrustum(calculateRight(), calculateTop(), zNear, calculateZFar());
    }

    public void testPrint() {
        logger.severe("calculateAngle() = " + Math.toDegrees(calculateAngle()));
        logger.severe("calculateRight() = " + calculateRight() + "; calculateTop() = " + calculateTop());
        logger.severe("calculated Z = " + calculateZ() + "; zNear = " + zNear + "; calculated ZFar = " + calculateZFar() + "; rotateX = " + Math.toDegrees(rotateX) + "; rotateY = " + Math.toDegrees(rotateY) + "");
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
