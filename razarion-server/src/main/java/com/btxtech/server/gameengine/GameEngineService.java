package com.btxtech.server.gameengine;

import com.btxtech.server.persistence.GameEngineConfigPersistence;
import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.GameEngineInitEvent;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.command.BaseCommand;
import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.GameLogicListener;
import com.btxtech.shared.gameengine.planet.GameLogicService;
import com.btxtech.shared.gameengine.planet.PlanetService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.system.ExceptionHandler;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 * Created by Beat
 * 18.04.2017.
 */
@ApplicationScoped
public class GameEngineService implements GameLogicListener {
    @Inject
    private Event<GameEngineInitEvent> gameEngineInitEvent;
    @Inject
    private PlanetService planetService;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private GameEngineConfigPersistence gameEngineConfigPersistence;
    @Inject
    private GameLogicService gameLogicService;
    @Inject
    private ClientGameConnectionService clientGameConnectionService;

    public void start() {
        GameEngineConfig gameEngineConfig = gameEngineConfigPersistence.load4Server();
        gameEngineInitEvent.fire(new GameEngineInitEvent(gameEngineConfig));
        planetService.initialise(gameEngineConfig.getPlanetConfig());
        gameLogicService.setGameLogicListener(this);
        planetService.start();
    }

    public void stop() {
        planetService.stop();
    }

    public void fillSyncItems(PlanetConfig planetConfig, UserContext userContext) {
        planetService.fillSyncItems(planetConfig, userContext);
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

    }

    @Override
    public void onBoxPicked(HumanPlayerId humanPlayerId, BoxContent boxContent) {

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
}
