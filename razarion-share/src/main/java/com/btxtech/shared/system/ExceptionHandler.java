package com.btxtech.shared.system;

import com.btxtech.shared.system.alarm.AlarmRaisedException;
import com.btxtech.shared.system.alarm.AlarmService;

/**
 * Created by Beat
 * 24.06.2016.
 */
@Deprecated
public abstract class ExceptionHandler {

    private final AlarmService alarmService;

    public ExceptionHandler(AlarmService alarmService) {
        this.alarmService = alarmService;
    }

    protected abstract void handleExceptionInternal(String message, Throwable t);

    public void handleException(Throwable throwable) {
        handleException(null, throwable);
    }

    public void handleException(String message, Throwable throwable) {
        if (throwable instanceof AlarmRaisedException) {
            alarmService.riseAlarm((AlarmRaisedException) throwable);
        }
        handleExceptionInternal(message, throwable);
    }
}