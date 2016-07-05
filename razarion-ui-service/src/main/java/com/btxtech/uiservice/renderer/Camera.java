package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Ray3d;
import com.btxtech.shared.datatypes.Vertex;
import org.jboss.errai.databinding.client.api.Bindable;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 25.04.2015.
 */
@Singleton
@Bindable
public class Camera {
    @Inject
    private Event<CameraMovedEvent> cameraMovedEvent;
    private double translateX;
    private double translateY;
    private double translateZ;
    private double rotateX;
    private double rotateZ;

    private Logger logger = Logger.getLogger(Camera.class.getName());

    @PostConstruct
    public void init() {
        translateX = 1000;
        translateY = 500;
        translateZ = 720;
        rotateX = Math.toRadians(35);
        rotateZ = Math.toRadians(0);
    }

    public double getTranslateX() {
        return translateX;
    }

    public void setTranslateX(double translateX) {
        this.translateX = translateX;
        fireChanged();
    }

    public double getTranslateY() {
        return translateY;
    }

    public void setTranslateY(double translateY) {
        this.translateY = translateY;
        fireChanged();
    }

    public void setTranslateDeltaXY(int deltaX, int deltaY) {
        translateX += deltaX;
        translateY += deltaY;
        fireChanged();
    }

    public double getTranslateZ() {
        return translateZ;
    }

    public void setTranslateZ(double translateZ) {
        this.translateZ = translateZ;
        fireChanged();
    }

    public double getRotateX() {
        return rotateX;
    }

    public void setRotateX(double rotateX) {
        this.rotateX = rotateX;
        fireChanged();
    }

    public void setRotateZ(double rotateZ) {
        this.rotateZ = rotateZ;
        fireChanged();
    }

    public double getRotateZ() {
        return rotateZ;
    }

    public Matrix4 createMatrix() {
        return createNormMatrix().multiply(Matrix4.createTranslation(-translateX, -translateY, -translateZ));
    }

    public Matrix4 createNormMatrix() {
        return Matrix4.createXRotation(-rotateX).multiply(Matrix4.createZRotation(-rotateZ));
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
        fireChanged();
    }

    public void setFront() {
        translateX = 50;
        translateY = -20;
        translateZ = -10;
        rotateX = Math.toRadians(90);
        rotateZ = Math.toRadians(0);
        fireChanged();
    }

    public void setGame() {
        // translateX = 500;
        // translateY = -140;
        translateX = 1000;
        translateY = 500;
        translateZ = -720;
        rotateX = Math.toRadians(0);
        rotateZ = Math.toRadians(0);
        fireChanged();
    }

    private void fireChanged() {
        // cameraMovedEvent.fire(new CameraMovedEvent());
    }

    public Ray3d toWorld(Ray3d pickRay) {
        Vertex start = createMatrix().invert().multiply(pickRay.getStart(), 1);
        Vertex direction = createNormMatrix().invert().multiply(pickRay.getDirection(), 1);
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
