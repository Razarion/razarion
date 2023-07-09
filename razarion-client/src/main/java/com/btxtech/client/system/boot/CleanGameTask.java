package com.btxtech.client.system.boot;

import com.btxtech.client.system.LifecycleService;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.DeferredStartup;
import com.btxtech.uiservice.user.UserUiService;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Created by Beat
 * 25.04.2017.
 */
@Dependent
public class CleanGameTask extends AbstractStartupTask {
    @Inject
    private LifecycleService lifecycleService;
    @Inject
    private UserUiService userUiService;

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        deferredStartup.setDeferred();
        userUiService.stop();
        lifecycleService.clearAndHold(deferredStartup);
    }
}
