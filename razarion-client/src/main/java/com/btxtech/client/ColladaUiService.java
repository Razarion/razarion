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
    private double xRotation;
    private double yRotation;

    public double getGeneralScale() {
        return generalScale;
    }

    public void setGeneralScale(double generalScale) {
        this.generalScale = generalScale;
    }

    public double getXRotation() {
        return xRotation;
    }

    public void setXRotation(double xRotation) {
        this.xRotation = xRotation;
    }

    public double getYRotation() {
        return yRotation;
    }

    public void setYRotation(double yRotation) {
        this.yRotation = yRotation;
    }

    public Vertex getDirection() {
        return Matrix4.createXRotation(xRotation).multiply(Matrix4.createYRotation(yRotation)).multiply(new Vertex(0, 0, -1), 1.0);
    }

}
