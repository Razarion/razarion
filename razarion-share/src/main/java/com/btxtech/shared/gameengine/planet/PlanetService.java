package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.MasterPlanetConfig;
import com.btxtech.shared.dto.SlaveSyncItemInfo;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.PlanetMode;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.planet.bot.BotService;
import com.btxtech.shared.gameengine.planet.pathing.PathingService;
import com.btxtech.shared.gameengine.planet.projectile.ProjectileService;
import com.btxtech.shared.gameengine.planet.quest.QuestService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * Created by Beat
 * 13.07.2016.
 */
@Singleton
public class PlanetService implements Runnable { // Only available in worker. On ui part is GameUiControl
    public static final PlanetMode MODE = PlanetMode.MASTER;
    public static final int TICK_TIME_MILLI_SECONDS = 100;
    public static final double TICK_FACTOR = (double) TICK_TIME_MILLI_SECONDS / 1000.0;
    // private Logger logger = Logger.getLogger(PlanetService.class.getName());
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private Event<PlanetActivationEvent> activationEvent;
    @Inject
    private SimpleExecutorService simpleExecutorService;
    @Inject
    private PathingService pathingService;
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private QuestService questService;
    @Inject
    private BoxService boxService;
    @Inject
    private ProjectileService projectileService;
    @Inject
    private BotService botService;
    @Inject
    private SyncItemContainerService syncItemContainerService;
    @Inject
    private TerrainService terrainService;
    @Inject
    private ResourceService resourceService;
    private boolean pause;
    private SimpleScheduledFuture scheduledFuture;
    private PlanetConfig planetConfig;
    private Collection<PlanetTickListener> tickListeners = new ArrayList<>();

    @PostConstruct
    public void postConstruct() {
        scheduledFuture = simpleExecutorService.scheduleAtFixedRate(TICK_TIME_MILLI_SECONDS, false, this, SimpleExecutorService.Type.GAME_ENGINE);
    }

    public void initialise(PlanetConfig planetConfig, GameEngineMode gameEngineMode, MasterPlanetConfig masterPlanetConfig, SlaveSyncItemInfo slaveSyncItemInfo, Runnable finishCallback, Consumer<String> failCallback) {
        this.planetConfig = planetConfig;
        syncItemContainerService.clear();
        terrainService.setup(planetConfig, () -> {
            activationEvent.fire(new PlanetActivationEvent(planetConfig, gameEngineMode, masterPlanetConfig, slaveSyncItemInfo, PlanetActivationEvent.Type.INITIALIZE));
            finishCallback.run();
        }, failCallback);
    }

    public void start(Collection<BotConfig> botConfigs) {
        scheduledFuture.start();
        if (botConfigs != null) {
            botService.startBots(botConfigs);
        }
    }

    public void stop() {
        scheduledFuture.cancel();
        activationEvent.fire(new PlanetActivationEvent(null, null, null, null, PlanetActivationEvent.Type.STOP));
        syncItemContainerService.clear();
        terrainService.clean();
    }

    @Override
    public void run() {
        if (pause) {
            return;
        }
        try {
            pathingService.tick();
            questService.checkPositionCondition();
            baseItemService.tick();
            boxService.tick();
            projectileService.tick();
            notifyTickListeners();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    public PlanetConfig getPlanetConfig() {
        return planetConfig;
    }

    public boolean isPause() {
        return pause;
    }

    public void setPause(boolean pause) {
        this.pause = pause;
    }

    public void addTickListener(PlanetTickListener planetTickListener) {
        tickListeners.add(planetTickListener);
    }

    public void removeTickListener(PlanetTickListener planetTickListener) {
        tickListeners.remove(planetTickListener);
    }

    private void notifyTickListeners() {
        for (PlanetTickListener tickListener : tickListeners) {
            tickListener.onPostTick();
        }
    }

    public SlaveSyncItemInfo generateSlaveSyncItemInfo(UserContext userContext) {
        SlaveSyncItemInfo slaveSyncItemInfo = new SlaveSyncItemInfo();
        slaveSyncItemInfo.setSyncBaseItemInfos(baseItemService.getSyncBaseItemInfos());
        slaveSyncItemInfo.setPlayerBaseInfos(baseItemService.getPlayerBaseInfos());
        PlayerBaseFull playerBaseFull = baseItemService.getPlayerBase4HumanPlayerId(userContext.getHumanPlayerId());
        if (playerBaseFull != null) {
            slaveSyncItemInfo.setActualBaseId(playerBaseFull.getBaseId());
        }
        slaveSyncItemInfo.setSyncResourceItemInfos(resourceService.getSyncResourceItemInfos());
        return slaveSyncItemInfo;
    }
}
