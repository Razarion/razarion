package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.btxtech.shared.gameengine.datatypes.ModalDialogManager;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.command.BaseCommand;
import com.btxtech.shared.gameengine.datatypes.command.PathToDestinationCommand;
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
public class GameLogicService { // Rename to game control or so (See StoryboardService)
    private Logger logger = Logger.getLogger(GameLogicService.class.getName());
    @Inject
    private PlanetService planetService;
    @Inject
    private QuestService questService;
    @Inject
    private BotService botService;
    private Optional<GameLogicDelegate> gameLogicDelegate = Optional.empty();
    private ModalDialogManager modalDialogManager;

    public void setModalDialogManager(ModalDialogManager modalDialogManager) {
        this.modalDialogManager = modalDialogManager;
    }

    public void setGameLogicDelegate(GameLogicDelegate gameLogicDelegate) {
        this.gameLogicDelegate = Optional.of(gameLogicDelegate);
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
        // TODO connectionService.sendSyncInfo(syncItem);
        System.out.println("GameLogicService.onCommandSent() " + syncItem + " " + baseCommand);
        gameLogicDelegate.ifPresent(delegate -> delegate.onCommandSent(syncItem, baseCommand));
    }

    public void onInvalidPath(BaseCommand baseCommand) {
        logger.severe("Path is invalid: " + ((PathToDestinationCommand) baseCommand).getPathToDestination());
        // TODO  connectionService.sendSyncInfo(syncItem);
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

    public void onBaseCreated(PlayerBase playerBase) {
        // TODO sendBaseChangedPacket(BaseChangedPacket.Type.CREATED, base.getSimpleBase());
        System.out.println("GameLogicService.onBaseCreated(): " + playerBase);
    }

    public void onBaseKilled(PlayerBase playerBase, SyncBaseItem actor) {
        System.out.println("GameLogicService.onBaseKilled(). base: " + playerBase + " killed by: " + actor);
    }

    public void onBaseRemoved(PlayerBase playerBase) {
        System.out.println("GameLogicService.onBaseRemoved(). base: " + playerBase);
    }

    public void onStartBuildingSyncBaseItem(SyncBaseItem createdBy, SyncBaseItem syncBaseItem) {
        System.out.println("GameLogicService.onStartBuildingSyncBaseItem(): " + createdBy + " " + syncBaseItem);
    }

    public void onSynBuilderStopped(SyncBaseItem syncBaseItem, SyncBaseItem currentBuildup) {
        System.out.println("GameLogicService.onSynBuilderStopped(): " + syncBaseItem + " currentBuildup: " + currentBuildup);
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
        System.out.println("GameLogicService.onAttacked(). target: " + target + " actor: " + actor + ". damage: " + damage);
    }

    public void onSyncFactoryProgress(SyncBaseItem syncBaseItem) {
        System.out.println("GameLogicService.onSyncFactoryProgress(): " + syncBaseItem);
    }

    public void onSyncFactoryStopped(SyncBaseItem syncBaseItem) {
        System.out.println("GameLogicService.onSyncFactoryStopped(): " + syncBaseItem);
    }

    public void onProjectileFired(SyncBaseItem syncBaseItem, Vertex muzzlePosition, Vertex muzzleDirection, Integer clipId, long timeStamp) {
        System.out.println("GameLogicService.onProjectileFired(): " + System.currentTimeMillis() + ": " + syncBaseItem);
        gameLogicDelegate.ifPresent(delegate -> delegate.onProjectileFired(syncBaseItem, muzzlePosition, muzzleDirection, clipId, timeStamp));
    }

    public void onProjectileDetonation(SyncBaseItem syncBaseItem, Vertex position, Integer clipId, long timeStamp) {
        System.out.println("GameLogicService.onProjectileDetonation(): " + System.currentTimeMillis() + ": " + syncBaseItem);
        gameLogicDelegate.ifPresent(delegate -> delegate.onProjectileDetonation(syncBaseItem, position, clipId, timeStamp));
    }

    public void onSpawnSyncItem(SyncBaseItem syncBaseItem) {
        System.out.println("GameLogicService.onSpawnSyncItem(): " + syncBaseItem);
    }

    public void onSpawnSyncItemFinished(SyncBaseItem syncBaseItem) {
        System.out.println("GameLogicService.onSpawnSyncItemFinished(): " + syncBaseItem);
        questService.onSyncItemBuilt(syncBaseItem);
    }

    public void onKilledSyncBaseItem(SyncBaseItem target, SyncBaseItem actor, long timeStamp) {
        System.out.println("GameLogicService.onKilledSyncBaseItem(). target: " + target + " actor: " + actor);
        gameLogicDelegate.ifPresent(delegate -> delegate.onKilledSyncBaseItem(target,  actor,  timeStamp));

        questService.onSyncItemKilled(target, actor);

        if (target.getBase().getCharacter().isBot()) {
            botService.enrageOnKill(target, actor.getBase());
        }
    }

    public void onSyncBaseItemRemoved(SyncBaseItem target) {
        System.out.println("GameLogicService.onSyncBaseItemRemoved(). target: " + target);
        gameLogicDelegate.ifPresent(delegate -> delegate.onSyncBaseItemRemoved(target));
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
    }

    public void onResourceExhausted(SyncResourceItem syncResourceItem) {
        System.out.println("GameLogicService.onResourceExhausted(): " + syncResourceItem);
    }

    public void onBoxCreated(SyncBoxItem syncBoxItem) {
        System.out.println("GameLogicService.onBoxCreated(): " + syncBoxItem);
    }

    public void onBoxPicket(SyncBoxItem box, SyncBaseItem picker, BoxContent boxContent) {
        System.out.println("GameLogicService.onBoxPicket(): " + box + " picker: " + picker + " boxContent: " + boxContent);
        if (modalDialogManager != null) {
            modalDialogManager.showBoxPicked(boxContent);
        }
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

    public void onInventoryItemPlaced(UserContext userContext, InventoryItem inventoryItem) {
        System.out.println("GameLogicService.onInventoryItemPlaced(): ");
        questService.onInventoryItemPlaced(userContext, inventoryItem);
    }

    public void onSurrenderBase(PlayerBase playerBase) {
        System.out.println("GameLogicService.onSurrenderBase(): " + playerBase);
    }

    public void onSyncBaseItemIdle(SyncBaseItem syncBaseItem) {
        gameLogicDelegate.ifPresent(delegate -> delegate.onSyncBaseItemIdle(syncBaseItem));
    }
}
