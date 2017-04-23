package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.command.BaseCommand;
import com.btxtech.shared.gameengine.datatypes.exception.InsufficientFundsException;
import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.exception.PathCanNotBeFoundException;
import com.btxtech.shared.gameengine.datatypes.exception.PlaceCanNotBeFoundException;
import com.btxtech.shared.gameengine.datatypes.exception.PositionTakenException;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.bot.BotService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.gameengine.planet.quest.QuestService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 25.07.2016.
 */
@ApplicationScoped
public class GameLogicService {
    private Logger logger = Logger.getLogger(GameLogicService.class.getName());
    @Inject
    private PlanetService planetService;
    @Inject
    private QuestService questService;
    @Inject
    private BotService botService;
    private Optional<GameLogicListener> gameLogicListener = Optional.empty();

    public void setGameLogicListener(GameLogicListener gameLogicListener) {
        this.gameLogicListener = Optional.of(gameLogicListener);
    }

    public void onInsufficientFundsException(InsufficientFundsException e) {
        // TODO connectionService.sendSyncInfo(syncItem);
        // TODO baseService.sendAccountBaseUpdate(syncItem);
        logger.log(Level.SEVERE, "GameLogicService.onInsufficientFundsException() ", e);
    }

    public void onPathCanNotBeFoundException(PathCanNotBeFoundException e) {
        // TODO connectionService.sendSyncInfo(syncItem);
        logger.log(Level.SEVERE, "GameLogicService.onPathCanNotBeFoundException() ", e);
    }

    public void onItemDoesNotExistException(ItemDoesNotExistException e) {
        // TODO connectionService.sendSyncInfo(syncItem);
        logger.log(Level.SEVERE, "GameLogicService.onItemDoesNotExistException() ", e);
    }

    public void onPositionTakenException(PositionTakenException e) {
        e.printStackTrace();
    }

    public void onPlaceCanNotBeFoundException(PlaceCanNotBeFoundException e) {
        e.printStackTrace();
    }

    public void onCommandSent(SyncBaseItem syncItem, BaseCommand baseCommand) {
        System.out.println("GameLogicService.onCommandSent() " + syncItem + " " + baseCommand);
        gameLogicListener.ifPresent(listener -> listener.onCommandSent(syncItem, baseCommand));
    }

    public void onSyncItemDeactivated(SyncItem activeItem) {
        System.out.println("GameLogicService.onSyncItemDeactivated(): " + activeItem);


//  TODO      connectionService.sendSyncInfo(activeItem);

//   TODO     if (syncBaseItem.hasSyncHarvester()) {
//   TODO         activityService.sendAccountBaseUpdate((SyncBaseItem) activeItem);
//   TODO     }
//   TODO     if (syncBaseItem.isMoneyEarningOrConsuming()) {
//   TODO         activityService.sendAccountBaseUpdate((SyncBaseItem) activeItem);
//    TODO    }

    }

    public void onBaseCreated(PlayerBaseFull playerBase) {
        System.out.println("GameLogicService.onBaseCreated(): " + playerBase);
        gameLogicListener.ifPresent(listener -> listener.onBaseCreated(playerBase));
    }

    public void onBaseSlaveCreated(PlayerBase playerBase) {
        System.out.println("GameLogicService.onBaseSlaveCreated(): " + playerBase);
        gameLogicListener.ifPresent(listener -> listener.onBaseSlaveCreated(playerBase));
    }

    public void onBaseKilled(PlayerBase playerBase, SyncBaseItem actor) {
        System.out.println("GameLogicService.onBaseKilled(). base: " + playerBase + " killed by: " + actor);
        questService.onBaseKilled(actor);
        gameLogicListener.ifPresent(listener -> listener.onBaseDeleted(playerBase));
    }

    public void onBaseRemoved(PlayerBase playerBase) {
        System.out.println("GameLogicService.onBaseRemoved(). base: " + playerBase);
        gameLogicListener.ifPresent(listener -> listener.onBaseDeleted(playerBase));
    }

    public void onSurrenderBase(PlayerBase playerBase) {
        System.out.println("GameLogicService.onSurrenderBase(): " + playerBase);
        gameLogicListener.ifPresent(listener -> listener.onBaseDeleted(playerBase));
    }

