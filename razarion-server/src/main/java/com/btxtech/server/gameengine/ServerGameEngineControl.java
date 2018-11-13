package com.btxtech.server.gameengine;

import com.btxtech.server.connection.ClientSystemConnectionService;
import com.btxtech.server.persistence.StaticGameConfigPersistence;
import com.btxtech.server.persistence.backup.BackupPlanetOverview;
import com.btxtech.server.persistence.backup.PlanetBackupMongoDb;
import com.btxtech.server.persistence.history.HistoryPersistence;
import com.btxtech.server.persistence.item.ItemTrackerPersistence;
import com.btxtech.server.persistence.server.ServerGameEnginePersistence;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.StaticGameInitEvent;
import com.btxtech.shared.gameengine.datatypes.BackupPlanetInfo;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotSceneConflictConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotSceneIndicationInfo;
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
import com.btxtech.shared.system.ExceptionHandler;
import com.fasterxml.jackson.core.JsonProcessingException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 18.04.2017.
 */
@ApplicationScoped
public class ServerGameEngineControl implements GameLogicListener, BaseRestoreProvider, PlanetTickListener {
    private Logger logger = Logger.getLogger(ServerGameEngineControl.class.getName());
    @Inject
    private Event<StaticGameInitEvent> gameEngineInitEvent;
    @Inject
    private PlanetService planetService;
    @Inject
    private StaticGameConfigPersistence staticGameConfigPersistence;
    @Inject
    private GameLogicService gameLogicService;
    @Inject
    private ClientGameConnectionService clientGameConnectionService;
    @Inject
    private ClientSystemConnectionService systemConnectionService;
    @Inject
    private ServerGameEnginePersistence serverGameEnginePersistence;
    @Inject
    private TerrainShapeService terrainShapeService;
    @Inject
    private QuestService questService;
    @Inject
    private UserService userService;
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private BotService botService;
    @Inject
    private ResourceService resourceService;
    @Inject
    private BoxService boxService;
    @Inject
    private PlanetBackupMongoDb planetBackupMongoDb;
    @Inject
    private ServerInventoryService serverInventoryService;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private ItemTrackerPersistence itemTrackerPersistence;
    @Inject
    private HistoryPersistence historyPersistence;
    //    @Inject
//    private DebugGui debugGui;
    private final Object reloadLook = new Object();

    public void start(BackupPlanetInfo backupPlanetInfo, boolean activateQuests) {
        //debugGui.display();
        PlanetConfig planetConfig = serverGameEnginePersistence.readPlanetConfig();
        BackupPlanetInfo finaBackupPlanetInfo = setupBackupPlanetInfo(backupPlanetInfo, planetConfig);
        gameEngineInitEvent.fire(new StaticGameInitEvent(staticGameConfigPersistence.loadStaticGameConfig()));
        terrainShapeService.start();
        planetService.initialise(planetConfig, GameEngineMode.MASTER, serverGameEnginePersistence.readMasterPlanetConfig(), () -> {
            gameLogicService.setGameLogicListener(this);
            if (finaBackupPlanetInfo != null) {
                planetService.restoreBases(finaBackupPlanetInfo, this);
            }
            planetService.start();
            planetService.addTickListener(this);
            resourceService.startResourceRegions();
            boxService.startBoxRegions(serverGameEnginePersistence.readBoxRegionConfigs());
            botService.startBots(serverGameEnginePersistence.readBotConfigs(), serverGameEnginePersistence.readBotSceneConfigs());
            planetService.enableTracking(false);
        }, failText -> logger.severe("TerrainSetup failed: " + failText));
        if (activateQuests) {
            activateQuests(finaBackupPlanetInfo);
        }
    }

    private BackupPlanetInfo setupBackupPlanetInfo(BackupPlanetInfo backupPlanetInfo, PlanetConfig planetConfig) {
        if (backupPlanetInfo != null) {
            return backupPlanetInfo;
        } else {
            return planetBackupMongoDb.loadLastBackup(planetConfig.getPlanetId());
        }
    }

    @SecurityCheck
    public void reloadStatic() {
        synchronized (reloadLook) {
            long time = System.currentTimeMillis();
            gameEngineInitEvent.fire(new StaticGameInitEvent(staticGameConfigPersistence.loadStaticGameConfig()));
            logger.info("reloadStatic(). Time used: " + (System.currentTimeMillis() - time));
        }
    }

    @SecurityCheck
    public void restartBots() {
        synchronized (reloadLook) {
            botService.killAllBots();
            botService.startBots(serverGameEnginePersistence.readBotConfigs(), serverGameEnginePersistence.readBotSceneConfigs());
        }
    }

    @SecurityCheck
    public void reloadPlanet() {
        synchronized (reloadLook) {
            PlanetConfig newPlanetConfig = serverGameEnginePersistence.readPlanetConfig();
            PlanetConfig currentPlanetConfig = planetService.getPlanetConfig();
            currentPlanetConfig.setItemTypeLimitation(newPlanetConfig.getItemTypeLimitation());
            currentPlanetConfig.setHouseSpace(newPlanetConfig.getHouseSpace());
            currentPlanetConfig.setStartRazarion(newPlanetConfig.getStartRazarion());
            currentPlanetConfig.setStartBaseItemTypeId(newPlanetConfig.getStartBaseItemTypeId());
        }
    }

