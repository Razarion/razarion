package com.btxtech.uiservice.cdimock;

import com.btxtech.uiservice.renderer.RenderService;
import com.btxtech.uiservice.renderer.RenderUnitControl;

import javax.enterprise.context.ApplicationScoped;

/**
 * Created by Beat
 * 24.01.2017.
 */
@ApplicationScoped
public class TestRenderService extends RenderService {
    @Override
    protected void internalSetup() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void prepareMainRendering() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void prepareDepthBufferRendering() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void prepare(RenderUnitControl renderUnitControl) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean depthTextureSupported() {
        throw new UnsupportedOperationException();
    }
}
