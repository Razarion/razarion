package com.btxtech.client;

import com.btxtech.client.renderer.engine.RenderService;
import com.btxtech.client.renderer.model.Normal;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.client.renderer.model.Shadow;
import com.btxtech.client.renderer.model.Shadowing;
import com.btxtech.client.renderer.model.ViewTransformation;
import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.client.utils.GradToRadConverter;
import com.google.gwt.user.client.ui.DoubleBox;
import org.jboss.errai.databinding.client.api.Bindable;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;

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
    private ViewTransformation viewTransformation;
    @Inject
    @Normal
    private ProjectionTransformation normalProjectionTransformation;
    @Inject
    @Shadow
    private ProjectionTransformation shadowProjectionTransformation;

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
        if (viewTransformation == null) {
            return 0;
        }
        return viewTransformation.getTranslateX();
    }

    public void setViewTransformationX(double viewTransformationX) {
        if (viewTransformation == null) {
            return;
        }
        viewTransformation.setTranslateX(viewTransformationX);
    }

    public double getViewTransformationY() {
        if (viewTransformation == null) {
            return 0;
        }
        return viewTransformation.getTranslateY();
    }

    public void setViewTransformationY(double viewTransformationY) {
        if (viewTransformation == null) {
            return;
        }
        viewTransformation.setTranslateY(viewTransformationY);
    }

    public double getViewTransformationZ() {
        if (viewTransformation == null) {
            return 0;
        }
        return viewTransformation.getTranslateZ();
    }

    public void setViewTransformationZ(double viewTransformationZ) {
        if (viewTransformation == null) {
            return;
        }
        viewTransformation.setTranslateZ(viewTransformationZ);
    }

    public double getViewTransformationRotateX() {
        if (viewTransformation == null) {
            return 0;
        }
        return viewTransformation.getRotateX();
    }

    public void setViewTransformationRotateX(double viewTransformationRotateX) {
        if (viewTransformation == null) {
            return;
        }
        viewTransformation.setRotateX(viewTransformationRotateX);
    }

    public double getViewTransformationRotateZ() {
        if (viewTransformation == null) {
            return 0;
        }
        return viewTransformation.getRotateZ();
    }

    public void setViewTransformationRotateZ(double viewTransformationRotateZ) {
        if (viewTransformation == null) {
            return;
        }
        viewTransformation.setRotateZ(viewTransformationRotateZ);
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

    public double getShadowProjectionTransformationZoom() {
        if (shadowProjectionTransformation == null) {
            return 0;
        }
        return shadowProjectionTransformation.getFovY();
    }

    public void setShadowProjectionTransformationZoom(double fovY) {
        if (shadowProjectionTransformation == null) {
            return;
        }
        shadowProjectionTransformation.setFovY(fovY);
    }

    public double getShadowProjectionTransformationZNear() {
        if (shadowProjectionTransformation == null) {
            return 0;
        }
        logger.severe("getShadowProjectionTransformationZNear: " + shadowProjectionTransformation.getZNear());
        return shadowProjectionTransformation.getZNear();
    }

    public void setShadowProjectionTransformationZNear(double zNear) {
        if (shadowProjectionTransformation == null) {
            return;
        }
        logger.severe("setShadowProjectionTransformationZNear: " + zNear);
        shadowProjectionTransformation.setZNear(zNear);
    }

    public double getShadowProjectionTransformationZFar() {
        if (shadowProjectionTransformation == null) {
            return 0;
        }
        return shadowProjectionTransformation.getZFar();
    }

    public void setShadowProjectionTransformationZFar(double zFar) {
        if (shadowProjectionTransformation == null) {
            return;
        }
        shadowProjectionTransformation.setZFar(zFar);
    }
}
