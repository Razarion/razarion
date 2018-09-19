package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.dto.InitialSlaveSyncItemInfo;
import com.btxtech.shared.dto.MasterPlanetConfig;
import com.btxtech.shared.gameengine.datatypes.BackupPlanetInfo;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.energy.EnergyService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.pathing.PathingService;
import com.btxtech.shared.gameengine.planet.projectile.ProjectileService;
import com.btxtech.shared.gameengine.planet.quest.QuestService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;
import com.btxtech.shared.system.debugtool.DebugHelperStatic;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 13.07.2016.
 */
@Singleton
public class PlanetService implements Runnable { // Only available in worker. On ui part is GameUiControl
    public static final int DEFAULT_TICK_TIME_MILLI_SECONDS = 100;
    public static int TICK_TIME_MILLI_SECONDS = DEFAULT_TICK_TIME_MILLI_SECONDS; // Only access in testing
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
    private long tickCount;
    private GameEngineMode gameEngineMode;
    private boolean tickRunning;
    private List<DebugHelperStatic.TickData> tickDatas = new ArrayList<>();

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

    public BackupPlanetInfo backup(boolean saveUnregistered) {
        BackupPlanetInfo backupPlanetInfo = new BackupPlanetInfo();
        backupPlanetInfo.setDate(new Date());
        backupPlanetInfo.setPlanetId(planetConfig.getPlanetId());
        baseItemService.fillBackup(backupPlanetInfo, saveUnregistered);
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
            tickRunning = true;
            SynchronizationSendingContext synchronizationSendingContext = null;
            Collection<SyncBaseItem> executedCommands=null;
            Collection<SyncBaseItem> pendingIdlesToSend = null;
            if (gameEngineMode == GameEngineMode.MASTER) {
                synchronizationSendingContext = new SynchronizationSendingContext();
                executedCommands = new LinkedList<>();
                pendingIdlesToSend = new LinkedList<>();
            }
            planetServiceTracker.startTick();
            questService.tick();
            planetServiceTracker.afterQuestService();
            pathingService.tick(synchronizationSendingContext);
            planetServiceTracker.afterPathingService();
            baseItemService.tick(pendingIdlesToSend, executedCommands);
            planetServiceTracker.afterBaseItemService();
            projectileService.tick();
            planetServiceTracker.afterProjectileService();
            energyService.tick();
            planetServiceTracker.afterEnergyService();
            boxService.tick();
            planetServiceTracker.afterBoxService();
            notifyTickListeners(synchronizationSendingContext);
            planetServiceTracker.afterTickListener();
            planetServiceTracker.endTick();
            /// --- new experimental
            tickCount++;
            baseItemService.afterTick(pendingIdlesToSend, tickCount, executedCommands);
            /// --- new experimental ends

            DebugHelperStatic.appendAfterTick(tickDatas, tickCount, syncItemContainerService);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
        tickRunning = false;
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

    public InitialSlaveSyncItemInfo generateSlaveSyncItemInfo(HumanPlayerId humanPlayerId) {
        InitialSlaveSyncItemInfo initialSlaveSyncItemInfo = new InitialSlaveSyncItemInfo();
        initialSlaveSyncItemInfo.setTickCount(tickCount);
        initialSlaveSyncItemInfo.setSyncBaseItemInfos(baseItemService.getSyncBaseItemInfos());
        initialSlaveSyncItemInfo.setPlayerBaseInfos(baseItemService.getPlayerBaseInfos());
        PlayerBase playerBase = baseItemService.getPlayerBase4HumanPlayerId(humanPlayerId);
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

    public boolean isTickRunning() {
        return tickRunning;
    }

    public List<DebugHelperStatic.TickData> getTickDatas() {
        return tickDatas;
    }

    public GameEngineMode getGameEngineMode() {
        return gameEngineMode;
    }
}
