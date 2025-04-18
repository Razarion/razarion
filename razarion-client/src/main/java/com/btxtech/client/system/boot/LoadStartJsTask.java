package com.btxtech.client.system.boot;

import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.Boot;
import com.btxtech.uiservice.system.boot.DeferredStartup;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 06.03.2017.
 */

public class LoadStartJsTask extends AbstractStartupTask {
    private final Logger logger = Logger.getLogger(Boot.class.getName());

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        try {
            overrideStartTime((long) getNativeCtlStartTime());
        } catch (Throwable throwable) {
            logger.log(Level.SEVERE, "LoadStartJsTask overrideStartTime failed", throwable);
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
