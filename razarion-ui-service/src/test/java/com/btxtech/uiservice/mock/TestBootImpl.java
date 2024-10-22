package com.btxtech.uiservice.mock;

import com.btxtech.shared.system.alarm.AlarmService;
import com.btxtech.uiservice.system.boot.Boot;
import com.btxtech.uiservice.system.boot.BootContext;
import com.btxtech.uiservice.system.boot.StartupSeq;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * 25.04.2017.
 */
@Singleton
public class TestBootImpl extends Boot {

    @Inject
    public TestBootImpl(AlarmService alarmService) {
        super(alarmService);
    }

    @Override
    protected StartupSeq getWarm() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected BootContext createBootContext() {
        return null;
    }
}
