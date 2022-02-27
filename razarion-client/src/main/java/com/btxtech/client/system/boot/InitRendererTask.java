package com.btxtech.client.system.boot;

import com.btxtech.client.gwtangular.GwtAngularService;
import com.btxtech.client.renderer.webgl.WebGLTextureContainer;
import com.btxtech.uiservice.renderer.ThreeJsRendererService;
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
    @Inject
    private GwtAngularService gwtAngularService;

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        // Injection does not work here
        //gwtAngularService.getThreeJsRendererService().init();
    }
}
