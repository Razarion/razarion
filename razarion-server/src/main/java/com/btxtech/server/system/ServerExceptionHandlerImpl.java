package com.btxtech.server.system;

import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.alarm.AlarmService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 28.06.2016.
 */
@Singleton
public class ServerExceptionHandlerImpl extends ExceptionHandler {
    @Inject
    private Logger logger;

    @Inject
    public ServerExceptionHandlerImpl(AlarmService alarmService) {
        super(alarmService);
    }

    @Override
    protected void handleExceptionInternal(String message, Throwable t) {
        logger.log(Level.SEVERE, message, t);
    }
}