    @SecurityCheck
    public void restartResourceRegions() {
        synchronized (reloadLook) {
            resourceService.reloadResourceRegions(serverGameEnginePersistence.readMasterPlanetConfig().getResourceRegionConfigs());
        }
    }

    @SecurityCheck
    public void restartBoxRegions() {
        synchronized (reloadLook) {
            boxService.stopBoxRegions();
            boxService.startBoxRegions(serverGameEnginePersistence.readBoxRegionConfigs());
        }
    }

    @SecurityCheck
    public void restartPlanet() {
        long time = System.currentTimeMillis();
        BackupPlanetInfo backupPlanetInfo = planetService.backup(true);
        stop();
        start(backupPlanetInfo, false);
        logger.info("ServerGameEngineControl.restartPlanet() in: " + (System.currentTimeMillis() - time));
    }

    @SecurityCheck
    public void backupPlanet() throws JsonProcessingException {
        long time = System.currentTimeMillis();
        BackupPlanetInfo backupPlanetInfo = planetService.backup(false);
        planetBackupMongoDb.saveBackup(backupPlanetInfo);
        logger.info("ServerGameEngineControl.backupPlanet() in: " + (System.currentTimeMillis() - time));
    }

    @SecurityCheck
    public void restorePlanet(BackupPlanetOverview backupPlanetOverview) {
        long time = System.currentTimeMillis();
        BackupPlanetInfo backupPlanetInfo = planetBackupMongoDb.loadBackup(backupPlanetOverview);
        stop();
        start(backupPlanetInfo, true);
        logger.info("ServerGameEngineControl.restorePlanet() in: " + (System.currentTimeMillis() - time));
    }

    private void activateQuests(BackupPlanetInfo backupPlanetInfo) {
        questService.clean();
        Collection<Integer> planetQuestId = serverGameEnginePersistence.readAllQuestIds();
        if (planetQuestId == null || planetQuestId.isEmpty()) {
            return;
        }
        for (Map.Entry<HumanPlayerId, QuestConfig> entry : userService.findActiveQuests4Users(planetQuestId).entrySet()) {
            questService.activateCondition(entry.getKey(), entry.getValue());
        }
        questService.restore(backupPlanetInfo);
    }

    public void stop() {
        planetService.removeTickListener(this);
        planetService.stop();
        botService.killAllBots();
    }

