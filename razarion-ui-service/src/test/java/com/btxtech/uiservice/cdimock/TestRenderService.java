package com.btxtech.uiservice.cdimock;

import com.btxtech.uiservice.renderer.RenderService;

import javax.enterprise.context.ApplicationScoped;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 24.01.2017.
 */
@ApplicationScoped
public class TestRenderService extends RenderService {
    private Logger logger = Logger.getLogger(TestRenderService.class.getName());

    @Override
    protected void internalSetup() {
        logger.fine("internalSetup()");
    }

    @Override
    protected void prepareMainRendering() {
        logger.fine("prepareMainRendering()");
    }

    @Override
    protected void prepareDepthBufferRendering() {
        logger.fine("prepareDepthBufferRendering()");
    }

    @Override
    protected void prepare() {
        logger.fine("prepare()");
    }
}
