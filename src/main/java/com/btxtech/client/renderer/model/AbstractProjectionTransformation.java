package com.btxtech.client.renderer.model;

import com.btxtech.shared.primitives.Matrix4;

/**
 * Created by Beat
 * 26.09.2015.
 */
public abstract class AbstractProjectionTransformation implements ProjectionTransformation {
    private double fovY;
    private double aspectRatio;
    private double zNear;
    private double zFar;

    @Override
    public double getFovY() {
        return fovY;
    }

    @Override
    public void setFovY(double fovY) {
        this.fovY = fovY;
    }

    @Override
    public double calculateFovX() {
        double top = zNear * Math.tan(fovY / 2.0);
        double right = top * aspectRatio;
        return Math.atan(right / zNear) * 2.0;
    }

    @Override
    public double getAspectRatio() {
        return aspectRatio;
    }

    @Override
    public void setAspectRatio(double aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    @Override
    public double getZNear() {
        return zNear;
    }

    @Override
    public void setZNear(double zNear) {
        this.zNear = zNear;
    }

    @Override
    public double getZFar() {
        return zFar;
    }

    @Override
    public void setZFar(double zFar) {
        this.zFar = zFar;
    }

    @Override
    public Matrix4 createMatrix() {
        return makePerspectiveFrustum(fovY, aspectRatio, zNear, zFar);
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
     *
     * @param right
     * @param top
     * @param zNear
     * @param zFar
     * @return
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

}
