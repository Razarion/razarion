package com.btxtech.client.system.boot;

import com.btxtech.client.system.LifecycleService;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.DeferredStartup;
import com.btxtech.uiservice.user.UserUiService;

import javax.inject.Inject;

/**
 * Created by Beat
 * 25.04.2017.
 */

public class CleanGameTask extends AbstractStartupTask {

    private LifecycleService lifecycleService;

    private UserUiService userUiService;

    @Inject
    public CleanGameTask(UserUiService userUiService, LifecycleService lifecycleService) {
        this.userUiService = userUiService;
        this.lifecycleService = lifecycleService;
    }

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        deferredStartup.setDeferred();
        userUiService.stop();
        lifecycleService.clearAndHold(deferredStartup);
    }
}
