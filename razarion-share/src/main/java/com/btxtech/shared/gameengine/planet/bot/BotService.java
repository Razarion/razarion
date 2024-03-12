package com.btxtech.shared.gameengine.planet.bot;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.AbstractBotCommandConfig;
import com.btxtech.shared.dto.KillBotCommandConfig;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.planet.GameLogicService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.system.ExceptionHandler;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * User: beat
 * Date: 10.10.2011
 * Time: 13:30:00
 */
@Singleton
public class BotService {
    private final Logger logger = Logger.getLogger(BotService.class.getName());
    @Inject
    private Instance<BotRunner> botRunnerInstance;
    @Inject
    private ExceptionHandler exceptionHandler;
    private final Collection<BotRunner> botRunners = new ArrayList<>();

    public void startBots(Collection<BotConfig> botConfigs) {
        if (botConfigs != null) {
            botConfigs.forEach(botConfig -> {
                try {
                    startBot(botConfig);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Starting bot failed: " + botConfig, e);
                }
            });
        }
    }

    public BotRunner startBot(BotConfig botConfig) {
        BotRunner botRunner = botRunnerInstance.get();
        synchronized (botRunners) {
            botRunners.add(botRunner);
        }
        botRunner.start(botConfig);
        return botRunner;
    }

    public void killAllBots() {
        synchronized (botRunners) {
            botRunners.forEach(BotRunner::kill);
            botRunners.clear();
        }
    }

    private void killBot(int botId) {
        BotRunner botRunner = getBotRunner(botId);
        botRunner.kill();
        synchronized (botRunners) {
            botRunners.remove(botRunner);
        }
    }

    public void onKill(SyncBaseItem target, PlayerBase actor) {
        try {
            if (target.getBase().getCharacter().isBot()) {
                BotRunner botRunner = getBotRunner(target.getBase());
                if (botRunner != null) {
                    botRunner.enrageOnKill(target, actor);
                }
            }
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    public boolean isInRealm(DecimalPosition position) {
        synchronized (botRunners) {
            for (BotRunner botRunner : botRunners) {
                if (botRunner.isInRealm(position)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void executeCommands(List<? extends AbstractBotCommandConfig> botCommandConfigs) {
        for (AbstractBotCommandConfig botCommandConfig : botCommandConfigs) {
            try {
                BotRunner botRunner = getBotRunner4AuxiliaryId(botCommandConfig.getBotAuxiliaryId());
                if (botCommandConfig instanceof KillBotCommandConfig) {
                    killBot(botRunner.getBotConfig().getId());
                } else {
                    botRunner.executeCommand(botCommandConfig);
                }
            } catch (Throwable t) {
                exceptionHandler.handleException(t);
            }
        }
    }

    public BotRunner getBotRunner(int botId) {
        synchronized (botRunners) {
            for (BotRunner botRunner : botRunners) {
                if (botRunner.getBotConfig().getId() == botId) {
                    return botRunner;
                }
            }
        }
        throw new IllegalArgumentException("No bot runner for id: " + botId);
    }

    public BotRunner getBotRunner4AuxiliaryId(Integer auxiliaryId) {
        if (auxiliaryId == null) {
            throw new IllegalArgumentException("AuxiliaryId is null");
        }
        synchronized (botRunners) {
            for (BotRunner botRunner : botRunners) {
                if (botRunner.getBotConfig().getAuxiliaryId() != null && botRunner.getBotConfig().getAuxiliaryId() == auxiliaryId.intValue()) {
                    return botRunner;
                }
            }
        }
        throw new IllegalArgumentException("No bot runner for auxiliaryId: " + auxiliaryId);
    }

    private BotRunner getBotRunner(PlayerBase playerBase) {
        synchronized (botRunners) {
            for (BotRunner botRunner : botRunners) {
                if (playerBase.equals(botRunner.getBase())) {
                    return botRunner;
                }
            }
        }
        return null;
    }

    public void onBotSyncBaseItemCreated(SyncBaseItem syncBaseItem, SyncBaseItem createdBy) {
        synchronized (botRunners) {
            for (BotRunner botRunner : botRunners) {
                if (botRunner.onSyncBaseItemCreated(syncBaseItem, createdBy)) {
                    return;
                }
            }
        }
    }
}
