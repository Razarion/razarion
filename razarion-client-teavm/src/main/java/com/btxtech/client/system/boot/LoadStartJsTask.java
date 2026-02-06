package com.btxtech.client.system.boot;

import com.btxtech.client.jso.JsConsole;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.DeferredStartup;
import org.teavm.jso.JSBody;

public class LoadStartJsTask extends AbstractStartupTask {

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        try {
            overrideStartTime((long) getNativeCtlStartTime());
        } catch (Throwable throwable) {
            JsConsole.warn("LoadStartJsTask overrideStartTime failed: " + throwable.getMessage());
        }
    }

    @JSBody(script = "if (typeof window.RAZ_startTime === 'undefined') { throw 'RAZ_startTime not defined'; } " +
            "if (typeof window.RAZ_startTime !== 'number') { throw 'RAZ_startTime is not a number'; } " +
            "return window.RAZ_startTime;")
    private static native double getNativeCtlStartTime();
}
