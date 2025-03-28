package com.btxtech.server;

import com.btxtech.shared.system.alarm.AlarmService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RazarionServerConfiguration {

    @Bean
    public AlarmService alarmService() {
        return new AlarmService();
    }
}
