package com.btxtech.shared.system;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class AlarmService {
    private List<String> alarms = new ArrayList<>();

    public void riseAlarm(String alarm) {
        alarms.add(alarm);
    }

    public List<String> getAlarms() {
        return alarms;
    }
}