    public void onStartBuildingSyncBaseItem(SyncBaseItem createdBy, SyncBaseItem syncBaseItem) {
        System.out.println("GameLogicService.onStartBuildingSyncBaseItem(): " + createdBy + " " + syncBaseItem);
        gameLogicListener.ifPresent(listener -> listener.onStartBuildingSyncBaseItem(createdBy, syncBaseItem));
    }

    public void onSynBuilderStopped(SyncBaseItem syncBaseItem, SyncBaseItem currentBuildup) {
        System.out.println("GameLogicService.onSynBuilderStopped(): " + syncBaseItem + " currentBuildup: " + currentBuildup);
        gameLogicListener.ifPresent(listener -> listener.onSynBuilderStopped(syncBaseItem, currentBuildup));
    }

    public void onSyncItemUnloaded(SyncBaseItem syncItem) {
        System.out.println("GameLogicService.onSyncItemUnloaded(): " + syncItem);
        // TODO connectionService.sendSyncInfo(syncItem);

    }

    public void onSyncItemLoaded(SyncBaseItem syncBaseItem, SyncBaseItem loadedSyncBaseItem) {
        System.out.println("GameLogicService.onSyncItemLoaded(): " + syncBaseItem + " " + loadedSyncBaseItem);
        // TODO connectionService.sendSyncInfo(syncItem);
    }

    public void onSyncBuilderStopped(SyncBaseItem syncBaseItem) {
        System.out.println("GameLogicService.onSyncBuilderStopped(): " + syncBaseItem);
    }

    public void onHealthIncreased(SyncBaseItem syncBaseItem) {
        System.out.println("GameLogicService.onHealthIncreased(): " + syncBaseItem);
    }

    public void onBuildup(SyncBaseItem syncBaseItem) {
        System.out.println("GameLogicService.onBuildup(): " + syncBaseItem);
        questService.onSyncItemBuilt(syncBaseItem);
    }

    public void onAttacked(SyncBaseItem target, SyncBaseItem actor, double damage) {
        // System.out.println("GameLogicService.onAttacked(). target: " + target + " actor: " + actor + ". damage: " + damage);
    }

    public void onSyncFactoryProgress(SyncBaseItem syncBaseItem) {
        System.out.println("GameLogicService.onSyncFactoryProgress(): " + syncBaseItem);
    }

    public void onSyncFactoryStopped(SyncBaseItem syncBaseItem) {
        System.out.println("GameLogicService.onSyncFactoryStopped(): " + syncBaseItem);
    }

    public void onProjectileFired(SyncBaseItem syncBaseItem, Vertex muzzlePosition, Vertex target) {
        // System.out.println("GameLogicService.onProjectileFired(): " + System.currentTimeMillis() + ": " + syncBaseItem);
        gameLogicListener.ifPresent(listener -> listener.onProjectileFired(syncBaseItem.getItemType().getId(), muzzlePosition, target));
    }

    public void onProjectileDetonation(SyncBaseItem syncBaseItem, Vertex position) {
        // System.out.println("GameLogicService.onProjectileDetonation(): " + System.currentTimeMillis() + ": " + syncBaseItem);
        gameLogicListener.ifPresent(listener -> listener.onProjectileDetonation(syncBaseItem.getBaseItemType().getId(), position));
    }

    public void onSpawnSyncItemStart(SyncBaseItem syncBaseItem) {
        System.out.println("GameLogicService.onSpawnSyncItemStart(): " + syncBaseItem);
        // gameLogicDelegate.ifPresent(delegate -> delegate.onSpawnSyncItemStart(syncBaseItem));
        gameLogicListener.ifPresent(listener -> listener.onSpawnSyncItemStart(syncBaseItem));
    }

    public void onSpawnSyncItemFinished(SyncBaseItem syncBaseItem) {
        System.out.println("GameLogicService.onSpawnSyncItemFinished(): " + syncBaseItem);
        questService.onSyncItemBuilt(syncBaseItem);
    }

    public void onKilledSyncBaseItem(SyncBaseItem target, SyncBaseItem actor) {
        System.out.println("GameLogicService.onKilledSyncBaseItem(). target: " + target + " actor: " + actor);
        questService.onSyncItemKilled(target, actor);
        gameLogicListener.ifPresent(listener -> listener.onSyncItemKilled(target, actor));
        if (target.getBase().getCharacter().isBot()) {
            botService.enrageOnKill(target, actor.getBase());
        }
    }

