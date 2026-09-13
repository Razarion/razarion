package com.btxtech.uiservice.system.boot;

import jakarta.inject.Inject;

/**
 * User: Razarion contributors
 * Date: 18.02.2010
 * Time: 12:50:50
 */
public class DeferredBackgroundStartupTestTask extends AbstractStartupTask {

    private StartupTestTaskMonitor startupTestTaskMonitor;
    private DeferredStartup deferredStartup;

    @Inject
    public DeferredBackgroundStartupTestTask(StartupTestTaskMonitor startupTestTaskMonitor) {
        this.startupTestTaskMonitor = startupTestTaskMonitor;
    }

    @Override
    protected void privateStart(final DeferredStartup deferredStartup) {
        this.deferredStartup = deferredStartup;
        deferredStartup.setDeferred();
        deferredStartup.setBackground();
        startupTestTaskMonitor.addStartupTestTaskMonitor(this);
    }

    public void finished() {
        deferredStartup.finished();
    }

    public void failed(Throwable throwable) {
        deferredStartup.failed(throwable);
    }
}
