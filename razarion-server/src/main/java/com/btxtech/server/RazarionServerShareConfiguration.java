package com.btxtech.server;

import com.btxtech.shared.gameengine.InitializeService;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.planet.bot.BotService;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.alarm.AlarmService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RazarionServerShareConfiguration {

    @Bean
    public AlarmService alarmService() {
        return new AlarmService();
    }

    @Bean
    public ExceptionHandler exceptionHandler(AlarmService alarmService) {
        return new ExceptionHandler(alarmService) {
            @Override
            protected void handleExceptionInternal(String message, Throwable t) {

            }
        };
    }

    @Bean
    public InitializeService initializeService() {
        return new InitializeService();
    }

    @Bean
    public BotService botService(ExceptionHandler exceptionHandler/* X,
                                TODO Provider<BotRunner> botRunnerInstance*/) {
        return new BotService(exceptionHandler, null); // TODO replace NUK
    }

    @Bean
    public TerrainTypeService terrainTypeService(InitializeService initializeService) {
        return new TerrainTypeService(initializeService);
    }
}