    public void onSyncBaseItemRemoved(SyncBaseItem target) {
        System.out.println("GameLogicService.onSyncBaseItemRemoved(). target: " + target);
        gameLogicListener.ifPresent(listener -> listener.onSyncItemRemoved(target));
    }

    // TODO when to call?
    public void onSyncBaseItemCreated(SyncBaseItem syncBaseItem, SyncBaseItem createdBy) {
        System.out.println("GameLogicService.onSyncBaseItemCreated(): " + syncBaseItem);
        // TODO planetService.syncItemActivated(syncBaseItem);
        // TODO historyService.addItemCreatedEntry(syncBaseItem);
        // TODO planetService.addGuardingBaseItem(syncBaseItem);
        // TODO planetService.interactionGuardingItems(syncBaseItem);
        // TODO connectionService.sendSyncInfo(syncBaseItem);
        // TODO may be inform bot about new bot item
    }

    public void onResourcesHarvested(SyncBaseItem syncBaseItem, double harvestedResources, SyncResourceItem resource) {
        questService.onHarvested(syncBaseItem, harvestedResources);
        // System.out.println("GameLogicService.onResourcesHarvested(). Harvester: " + syncBaseItem + ". Amount: " + harvestedResources + ". SyncResourceItem: " + resource);
    }

    public void onResourceCreated(SyncResourceItem syncResourceItem) {
        System.out.println("GameLogicService.onResourceCreated(): " + syncResourceItem);
        gameLogicListener.ifPresent(listener -> listener.onResourceCreated(syncResourceItem));
    }

    public void onResourceExhausted(SyncResourceItem syncResourceItem) {
        System.out.println("GameLogicService.onResourceExhausted(): " + syncResourceItem);
        gameLogicListener.ifPresent(listener -> listener.onResourceDeleted(syncResourceItem));
    }

    public void onBoxCreated(SyncBoxItem syncBoxItem) {
        System.out.println("GameLogicService.onBoxCreated(): " + syncBoxItem);
        gameLogicListener.ifPresent(listener -> listener.onBoxCreated(syncBoxItem));
    }

    public void onBoxPicket(SyncBoxItem box, SyncBaseItem picker, BoxContent boxContent) {
        System.out.println("GameLogicService.onBoxPicket(): " + box + " picker: " + picker + " boxContent: " + boxContent);
        gameLogicListener.ifPresent(listener -> listener.onBoxPicked(picker.getBase().getHumanPlayerId(), boxContent));
        gameLogicListener.ifPresent(listener -> listener.onSyncBoxDeleted(box));
        questService.onSyncBoxItemPicked(picker);
    }

    public void onBuilderNoMoney(SyncBaseItem syncBaseItem) {
        System.out.println("GameLogicService.onBuilderNoMoney(): ");
    }

    public void onItemLimitExceededExceptionBuilder(SyncBaseItem syncBaseItem) {
        System.out.println("GameLogicService.onItemLimitExceededExceptionBuilder(): " + syncBaseItem);
    }

    public void onHouseSpaceExceededExceptionBuilder(SyncBaseItem syncBaseItem) {
        System.out.println("GameLogicService.onHouseSpaceExceededExceptionBuilder(): " + syncBaseItem);
    }

    public void onBuildingSyncItem(SyncBaseItem syncBaseItem, BaseItemType toBeBuilt) {
        System.out.println("GameLogicService.onBuildingSyncItem(): " + syncBaseItem + " toBeBuilt: " + toBeBuilt);
    }

    public void onFactoryLevelLimitation4ItemTypeExceeded() {
        System.out.println("GameLogicService.onFactoryLevelLimitation4ItemTypeExceeded(): ");
    }

    public void onFactoryHouseSpaceExceeded() {
        System.out.println("GameLogicService.onFactoryHouseSpaceExceeded(): ");
    }

    public void onFactoryNoMoney() {
        System.out.println("GameLogicService.onFactoryNoMoney(): ");
    }

    public void onFactorySyncItem(SyncBaseItem syncBaseItem, BaseItemType toBeBuilt) {
        System.out.println("GameLogicService.onFactorySyncItem(): ");
        questService.onSyncItemBuilt(syncBaseItem);
    }

    public void onSyncBaseItemIdle(SyncBaseItem syncBaseItem) {
        // gameLogicDelegate.ifPresent(delegate -> delegate.onSyncBaseItemIdle(syncBaseItem));
        gameLogicListener.ifPresent(listener -> listener.onSyncBaseItemIdle(syncBaseItem));
    }
}
