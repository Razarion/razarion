package com.btxtech.client.menu;

import com.btxtech.client.renderer.engine.RenderService;
import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.model.Lighting;
import com.btxtech.client.renderer.model.Normal;
import com.btxtech.client.renderer.model.ProjectionTransformation;
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
    private RenderService renderService;
    @Inject
    private Camera camera;
    @Inject
    private Lighting lighting;

    public void setWireMode(boolean wireMode) {
        if (renderService == null) {
            return;
        }
        renderService.showWire(wireMode);
    }

    public boolean getWireMode() {
        return renderService != null && renderService.isWire();
    }

    public void setShowMonitor(boolean showMonitor) {
        if (renderService == null) {
            return;
        }
        renderService.setShowMonitor(showMonitor);
    }

    public boolean getShowMonitor() {
        return renderService != null && renderService.isShowMonitor();
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

    public double getBumpMapDepth() {
        if (lighting == null) {
            return 0;
        }
        return lighting.getBumpMapDepth();
    }

    public void setBumpMapDepth(double bumpMapDepth) {
        if (lighting == null) {
            return;
        }
        lighting.setBumpMapDepth(bumpMapDepth);
    }
}
