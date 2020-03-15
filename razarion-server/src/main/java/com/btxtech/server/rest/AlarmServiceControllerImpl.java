package com.btxtech.server.rest;

import com.btxtech.server.user.SecurityCheck;
import com.btxtech.shared.rest.AlarmServiceController;
import com.btxtech.shared.system.alarm.Alarm;
import com.btxtech.shared.system.alarm.AlarmService;

import javax.inject.Inject;
import java.util.List;

public class AlarmServiceControllerImpl implements AlarmServiceController {
    @Inject
    private AlarmService alarmService;

    @Override
    @SecurityCheck
    public List<Alarm> getAlarms() {
        return alarmService.getAlarms();
    }
}
