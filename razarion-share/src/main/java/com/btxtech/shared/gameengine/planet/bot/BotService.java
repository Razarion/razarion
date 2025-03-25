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
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: beat
 * Date: 10.10.2011
 * Time: 13:30:00
 */
@Singleton
public class BotService {
    private final Logger logger = Logger.getLogger(BotService.class.getName());

    private final Provider<BotRunner> botRunnerInstance;

    private final ExceptionHandler exceptionHandler;
    private final Collection<BotRunner> botRunners = new ArrayList<>();
    private Collection<BotConfig> botConfigs;

    @Inject
    public BotService(ExceptionHandler exceptionHandler, Provider<com.btxtech.shared.gameengine.planet.bot.BotRunner> botRunnerInstance) {
        this.exceptionHandler = exceptionHandler;
        this.botRunnerInstance = botRunnerInstance;
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

    static public List<BabylonDecal> generateBotDecals(List<BotConfig> botConfigs) {
        List<BabylonDecal> babylonDecals = new ArrayList<>();
        if (botConfigs != null) {
            botConfigs.forEach(botConfig -> {
                if (botConfig.getGroundBabylonMaterialId() != null && botConfig.getRealm() != null) {
                    PlaceConfig placeConfig = botConfig.getRealm();
                    if (placeConfig.getPolygon2D() != null) {
                        Rectangle2D aabb = placeConfig.getPolygon2D().toAabb();
                        babylonDecals.add(createBaseBabylonDecal(botConfig,
                                aabb.center(),
                                aabb.width(),
                                aabb.height()));
                    } else if (placeConfig.getPosition() != null) {
                        if (placeConfig.getRadius() != null) {
                            babylonDecals.add(createBaseBabylonDecal(botConfig,
                                    placeConfig.getPosition(),
                                    placeConfig.getRadius() * 2,
                                    placeConfig.getRadius() * 2));
                        }
                    } else {
                        throw new IllegalArgumentException("Illegal PlaceConfig: to find a random place, a polygon or a position must be set");
                    }

                }
            });
        }
        return babylonDecals;
    }

    private static BabylonDecal createBaseBabylonDecal(BotConfig botConfig, DecimalPosition center, double width, double height) {
        BabylonDecal babylonDecal = new BabylonDecal();
        babylonDecal.babylonMaterialId = botConfig.getGroundBabylonMaterialId();
        babylonDecal.xPos = center.getX();
        babylonDecal.yPos = center.getY();
        babylonDecal.xSize = width;
        babylonDecal.ySize = height;
        return babylonDecal;
    }
}
