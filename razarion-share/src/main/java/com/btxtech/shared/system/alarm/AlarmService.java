package com.btxtech.shared.system.alarm;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ApplicationScoped
public class AlarmService {
    private List<Alarm> alarms = new ArrayList<>();
    private List<AlarmServiceListener> listeners = new ArrayList<>();

    public void riseAlarm(Alarm.Type type) {
        Alarm alarm = new Alarm().type(type).date(new Date());
        alarms.add(alarm);
        fire(alarm);
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

}
