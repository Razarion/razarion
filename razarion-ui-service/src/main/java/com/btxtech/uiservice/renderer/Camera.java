package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Ray3d;
import com.btxtech.shared.datatypes.Vertex;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 25.04.2015.
 */
@ApplicationScoped
public class Camera {
    private Logger logger = Logger.getLogger(Camera.class.getName());
    @Inject
    private ProjectionTransformation projectionTransformation;
    private double translateX;
    private double translateY;
    private double translateZ = 80;
    private double rotateX= Math.toRadians(35);
    private double rotateZ;
    private Matrix4 matrix4;
    private Matrix4 normMatrix4;

    public double getTranslateX() {
        return translateX;
    }

    public void setTranslateX(double translateX) {
        this.translateX = translateX;
        setupMatrices();
    }

    public double getTranslateY() {
        return translateY;
    }

    public void setTranslateY(double translateY) {
        this.translateY = translateY;
        setupMatrices();
    }

    public void setTranslateXY(double x, double y) {
        translateX = x;
        translateY = y;
        setupMatrices();
    }

    public double getTranslateZ() {
        return translateZ;
    }

    public void setTranslateZ(double translateZ) {
        this.translateZ = translateZ;
        setupMatrices();
    }

    public double getRotateX() {
        return rotateX;
    }

    public void setRotateX(double rotateX) {
        this.rotateX = rotateX;
        setupMatrices();
    }

    public void setRotateZ(double rotateZ) {
        this.rotateZ = rotateZ;
        setupMatrices();
    }

    public double getRotateZ() {
        return rotateZ;
    }

    public Matrix4 getMatrix() {
        return matrix4;
    }

    public Matrix4 getNormMatrix() {
        return normMatrix4;
    }

    public Vertex getPosition() {
        return new Vertex(translateX, translateY, translateZ);
    }

    public Vertex getDirection() {
        return Matrix4.createZRotation(rotateZ).multiply(Matrix4.createXRotation(rotateX)).multiply(new Vertex(0, 0, -1), 1.0);
    }

    public void setTop() {
        translateX = 0;
        translateY = 0;
        translateZ = 1000;
        rotateX = Math.toRadians(0);
        rotateZ = Math.toRadians(0);
        setupMatrices();
    }

    public void setFront() {
        translateX = 50;
        translateY = -20;
        translateZ = -10;
        rotateX = Math.toRadians(90);
        rotateZ = Math.toRadians(0);
        setupMatrices();
    }

    public void setGame() {
        // translateX = 500;
        // translateY = -140;
        translateX = 1000;
        translateY = 500;
        translateZ = -720;
        rotateX = Math.toRadians(0);
        rotateZ = Math.toRadians(0);
        setupMatrices();
    }

    private void setupMatrices() {
        matrix4 = Matrix4.createXRotation(-rotateX).multiply(Matrix4.createZRotation(-rotateZ)).multiply(Matrix4.createTranslation(-translateX, -translateY, -translateZ));
        normMatrix4 = matrix4.normTransformation();
        projectionTransformation.setupMatrices();
    }

    public Ray3d toWorld(Ray3d pickRay) {
        Vertex start = getMatrix().invert().multiply(pickRay.getStart(), 1);
        Vertex direction = getNormMatrix().invert().multiply(pickRay.getDirection(), 1);
        return new Ray3d(start, direction);
    }

    public void testPrint() {
        logger.severe("translateX = " + translateX + "; translateY = " + translateY + "; translateZ = " + translateZ + "; rotateX = Math.toRadians(" + Math.toDegrees(rotateX) + "); rotateZ = Math.toRadians(" + Math.toDegrees(rotateZ) + ");");
    }

    @Override
    public String toString() {
        return "Camera{" +
                "translateX=" + translateX +
                ", translateY=" + translateY +
                ", translateZ=" + translateZ +
                ", rotateX=" + Math.toDegrees(rotateX) +
                ", rotateZ=" + Math.toDegrees(rotateZ) +
                '}';
    }
}
