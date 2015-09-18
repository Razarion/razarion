package com.btxtech.client;

import com.btxtech.client.renderer.model.Shadowing;
import com.btxtech.client.terrain.TerrainSurface;
import org.jboss.errai.databinding.client.api.Bindable;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 18.09.2015.
 */
@Bindable
public class MenuModel {
    @Inject
    private Shadowing shadowing;
    @Inject
    private TerrainSurface terrainSurface;

    public double getSurfaceSlider() {
        if (terrainSurface == null) {
            return 0;
        }
        return terrainSurface.getEdgeDistance();
    }

    public void setSurfaceSlider(double surfaceSlider) {
        if (terrainSurface == null) {
            return;
        }
        terrainSurface.setEdgeDistance(surfaceSlider);
    }

    public double getShadowLightPosX() {
        if (shadowing == null) {
            return 0;
        }
        return shadowing.getX();
    }

    public void setShadowLightPosX(double shadowLightPosX) {
        if (shadowing == null) {
            return;
        }
        shadowing.setX(shadowLightPosX);
    }

    public double getShadowLightPosY() {
        if (shadowing == null) {
            return 0;
        }
        return shadowing.getY();
    }

    public void setShadowLightPosY(double shadowLightPosY) {
        if (shadowing == null) {
            return;
        }
        shadowing.setY(shadowLightPosY);
    }

    public double getShadowLightPosZ() {
        if (shadowing == null) {
            return 0;
        }
        return shadowing.getZ();
    }

    public void setShadowLightPosZ(double shadowLightPosZ) {
        if (shadowing == null) {
            return;
        }
        shadowing.setZ(shadowLightPosZ);
    }

    public double getShadowLightRotateX() {
        if (shadowing == null) {
            return 0;
        }
        return shadowing.getRotateX();
    }

    public void setShadowLightRotateX(double shadowLightRotateX) {
        if (shadowing == null) {
            return;
        }
        shadowing.setRotateX(shadowLightRotateX);
    }

    public double getShadowLightRotateZ() {
        if (shadowing == null) {
            return 0;
        }
        return shadowing.getRotateZ();
    }

    public void setShadowLightRotateZ(double shadowLightRotateZ) {
        if (shadowing == null) {
            return;
        }
        shadowing.setRotateZ(shadowLightRotateZ);
    }
}