    public void shutdown() {
        try {
            planetBackupMongoDb.saveBackup(planetService.backup(false));
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
        try {
            stop();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    public void onLevelChanged(HumanPlayerId humanPlayerId, int levelId) {
        baseItemService.updateLevel(humanPlayerId, levelId);
    }

    @Override
    public void onBaseCreated(PlayerBaseFull playerBase) {
        clientGameConnectionService.onBaseCreated(playerBase);
        itemTrackerPersistence.onBaseCreated(playerBase);
    }

    @Override
    public void onBaseDeleted(PlayerBase playerBase, PlayerBase actor) {
        clientGameConnectionService.onBaseDeleted(playerBase);
        itemTrackerPersistence.onBaseDeleted(playerBase, actor);
    }

    @Override
    public void onSpawnSyncItemStart(SyncBaseItem syncBaseItem) {
        itemTrackerPersistence.onSpawnSyncItemStart(syncBaseItem);
    }

    @Override
    public void onSpawnSyncItemNoSpan(SyncBaseItem syncBaseItem) {
        itemTrackerPersistence.onSpawnSyncItemNoSpan(syncBaseItem);
    }

    @Override
    public void onBuildingSyncItem(SyncBaseItem syncBaseItem, SyncBaseItem createdBy) {
        itemTrackerPersistence.onBuildingSyncItem(syncBaseItem, createdBy);
    }

    @Override
    public void onFactorySyncItem(SyncBaseItem syncBaseItem, SyncBaseItem createdBy) {
        itemTrackerPersistence.onFactorySyncItem(syncBaseItem, createdBy);
    }

    @Override
    public void onSyncBaseItemKilledMaster(SyncBaseItem syncBaseItem, SyncBaseItem actor) {
        clientGameConnectionService.onSyncItemRemoved(syncBaseItem, true);
        itemTrackerPersistence.onSyncBaseItemKilled(syncBaseItem, actor);
    }

    @Override
    public void onSyncBaseItemRemoved(SyncBaseItem syncBaseItem) {
        clientGameConnectionService.onSyncItemRemoved(syncBaseItem, false);
        itemTrackerPersistence.onSyncBaseItemRemoved(syncBaseItem);
    }

    @Override
    public void onResourceCreated(SyncResourceItem syncResourceItem) {
        clientGameConnectionService.onSyncResourceItemCreated(syncResourceItem);
        itemTrackerPersistence.onResourceCreated(syncResourceItem);
    }

    @Override
    public void onResourceDeleted(SyncResourceItem syncResourceItem) {
        clientGameConnectionService.onSyncItemRemoved(syncResourceItem, false);
        itemTrackerPersistence.onResourceDeleted(syncResourceItem);
    }

    @Override
    public void onBoxCreated(SyncBoxItem syncBoxItem) {
        clientGameConnectionService.onSyncBoxCreated(syncBoxItem);
        itemTrackerPersistence.onSyncBoxCreated(syncBoxItem);
    }

    @Override
    public void onSyncBoxDeleted(SyncBoxItem box) {
        clientGameConnectionService.onSyncItemRemoved(box, false);
        itemTrackerPersistence.onSyncBoxDeleted(box);
    }

    @Override
    public void onBoxPicked(HumanPlayerId humanPlayerId, BoxContent boxContent) {
        serverInventoryService.onBoxPicked(humanPlayerId, boxContent);
        systemConnectionService.onBoxPicked(humanPlayerId, boxContent);
    }

    @Override
    public void onQuestProgressUpdate(HumanPlayerId humanPlayerId, QuestProgressInfo questProgressInfo) {
        systemConnectionService.onQuestProgressInfo(humanPlayerId, questProgressInfo);
    }

    @Override
    public void onBotSceneConflictChanged(HumanPlayerId humanPlayerId, boolean raise, BotSceneConflictConfig newConflict, BotSceneConflictConfig oldConflict, BotSceneIndicationInfo botSceneIndicationInfo) {
        systemConnectionService.onBotSceneConflictChanged(humanPlayerId, botService.getBotSceneIndicationInfos(humanPlayerId));
        historyPersistence.onBotSceneConflictChanged(humanPlayerId, raise, newConflict, oldConflict, botSceneIndicationInfo);
    }

    @Override
    public void onBotSceneConflictsChanged(Collection<HumanPlayerId> activeHumanPlayerIds, boolean raise, BotSceneConflictConfig newConflict, BotSceneConflictConfig oldConflict, BotSceneIndicationInfo botSceneIndicationInfo) {
        activeHumanPlayerIds.forEach(humanPlayerId -> onBotSceneConflictChanged(humanPlayerId, raise, newConflict, oldConflict, botSceneIndicationInfo));
    }

    @Override
    public void onResourcesBalanceChanged(PlayerBase playerBase, int resources) {
        clientGameConnectionService.sendResourcesBalanceChanged(playerBase, resources);
    }

    public PlanetConfig getPlanetConfig() {
        return planetService.getPlanetConfig();
    }

    public void updateUserName(UserContext userContext, String name) {
        PlayerBase playerBase = baseItemService.getPlayerBase4HumanPlayerId(userContext.getHumanPlayerId());
        if (playerBase != null) {
            playerBase = baseItemService.changeBaseNameChanged(playerBase.getBaseId(), name);
            clientGameConnectionService.onBaseNameChanged(playerBase);
        }
    }

    public void updateHumanPlayerId(UserContext userContext) {
        PlayerBase playerBase = baseItemService.getPlayerBase4HumanPlayerId(userContext.getHumanPlayerId());
        if (playerBase != null) {
            playerBase = baseItemService.updateHumanPlayerId(playerBase.getBaseId(), userContext.getHumanPlayerId());
            clientGameConnectionService.onBaseHumanPlayerIdChanged(playerBase);
        }
        questService.updateHumanPlayerId(userContext.getHumanPlayerId());
    }

    @Override
    public Integer getLevel(PlayerBaseInfo playerBaseInfo) {
        if (playerBaseInfo.getHumanPlayerId() == null) {
            throw new IllegalStateException("Can not restore base with id: " + playerBaseInfo.getBaseId() + " name: " + playerBaseInfo.getName() + " may be this is a bot");
        }
        return userService.getUserContextTransactional(playerBaseInfo.getHumanPlayerId()).getLevelId();

    }

    @Override
    public Map<Integer, Integer> getUnlockedItemLimit(PlayerBaseInfo playerBaseInfo) {
        if (playerBaseInfo.getHumanPlayerId() == null) {
            throw new IllegalStateException("Can not restore base with id: " + playerBaseInfo.getBaseId() + " name: " + playerBaseInfo.getName() + " may be this is a bot");
        }
        return userService.getUserContextTransactional(playerBaseInfo.getHumanPlayerId()).getUnlockedItemLimit();
    }

    @Override
    public String getName(PlayerBaseInfo playerBaseInfo) {
        if (playerBaseInfo.getHumanPlayerId() == null) {
            throw new IllegalStateException("Can not restore base with id: " + playerBaseInfo.getBaseId() + " name: " + playerBaseInfo.getName() + " may be this is a bot");
        }
        return userService.getUserContextTransactional(playerBaseInfo.getHumanPlayerId()).getName();
    }

    @Override
    public void onPostTick(SynchronizationSendingContext synchronizationSendingContext) {
//        Set<SyncBaseItem> tmpAlreadySentSyncBaseItems = alreadySentSyncBaseItems;
//        alreadySentSyncBaseItems = new HashSet<>();
//        pathingChangesDisruptor.onPostTick(synchronizationSendingContext.getCollisions(), tmpAlreadySentSyncBaseItems);
    }
}
