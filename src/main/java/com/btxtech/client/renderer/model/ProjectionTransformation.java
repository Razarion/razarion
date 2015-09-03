package com.btxtech.client.renderer.model;

import com.btxtech.shared.primitives.Matrix4;

import javax.inject.Singleton;

/**
 * http://www.songho.ca/opengl/gl_projectionmatrix.html
 * <p/>
 * Created by Beat
 * 13.04.2015.
 */
@Singleton
public class ProjectionTransformation {
    private double fovY;
    private double aspectRatio;
    private double zNear;
    private double zFar;

    public ProjectionTransformation() {
        setGame();
    }

    public double getFovY() {
        return fovY;
    }

    public void setFovY(double fovY) {
        this.fovY = fovY;
    }

    public double getAspectRatio() {
        return aspectRatio;
    }

    public void setAspectRatio(double aspectRatio) {
        this.aspectRatio = aspectRatio;
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

    public Matrix4 createMatrix() {
        return makePerspective(fovY, aspectRatio, zNear, zFar);
    }

    public void setGame() {
        fovY = Math.toRadians(45);
        aspectRatio = 4.0 / 3.0;
        zNear = 0.1;
        zFar = 200;
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
    public static Matrix4 makePerspective(double fovY, double aspectRatio, double zNear, double zFar) {
        double top = zNear * Math.tan(fovY / 2.0);
        double right = top * aspectRatio;

        return makeBalancedFrustum(right, top, zNear, zFar);
    }

    public static Matrix4 makeBalancedFrustum(double right, double top, double zNear, double zFar) {
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
}
