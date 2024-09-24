package com.btxtech.uiservice.cdimock;

import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.alarm.AlarmService;
import com.btxtech.uiservice.system.boot.AbstractStartupTask;
import com.btxtech.uiservice.system.boot.Boot;
import com.btxtech.uiservice.system.boot.StartupSeq;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Created by Beat
 * 25.04.2017.
 */
@Singleton
public class TestBootImpl extends Boot {
    @Inject
    public TestBootImpl(AlarmService alarmService, ExceptionHandler exceptionHandler, Provider<AbstractStartupTask> taskInstance) {
        super(alarmService, exceptionHandler, taskInstance);
    }

    @Override
    protected StartupSeq getWarm() {
        throw new UnsupportedOperationException();
    }
}
