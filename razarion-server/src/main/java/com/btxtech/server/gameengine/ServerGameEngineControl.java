package com.btxtech.server.gameengine;

import com.btxtech.server.connection.ClientSystemConnectionService;
import com.btxtech.server.persistence.StaticGameConfigPersistence;
import com.btxtech.server.persistence.backup.BackupPlanetOverview;
import com.btxtech.server.persistence.backup.PlanetBackupMongoDb;
import com.btxtech.server.persistence.server.ServerGameEnginePersistence;
import com.btxtech.server.user.SecurityCheck;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.SlaveSyncItemInfo;
import com.btxtech.shared.gameengine.StaticGameInitEvent;
import com.btxtech.shared.gameengine.datatypes.BackupPlanetInfo;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.command.BaseCommand;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.BoxService;
import com.btxtech.shared.gameengine.planet.GameLogicListener;
import com.btxtech.shared.gameengine.planet.GameLogicService;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.ResourceService;
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
public class ServerGameEngineControl implements GameLogicListener {
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
    private final Object reloadLook = new Object();

    public void start(BackupPlanetInfo backupPlanetInfo, boolean activateQuests) {
        PlanetConfig planetConfig = serverGameEnginePersistence.readPlanetConfig();
        BackupPlanetInfo finaBackupPlanetInfo = setupBackupPlanetInfo(backupPlanetInfo, planetConfig);
        gameEngineInitEvent.fire(new StaticGameInitEvent(staticGameConfigPersistence.loadStaticGameConfig()));
        terrainShapeService.start();
        planetService.initialise(planetConfig, GameEngineMode.MASTER, serverGameEnginePersistence.readMasterPlanetConfig(), null, () -> {
            gameLogicService.setGameLogicListener(this);
            planetService.start();
            if (finaBackupPlanetInfo != null) {
                planetService.restoreBases(finaBackupPlanetInfo);
            }
            resourceService.startResourceRegions();
            boxService.startBoxRegions(serverGameEnginePersistence.readBoxRegionConfigs());
            botService.startBots(serverGameEnginePersistence.readBotConfigs());
            //planetService.enableTracking(true);
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
            botService.startBots(serverGameEnginePersistence.readBotConfigs());
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
            currentPlanetConfig.setStartBaseItemTypeId(newPlanetConfig.getStartRazarion());
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
        planetService.stop();
        botService.killAllBots();
    }

    public void shutdown() {
        try {
            planetBackupMongoDb.saveBackup(planetService.backup(false));
        } catch (JsonProcessingException e) {
            exceptionHandler.handleException(e);
        }
        stop();
    }

    public void onLevelChanged(HumanPlayerId humanPlayerId, int levelId) {
        baseItemService.updateLevel(humanPlayerId, levelId);
    }

    public SlaveSyncItemInfo generateSlaveSyncItemInfo(UserContext userContext) {
        return planetService.generateSlaveSyncItemInfo(userContext);
    }

    @Override
    public void onBaseCreated(PlayerBaseFull playerBase) {
        clientGameConnectionService.onBaseCreated(playerBase);
    }

    @Override
    public void onBaseDeleted(PlayerBase playerBase) {
        clientGameConnectionService.onBaseDeleted(playerBase);
    }

    @Override
    public void onSpawnSyncItemStart(SyncBaseItem syncBaseItem) {
        clientGameConnectionService.onSpawnSyncItemStart(syncBaseItem);
        clientGameConnectionService.sendSyncBaseItem(syncBaseItem);
    }

    @Override
    public void onSyncBaseItemKilledMaster(SyncBaseItem syncBaseItem, SyncBaseItem actor) {
        clientGameConnectionService.onSyncItemRemoved(syncBaseItem, true);
    }

    @Override
    public void onSyncBaseItemRemoved(SyncBaseItem syncBaseItem) {
        clientGameConnectionService.onSyncItemRemoved(syncBaseItem, false);
    }

    @Override
    public void onResourceCreated(SyncResourceItem syncResourceItem) {
        clientGameConnectionService.onSyncResourceItemCreated(syncResourceItem);
    }

    @Override
    public void onResourceDeleted(SyncResourceItem syncResourceItem) {
        clientGameConnectionService.onSyncItemRemoved(syncResourceItem, false);
    }

    @Override
    public void onBoxCreated(SyncBoxItem syncBoxItem) {
        clientGameConnectionService.onSyncBoxCreated(syncBoxItem);
    }

    @Override
    public void onSyncBoxDeleted(SyncBoxItem box) {
        clientGameConnectionService.onSyncItemRemoved(box, false);
    }

    @Override
    public void onBoxPicked(HumanPlayerId humanPlayerId, BoxContent boxContent) {
        serverInventoryService.onBoxPicked(humanPlayerId, boxContent);
        systemConnectionService.onBoxPicked(humanPlayerId, boxContent);
    }

    @Override
    public void onSyncBaseItemIdle(SyncBaseItem syncBaseItem) {
        clientGameConnectionService.sendSyncBaseItem(syncBaseItem);
    }

    @Override
    public void onCommandSent(SyncBaseItem syncItem, BaseCommand baseCommand) {
        clientGameConnectionService.sendSyncBaseItem(syncItem);
    }

    @Override
    public void onSynBuilderStopped(SyncBaseItem syncBaseItem, SyncBaseItem currentBuildup) {
        if (currentBuildup != null) {
            clientGameConnectionService.sendSyncBaseItem(currentBuildup);
        }
        clientGameConnectionService.sendSyncBaseItem(syncBaseItem);
    }

    @Override
    public void onStartBuildingSyncBaseItem(SyncBaseItem createdBy, SyncBaseItem syncBaseItem) {
        clientGameConnectionService.sendSyncBaseItem(syncBaseItem);
        clientGameConnectionService.sendSyncBaseItem(createdBy);
    }

    @Override
    public void onQuestProgressUpdate(HumanPlayerId humanPlayerId, QuestProgressInfo questProgressInfo) {
        systemConnectionService.onQuestProgressInfo(humanPlayerId, questProgressInfo);
    }

    @Override
    public void onResourcesBalanceChanged(PlayerBase playerBase, int resources) {
        clientGameConnectionService.sendResourcesBalanceChanged(playerBase, resources);
    }

    public PlanetConfig getPlanetConfig() {
        return planetService.getPlanetConfig();
    }
}
