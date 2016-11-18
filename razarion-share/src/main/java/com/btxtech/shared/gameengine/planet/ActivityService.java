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
public class ActivityService {
    private Logger logger = Logger.getLogger(ActivityService.class.getName());
    @Inject
    private PlanetService planetService;
    @Inject
    private QuestService questService;
    private Optional<ClipService> clipService = Optional.empty();
    private ModalDialogManager modalDialogManager;

    public void setModalDialogManager(ModalDialogManager modalDialogManager) {
        this.modalDialogManager = modalDialogManager;
    }

    public void setClipService(ClipService clipService) {
        this.clipService = Optional.of(clipService);
    }

    public void onInsufficientFundsException(InsufficientFundsException e) {
        // TODO connectionService.sendSyncInfo(syncItem);
        // TODO baseService.sendAccountBaseUpdate(syncItem);
        logger.log(Level.SEVERE, "ActivityService.onInsufficientFundsException() ", e);
    }

    public void onPathCanNotBeFoundException(PathCanNotBeFoundException e) {
        // TODO connectionService.sendSyncInfo(syncItem);
        logger.log(Level.SEVERE, "ActivityService.onPathCanNotBeFoundException() ", e);
    }

    public void onItemDoesNotExistException(ItemDoesNotExistException e) {
        // TODO connectionService.sendSyncInfo(syncItem);
        logger.log(Level.SEVERE, "ActivityService.onItemDoesNotExistException() ", e);
    }

    public void onPositionTakenException(PositionTakenException e) {
        e.printStackTrace();
    }

    public void onPlaceCanNotBeFoundException(PlaceCanNotBeFoundException e) {
        e.printStackTrace();
    }

    public void onCommandSent(SyncBaseItem syncItem, BaseCommand baseCommand) {
        // TODO connectionService.sendSyncInfo(syncItem);
        System.out.println("ActivityService.onCommandSent() " + syncItem + " " + baseCommand);
    }

    public void onInvalidPath(BaseCommand baseCommand) {
        logger.severe("Path is invalid: " + ((PathToDestinationCommand) baseCommand).getPathToDestination());
        // TODO  connectionService.sendSyncInfo(syncItem);
    }

    public void onSyncItemDeactivated(SyncItem activeItem) {
        System.out.println("ActivityService.onSyncItemDeactivated(): " + activeItem);


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
        System.out.println("ActivityService.onBaseCreated(): " + playerBase);
    }

    public void onBaseKilled(PlayerBase playerBase, SyncBaseItem actor) {
        System.out.println("ActivityService.onBaseKilled(). base: " + playerBase + " killed by: " + actor);
    }

    public void onBaseRemoved(PlayerBase playerBase) {
        System.out.println("ActivityService.onBaseRemoved(). base: " + playerBase);
    }

    public void onStartBuildingSyncBaseItem(SyncBaseItem createdBy, SyncBaseItem syncBaseItem) {
        System.out.println("ActivityService.onStartBuildingSyncBaseItem(): " + createdBy + " " + syncBaseItem);
    }

    public void onSynBuilderStopped(SyncBaseItem syncBaseItem, SyncBaseItem currentBuildup) {
        System.out.println("ActivityService.onSynBuilderStopped(): " + syncBaseItem + " currentBuildup: " + currentBuildup);
    }

    public void onSyncItemUnloaded(SyncBaseItem syncItem) {
        System.out.println("ActivityService.onSyncItemUnloaded(): " + syncItem);
        // TODO connectionService.sendSyncInfo(syncItem);

    }

    public void onSyncItemLoaded(SyncBaseItem syncBaseItem, SyncBaseItem loadedSyncBaseItem) {
        System.out.println("ActivityService.onSyncItemLoaded(): " + syncBaseItem + " " + loadedSyncBaseItem);
        // TODO connectionService.sendSyncInfo(syncItem);
    }

    public void onSyncBuilderStopped(SyncBaseItem syncBaseItem) {
        System.out.println("ActivityService.onSyncBuilderStopped(): " + syncBaseItem);
    }

    public void onHealthIncreased(SyncBaseItem syncBaseItem) {
        System.out.println("ActivityService.onHealthIncreased(): " + syncBaseItem);
    }

    public void onBuildup(SyncBaseItem syncBaseItem) {
        System.out.println("ActivityService.onBuildup(): " + syncBaseItem);
        questService.onSyncItemBuilt(syncBaseItem);
    }

    public void onAttacked(SyncBaseItem target, SyncBaseItem actor, double damage) {
        System.out.println("ActivityService.onAttacked(). target: " + target + " actor: " + actor + ". damage: " + damage);
    }

    public void onSyncFactoryProgress(SyncBaseItem syncBaseItem) {
        System.out.println("ActivityService.onSyncFactoryProgress(): " + syncBaseItem);
    }

    public void onSyncFactoryStopped(SyncBaseItem syncBaseItem) {
        System.out.println("ActivityService.onSyncFactoryStopped(): " + syncBaseItem);
    }

    public void onProjectileFired(SyncBaseItem syncBaseItem, Vertex muzzlePosition, Vertex muzzleDirection, Integer clipId, long timeStamp) {
        System.out.println("ActivityService.onProjectileFired(): " + System.currentTimeMillis() + ": " + syncBaseItem);
        if (clipId != null) {
            clipService.ifPresent(effectService -> effectService.playClip(muzzlePosition, muzzleDirection, clipId, timeStamp));
        } else {
            logger.warning("No MuzzleFlashClipId configured for: " + syncBaseItem);
        }
    }

