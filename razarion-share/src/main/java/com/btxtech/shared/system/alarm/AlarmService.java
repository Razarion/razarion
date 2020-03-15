package com.btxtech.shared.system.alarm;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class AlarmService {

    private List<Alarm> alarms = new ArrayList<>();

    public void riseAlarm(Alarm.Type type) {
        alarms.add(new Alarm(type));
    }

    public List<Alarm> getAlarms() {
        return alarms;
    }
}
