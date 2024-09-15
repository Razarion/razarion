package com.btxtech.common.system;

import com.btxtech.shared.rest.LoggingProvider;
import com.btxtech.shared.system.debugtool.DebugHelper;
import elemental2.dom.DomGlobal;
import com.btxtech.client.Caller;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * on 13.02.2018.
 */
@Singleton
public class ClientDebugHelper implements DebugHelper {
    // private Logger logger = Logger.getLogger(ClientDebugHelper.class.getName());
    @Inject
    private Caller<LoggingProvider> caller;
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;

    @Override
    public void debugToDb(String debugMessage) {
        caller.call(ignore -> {
                }, exceptionHandler.restErrorHandler("LoggingProvider.jsonDebugDbLogger()")
        ).jsonDebugDbLogger(debugMessage);
    }

    @Override
    public void debugToConsole(String debugMessage) {
        // DomGlobal.console.error(debugMessage); // Shows stack trace
        DomGlobal.console.log(debugMessage);
        // DomGlobal.console.warn("warn"); // Shows stack trace
        // DomGlobal.console.trace("trace"); // Shows stack trace
        // DomGlobal.console.debug("debug"); // Hidden per default
        // DomGlobal.console.info("info");
    }
}
