package com.btxtech.shared.gameengine.planet.bot;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.dto.AbstractBotCommandConfig;
import com.btxtech.shared.dto.KillBotCommandConfig;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.terrain.BabylonDecal;
import com.btxtech.shared.gameengine.planet.terrain.BotGround;
import com.btxtech.shared.gameengine.planet.terrain.BotGroundSlopeBox;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class BotService {
    private final Logger logger = Logger.getLogger(BotService.class.getName());
    private final Provider<BotRunner> botRunnerInstance;
    private final Collection<BotRunner> botRunners = new ArrayList<>();
    private Collection<BotConfig> botConfigs;

    @Inject
    public BotService(Provider<BotRunner> botRunnerInstance) {
        this.botRunnerInstance = botRunnerInstance;
    }

    static public List<BotGround> generateBotGrounds(List<BotConfig> botConfigs) {
        List<BotGround> botGrounds = new ArrayList<>();
        if (botConfigs != null) {
            botConfigs.stream().filter(botConfig -> botConfig.getGroundBoxModel3DEntityId() != null).forEach(botConfig -> {
                BotGround botGround = new BotGround();
                botGround.model3DId = botConfig.getGroundBoxModel3DEntityId();
                botGround.height = botConfig.getGroundBoxHeight() != null ? botConfig.getGroundBoxHeight() : 0;
                botGround.positions = botConfig.getGroundBoxPositions() != null ? botConfig.getGroundBoxPositions().stream().toArray(value -> new DecimalPosition[botConfig.getGroundBoxPositions().size()]) : null;
                botGround.botGroundSlopeBoxes = botConfig.getBotGroundSlopeBoxes() != null ? botConfig.getBotGroundSlopeBoxes().stream().toArray(value -> new BotGroundSlopeBox[botConfig.getBotGroundSlopeBoxes().size()]) : null;
                botGrounds.add(botGround);
            });
        }
        return botGrounds;
    }

    public void startBots(Collection<BotConfig> botConfigs) {
        this.botConfigs = botConfigs;
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
            logger.log(Level.SEVERE, t.getMessage(), t);
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
                logger.log(Level.SEVERE, t.getMessage(), t);
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
