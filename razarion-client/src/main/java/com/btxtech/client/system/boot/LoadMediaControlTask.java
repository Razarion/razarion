package com.btxtech.client.system.boot;

import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.DeferredStartup;

import javax.enterprise.context.Dependent;

/**
 * Created by Beat
 * 25.01.2017.
 */
@Dependent
public class LoadMediaControlTask extends AbstractStartupTask {

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        deferredStartup.setDeferred();
        deferredStartup.setBackground();
        deferredStartup.finished();
    }
}
