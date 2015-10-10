package com.btxtech.client;

import com.btxtech.client.renderer.engine.RenderService;
import com.btxtech.client.renderer.model.Normal;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.client.renderer.model.Shadowing;
import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.terrain.TerrainSurface;
import org.jboss.errai.databinding.client.api.Bindable;

import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 18.09.2015.
 */
@Bindable
public class MenuModel {
    private Logger logger = Logger.getLogger(MenuModel.class.getName());
    @Inject
    private Shadowing shadowing;
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    private RenderService renderService;
    @Inject
    private Camera camera;
    @Inject
    @Normal
    private ProjectionTransformation normalProjectionTransformation;

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

    public double getShadowLightRotateY() {
        if (shadowing == null) {
            return 0;
        }
        return shadowing.getRotateY();
    }

    public void setShadowLightRotateY(double shadowLightRotateY) {
        if (shadowing == null) {
            return;
        }
        shadowing.setRotateY(shadowLightRotateY);
    }

    public boolean getShowDeepMap() {
        return renderService != null && renderService.isShowDeep();
    }

    public void setShowDeepMap(boolean showDeepMap) {
        if (renderService == null) {
            return;
        }
        renderService.setShowDeep(showDeepMap);
    }

    public double getViewTransformationX() {
        if (camera == null) {
            return 0;
        }
        return camera.getTranslateX();
    }

    public void setViewTransformationX(double viewTransformationX) {
        if (camera == null) {
            return;
        }
        camera.setTranslateX(viewTransformationX);
    }

    public double getViewTransformationY() {
        if (camera == null) {
            return 0;
        }
        return camera.getTranslateY();
    }

    public void setViewTransformationY(double viewTransformationY) {
        if (camera == null) {
            return;
        }
        camera.setTranslateY(viewTransformationY);
    }

    public double getViewTransformationZ() {
        if (camera == null) {
            return 0;
        }
        return camera.getTranslateZ();
    }

    public void setViewTransformationZ(double viewTransformationZ) {
        if (camera == null) {
            return;
        }
        camera.setTranslateZ(viewTransformationZ);
    }

    public double getViewTransformationRotateX() {
        if (camera == null) {
            return 0;
        }
        return camera.getRotateX();
    }

    public void setViewTransformationRotateX(double viewTransformationRotateX) {
        if (camera == null) {
            return;
        }
        camera.setRotateX(viewTransformationRotateX);
    }

    public double getViewTransformationRotateZ() {
        if (camera == null) {
            return 0;
        }
        return camera.getRotateZ();
    }

    public void setViewTransformationRotateZ(double viewTransformationRotateZ) {
        if (camera == null) {
            return;
        }
        camera.setRotateZ(viewTransformationRotateZ);
    }

    public double getProjectionTransformationZoom() {
        if (normalProjectionTransformation == null) {
            return 0;
        }
        return normalProjectionTransformation.getFovY();
    }

    public void setProjectionTransformationZoom(double fovY) {
        if (normalProjectionTransformation == null) {
            return;
        }
        normalProjectionTransformation.setFovY(fovY);
    }

    public double getShadowProjectionTransformationZNear() {
        if (shadowing == null) {
            return 0;
        }
        return shadowing.getZNear();
    }

    public void setShadowProjectionTransformationZNear(double zNear) {
        if (shadowing == null) {
            return;
        }
        logger.severe("setShadowProjectionTransformationZNear: " + zNear);
        shadowing.setZNear(zNear);
    }

    public double getShadowAlpha() {
        if (shadowing == null) {
            return 0;
        }
        return shadowing.getShadowAlpha();
    }

    public void setShadowAlpha(double shadowAlpha) {
        if (shadowing == null) {
            return;
        }
        shadowing.setShadowAlpha(shadowAlpha);
    }




}
