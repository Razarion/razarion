package com.btxtech.shared.gameengine.planet.bot;

import com.btxtech.shared.cdimock.TestSimpleScheduledFuture;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.planet.basic.BaseBasicTest;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.system.SimpleExecutorService;

import java.util.List;

/**
 * Created by Beat
 * 03.05.2017.
 */
public class BaseBotServiceTest extends BaseBasicTest {
    private TestSimpleScheduledFuture botScheduledFuture;

    protected void startBots(List<BotConfig> botConfigs) {
        getBotService().startBots(botConfigs);
        botScheduledFuture = getTestSimpleExecutorService().getScheduleAtFixedRate(SimpleExecutorService.Type.BOT_TICKER);
    }

    protected void tickBotRunner() {
        botScheduledFuture.invokeRun();
    }

    protected void tickBotSceneRunner() {
        getTestSimpleExecutorService().getScheduleAtFixedRate(SimpleExecutorService.Type.BOT_SCENE_TICKER).invokeRun();
    }

    protected SyncBaseItem findFirstBotItemHighestId(int botId, int baseItemTypeId) {
        PlayerBaseFull botBase = (PlayerBaseFull) getBotBase(botId);
        return findSyncBaseItemHighestId(botBase, baseItemTypeId);
    }
}
