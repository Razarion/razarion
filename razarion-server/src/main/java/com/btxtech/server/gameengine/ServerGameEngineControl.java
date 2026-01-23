package com.btxtech.server.gameengine;

import com.btxtech.server.model.engine.BackupPlanetOverview;
import com.btxtech.server.service.engine.PlanetBackupService;
import com.btxtech.server.service.engine.PlanetCrudService;
import com.btxtech.server.service.engine.ServerGameEngineService;
import com.btxtech.server.service.engine.StaticGameConfigService;
import com.btxtech.server.service.tracking.UserActivityService;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.dto.ServerGameEngineConfig;
import com.btxtech.shared.gameengine.InitializeService;
import com.btxtech.shared.gameengine.datatypes.BackupPlanetInfo;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.packets.PlayerBaseInfo;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.BaseRestoreProvider;
import com.btxtech.shared.gameengine.planet.BoxService;
import com.btxtech.shared.gameengine.planet.GameLogicListener;
import com.btxtech.shared.gameengine.planet.GameLogicService;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.PlanetTickListener;
import com.btxtech.shared.gameengine.planet.ResourceService;
import com.btxtech.shared.gameengine.planet.SynchronizationSendingContext;
import com.btxtech.shared.gameengine.planet.bot.BotService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.gameengine.planet.quest.QuestService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Service
public class ServerGameEngineControl implements GameLogicListener, BaseRestoreProvider, PlanetTickListener {
    private final Logger logger = LoggerFactory.getLogger(ServerGameEngineControl.class);
    private final Object reloadLook = new Object();
    private final InitializeService initializeService;
    private final PlanetService planetService;
    private final StaticGameConfigService staticGameConfigPersistence;
    private final GameLogicService gameLogicService;
    private final ClientGameConnectionService clientGameConnectionService;
    private final ClientSystemConnectionService systemConnectionService;
    private final ServerGameEngineService serverGameEngineService;
    private final PlanetCrudService planetCrudService;
    private final QuestService questService;
    private final UserService userService;
    private final BaseItemService baseItemService;
    private final BotService botService;
    private final ResourceService resourceService;
    private final BoxService boxService;
    private final PlanetBackupService planetBackupService;
    private final UserActivityService userActivityService;
    // @Inject
    // TODOprivate ServerInventoryService serverInventoryService;
    private boolean running;

    public ServerGameEngineControl(InitializeService initializeService,
                                   PlanetService planetService,
                                   BoxService boxService,
                                   StaticGameConfigService staticGameConfigPersistence,
                                   GameLogicService gameLogicService,
                                   ClientGameConnectionService clientGameConnectionService,
                                   ClientSystemConnectionService systemConnectionService,
                                   ServerGameEngineService serverGameEngineService,
                                   PlanetCrudService planetCrudService,
                                   QuestService questService,
                                   UserService userService,
                                   BaseItemService baseItemService,
                                   BotService botService,
                                   ResourceService resourceService, PlanetBackupService planetBackupService,
                                   UserActivityService userActivityService) {
        this.initializeService = initializeService;
        this.planetService = planetService;
        this.boxService = boxService;
        this.staticGameConfigPersistence = staticGameConfigPersistence;
        this.gameLogicService = gameLogicService;
        this.clientGameConnectionService = clientGameConnectionService;
        this.systemConnectionService = systemConnectionService;
        this.serverGameEngineService = serverGameEngineService;
        this.planetCrudService = planetCrudService;
        this.questService = questService;
        this.userService = userService;
        this.baseItemService = baseItemService;
        this.botService = botService;
        this.resourceService = resourceService;
        this.planetBackupService = planetBackupService;
        this.userActivityService = userActivityService;
    }

    public void start(BackupPlanetInfo backupPlanetInfo, boolean activateQuests) {
        ServerGameEngineConfig serverGameEngineConfig = serverGameEngineService.serverGameEngineConfig();
        PlanetConfig planetConfig = planetCrudService.read(serverGameEngineConfig.getPlanetConfigId());
        BackupPlanetInfo finaBackupPlanetInfo = setupBackupPlanetInfo(backupPlanetInfo, planetConfig);
        planetService.initialise(planetConfig, GameEngineMode.MASTER, serverGameEngineService.readMasterPlanetConfig(), () -> {
            gameLogicService.setGameLogicListener(this);
            if (finaBackupPlanetInfo != null) {
                planetService.restoreBases(finaBackupPlanetInfo, this);
            }
            planetService.start();
            planetService.addTickListener(this);
            resourceService.startResourceRegions();
            boxService.startBoxRegions(serverGameEngineService.readBoxRegionConfigs());
            botService.startBots(serverGameEngineConfig.getBotConfigs());
            planetService.enableTracking(false);
        }, failText -> logger.error("TerrainSetup failed: " + failText));
        if (activateQuests) {
            activateQuests(finaBackupPlanetInfo);
        }
        running = true;
    }