    public void onProjectileDetonation(SyncBaseItem syncBaseItem, Vertex position, Integer clipId, long timeStamp) {
        System.out.println("ActivityService.onProjectileDetonation(): " + System.currentTimeMillis() + ": " + syncBaseItem);
        if (clipId != null) {
            clipService.ifPresent(effectService -> effectService.playClip(position, clipId, timeStamp));
        } else {
            logger.warning("No projectile detonation configured for: " + syncBaseItem);
        }
    }

    public void onSpawnSyncItem(SyncBaseItem syncBaseItem) {
        System.out.println("ActivityService.onSpawnSyncItem(): " + syncBaseItem);
    }

    public void onSpawnSyncItemFinished(SyncBaseItem syncBaseItem) {
        System.out.println("ActivityService.onSpawnSyncItemFinished(): " + syncBaseItem);
        questService.onSyncItemBuilt(syncBaseItem);
    }

    public void onKilledSyncBaseItem(SyncBaseItem target, SyncBaseItem actor, long timeStamp) {
        System.out.println("ActivityService.onKilledSyncBaseItem(). target: " + target + " actor: " + actor);

        if (target.getBaseItemType().getExplosionClipId() != null) {
            clipService.ifPresent(effectService -> effectService.playClip(target.getSyncPhysicalArea().getPosition(), target.getBaseItemType().getExplosionClipId(), timeStamp));
        } else {
            logger.warning("No explosion ClipId configured for: " + target);
        }

        questService.onSyncItemKilled(target, actor);
    }

    public void onSyncBaseItemRemoved(SyncBaseItem target) {
        System.out.println("ActivityService.onSyncBaseItemRemoved(). target: " + target);
    }

    // TODO when to call?
    public void onSyncBaseItemCreated(SyncBaseItem syncBaseItem, SyncBaseItem createdBy) {
        System.out.println("ActivityService.onSyncBaseItemCreated(): " + syncBaseItem);
        // TODO planetService.syncItemActivated(syncBaseItem);
        // TODO historyService.addItemCreatedEntry(syncBaseItem);
        // TODO planetService.addGuardingBaseItem(syncBaseItem);
        // TODO planetService.interactionGuardingItems(syncBaseItem);
        // TODO connectionService.sendSyncInfo(syncBaseItem);
        // TODO may be inform bot about new bot item
    }

    public void onResourcesHarvested(SyncBaseItem syncBaseItem, double harvestedResources, SyncResourceItem resource) {
        questService.onHarvested(syncBaseItem, harvestedResources);
        // System.out.println("ActivityService.onResourcesHarvested(). Harvester: " + syncBaseItem + ". Amount: " + harvestedResources + ". SyncResourceItem: " + resource);
    }

    public void onResourceCreated(SyncResourceItem syncResourceItem) {
        System.out.println("ActivityService.onResourceCreated(): " + syncResourceItem);
    }

    public void onResourceExhausted(SyncResourceItem syncResourceItem) {
        System.out.println("ActivityService.onResourceExhausted(): " + syncResourceItem);
    }

    public void onBoxCreated(SyncBoxItem syncBoxItem) {
        System.out.println("ActivityService.onBoxCreated(): " + syncBoxItem);
    }

    public void onBoxPicket(SyncBoxItem box, SyncBaseItem picker, BoxContent boxContent) {
        System.out.println("ActivityService.onBoxPicket(): " + box + " picker: " + picker + " boxContent: " + boxContent);
        if (modalDialogManager != null) {
            modalDialogManager.showBoxPicked(boxContent);
        }
        questService.onSyncBoxItemPicked(picker);
    }

    public void onBuilderNoMoney(SyncBaseItem syncBaseItem) {
        System.out.println("ActivityService.onBuilderNoMoney(): ");
    }

    public void onItemLimitExceededExceptionBuilder(SyncBaseItem syncBaseItem) {
        System.out.println("ActivityService.onItemLimitExceededExceptionBuilder(): " + syncBaseItem);
    }

    public void onHouseSpaceExceededExceptionBuilder(SyncBaseItem syncBaseItem) {
        System.out.println("ActivityService.onHouseSpaceExceededExceptionBuilder(): " + syncBaseItem);
    }

    public void onBuildingSyncItem(SyncBaseItem syncBaseItem, BaseItemType toBeBuilt) {
        System.out.println("ActivityService.onBuildingSyncItem(): " + syncBaseItem + " toBeBuilt: " + toBeBuilt);
    }

    public void onFactoryLevelLimitation4ItemTypeExceeded() {
        System.out.println("ActivityService.onFactoryLevelLimitation4ItemTypeExceeded(): ");
    }

    public void onFactoryHouseSpaceExceeded() {
        System.out.println("ActivityService.onFactoryHouseSpaceExceeded(): ");
    }

    public void onFactoryNoMoney() {
        System.out.println("ActivityService.onFactoryNoMoney(): ");
    }

    public void onFactorySyncItem(SyncBaseItem syncBaseItem, BaseItemType toBeBuilt) {
        System.out.println("ActivityService.onFactorySyncItem(): ");
        questService.onSyncItemBuilt(syncBaseItem);
    }

    public void onInventoryItemPlaced(UserContext userContext, InventoryItem inventoryItem) {
        System.out.println("ActivityService.onInventoryItemPlaced(): ");
        questService.onInventoryItemPlaced(userContext, inventoryItem);
    }

    public void onSurrenderBase(PlayerBase playerBase) {
        System.out.println("ActivityService.onSurrenderBase(): " + playerBase);
    }
}
