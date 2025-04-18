package com.btxtech.uiservice.system.boot;

import com.btxtech.shared.system.alarm.Alarm;
import com.btxtech.shared.system.alarm.AlarmRaiser;

public class AlarmRaisingTestTask extends AbstractStartupTask {
    @Override
    protected void privateStart(DeferredStartup deferredStartup) {
        AlarmRaiser.onNull(null, Alarm.Type.NO_WARM_GAME_UI_CONTEXT, "Test boot alarm", 124);
    }
}