    private BackupPlanetInfo setupBackupPlanetInfo(BackupPlanetInfo backupPlanetInfo, PlanetConfig planetConfig) {
        if (backupPlanetInfo != null) {
            return backupPlanetInfo;
        } else {
            try {
                return planetBackupService.loadLastBackup(planetConfig.getId());
            } catch (Throwable t) {
                logger.error("Error loadLastBackup", t);
                return null;
            }
        }
    }

    public void reloadStatic() {
        synchronized (reloadLook) {
            long time = System.currentTimeMillis();
            initializeService.setStaticGameConfig(staticGameConfigPersistence.loadStaticGameConfig());
            logger.info("reloadStatic(). Time used: " + (System.currentTimeMillis() - time));
        }
    }

//    public void restartBots() {
//        synchronized (reloadLook) {
//            botService.killAllBots();
//            botService.startBots(serverGameEngineCrudPersistence.readBotConfigs());
//        }
//    }

    public void restartResourceRegions() {
        synchronized (reloadLook) {
            resourceService.reloadResourceRegions(serverGameEngineService.readMasterPlanetConfig().getResourceRegionConfigs());
        }
    }

    public void restartBoxRegions() {
        synchronized (reloadLook) {
            boxService.stopBoxRegions();
            boxService.startBoxRegions(serverGameEngineService.readBoxRegionConfigs());
        }
    }

    public void restartPlanet() {
        if (!running) {
            return;
        }
        long time = System.currentTimeMillis();
        BackupPlanetInfo backupPlanetInfo = planetService.backup();
        stop();
        start(backupPlanetInfo, false);
        logger.info("ServerGameEngineControl.restartPlanet() in: " + (System.currentTimeMillis() - time));
    }

    public void backupPlanet() throws JsonProcessingException {
        if (!running) {
            return;
        }
        long time = System.currentTimeMillis();
        BackupPlanetInfo backupPlanetInfo = planetService.backup();
        planetBackupService.saveBackup(backupPlanetInfo);
        logger.info("ServerGameEngineControl.backupPlanet() in: " + (System.currentTimeMillis() - time));
    }

    public void restorePlanet(BackupPlanetOverview backupPlanetOverview) {
        long time = System.currentTimeMillis();
        BackupPlanetInfo backupPlanetInfo = planetBackupService.loadBackup(backupPlanetOverview);
        stop();
        start(backupPlanetInfo, true);
        logger.info("ServerGameEngineControl.restorePlanet() in: " + (System.currentTimeMillis() - time));
    }

    private void activateQuests(BackupPlanetInfo backupPlanetInfo) {
//  TODO      questService.clean();
//        Collection<Integer> planetQuestId = serverGameEngineCrudPersistence.readAllQuestIds();
//        if (planetQuestId == null || planetQuestId.isEmpty()) {
//            return;
//        }
//        for (Map.Entry<Integer, QuestConfig> entry : userService.findActiveQuests4Users(planetQuestId).entrySet()) {
//            questService.activateCondition(entry.getKey(), entry.getValue());
//        }
//        questService.restore(backupPlanetInfo);
    }

    public void stop() {
        planetService.removeTickListener(this);
        if (running) {
            planetService.stop();
            botService.killAllBots();
        }
    }

    public void shutdown() {
        logger.info("ServerGameEngineControl.shutdown()");
        try {
            if (running) {
                planetBackupService.saveBackup(planetService.backup());
            }
        } catch (Throwable t) {
            logger.warn(t.getMessage(), t);
        }
        try {
            stop();
        } catch (Throwable t) {
            logger.warn(t.getMessage(), t);
        }
    }

    public void onLevelChanged(String userId, int levelId) {
        baseItemService.updateLevel(userId, levelId);
    }

    @Override
    public void onBaseCreated(PlayerBaseFull playerBase) {
        clientGameConnectionService.onBaseCreated(playerBase);
        if (playerBase.getUserId() != null) {
            this.userActivityService.onBaseCreated(playerBase.getUserId(), playerBase.getBaseId());
        }
    }

    @Override
    public void onBaseDeleted(PlayerBase playerBase, PlayerBase actor) {
        clientGameConnectionService.onBaseDeleted(playerBase);
        // TODO itemTrackerPersistence.onBaseDeleted(playerBase, actor);
    }

    @Override
    public void onSpawnSyncItemStart(SyncBaseItem syncBaseItem) {
        clientGameConnectionService.onSyncItemSpawnStart(syncBaseItem);
        // TODO itemTrackerPersistence.onSpawnSyncItemStart(syncBaseItem);
    }

    @Override
    public void onSpawnSyncItemNoSpan(SyncBaseItem syncBaseItem) {
        // TODO itemTrackerPersistence.onSpawnSyncItemNoSpan(syncBaseItem);
    }

