package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.MasterPlanetConfig;
import com.btxtech.shared.dto.SlaveSyncItemInfo;
import com.btxtech.shared.gameengine.datatypes.BackupPlanetInfo;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.PlanetMode;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.energy.EnergyService;
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
import java.util.Date;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 13.07.2016.
 */
@Singleton
public class PlanetService implements Runnable { // Only available in worker. On ui part is GameUiControl
    public static final PlanetMode MODE = PlanetMode.MASTER;
    public static final int TICK_TIME_MILLI_SECONDS = 100;
    public static final int TICKS_PER_SECONDS = (int) (1000.0 / TICK_TIME_MILLI_SECONDS);
    public static final double TICK_FACTOR = (double) TICK_TIME_MILLI_SECONDS / 1000.0;
    private Logger logger = Logger.getLogger(PlanetService.class.getName());
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
    private SyncItemContainerService syncItemContainerService;
    @Inject
    private TerrainService terrainService;
    @Inject
    private ResourceService resourceService;
    @Inject
    private EnergyService energyService;
    private boolean pause;
    private SimpleScheduledFuture scheduledFuture;
    private PlanetConfig planetConfig;
    private Collection<PlanetTickListener> tickListeners = new ArrayList<>();
    private PlanetServiceTracker planetServiceTracker = new PlanetServiceTracker();

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

    public void start() {
        planetServiceTracker.clear();
        scheduledFuture.start();
    }

    public void stop() {
        scheduledFuture.cancel();
        activationEvent.fire(new PlanetActivationEvent(null, null, null, null, PlanetActivationEvent.Type.STOP));
        syncItemContainerService.clear();
        terrainService.clean();
    }

    public BackupPlanetInfo backup(boolean saveUnregistered) {
        BackupPlanetInfo backupPlanetInfo = new BackupPlanetInfo();
        backupPlanetInfo.setDate(new Date());
        backupPlanetInfo.setPlanetId(planetConfig.getPlanetId());
        baseItemService.fillBackup(backupPlanetInfo, saveUnregistered);
        questService.fillBackup(backupPlanetInfo);
        return backupPlanetInfo;
    }

    public void restoreBases(BackupPlanetInfo backupPlanetInfo) {
        long time = System.currentTimeMillis();
        baseItemService.restore(backupPlanetInfo);
        energyService.tick();
        logger.info("BackupPlanetInfo.restoreBases() in:" + (System.currentTimeMillis() - time));
    }

    @Override
    public void run() {
        if (pause) {
            return;
        }
        try {
            planetServiceTracker.startTick();
            questService.tick();
            planetServiceTracker.afterQuestService();
            pathingService.tick();
            planetServiceTracker.afterPathingService();
            baseItemService.tick();
            planetServiceTracker.afterBaseItemService();
            projectileService.tick();
            planetServiceTracker.afterProjectileService();
            energyService.tick();
            planetServiceTracker.afterEnergyService();
            boxService.tick();
            planetServiceTracker.afterBoxService();
            planetServiceTracker.endTick();
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
        PlayerBase playerBase = baseItemService.getPlayerBase4HumanPlayerId(userContext.getHumanPlayerId());
        if (playerBase != null) {
            slaveSyncItemInfo.setActualBaseId(playerBase.getBaseId());
        }
        slaveSyncItemInfo.setSyncResourceItemInfos(resourceService.getSyncResourceItemInfos());
        slaveSyncItemInfo.setSyncBoxItemInfos(boxService.getSyncBoxItemInfos());
        return slaveSyncItemInfo;
    }

    public void enableTracking(boolean track) {
        planetServiceTracker.setRunning(track);
    }
}
