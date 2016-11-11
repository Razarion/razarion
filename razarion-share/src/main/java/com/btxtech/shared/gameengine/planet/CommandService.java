package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.datatypes.Path;
import com.btxtech.shared.gameengine.datatypes.command.AttackCommand;
import com.btxtech.shared.gameengine.datatypes.command.BaseCommand;
import com.btxtech.shared.gameengine.datatypes.command.BuilderCommand;
import com.btxtech.shared.gameengine.datatypes.command.BuilderFinalizeCommand;
import com.btxtech.shared.gameengine.datatypes.command.FactoryCommand;
import com.btxtech.shared.gameengine.datatypes.command.HarvestCommand;
import com.btxtech.shared.gameengine.datatypes.command.MoveCommand;
import com.btxtech.shared.gameengine.datatypes.command.PickupBoxCommand;
import com.btxtech.shared.gameengine.datatypes.exception.InsufficientFundsException;
import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.gameengine.planet.pathing.PathingService;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 18.07.2016.
 */
@Singleton
public class CommandService { // Is part of the Base service
    private Logger logger = Logger.getLogger(CommandService.class.getName());
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private PathingService pathingService;
    @Inject
    private ActivityService activityService;
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private SyncItemContainerService syncItemContainerService;

    public void move(Collection<SyncBaseItem> syncBaseItems, DecimalPosition destination) {
        for (SyncBaseItem syncBaseItem : syncBaseItems) {
            move(syncBaseItem, destination);
        }
    }

    public void move(SyncBaseItem syncBaseItem, DecimalPosition destination) {
        MoveCommand moveCommand = new MoveCommand();
        moveCommand.setId(syncBaseItem.getId());
        moveCommand.setTimeStamp();
        moveCommand.setPathToDestination(pathingService.setupPathToDestination(syncBaseItem, destination));
        executeCommand(moveCommand);
    }

    public void build(SyncBaseItem builder, DecimalPosition positionToBeBuild, BaseItemType itemTypeToBuild) {
        BuilderCommand builderCommand = new BuilderCommand();
        builderCommand.setId(builder.getId());
        builderCommand.setTimeStamp();
        builderCommand.setToBeBuiltId(itemTypeToBuild.getId());
        builderCommand.setPositionToBeBuilt(positionToBeBuild);
        Path path = pathingService.setupPathToDestination(builder, builder.getBaseItemType().getBuilderType().getRange(), positionToBeBuild, itemTypeToBuild.getPhysicalAreaConfig().getRadius());
        if (moveIfPathTargetUnreachable(builder, path)) {
            return;
        }
        builderCommand.setPathToDestination(path);
        executeCommand(builderCommand);
    }

    public void finalizeBuild(SyncBaseItem builder, SyncBaseItem building) {
        BuilderFinalizeCommand builderFinalizeCommand = new BuilderFinalizeCommand();
        builderFinalizeCommand.setId(builder.getId());
        builderFinalizeCommand.setTimeStamp();
        builderFinalizeCommand.setBuildingId(building.getId());
        Path path = pathingService.setupPathToDestination(builder, builder.getBaseItemType().getBuilderType().getRange(), building);
        if (moveIfPathTargetUnreachable(builder, path)) {
            return;
        }
        builderFinalizeCommand.setPathToDestination(path);
        executeCommand(builderFinalizeCommand);
    }

    public void finalizeBuild(Collection<SyncBaseItem> builders, SyncBaseItem building) {
        for (SyncBaseItem builder : builders) {
            finalizeBuild(builder, building);
        }
    }

    public void fabricate(Collection<SyncBaseItem> factories, BaseItemType itemTypeToBuild) {
        for (SyncBaseItem factory : factories) {
            fabricate(factory, itemTypeToBuild);
        }
    }

    public void fabricate(SyncBaseItem factory, BaseItemType itemTypeToBuild) {
        FactoryCommand factoryCommand = new FactoryCommand();
        factoryCommand.setId(factory.getId());
        factoryCommand.setTimeStamp();
        factoryCommand.setToBeBuiltId(itemTypeToBuild.getId());
        executeCommand(factoryCommand);
    }

