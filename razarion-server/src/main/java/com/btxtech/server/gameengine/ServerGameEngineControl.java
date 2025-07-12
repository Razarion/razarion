package com.btxtech.server.gameengine;

import com.btxtech.server.model.Roles;
import com.btxtech.server.model.engine.BackupPlanetOverview;
import com.btxtech.server.service.engine.PlanetCrudService;
import com.btxtech.server.service.engine.ServerGameEngineService;
import com.btxtech.server.service.engine.StaticGameConfigService;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.datatypes.UserContext;
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
import jakarta.annotation.security.RolesAllowed;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class ServerGameEngineControl implements GameLogicListener, BaseRestoreProvider, PlanetTickListener {
    private final Logger logger = Logger.getLogger(ServerGameEngineControl.class.getName());
    private final Object reloadLook = new Object();
    @Inject
    private InitializeService initializeService;
    @Inject
    private PlanetService planetService;
    @Inject
    private StaticGameConfigService staticGameConfigPersistence;
    @Inject
    private GameLogicService gameLogicService;
    @Inject
    private ClientGameConnectionService clientGameConnectionService;
    @Inject
    private ClientSystemConnectionService systemConnectionService;
    @Inject
    private ServerGameEngineService serverGameEngineService;
    @Inject
    private PlanetCrudService planetCrudService;
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
    // @Inject
    // TODO private PlanetBackupMongoDb planetBackupMongoDb;
    // @Inject
    // TODOprivate ServerInventoryService serverInventoryService;
    private boolean running;

    public void start(BackupPlanetInfo backupPlanetInfo, boolean activateQuests) {
        List<ServerGameEngineConfig> serverGameEngineConfigs = serverGameEngineService.read();
        if (serverGameEngineConfigs.isEmpty()) {
            return;
        }
        ServerGameEngineConfig serverGameEngineConfig = serverGameEngineConfigs.get(0);
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
        }, failText -> logger.severe("TerrainSetup failed: " + failText));
        if (activateQuests) {
            activateQuests(finaBackupPlanetInfo);
        }
        running = true;
    }

    private BackupPlanetInfo setupBackupPlanetInfo(BackupPlanetInfo backupPlanetInfo, PlanetConfig planetConfig) {
        if (backupPlanetInfo != null) {
            return backupPlanetInfo;
        } else {
            // TODO return planetBackupMongoDb.loadLastBackup(planetConfig.getId());
            BackupPlanetInfo empty = new BackupPlanetInfo();
            empty.setBackupComparisionInfos(new ArrayList<>());
            empty.setPlayerBaseInfos(new ArrayList<>());
            empty.setSyncBaseItemInfos(new ArrayList<>());
            return empty;
        }
    }

    @RolesAllowed(Roles.ADMIN)
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

    @RolesAllowed(Roles.ADMIN)
    public void restartResourceRegions() {
        synchronized (reloadLook) {
            resourceService.reloadResourceRegions(serverGameEngineService.readMasterPlanetConfig().getResourceRegionConfigs());
        }
    }

    @RolesAllowed(Roles.ADMIN)
    public void restartBoxRegions() {
        synchronized (reloadLook) {
            boxService.stopBoxRegions();
            boxService.startBoxRegions(serverGameEngineService.readBoxRegionConfigs());
        }
    }

    @RolesAllowed(Roles.ADMIN)
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

    @RolesAllowed(Roles.ADMIN)
    public void backupPlanet() throws JsonProcessingException {
        if (!running) {
            return;
        }
        long time = System.currentTimeMillis();
        BackupPlanetInfo backupPlanetInfo = planetService.backup();
        // TODO planetBackupMongoDb.saveBackup(backupPlanetInfo);
        logger.info("ServerGameEngineControl.backupPlanet() in: " + (System.currentTimeMillis() - time));
    }

    @RolesAllowed(Roles.ADMIN)
    public void restorePlanet(BackupPlanetOverview backupPlanetOverview) {
//  TODO      long time = System.currentTimeMillis();
//        BackupPlanetInfo backupPlanetInfo = planetBackupMongoDb.loadBackup(backupPlanetOverview);
//        stop();
//        start(backupPlanetInfo, true);
//        logger.info("ServerGameEngineControl.restorePlanet() in: " + (System.currentTimeMillis() - time));
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
        try {
            if (running) {
                // TODO planetBackupMongoDb.saveBackup(planetService.backup());
            }
        } catch (Throwable t) {
            logger.log(Level.WARNING, t.getMessage(), t);
        }
        try {
            stop();
        } catch (Throwable t) {
            logger.log(Level.WARNING, t.getMessage(), t);
        }
    }

    public void onLevelChanged(String userId, int levelId) {
        baseItemService.updateLevel(userId, levelId);
    }

    @Override
    public void onBaseCreated(PlayerBaseFull playerBase) {
        clientGameConnectionService.onBaseCreated(playerBase);
        // TODO itemTrackerPersistence.onBaseCreated(playerBase);
    }

    @Override
    public void onBaseDeleted(PlayerBase playerBase, PlayerBase actor) {
        clientGameConnectionService.onBaseDeleted(playerBase);
        // TODO itemTrackerPersistence.onBaseDeleted(playerBase, actor);
    }

    @Override
    public void onSpawnSyncItemStart(SyncBaseItem syncBaseItem) {
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

    public void updateUserName(UserContext userContext, String name) {
        PlayerBase playerBase = baseItemService.getPlayerBase4UserId(userContext.getUserId());
        if (playerBase != null) {
            playerBase = baseItemService.changeBaseNameChanged(playerBase.getBaseId(), name);
            clientGameConnectionService.onBaseNameChanged(playerBase);
        }
    }

    public void updateHumanPlayerId(UserContext userContext) {
        PlayerBase playerBase = baseItemService.getPlayerBase4UserId(userContext.getUserId());
        if (playerBase != null) {
            playerBase = baseItemService.updateHumanPlayerId(playerBase.getBaseId(), userContext.getUserId());
            clientGameConnectionService.onBaseHumanPlayerIdChanged(playerBase);
        }
        questService.updateUserId(userContext.getUserId());
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
