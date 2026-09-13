package com.btxtech.uiservice.system.boot;

import jakarta.inject.Inject;

/**
 * User: Razarion contributors
 * Date: 18.02.2010
 * Time: 12:50:50
 */
public class DeferredStartupTestTask extends AbstractStartupTask {

    private StartupTestTaskMonitor startupTestTaskMonitor;
    protected DeferredStartup deferredStartup;

    @Inject
    public DeferredStartupTestTask(StartupTestTaskMonitor startupTestTaskMonitor) {
        this.startupTestTaskMonitor = startupTestTaskMonitor;
    }

    @Override
    protected void privateStart(final DeferredStartup deferredStartup) {
        this.deferredStartup = deferredStartup;
        deferredStartup.setDeferred();
        startupTestTaskMonitor.addDeferredStartupTestTask(this);
    }

    public void finished() {
        deferredStartup.finished();
    }

    public void failed(String error) {
        deferredStartup.failed(error);
    }

    public void failed(Throwable throwable) {
        deferredStartup.failed(throwable);
    }
}
