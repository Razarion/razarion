package com.btxtech.client.system.boot;

import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.DeferredStartup;

import javax.inject.Inject;

/**
 * Created by Beat
 * 06.03.2017.
 */

public class LoadStartJsTask extends AbstractStartupTask {

    private ExceptionHandler exceptionHandler;

    @Inject
    public LoadStartJsTask(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        try {
            overrideStartTime((long) getNativeCtlStartTime());
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
        }
    }

    private native double getNativeCtlStartTime() /*-{
        if (typeof ($wnd.RAZ_startTime) === 'undefined') {
            throw "RAZ_startTime not defined"
        }
        if (typeof ($wnd.RAZ_startTime) !== "number") {
            throw "RAZ_startTime is not a number. Is is: " + typeof ($wnd.RAZ_startTime) + " value: " + $wnd.RAZ_startTime;
        }
        return $wnd.RAZ_startTime;
    }-*/;

}
