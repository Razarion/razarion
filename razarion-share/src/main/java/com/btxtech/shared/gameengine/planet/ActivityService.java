package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.command.BaseCommand;
import com.btxtech.shared.gameengine.datatypes.command.PathToDestinationCommand;
import com.btxtech.shared.gameengine.datatypes.exception.InsufficientFundsException;
import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.exception.PathCanNotBeFoundException;
import com.btxtech.shared.gameengine.datatypes.exception.PlaceCanNotBeFoundException;
import com.btxtech.shared.gameengine.datatypes.exception.PositionTakenException;
import com.btxtech.shared.gameengine.planet.condition.ConditionService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.gameengine.planet.projectile.Projectile;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
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
    private ConditionService conditionService;
    private Collection<Function<SyncBaseItem, Boolean>> spawnFinishCallback = new ArrayList<>();
    private Optional<ClipService> clipService = Optional.empty();

    public void setClipService(ClipService clipService) {
        this.clipService = Optional.of(clipService);
    }

    public void onInsufficientFundsException(InsufficientFundsException e) {
        // TODO connectionService.sendSyncInfo(syncItem);
        // TODO baseService.sendAccountBaseUpdate(syncItem);
        e.printStackTrace();
    }

    public void onPathCanNotBeFoundException(PathCanNotBeFoundException e) {
        // TODO connectionService.sendSyncInfo(syncItem);
        e.printStackTrace();
    }

    public void onItemDoesNotExistException(ItemDoesNotExistException e) {
        // TODO connectionService.sendSyncInfo(syncItem);
        e.printStackTrace();
    }

    public void onPositionTakenException(PositionTakenException e) {
        e.printStackTrace();
    }

    public void onPlaceCanNotBeFoundException(PlaceCanNotBeFoundException e) {
        e.printStackTrace();
    }

    public void onThrowable(Throwable t) {
        // TODO connectionService.sendSyncInfo(syncItem);
        t.printStackTrace();
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

    public void onNewPathRecalculation(SyncBaseItem syncBaseItem) {
        System.out.println("ActivityService.onNewPathRecalculation(): " + syncBaseItem);
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

    public void onSyncBaseItemCreatedBy(SyncBaseItem createdBy, SyncBaseItem syncBaseItem) {
        System.out.println("ActivityService.onSyncBaseItemCreatedBy(): " + createdBy + " " + syncBaseItem);
    }

    public void onSynBuilderStopped(SyncBaseItem syncBaseItem, SyncBaseItem currentBuildup) {
        System.out.println("ActivityService.onSynBuilderStopped(): " + syncBaseItem + " " + currentBuildup);
    }

    public void onSyncItemUnloaded(SyncBaseItem syncItem) {
        System.out.println("ActivityService.onSyncItemUnloaded(): " + syncItem);
        // TODO connectionService.sendSyncInfo(syncItem);

    }

    public void onSyncItemLoaded(SyncBaseItem syncBaseItem, SyncBaseItem loadedSyncBaseItem) {
        System.out.println("ActivityService.onSyncItemLoaded(): " + syncBaseItem + " " + loadedSyncBaseItem);
        // TODO connectionService.sendSyncInfo(syncItem);
    }

    public void onSyncBuilderProgress(SyncBaseItem syncBaseItem) {
        System.out.println("ActivityService.onSyncBuilderProgress(): " + syncBaseItem);
    }

    public void onSyncBuilderStopped(SyncBaseItem syncBaseItem) {
        System.out.println("ActivityService.onSyncBuilderStopped(): " + syncBaseItem);
    }

    public void onHealthIncreased(SyncBaseItem syncBaseItem) {
        System.out.println("ActivityService.onHealthIncreased(): " + syncBaseItem);
    }

    public void onBuildup(SyncBaseItem syncBaseItem) {
        System.out.println("ActivityService.onBuildup(): " + syncBaseItem);
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
            clipService.ifPresent(effectService -> effectService.playClip(position, Vertex.Z_NORM, clipId, timeStamp));
        } else {
            logger.warning("No projectile detonation configured for: " + syncBaseItem);
        }
    }

    public void onSpawnSyncItem(SyncBaseItem syncBaseItem) {
        System.out.println("ActivityService.onSpawnSyncItem(): " + syncBaseItem);
    }

    public void onKilledSyncBaseItem(SyncBaseItem target, SyncBaseItem actor) {
        System.out.println("ActivityService.onKilledSyncBaseItem(). target: " + target + " actor: " + actor);
    }

    public void onSyncBaseItemRemoved(SyncBaseItem target) {
        System.out.println("ActivityService.onSyncBaseItemRemoved(). target: " + target);
    }

    public void onSpawnSyncItemFinished(SyncBaseItem syncBaseItem) {
        System.out.println("ActivityService.onSpawnSyncItemFinished(): " + syncBaseItem);
        conditionService.onSyncItemBuilt(syncBaseItem);
        Collection<Function<SyncBaseItem, Boolean>> tmp = new ArrayList<>(spawnFinishCallback);
        for (Function<SyncBaseItem, Boolean> callback : tmp) {
            if (callback.apply(syncBaseItem)) {
                spawnFinishCallback.remove(callback);
            }
        }
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

    public void addSpanFinishedCallback(Function<SyncBaseItem, Boolean> callback) {
        System.out.println("****** addSpanFinishedCallback");
        spawnFinishCallback.add(callback);
    }

    public void removeSpanFinishedCallback(Function<SyncBaseItem, Boolean> callback) {
        spawnFinishCallback.add(callback);
    }

    public void onResourcesHarvested(SyncBaseItem syncBaseItem, double harvestedResources, SyncResourceItem resource) {
        // System.out.println("ActivityService.onResourcesHarvested(). Harvester: " + syncBaseItem + ". Amount: " + harvestedResources + ". SyncResourceItem: " + resource);
    }

    public void onResourceCreated(SyncResourceItem syncResourceItem) {
        System.out.println("ActivityService.onResourceCreated(): " + syncResourceItem);
    }

    public void onResourceExhausted(SyncResourceItem syncResourceItem) {
        System.out.println("ActivityService.onResourceExhausted(): " + syncResourceItem);
    }
}
