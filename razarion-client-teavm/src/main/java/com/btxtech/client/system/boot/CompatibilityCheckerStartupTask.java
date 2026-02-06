package com.btxtech.client.system.boot;

import com.btxtech.client.jso.JsConsole;
import com.btxtech.client.jso.JsWindow;
import com.btxtech.shared.Constants;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.BootContext;
import com.btxtech.uiservice.system.boot.DeferredStartup;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

public class CompatibilityCheckerStartupTask extends AbstractStartupTask {
    private static final int RELOAD_DELAY = 2000;
    private final BootContext bootContext;

    public CompatibilityCheckerStartupTask(BootContext bootContext) {
        this.bootContext = bootContext;
    }

    @JSFunctor
    private interface StringCallback extends JSObject {
        void call(String value);
    }

    @JSBody(params = {"onSuccess", "onError"}, script =
            "fetch('/rest/servermgmt/interfaceVersion')" +
            ".then(function(response) { if (response.ok) { return response.text(); } else { throw new Error('HTTP ' + response.status); } })" +
            ".then(function(text) { onSuccess(text); })" +
            ".catch(function(error) { onError('' + error); });")
    private static native void fetchInterfaceVersion(StringCallback onSuccess, StringCallback onError);

    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        deferredStartup.setDeferred();
        fetchInterfaceVersion(
                text -> {
                    try {
                        int interfaceVersion = Integer.parseInt(text.trim());
                        if (Constants.INTERFACE_VERSION == interfaceVersion) {
                            deferredStartup.finished();
                        } else {
                            JsConsole.error("Wrong client interface version: " + Constants.INTERFACE_VERSION
                                    + ". Server: " + interfaceVersion);
                            bootContext.getSimpleExecutorService().schedule(RELOAD_DELAY,
                                    JsWindow::reload,
                                    SimpleExecutorService.Type.RELOAD_CLIENT_WRONG_INTERFACE_VERSION);
                        }
                    } catch (Throwable t) {
                        JsConsole.warn("CompatibilityChecker parse failed: " + t.getMessage());
                        deferredStartup.finished();
                    }
                },
                error -> {
                    JsConsole.warn("CompatibilityChecker fetch failed: " + error);
                    JsConsole.warn("Make sure backend is running on http://127.0.0.1:8080");
                    JsConsole.warn("Continuing anyway...");
                    deferredStartup.finished();
                }
        );
    }
}
