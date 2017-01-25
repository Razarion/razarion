package com.btxtech.uiservice.system.boot;

import com.btxtech.client.imageservice.ImageUiService;
import com.btxtech.shared.utils.Shape3DUtils;
import com.btxtech.uiservice.Shape3DUiService;
import com.btxtech.uiservice.control.GameUiControl;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.Collection;
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

        Collection<Integer> textureIds = Shape3DUtils.getAllTextures(gameUiControl.getGameUiControlConfig().getVisualConfig().getShape3Ds());
        if (textureIds.isEmpty()) {
            deferredStartup.finished();
        } else {
            imageUiService.preloadImages(textureIds, deferredStartup);
        }
    }
}
