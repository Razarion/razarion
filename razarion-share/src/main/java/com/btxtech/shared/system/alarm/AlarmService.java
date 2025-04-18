package com.btxtech.shared.system.alarm;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Singleton
public class AlarmService {
    private final List<Alarm> alarms = new ArrayList<>();
    private final List<AlarmServiceListener> listeners = new ArrayList<>();

    @Inject
    public AlarmService() {
    }

    public void riseAlarm(Alarm.Type type) {
        riseAlarm(type, null, null);
    }

    public void riseAlarm(Alarm.Type type, String text) {
        riseAlarm(type, text, null);
    }

    public void riseAlarm(Alarm.Type type, Integer id) {
        riseAlarm(type, null, id);
    }

    public void riseAlarm(Alarm.Type type, String text, Integer id) {
        Alarm alarm = new Alarm().type(type).date(new Date()).text(text).id(id);
        alarms.add(alarm);
        fire(alarm);
    }

    public void riseAlarm(AlarmRaisedException alarmRaisedException) {
        riseAlarm(alarmRaisedException.getType(),
                alarmRaisedException.getText(),
                alarmRaisedException.getId());
    }

    public List<Alarm> getAlarms() {
        return alarms;
    }

    public void addListener(AlarmServiceListener alarmServiceListener) {
        listeners.add(alarmServiceListener);
    }

    public void removeListener(AlarmServiceListener alarmServiceListener) {
        listeners.remove(alarmServiceListener);
    }

    private void fire(Alarm alarm) {
        listeners.forEach(alarmServiceListener -> alarmServiceListener.alarmRaised(alarm));
    }

    public boolean hasAlarms() {
        return !alarms.isEmpty();
    }
}
