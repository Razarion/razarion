package com.btxtech.uiservice.system.boot;

/**
 * User: Razarion contributors
 * Date: 18.02.2010
 * Time: 12:50:50
 */
public class DeferredFinishStartupTestTask extends AbstractStartupTask {

    @Override
    protected void privateStart(final DeferredStartup deferredStartup) {
        deferredStartup.setDeferred();
        deferredStartup.finished();
    }

}
