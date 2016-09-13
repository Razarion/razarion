package com.btxtech.shared.gameengine.planet.bot;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.BotMoveCommandConfig;
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
import java.util.List;
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
        synchronized (botRunners) {
            botRunners.put(botConfig, botRunner);
        }
        botRunner.start(botConfig);
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

    public BotRunner getBotRunner(int botId) {
        for (Map.Entry<BotConfig, BotRunner> entry : botRunners.entrySet()) {
            if (entry.getKey().getId() == botId) {
                return entry.getValue();
            }
        }
        throw new IllegalArgumentException("No bot runner for id: " + botId);
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

    public void executeCommands(List<BotMoveCommandConfig> botMoveCommandConfigs) {
        for (BotMoveCommandConfig botMoveCommandConfig : botMoveCommandConfigs) {
            BotRunner botRunner = getBotRunner(botMoveCommandConfig.getBotId());
            botRunner.executeCommand(botMoveCommandConfig);
        }

    }
}
