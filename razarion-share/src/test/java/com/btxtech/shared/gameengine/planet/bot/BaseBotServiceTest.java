package com.btxtech.shared.gameengine.planet.bot;

import com.btxtech.shared.cdimock.TestSimpleScheduledFuture;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.planet.WeldMasterBaseTest;
import com.btxtech.shared.system.SimpleExecutorService;

import java.util.List;

/**
 * Created by Beat
 * 03.05.2017.
 */
public class BaseBotServiceTest extends WeldMasterBaseTest {
    private TestSimpleScheduledFuture botScheduledFuture;

    protected void startBot(List<BotConfig> botConfigs) {
        getWeldBean(BotService.class).startBots(botConfigs);
        botScheduledFuture = getTestSimpleExecutorService().getScheduleAtFixedRate(SimpleExecutorService.Type.BOT);
    }

    protected void tickBotRunner() {
        botScheduledFuture.invokeRun();
    }
}
