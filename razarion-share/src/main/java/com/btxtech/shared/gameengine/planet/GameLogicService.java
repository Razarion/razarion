package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.command.BaseCommand;
import com.btxtech.shared.gameengine.datatypes.exception.InsufficientFundsException;
import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.shared.gameengine.planet.bot.BotService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.gameengine.planet.quest.QuestService;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 25.07.2016.
 */
@Singleton
public class GameLogicService {
    private Logger logger = Logger.getLogger(GameLogicService.class.getName());
    @Inject
    private Instance<QuestService> questServiceInstance;
    @Inject
    private Instance<BotService> botServiceInstance;
    private Optional<GameLogicListener> gameLogicListener = Optional.empty();

    public void setGameLogicListener(GameLogicListener gameLogicListener) {
        this.gameLogicListener = Optional.of(gameLogicListener);
    }

    public void onInsufficientFundsException(InsufficientFundsException e) {
    }

    public void onItemDoesNotExistException(ItemDoesNotExistException e) {
        logger.log(Level.SEVERE, "GameLogicService.onItemDoesNotExistException() ", e);
    }

    public void onCommandSent(SyncBaseItem syncItem, BaseCommand baseCommand) {
        gameLogicListener.ifPresent(listener -> listener.onCommandSent(syncItem, baseCommand));
    }

    public void onBaseCreated(PlayerBaseFull playerBase) {
        gameLogicListener.ifPresent(listener -> listener.onBaseCreated(playerBase));
    }

    public void onBaseSlaveCreated(PlayerBase playerBase) {
        gameLogicListener.ifPresent(listener -> listener.onBaseSlaveCreated(playerBase));
    }

    public void onBaseKilled(PlayerBase playerBase, SyncBaseItem actor) {
        questServiceInstance.get().onBaseKilled(actor);
        gameLogicListener.ifPresent(listener -> listener.onBaseDeleted(playerBase));
    }

    public void onBaseRemoved(PlayerBase playerBase) {
        gameLogicListener.ifPresent(listener -> listener.onBaseDeleted(playerBase));
    }

    public void onSurrenderBase(PlayerBase playerBase) {
        gameLogicListener.ifPresent(listener -> listener.onBaseDeleted(playerBase));
    }

    public void onStartBuildingSyncBaseItem(SyncBaseItem createdBy, SyncBaseItem syncBaseItem) {
        gameLogicListener.ifPresent(listener -> listener.onStartBuildingSyncBaseItem(createdBy, syncBaseItem));
    }

    public void onSynBuilderStopped(SyncBaseItem syncBaseItem, SyncBaseItem currentBuildup) {
        gameLogicListener.ifPresent(listener -> listener.onSynBuilderStopped(syncBaseItem, currentBuildup));
    }

    public void onSyncItemUnloaded(SyncBaseItem syncItem) {
    }

    public void onSyncItemLoaded(SyncBaseItem syncBaseItem, SyncBaseItem loadedSyncBaseItem) {
    }

    public void onHealthIncreased(SyncBaseItem syncBaseItem) {
    }

    public void onBuildup(SyncBaseItem syncBaseItem) {
        questServiceInstance.get().onSyncItemBuilt(syncBaseItem);
    }

    public void onAttacked(SyncBaseItem target, SyncBaseItem actor, double damage) {
    }

    public void onSyncFactoryProgress(SyncBaseItem syncBaseItem) {
    }

    public void onSyncFactoryStopped(SyncBaseItem syncBaseItem) {
    }

    public void onProjectileFired(SyncBaseItem syncBaseItem, Vertex muzzlePosition, Vertex target) {
        gameLogicListener.ifPresent(listener -> listener.onProjectileFired(syncBaseItem.getItemType().getId(), muzzlePosition, target));
    }

    public void onProjectileDetonation(SyncBaseItem syncBaseItem, Vertex position) {
        gameLogicListener.ifPresent(listener -> listener.onProjectileDetonation(syncBaseItem.getBaseItemType().getId(), position));
    }

    public void onSpawnSyncItemStart(SyncBaseItem syncBaseItem) {
        gameLogicListener.ifPresent(listener -> listener.onSpawnSyncItemStart(syncBaseItem));
    }

