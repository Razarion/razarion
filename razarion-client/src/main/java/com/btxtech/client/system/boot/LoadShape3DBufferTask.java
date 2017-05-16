package com.btxtech.client.system.boot;

import com.btxtech.client.shape3d.ClientShape3DUiService;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.DeferredStartup;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 07.03.2016.
 */
@Dependent
public class LoadShape3DBufferTask extends AbstractStartupTask {
    @Inject
    private ClientShape3DUiService clientShape3DUiService;

    @Override
    protected void privateStart(final DeferredStartup deferredStartup) {
        deferredStartup.setDeferred();
        deferredStartup.setBackground();
        clientShape3DUiService.loadBuffer(deferredStartup);
    }
}