    public void harvest(Collection<SyncBaseItem> syncBaseItems, SyncResourceItem resource) {
        for (SyncBaseItem syncBaseItem : syncBaseItems) {
            harvest(syncBaseItem, resource);
        }
    }

    public void harvest(SyncBaseItem harvester, SyncResourceItem resource) {
        HarvestCommand harvestCommand = new HarvestCommand();
        Path path = pathingService.setupPathToDestination(harvester, harvester.getBaseItemType().getHarvesterType().getRange(), resource);
        if (moveIfPathTargetUnreachable(harvester, path)) {
            return;
        }
        harvestCommand.setPathToDestination(path);
        harvestCommand.setId(harvester.getId());
        harvestCommand.setTimeStamp();
        harvestCommand.setTarget(resource.getId());
        executeCommand(harvestCommand);
    }

    public void attack(Collection<SyncBaseItem> syncBaseItems, SyncBaseItem target) {
        for (SyncBaseItem syncBaseItem : syncBaseItems) {
            attack(syncBaseItem, target, syncBaseItem.getSyncPhysicalArea().canMove());
        }
    }

    public void attack(SyncBaseItem syncBaseItem, SyncBaseItem target, boolean followTarget) {
        Path path;
        AttackCommand attackCommand = new AttackCommand();
        if (followTarget) {
            path = pathingService.setupPathToDestination(syncBaseItem, syncBaseItem.getBaseItemType().getWeaponType().getRange(), target);
            if (moveIfPathTargetUnreachable(syncBaseItem, path)) {
                return;
            }
            attackCommand.setPathToDestination(path);
        }
        attackCommand.setId(syncBaseItem.getId());
        attackCommand.setTimeStamp();
        attackCommand.setTarget(target.getId());
        attackCommand.setFollowTarget(followTarget);
        executeCommand(attackCommand);
    }

    public void pickupBox(Collection<SyncBaseItem> syncBaseItems, SyncBoxItem box) {
        for (SyncBaseItem syncBaseItem : syncBaseItems) {
            pickupBox(syncBaseItem, box);
        }
    }

    public void pickupBox(SyncBaseItem picker, SyncBoxItem box) {
        PickupBoxCommand pickupBoxCommand = new PickupBoxCommand();
        Path path = pathingService.setupPathToDestination(picker, picker.getBaseItemType().getBoxPickupRange(), box);
        if (moveIfPathTargetUnreachable(picker, path)) {
            return;
        }
        pickupBoxCommand.setPathToDestination(path);
        pickupBoxCommand.setId(picker.getId());
        pickupBoxCommand.setTimeStamp();
        pickupBoxCommand.setSynBoxItemId(box.getId());
        executeCommand(pickupBoxCommand);
    }

    public void defend(SyncBaseItem attacker, SyncBaseItem target) {
        throw new UnsupportedOperationException();
    }

    public void loadContainer(SyncBaseItem container, Collection<SyncBaseItem> syncBaseItems) {
        throw new UnsupportedOperationException();
    }

    public void unloadContainer(SyncBaseItem container, Index unloadPos) {
        throw new UnsupportedOperationException();
    }

    private void executeCommand(BaseCommand baseCommand) {
        try {
            SyncBaseItem syncBaseItem = syncItemContainerService.getSyncBaseItem(baseCommand.getId());
            syncBaseItem.stop();
            syncBaseItem.executeCommand(baseCommand);
            baseItemService.addToActiveItemQueue(syncBaseItem);
            activityService.onCommandSent(syncBaseItem, baseCommand);
        } catch (ItemDoesNotExistException e) {
            activityService.onItemDoesNotExistException(e);
        } catch (InsufficientFundsException e) {
            activityService.onInsufficientFundsException(e);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    protected boolean moveIfPathTargetUnreachable(SyncBaseItem syncBaseItem, Path path) {
        return false;
// TODO       if (path.isDestinationReachable()) {
// TODO           return false;
// TODO       } else {
// TODO           move(syncBaseItem, path.getAlternativeDestination());
//  TODO          return true;
// TODO       }
    }
}
