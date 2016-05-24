package com.btxtech.client.renderer.model;

import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.common.MathHelper;
import com.btxtech.shared.primitives.Matrix4;
import com.btxtech.shared.primitives.Ray3d;
import com.btxtech.shared.primitives.Vertex;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * 26.09.2015.
 * <p/>
 * http://www.songho.ca/opengl/gl_projectionmatrix.html
 */
@Singleton
public class ProjectionTransformation {
    private static final int Z_NEAR_FALLBACK = 10;
    private static final int Z_FAR_FALLBACK = 5000000;
    // private Logger logger = Logger.getLogger(ProjectionTransformation.class.getName());
    private double fovY;
    private double aspectRatio;
    @Inject
    private Camera camera;
    @Inject
    private TerrainSurface terrainSurface;

    public ProjectionTransformation() {
        setFovY(Math.toRadians(45));
    }

    public double getFovY() {
        return fovY;
    }

    public void setFovY(double fovY) {
        this.fovY = fovY;
    }

    public double calculateFovX() {
        double zNear = calculateZNear();
        double top = zNear * Math.tan(fovY / 2.0);
        double right = top * aspectRatio;
        return Math.atan(right / zNear) * 2.0;
    }

    public double getAspectRatio() {
        return aspectRatio;
    }

    public void setAspectRatio(double aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public Matrix4 createMatrix() {
        return makePerspectiveFrustum(fovY, aspectRatio, calculateZNear(), calculateZFar());
    }

    private double calculateZFar() {
        double angle1 = camera.getRotateX() + fovY / 2.0;
        double angle2 = camera.getRotateX() - fovY / 2.0;

        if (Math.abs(angle1) >= MathHelper.QUARTER_RADIANT || Math.abs(angle2) >= MathHelper.QUARTER_RADIANT) {
            return Z_FAR_FALLBACK;
        }

        double height = camera.getTranslateZ() - terrainSurface.getLowestPointInView();

        double leg1 = height / Math.cos(angle1);
        double leg2 = height / Math.cos(angle2);

        double zFar1 = leg1 * Math.cos(fovY / 2.0);
        double zFar2 = leg2 * Math.cos(fovY / 2.0);

        return Math.max(zFar1, zFar2);
    }

    private double calculateZNear() {
        double angle1 = camera.getRotateX() + fovY / 2.0;
        double angle2 = camera.getRotateX() - fovY / 2.0;

        if (Math.abs(angle1) >= MathHelper.QUARTER_RADIANT && Math.abs(angle2) >= MathHelper.QUARTER_RADIANT) {
            return Z_NEAR_FALLBACK;
        }

        double height = camera.getTranslateZ() - terrainSurface.getHighestPointInView();

        double leg1 = height / Math.cos(angle1);
        double leg2 = height / Math.cos(angle2);

        double zNear1 = leg1 * Math.cos(fovY / 2.0);
        double zNear2 = leg2 * Math.cos(fovY / 2.0);

        if (Math.abs(angle1) >= MathHelper.QUARTER_RADIANT) {
            return Math.max(zNear2, Z_NEAR_FALLBACK);
        }
        if (Math.abs(angle2) >= MathHelper.QUARTER_RADIANT) {
            return Math.max(zNear1, Z_NEAR_FALLBACK);
        }

        return Math.max(Math.min(zNear1, zNear2), Z_NEAR_FALLBACK);
    }

    /**
     * Calculates the perspective frustum projection matrix
     *
     * @param fovY        field of view y in radians
     * @param aspectRatio aspect ratio width / height
     * @param zNear       z near
     * @param zFar        z far
     * @return perspective frustum projection matrix
     */
    public static Matrix4 makePerspectiveFrustum(double fovY, double aspectRatio, double zNear, double zFar) {
        double top = zNear * Math.tan(fovY / 2.0);
        double right = top * aspectRatio;

        return makeBalancedPerspectiveFrustum(right, top, zNear, zFar);
    }

    public static Matrix4 makeBalancedPerspectiveFrustum(double right, double top, double zNear, double zFar) {
        double x = zNear / right;
        double y = zNear / top;
        double a = -(zFar + zNear) / (zFar - zNear);
        double b = -2 * zFar * zNear / (zFar - zNear);

        return new Matrix4(new double[][]{
                {x, 0, 0, 0},
                {0, y, 0, 0},
                {0, 0, a, b},
                {0, 0, -1, 0}});
    }

//    /**
//     * Calculates the orthographic frustum projection matrix
//     *
//     * @param fovY        field of view y in radians
//     * @param aspectRatio aspect ratio width / height
//     * @param zNear       z near
//     * @param zFar        z far
//     * @return perspective frustum projection matrix
//     */
//    public static Matrix4 makeOrthographicFrustum(double fovY, double aspectRatio, double zNear, double zFar) {
//        double top = zNear * Math.tan(fovY / 2.0);
//        double right = top * aspectRatio;
//
//        return makeBalancedOrthographicFrustum(right, top, zNear, zFar);
//    }

    /**
     * http://www.songho.ca/opengl/gl_projectionmatrix.html
     */
    public static Matrix4 makeBalancedOrthographicFrustum(double right, double top, double zNear, double zFar) {
        double a = -2.0 / (zFar - zNear);
        double b = -(zFar + zNear) / (zFar - zNear);

        return new Matrix4(new double[][]{
                {1.0 / right, 0, 0, 0},
                {0, 1.0 / top, 0, 0},
                {0, 0, a, b},
                {0, 0, 0, 1}});
    }

    /**
     * Creates the pick ray for converting the mouse position to the model position
     *
     * @param ndc normalized device coordinates (-1 to 1)
     */
    public Ray3d createPickRay(DecimalPosition ndc) {
        double zNear = calculateZNear();
        double top = zNear * Math.tan(fovY / 2.0);
        double y = top * ndc.getY();
        double x = ndc.getX() * top * aspectRatio;
        double rotateY = -Math.atan(x / zNear);
        double rotateX = -Math.atan(y / zNear);
        Vertex direction = new Vertex(0, 0, -1);
        Matrix4 rotation = Matrix4.createXRotation(rotateX).multiply(Matrix4.createYRotation(rotateY));
        direction = rotation.multiply(direction, 1.0);
        return new Ray3d(new Vertex(0, 0, 0), direction);
    }

    @Override
    public String toString() {
        return "ProjectionTransformation{" +
                "fovY=" + Math.toDegrees(fovY) +
                ", aspectRatio=" + aspectRatio +
                '}';
    }
}