    public void onSpawnSyncItemFinished(SyncBaseItem syncBaseItem) {
        questServiceInstance.get().onSyncItemBuilt(syncBaseItem);
    }

    public void onSyncBaseItemKilledMaster(SyncBaseItem target, SyncBaseItem actor) {
        questServiceInstance.get().onSyncItemKilled(target, actor);
        gameLogicListener.ifPresent(listener -> listener.onSyncBaseItemKilledMaster(target, actor));
        if (target.getBase().getCharacter().isBot()) {
            botServiceInstance.get().enrageOnKill(target, actor.getBase());
        }
    }

    public void onSyncBaseItemKilledSlave(SyncBaseItem target) {
        gameLogicListener.ifPresent(listener -> listener.onSyncBaseItemKilledSlave(target));
    }

    public void onSyncBaseItemRemoved(SyncBaseItem target) {
        gameLogicListener.ifPresent(listener -> listener.onSyncBaseItemRemoved(target));
    }

    public void onResourcesHarvested(SyncBaseItem syncBaseItem, double harvestedResources, SyncResourceItem resource) {
        questServiceInstance.get().onHarvested(syncBaseItem, harvestedResources);
    }

    public void onResourceCreated(SyncResourceItem syncResourceItem) {
        gameLogicListener.ifPresent(listener -> listener.onResourceCreated(syncResourceItem));
    }

    public void onResourceExhausted(SyncResourceItem syncResourceItem) {
        gameLogicListener.ifPresent(listener -> listener.onResourceDeleted(syncResourceItem));
    }

    public void onBoxCreated(SyncBoxItem syncBoxItem) {
        gameLogicListener.ifPresent(listener -> listener.onBoxCreated(syncBoxItem));
    }

    public void onBoxPicket(SyncBoxItem box, SyncBaseItem picker, BoxContent boxContent) {
        gameLogicListener.ifPresent(listener -> listener.onBoxPicked(picker.getBase().getHumanPlayerId(), boxContent));
        gameLogicListener.ifPresent(listener -> listener.onSyncBoxDeleted(box));
        questServiceInstance.get().onSyncBoxItemPicked(picker);
    }


    public void onBoxDeletedSlave(SyncBoxItem box) {
        gameLogicListener.ifPresent(listener -> listener.onSyncBoxDeletedSlave(box));
    }

    public void onBuilderNoMoney(SyncBaseItem syncBaseItem) {
    }

    public void onItemLimitExceededExceptionBuilder(SyncBaseItem syncBaseItem) {
    }

    public void onHouseSpaceExceededExceptionBuilder(SyncBaseItem syncBaseItem) {
    }

    public void onBuildingSyncItem(SyncBaseItem syncBaseItem, SyncBaseItem createdBy) {
        if (syncBaseItem.getBase().getCharacter().isBot()) {
            botServiceInstance.get().onBotSyncBaseItemCreated(syncBaseItem, createdBy);
        }
    }

    public void onFactoryLevelLimitation4ItemTypeExceeded() {
    }

    public void onFactoryHouseSpaceExceeded() {
    }

    public void onFactoryNoMoney() {
    }

    public void onFactorySyncItem(SyncBaseItem syncBaseItem, SyncBaseItem createdBy) {
        if (syncBaseItem.getBase().getCharacter().isBot()) {
            botServiceInstance.get().onBotSyncBaseItemCreated(syncBaseItem, createdBy);
        } else {
            questServiceInstance.get().onSyncItemBuilt(syncBaseItem);
        }
    }

    public void onSyncBaseItemIdle(SyncBaseItem syncBaseItem) {
        gameLogicListener.ifPresent(listener -> listener.onSyncBaseItemIdle(syncBaseItem));
    }

    public void onQuestProgressUpdate(HumanPlayerId humanPlayerId, QuestProgressInfo questProgressInfo) {
        gameLogicListener.ifPresent(listener -> listener.onQuestProgressUpdate(humanPlayerId, questProgressInfo));
    }

    public void onEnergyStateChanged(PlayerBase playerBase, int consuming, int generating) {
        gameLogicListener.ifPresent(listener -> listener.onEnergyStateChanged(playerBase, consuming, generating));
    }
}
