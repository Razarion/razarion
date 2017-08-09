package com.btxtech.server.gameengine;

import com.btxtech.server.connection.ClientSystemConnectionService;
import com.btxtech.server.persistence.StaticGameConfigPersistence;
import com.btxtech.server.persistence.server.ServerGameEnginePersistence;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.SlaveSyncItemInfo;
import com.btxtech.shared.gameengine.StaticGameInitEvent;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.command.BaseCommand;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.shared.gameengine.planet.GameLogicListener;
import com.btxtech.shared.gameengine.planet.GameLogicService;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.gameengine.planet.quest.QuestService;
import com.btxtech.shared.system.ExceptionHandler;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 18.04.2017.
 */
@ApplicationScoped
public class GameEngineService implements GameLogicListener {
    private Logger logger = Logger.getLogger(GameEngineService.class.getName());
    @Inject
    private Event<StaticGameInitEvent> gameEngineInitEvent;
    @Inject
    private PlanetService planetService;
    @Inject
    private ExceptionHandler exceptionHandler;
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

    public void start() {
        try {
            gameEngineInitEvent.fire(new StaticGameInitEvent(staticGameConfigPersistence.loadStaticGameConfig()));
            terrainShapeService.start();
            planetService.initialise(serverGameEnginePersistence.readPlanetConfig(), GameEngineMode.MASTER, serverGameEnginePersistence.readMasterPlanetConfig(), null, () -> {
                gameLogicService.setGameLogicListener(this);
                planetService.start(serverGameEnginePersistence.readBotConfigs());
            }, failText -> logger.severe("TerrainSetup failed: " + failText));
            activateQuests();
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
        }
    }

    private void activateQuests() {
        for (Map.Entry<HumanPlayerId, QuestConfig> entry : userService.findUserQuestForPlanet(serverGameEnginePersistence.readAllQuestIds()).entrySet()) {
            questService.activateCondition(entry.getKey(), entry.getValue());
        }
    }

    public void stop() {
        planetService.stop();
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
}
