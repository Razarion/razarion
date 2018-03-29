package com.btxtech.shared.gameengine.planet.bot;

import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotSceneConflictConfig;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;
import com.btxtech.shared.gameengine.planet.pathing.TerrainAreaFinder;
import com.btxtech.shared.system.ExceptionHandler;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Created by Beat
 * on 20.03.2018.
 */
@Dependent
public class BotSceneConflict {
    @Inject
    private BotService botService;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private Instance<TerrainAreaFinder> terrainAreaFinderInstance;
    @Inject
    private BaseItemService baseItemService;
    private HumanPlayerId humanPlayerId;
    private BotSceneConflictConfig botSceneConflictConfig;
    private BotRunner botRunner;
    private Long reRopTime;
    private long botStartTimeStamp;
    private int kills;

    public void start(BotSceneConflictConfig botSceneConflictConfig) {
        try {
            stop();
            this.botSceneConflictConfig = botSceneConflictConfig;
            setupRePop();
            startBot();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    public void tick() {
        try {
            if (botRunner.isBaseAlive()) {
                if (botSceneConflictConfig.getStopMillis() != null) {
                    if (botStartTimeStamp + botSceneConflictConfig.getStopMillis() > System.currentTimeMillis()) {
                        setupRePop();
                        stop();
                        return;
                    }
                }
                if (botSceneConflictConfig.getStopKills() != null && kills >= botSceneConflictConfig.getStopKills()) {
                    setupRePop();
                    stop();
                    return;
                }

                SyncBaseItem target = getTarget();
                if (target == null) {
                    return;
                }
                botRunner.attack(target);
                return;
            }
            stop();
            if (reRopTime == null && botSceneConflictConfig.getRePopMillis() != null) {
                reRopTime = System.currentTimeMillis() + (long) botSceneConflictConfig.getRePopMillis();
                return;
            }

            if (reRopTime != null && reRopTime < System.currentTimeMillis()) {
                return;
            }
            startBot();
            reRopTime = null;
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    private void setupRePop() {
        if (botSceneConflictConfig.getRePopMillis() != null) {
            reRopTime = System.currentTimeMillis() + (long) botSceneConflictConfig.getRePopMillis();
        } else {
            reRopTime = null;
        }
    }

    private void startBot() {
        SyncBaseItem target = getTarget();
        if (target == null) {
            return;
        }
        SyncPhysicalArea targetSyncPhysicalArea = target.getSyncPhysicalArea();
        TerrainAreaFinder terrainAreaFinder = terrainAreaFinderInstance.get();
        terrainAreaFinder.start(targetSyncPhysicalArea.getPosition2d(), targetSyncPhysicalArea.getTerrainType(), botSceneConflictConfig.getMinDistance(), botSceneConflictConfig.getMaxDistance());
        BotConfig botConfig = botSceneConflictConfig.getBotConfig().clone4BotScene(terrainAreaFinder.getRandomPosition());
        botRunner = botService.startBot(botConfig);
        botStartTimeStamp = System.currentTimeMillis();
        kills = 0;
    }

    private SyncBaseItem getTarget() {
        PlayerBaseFull playerBaseFull = baseItemService.getPlayerBaseFull4HumanPlayerId(humanPlayerId);
        return playerBaseFull.findItemsOfType(botSceneConflictConfig.getTargetBaseItemTypeId()).stream().findFirst().orElse(null);
    }

    public void stop() {
        if (botRunner != null) {
            botRunner.kill();
        }
    }

    public HumanPlayerId getHumanPlayerId() {
        return humanPlayerId;
    }

    public void setHumanPlayerId(HumanPlayerId humanPlayerId) {
        this.humanPlayerId = humanPlayerId;
    }

    public boolean onHumanKill(BotRunner botRunner) {
        if (this.botRunner == botRunner) {
            kills++;
            return true;
        }
        return false;
    }
}
