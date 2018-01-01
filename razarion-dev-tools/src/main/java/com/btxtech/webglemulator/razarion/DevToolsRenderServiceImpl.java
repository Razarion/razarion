package com.btxtech.webglemulator.razarion;

import com.btxtech.uiservice.renderer.RenderService;
import com.btxtech.uiservice.renderer.RenderUnitControl;
import com.btxtech.webglemulator.WebGlEmulatorController;
import com.btxtech.webglemulator.WebGlEmulatorShadowController;
import com.btxtech.webglemulator.webgl.WebGlEmulator;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Created by Beat
 * 12.07.2016.
 */
@ApplicationScoped
public class DevToolsRenderServiceImpl extends RenderService {
    @Inject
    private WebGlEmulator webGlEmulator;
    @Inject
    private WebGlEmulatorController webGlEmulatorController;
    @Inject
    private WebGlEmulatorShadowController shadowController;

    @Override
    protected void internalSetup() {

    }

    @Override
    protected void prepareMainRendering() {
        webGlEmulator.setCanvas(webGlEmulatorController.getCanvas());
        webGlEmulator.clear();
    }

    @Override
    protected void prepareDepthBufferRendering() {
        if (shadowController.isActive()) {
            webGlEmulator.setCanvas(shadowController.getCanvas());
            webGlEmulator.clear();
        } else {
            webGlEmulator.setCanvas(null);
        }
    }

    @Override
    protected void prepare(RenderUnitControl renderUnitControl) {

    }

    @Override
    public boolean depthTextureSupported() {
        return true;
    }
}