    @Override
    public void onBuildingSyncItem(SyncBaseItem syncBaseItem, SyncBaseItem createdBy) {
        // TODO itemTrackerPersistence.onBuildingSyncItem(syncBaseItem, createdBy);
    }

    @Override
    public void onFactorySyncItem(SyncBaseItem syncBaseItem, SyncBaseItem createdBy) {
        // TODO itemTrackerPersistence.onFactorySyncItem(syncBaseItem, createdBy);
    }

    @Override
    public void onSyncBaseItemKilledMaster(SyncBaseItem syncBaseItem, SyncBaseItem actor) {
        clientGameConnectionService.onSyncItemRemoved(syncBaseItem, true);
        // TODO itemTrackerPersistence.onSyncBaseItemKilled(syncBaseItem, actor);
    }

    @Override
    public void onSyncBaseItemRemoved(SyncBaseItem syncBaseItem) {
        clientGameConnectionService.onSyncItemRemoved(syncBaseItem, false);
        // TODO itemTrackerPersistence.onSyncBaseItemRemoved(syncBaseItem);
    }

    @Override
    public void onResourceCreated(SyncResourceItem syncResourceItem) {
        clientGameConnectionService.onSyncResourceItemCreated(syncResourceItem);
        // TODO itemTrackerPersistence.onResourceCreated(syncResourceItem);
    }

    @Override
    public void onResourceDeleted(SyncResourceItem syncResourceItem) {
        clientGameConnectionService.onSyncItemRemoved(syncResourceItem, false);
        // TODO itemTrackerPersistence.onResourceDeleted(syncResourceItem);
    }

    @Override
    public void onBoxCreated(SyncBoxItem syncBoxItem) {
        clientGameConnectionService.onSyncBoxCreated(syncBoxItem);
        // TODO itemTrackerPersistence.onSyncBoxCreated(syncBoxItem);
    }

    @Override
    public void onSyncBoxDeleted(SyncBoxItem box) {
        clientGameConnectionService.onSyncItemRemoved(box, false);
        // TODO itemTrackerPersistence.onSyncBoxDeleted(box);
    }

    @Override
    public void onBoxPicked(String userId, BoxContent boxContent) {
        // TODO serverInventoryService.onBoxPicked(userId, boxContent);
        // TODO systemConnectionService.onBoxPicked(userId, boxContent);
    }

    @Override
    public void onQuestProgressUpdate(String userId, QuestProgressInfo questProgressInfo) {
        systemConnectionService.onQuestProgressInfo(userId, questProgressInfo);
    }

    @Override
    public void onResourcesBalanceChanged(PlayerBase playerBase, int resources) {
        clientGameConnectionService.sendResourcesBalanceChanged(playerBase, resources);
    }

    public PlanetConfig getPlanetConfig() {
        return planetService.getPlanetConfig();
    }

    public void updateUserName(String userId, String name) {
        PlayerBase playerBase = baseItemService.getPlayerBase4UserId(userId);
        if (playerBase != null) {
            playerBase = baseItemService.changeBaseNameChanged(playerBase.getBaseId(), name);
            clientGameConnectionService.onBaseNameChanged(playerBase);
        }
    }

    @Override
    public Integer getLevel(PlayerBaseInfo playerBaseInfo) {
        if (playerBaseInfo.getUserId() == null) {
            throw new IllegalStateException("Can not restore base with id: " + playerBaseInfo.getBaseId() + " name: " + playerBaseInfo.getName() + " may be this is a bot");
        }
        return userService.getUserContextTransactional(playerBaseInfo.getUserId()).getLevelId();

    }

    @Override
    public Map<Integer, Integer> getUnlockedItemLimit(PlayerBaseInfo playerBaseInfo) {
        if (playerBaseInfo.getUserId() == null) {
            throw new IllegalStateException("Can not restore base with id: " + playerBaseInfo.getBaseId() + " name: " + playerBaseInfo.getName() + " may be this is a bot");
        }
        return userService.getUserContextTransactional(playerBaseInfo.getUserId()).getUnlockedItemLimit();
    }

    @Override
    public String getName(PlayerBaseInfo playerBaseInfo) {
        if (playerBaseInfo.getUserId() == null) {
            throw new IllegalStateException("Can not restore base with id: " + playerBaseInfo.getBaseId() + " name: " + playerBaseInfo.getName() + " may be this is a bot");
        }
        return userService.getUserContextTransactional(playerBaseInfo.getUserId()).getName();
    }

    @Override
    public void onPostTick(SynchronizationSendingContext synchronizationSendingContext) {
//        Set<SyncBaseItem> tmpAlreadySentSyncBaseItems = alreadySentSyncBaseItems;
//        alreadySentSyncBaseItems = new HashSet<>();
//        pathingChangesDisruptor.onPostTick(synchronizationSendingContext.getCollisions(), tmpAlreadySentSyncBaseItems);
    }
}
