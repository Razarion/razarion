package com.btxtech.client.system.boot;

import com.btxtech.client.imageservice.ImageUiService;
import com.btxtech.uiservice.Shape3DUiService;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.DeferredStartup;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.Set;

/**
 * Created by Beat
 * 25.01.2017.
 */
@Dependent
public class LoadMediaControlTask extends AbstractStartupTask {
    @Inject
    private Shape3DUiService shape3DUiService;
    @Inject
    private ImageUiService imageUiService;
    @Inject
    private GameUiControl gameUiControl;

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        deferredStartup.setDeferred();
        deferredStartup.setBackground();

        Set<Integer> allTextureIds = gameUiControl.getAllTextureIds();
        allTextureIds.addAll(gameUiControl.getAllBumpTextureIds());

        if (allTextureIds.isEmpty()) {
            deferredStartup.finished();
        } else {
            imageUiService.preloadImages(allTextureIds, deferredStartup);
        }
    }
}
