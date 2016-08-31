package com.btxtech.shared.gameengine.planet.bot;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.BotSyncBaseItemCreatedEvent;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: beat
 * Date: 10.10.2011
 * Time: 13:30:00
 */
@Singleton
public class BotService {
    private Logger logger = Logger.getLogger(BotService.class.getName());
    @Inject
    private Instance<BotRunner> botRunnerInstance;
    private final Map<BotConfig, BotRunner> botRunners = new HashMap<>();

    public void startBots(Collection<BotConfig> botConfigs) {
        for (BotConfig botConfig : botConfigs) {
            try {
                startBot(botConfig);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Starting bot failed: " + botConfig.getName(), e);
            }
        }
    }

    private void startBot(BotConfig botConfig) {
        BotRunner botRunner = botRunnerInstance.get();
        botRunner.start(botConfig);
        synchronized (botRunners) {
            botRunners.put(botConfig, botRunner);
        }
    }

    protected void killAllBots() {
        // Kill all bots
        synchronized (botRunners) {
            for (BotRunner botRunner : botRunners.values()) {
                botRunner.kill();
            }
        }
        botRunners.clear();
    }

    public void killBot(int botId) {
        synchronized (botRunners) {
            for (Iterator<Map.Entry<BotConfig, BotRunner>> iterator = botRunners.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<BotConfig, BotRunner> entry = iterator.next();
                if (entry.getKey().getId() == botId) {
                    entry.getValue().kill();
                    iterator.remove();
                    break;
                }
            }
        }
    }

    public BotRunner getBotRunner(BotConfig botConfig) {
        return botRunners.get(botConfig);
    }

    // TODO must be called from outside
    public void onBotItemKilled(SyncBaseItem syncBaseItem, PlayerBase actor) {
        synchronized (botRunners) {
            for (BotRunner botRunner : botRunners.values()) {
                botRunner.onBotItemKilled(syncBaseItem, actor);
            }
        }
    }

    public boolean isInRealm(DecimalPosition position) {
        synchronized (botRunners) {
            for (BotRunner botRunner : botRunners.values()) {
                if (botRunner.isInRealm(position)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void onBotSyncBaseItemCreatedEvent(@Observes BotSyncBaseItemCreatedEvent event) {
        synchronized (botRunners) {
            for (BotRunner botRunner : botRunners.values()) {
                if (botRunner.getBase() != null && botRunner.getBase().equals(event.getSyncBaseItem().getBase())) {
                    botRunner.onSyncBaseItemCreated(event.getSyncBaseItem(), event.getCreatedBy());
                    return;
                }
            }
        }
    }

}
