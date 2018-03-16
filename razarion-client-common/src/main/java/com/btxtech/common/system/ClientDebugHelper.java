package com.btxtech.common.system;

import com.btxtech.shared.rest.LoggingProvider;
import com.btxtech.shared.system.debugtool.DebugHelper;
import org.jboss.errai.common.client.api.Caller;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 13.02.2018.
 */
@Singleton
public class ClientDebugHelper implements DebugHelper {
    private Logger logger = Logger.getLogger(ClientDebugHelper.class.getName());
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
}
