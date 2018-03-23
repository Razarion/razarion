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
    private boolean over;


    public void init(Mood mood, BotSceneConflictConfig botSceneConflictConfig) {
        humanPlayerId = mood.getHumanPlayerId();
        this.botSceneConflictConfig = botSceneConflictConfig;
    }

    public void start() {
        try {
            SyncBaseItem target = getTarget();
            if (target == null) {
                over = true;
                return;
            }
            SyncPhysicalArea targetSyncPhysicalArea = target.getSyncPhysicalArea();

            TerrainAreaFinder terrainAreaFinder = terrainAreaFinderInstance.get();
            terrainAreaFinder.start(targetSyncPhysicalArea.getPosition2d(), targetSyncPhysicalArea.getTerrainType(), botSceneConflictConfig.getMinDistance(), botSceneConflictConfig.getMaxDistance());
            BotConfig botConfig = botSceneConflictConfig.getBotConfig().cloneWithAbsolutePosition(terrainAreaFinder.getRandomPosition());
            botRunner = botService.startBot(botConfig);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    public void tick() {
        try {
            if(over) {
                return;
            }
            if (!botRunner.isBaseAlive()) {
                over = true;
                return;
            }
            SyncBaseItem target = getTarget();
            if (target == null) {
                over = true;
                return;
            }
            botRunner.attack(target);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    public boolean isOver() {
        return over;
    }

    private SyncBaseItem getTarget() {
        PlayerBaseFull playerBaseFull = baseItemService.getPlayerBaseFull4HumanPlayerId(humanPlayerId);
        return playerBaseFull.findItemsOfType(botSceneConflictConfig.getTargetBaseItemTypeId()).stream().findFirst().orElse(null);
    }

    public void clean() {
        if (botRunner != null) {
            botRunner.kill();
        }
    }

    public HumanPlayerId getHumanPlayerId() {
        return humanPlayerId;
    }
}
