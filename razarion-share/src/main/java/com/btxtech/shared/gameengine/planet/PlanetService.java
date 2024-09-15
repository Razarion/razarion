package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.dto.InitialSlaveSyncItemInfo;
import com.btxtech.shared.dto.MasterPlanetConfig;
import com.btxtech.shared.gameengine.datatypes.BackupPlanetInfo;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
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
import com.btxtech.client.Event;
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
    // See Angular BabylonBaseItemImpl
    public static final int TICK_TIME_MILLI_SECONDS = 100; // Only access in testing
    public static final int TICKS_PER_SECONDS = (int) (1000.0 / TICK_TIME_MILLI_SECONDS);
    public static final double TICK_FACTOR = (double) TICK_TIME_MILLI_SECONDS / 1000.0;
    private final Logger logger = Logger.getLogger(PlanetService.class.getName());
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
    private SyncItemContainerServiceImpl syncItemContainerService;
    @Inject
    private TerrainService terrainService;
    @Inject
    private ResourceService resourceService;
    @Inject
    private EnergyService energyService;
    @Inject
    private SyncService syncService;
    private boolean pause;
    private SimpleScheduledFuture scheduledFuture;
    private PlanetConfig planetConfig;
    private final Collection<PlanetTickListener> tickListeners = new ArrayList<>();
    private final PlanetServiceTracker planetServiceTracker = new PlanetServiceTracker();
    private long tickCount;
    private GameEngineMode gameEngineMode;
    // private List<DebugHelperStatic.TickData> tickDatas = new ArrayList<>();

    @PostConstruct
    public void postConstruct() {
        scheduledFuture = simpleExecutorService.scheduleAtFixedRate(TICK_TIME_MILLI_SECONDS, false, this, SimpleExecutorService.Type.GAME_ENGINE);
    }

    public void initialise(PlanetConfig planetConfig, GameEngineMode gameEngineMode, MasterPlanetConfig masterPlanetConfig, Runnable finishCallback, Consumer<String> failCallback) {
        this.planetConfig = planetConfig;
        this.gameEngineMode = gameEngineMode;
        syncItemContainerService.clear();
        terrainService.setup(planetConfig, () -> {
            activationEvent.fire(new PlanetActivationEvent(planetConfig, gameEngineMode, masterPlanetConfig, PlanetActivationEvent.Type.INITIALIZE));
            finishCallback.run();
        }, failCallback);
    }

    public void start() {
        planetServiceTracker.clear();
        tickCount = 0;
        scheduledFuture.start();
    }

    public void stop() {
        scheduledFuture.cancel();
        activationEvent.fire(new PlanetActivationEvent(null, null, null, PlanetActivationEvent.Type.STOP));
        syncItemContainerService.clear();
        terrainService.clean();
    }

    public BackupPlanetInfo backup() {
        BackupPlanetInfo backupPlanetInfo = new BackupPlanetInfo();
        backupPlanetInfo.setDate(new Date());
        backupPlanetInfo.setPlanetId(planetConfig.getId());
        baseItemService.fillBackup(backupPlanetInfo);
        questService.fillBackup(backupPlanetInfo);
        return backupPlanetInfo;
    }

    public void restoreBases(BackupPlanetInfo backupPlanetInfo, BaseRestoreProvider baseRestoreProvider) {
        long time = System.currentTimeMillis();
        baseItemService.restore(backupPlanetInfo, baseRestoreProvider);
        energyService.tick();
        logger.info("Backup from '" + backupPlanetInfo.getDate() + "' loaded in: " + (System.currentTimeMillis() - time));
    }

    @Override
    public void run() {
        if (pause) {
            return;
        }
        try {
            SynchronizationSendingContext synchronizationSendingContext = null;
            if (gameEngineMode == GameEngineMode.MASTER) {
                synchronizationSendingContext = new SynchronizationSendingContext();
            }
            planetServiceTracker.startTick();
            questService.tick();
            planetServiceTracker.afterQuestService();
            pathingService.tick(synchronizationSendingContext);
            planetServiceTracker.afterPathingService();
            baseItemService.tick();
            planetServiceTracker.afterBaseItemService();
            projectileService.tick();
            planetServiceTracker.afterProjectileService();
            energyService.tick();
            planetServiceTracker.afterEnergyService();
            boxService.tick();
            planetServiceTracker.afterBoxService();
            notifyTickListeners(synchronizationSendingContext);
            planetServiceTracker.afterTickListener();
            /// --- new experimental
            tickCount++;
            if (gameEngineMode != GameEngineMode.MASTER) {
                baseItemService.processPendingReceivedTickInfos(tickCount);
            }
            syncService.sendTickInfo(tickCount);
            /// --- new experimental ends
            planetServiceTracker.endTick();

            // DebugHelperStatic.appendAfterTick(tickDatas, tickCount, syncItemContainerService);
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

    private void notifyTickListeners(SynchronizationSendingContext synchronizationSendingContext) {
        for (PlanetTickListener tickListener : tickListeners) {
            try {
                tickListener.onPostTick(synchronizationSendingContext);
            } catch (Throwable t) {
                exceptionHandler.handleException(t);
            }
        }
    }

    public InitialSlaveSyncItemInfo generateSlaveSyncItemInfo(int userId) {
        InitialSlaveSyncItemInfo initialSlaveSyncItemInfo = new InitialSlaveSyncItemInfo();
        initialSlaveSyncItemInfo.setTickCount(tickCount);
        initialSlaveSyncItemInfo.setSyncBaseItemInfos(baseItemService.getSyncBaseItemInfos());
        initialSlaveSyncItemInfo.setPlayerBaseInfos(baseItemService.getPlayerBaseInfos());
        PlayerBase playerBase = baseItemService.getPlayerBase4UserId(userId);
        if (playerBase != null) {
            initialSlaveSyncItemInfo.setActualBaseId(playerBase.getBaseId());
        }
        initialSlaveSyncItemInfo.setSyncResourceItemInfos(resourceService.getSyncResourceItemInfos());
        initialSlaveSyncItemInfo.setSyncBoxItemInfos(boxService.getSyncBoxItemInfos());
        return initialSlaveSyncItemInfo;
    }

    public void enableTracking(boolean track) {
        planetServiceTracker.setRunning(track);
    }

    public void initialSlaveSyncItemInfo(InitialSlaveSyncItemInfo initialSlaveSyncItemInfo) {
        this.tickCount = (long) initialSlaveSyncItemInfo.getTickCount();
        resourceService.setupSlave(initialSlaveSyncItemInfo);
        baseItemService.setupSlave(initialSlaveSyncItemInfo);
        boxService.setupSlave(initialSlaveSyncItemInfo);
    }

    public long getTickCount() {
        return this.tickCount;
    }

    public void setTickCount(long tickCount) {
        this.tickCount = tickCount;
    }

    public GameEngineMode getGameEngineMode() {
        return gameEngineMode;
    }
}
