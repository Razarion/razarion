package com.btxtech.client.system.boot;

import com.btxtech.uiservice.AssetService;
import com.btxtech.uiservice.renderer.BabylonRendererService;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.DeferredStartup;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 25.01.2017.
 */
@Dependent
public class InitRendererTask extends AbstractStartupTask {

    private BabylonRendererService threeJsRendererService;

    private AssetService assetService;

    @Inject
    public InitRendererTask(AssetService assetService, BabylonRendererService threeJsRendererService) {
        this.assetService = assetService;
        this.threeJsRendererService = threeJsRendererService;
    }

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        threeJsRendererService.runRenderer(assetService.getMeshContainers());
    }
}
