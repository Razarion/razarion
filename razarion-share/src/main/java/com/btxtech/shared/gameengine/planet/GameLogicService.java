package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.DecimalPosition;
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

import javax.inject.Inject;
import javax.inject.Provider;
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
    private final Logger logger = Logger.getLogger(GameLogicService.class.getName());

    private final Provider<QuestService> questServiceInstance;

    private final Provider<BotService> botServiceInstance;

    private final Provider<GuardingItemService> guardingItemServiceInstance;
    private Optional<GameLogicListener> gameLogicListener = Optional.empty();

    @Inject
    public GameLogicService(Provider<com.btxtech.shared.gameengine.planet.GuardingItemService> guardingItemServiceInstance, Provider<com.btxtech.shared.gameengine.planet.bot.BotService> botServiceInstance, Provider<com.btxtech.shared.gameengine.planet.quest.QuestService> questServiceInstance) {
        this.guardingItemServiceInstance = guardingItemServiceInstance;
        this.botServiceInstance = botServiceInstance;
        this.questServiceInstance = questServiceInstance;
    }

    public void setGameLogicListener(GameLogicListener gameLogicListener) {
        this.gameLogicListener = Optional.of(gameLogicListener);
    }

    public void onInsufficientFundsException(InsufficientFundsException e) {
    }

    public void onItemDoesNotExistException(ItemDoesNotExistException e) {
        logger.log(Level.SEVERE, "GameLogicService.onItemDoesNotExistException() ", e);
    }

    public void onMasterCommandSent(SyncBaseItem syncItem) {
        gameLogicListener.ifPresent(listener -> listener.onMasterCommandSent(syncItem));
    }

    public void onSlaveCommandSent(SyncBaseItem syncBaseItemSave, BaseCommand baseCommand) {
        gameLogicListener.ifPresent(listener -> listener.onSlaveCommandSent(syncBaseItemSave, baseCommand));
    }

    public void onBaseCreated(PlayerBaseFull playerBase) {
        gameLogicListener.ifPresent(listener -> listener.onBaseCreated(playerBase));
    }

    public void onBaseSlaveCreated(PlayerBase playerBase) {
        gameLogicListener.ifPresent(listener -> listener.onBaseSlaveCreated(playerBase));
    }

    public void onBaseKilled(PlayerBase playerBase, SyncBaseItem actor) {
        questServiceInstance.get().onBaseKilled(actor);
        gameLogicListener.ifPresent(listener -> listener.onBaseDeleted(playerBase, actor.getBase()));
    }

    public void onBaseRemoved(PlayerBase playerBase) {
        gameLogicListener.ifPresent(listener -> listener.onBaseDeleted(playerBase, null));
    }

    public void onSurrenderBase(PlayerBase playerBase) {
        gameLogicListener.ifPresent(listener -> listener.onBaseDeleted(playerBase, null));
    }

    public void onStartBuildingSyncBaseItem(SyncBaseItem createdBy, SyncBaseItem syncBaseItem) {
        gameLogicListener.ifPresent(listener -> listener.onStartBuildingSyncBaseItem(createdBy, syncBaseItem));
    }

    public void onSynBuilderStopped(SyncBaseItem syncBaseItem, SyncBaseItem currentBuildup) {
        gameLogicListener.ifPresent(listener -> listener.onSynBuilderStopped(syncBaseItem, currentBuildup));
    }

    public void onSyncItemLoaded(SyncBaseItem container, SyncBaseItem contained) {
        gameLogicListener.ifPresent(listener -> listener.onSyncItemLoaded(container, contained));
    }

    public void onSyncItemContainerUnloaded(SyncBaseItem container) {
        gameLogicListener.ifPresent(listener -> listener.onSyncItemContainerUnloaded(container));
    }

    public void onSyncItemUnloaded(SyncBaseItem contained) {
        gameLogicListener.ifPresent(listener -> listener.onSyncItemUnloaded(contained));
    }

    public void onHealthIncreased(SyncBaseItem syncBaseItem) {
    }

    public void onBuildup(SyncBaseItem syncBaseItem) {
        questServiceInstance.get().onSyncItemBuilt(syncBaseItem);
        guardingItemServiceInstance.get().add(syncBaseItem);
    }

    public void onAttacked(SyncBaseItem target, SyncBaseItem actor, double damage) {
    }

    public void onSyncFactoryProgress(SyncBaseItem syncBaseItem) {
    }

    public void onSyncFactoryStopped(SyncBaseItem syncBaseItem) {
    }

    public void onProjectileFired(SyncBaseItem syncBaseItem, DecimalPosition target) {
        gameLogicListener.ifPresent(listener -> listener.onProjectileFired(syncBaseItem, target));
    }

    public void onProjectileDetonation(SyncBaseItem syncBaseItem, DecimalPosition position) {
        gameLogicListener.ifPresent(listener -> listener.onProjectileDetonation(syncBaseItem.getBaseItemType().getId(), position));
    }

    public void onSpawnSyncItemStart(SyncBaseItem syncBaseItem) {
        gameLogicListener.ifPresent(listener -> listener.onSpawnSyncItemStart(syncBaseItem));
    }

    public void onSpawnSyncItemFinished(SyncBaseItem syncBaseItem) {
        questServiceInstance.get().onSyncItemBuilt(syncBaseItem);
    }


    public void onSpawnSyncItemNoSpan(SyncBaseItem syncBaseItem) {
        gameLogicListener.ifPresent(listener -> listener.onSpawnSyncItemNoSpan(syncBaseItem));
    }

    public void onSyncBaseItemKilledMaster(SyncBaseItem target, SyncBaseItem actor) {
        questServiceInstance.get().onSyncItemKilled(target, actor);
        gameLogicListener.ifPresent(listener -> listener.onSyncBaseItemKilledMaster(target, actor));
        botServiceInstance.get().onKill(target, actor.getBase());
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

    public void onBoxPicked(SyncBoxItem box, SyncBaseItem picker, BoxContent boxContent) {
        String userId = picker.getBase().getUserId();
        if (userId == null) {
            return;
        }

        gameLogicListener.ifPresent(listener -> listener.onBoxPicked(userId, boxContent));
        questServiceInstance.get().onSyncBoxItemPicked(userId);
    }

    public void onBoxDeleted(SyncBoxItem box) {
        gameLogicListener.ifPresent(listener -> listener.onSyncBoxDeleted(box));
    }

    public void onBuilderNoRazarion(SyncBaseItem syncBaseItem) {
    }

    public void onItemLimitExceededExceptionBuilder(SyncBaseItem syncBaseItem) {
    }

    public void onHouseSpaceExceededExceptionBuilder(SyncBaseItem syncBaseItem) {
    }

    public void onBuildingSyncItem(SyncBaseItem syncBaseItem, SyncBaseItem createdBy) {
        if (syncBaseItem.getBase().getCharacter().isBot()) {
            botServiceInstance.get().onBotSyncBaseItemCreated(syncBaseItem, createdBy);
        }
        gameLogicListener.ifPresent(listener -> listener.onBuildingSyncItem(syncBaseItem, createdBy));
    }

    public void onFactoryLimitation4ItemTypeExceeded() {
    }

    public void onFactoryHouseSpaceExceeded() {
    }

    public void onFactoryNoRazarion() {
    }

    public void onFactorySyncItem(SyncBaseItem syncBaseItem, SyncBaseItem createdBy) {
        if (syncBaseItem.getBase().getCharacter().isBot()) {
            botServiceInstance.get().onBotSyncBaseItemCreated(syncBaseItem, createdBy);
        } else {
            questServiceInstance.get().onSyncItemBuilt(syncBaseItem);
        }
        gameLogicListener.ifPresent(listener -> listener.onFactorySyncItem(syncBaseItem, createdBy));
    }

    public void onSyncBaseItemIdle(SyncBaseItem syncBaseItem) {
        gameLogicListener.ifPresent(listener -> listener.onSyncBaseItemIdle(syncBaseItem));
    }

    public void onQuestProgressUpdate(String userId, QuestProgressInfo questProgressInfo) {
        gameLogicListener.ifPresent(listener -> listener.onQuestProgressUpdate(userId, questProgressInfo));
    }

    public void onEnergyStateChanged(PlayerBase playerBase, int consuming, int generating) {
        gameLogicListener.ifPresent(listener -> listener.onEnergyStateChanged(playerBase, consuming, generating));
    }

    public void onResourcesBalanceChanged(PlayerBase playerBase, int resources) {
        gameLogicListener.ifPresent(listener -> listener.onResourcesBalanceChanged(playerBase, resources));
    }
}
