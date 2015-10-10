package com.btxtech.client.renderer.model;

import com.btxtech.shared.primitives.Matrix4;

/**
 * http://www.songho.ca/opengl/gl_projectionmatrix.html
 * <p/>
 * Created by Beat
 * 13.04.2015.
 */
public interface ProjectionTransformation {
    double getZNear();

    void setZNear(double zNear);

    double getZFar();

    void setZFar(double zFar);

    Matrix4 createMatrix();

    double getFovY();

    void setFovY(double fovY);

    double calculateFovX();

    double getAspectRatio();

    void setAspectRatio(double aspectRatio);
}
