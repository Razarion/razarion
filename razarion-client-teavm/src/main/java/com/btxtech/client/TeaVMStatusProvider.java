package com.btxtech.client;

import com.btxtech.shared.system.alarm.Alarm;
import com.btxtech.shared.system.alarm.AlarmService;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class TeaVMStatusProvider {

    private final AlarmService alarmService;

    @Inject
    public TeaVMStatusProvider(AlarmService alarmService) {
        this.alarmService = alarmService;
    }

    public Alarm[] getClientAlarms() {
        return alarmService.getAlarms().toArray(new Alarm[0]);
    }
}
