package com.btxtech.client;

import com.btxtech.shared.primitives.Matrix4;
import com.btxtech.shared.primitives.Vertex;
import org.jboss.errai.databinding.client.api.Bindable;

import javax.inject.Singleton;

/**
 * Created by Beat
 * 06.06.2016.
 */
@Singleton
@Bindable
public class ColladaUiService {
    private double generalScale = 10;
    private double rotateX = Math.toRadians(25);
    private double rotateZ = Math.toRadians(290);

    public double getGeneralScale() {
        return generalScale;
    }

    public void setGeneralScale(double generalScale) {
        this.generalScale = generalScale;
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

    public Vertex getDirection() {
        return Matrix4.createZRotation(rotateZ).multiply(Matrix4.createXRotation(rotateX)).multiply(new Vertex(0, 0, -1), 1.0);
    }

}
