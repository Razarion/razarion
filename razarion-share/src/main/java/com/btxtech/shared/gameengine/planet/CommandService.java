package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.command.AttackCommand;
import com.btxtech.shared.gameengine.datatypes.command.BaseCommand;
import com.btxtech.shared.gameengine.datatypes.command.BuilderCommand;
import com.btxtech.shared.gameengine.datatypes.command.BuilderFinalizeCommand;
import com.btxtech.shared.gameengine.datatypes.command.FactoryCommand;
import com.btxtech.shared.gameengine.datatypes.command.HarvestCommand;
import com.btxtech.shared.gameengine.datatypes.command.MoveCommand;
import com.btxtech.shared.gameengine.datatypes.command.PickupBoxCommand;
import com.btxtech.shared.gameengine.datatypes.command.SimplePath;
import com.btxtech.shared.gameengine.datatypes.exception.InsufficientFundsException;
import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.gameengine.planet.pathing.PathingService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;

/**
 * Created by Beat
 * 18.07.2016.
 */
@Singleton
public class CommandService { // Is part of the Base service
    // private Logger logger = Logger.getLogger(CommandService.class.getName());
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private PathingService pathingService;
    @Inject
    private GameLogicService gameLogicService;
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private ResourceService resourceService;
    @Inject
    private BoxService boxService;
    @Inject
    private SyncItemContainerService syncItemContainerService;
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    private TerrainService terrainService;

    public void move(Collection<Integer> syncBaseItemIds, DecimalPosition destination) {
        for (int syncBaseItemId : syncBaseItemIds) {
            SyncBaseItem syncBaseItem = syncItemContainerService.getSyncBaseItemSave(syncBaseItemId);
            move(syncBaseItem, destination);
        }
    }

    public void move(SyncBaseItem syncBaseItem, DecimalPosition destination) {
        if (!terrainService.getPathingAccess().isTerrainFree(destination)) {
            return;
        }
        MoveCommand moveCommand = new MoveCommand();
        moveCommand.setId(syncBaseItem.getId());
        moveCommand.updateTimeStamp();
        moveCommand.setSimplePath(pathingService.setupPathToDestination(syncBaseItem, destination));
        executeCommand(moveCommand);
    }

    public void build(int builderId, DecimalPosition positionToBeBuild, int itemTypeIdToBuild) {
        SyncBaseItem builder = syncItemContainerService.getSyncBaseItemSave(builderId);
        BaseItemType toBuild = itemTypeService.getBaseItemType(itemTypeIdToBuild);
        build(builder, positionToBeBuild, toBuild);
    }

    public void build(SyncBaseItem builder, DecimalPosition positionToBeBuild, BaseItemType itemTypeToBuild) {
        if (!terrainService.getPathingAccess().isTerrainFree(positionToBeBuild)) {
            return;
        }
        BuilderCommand builderCommand = new BuilderCommand();
        builderCommand.setId(builder.getId());
        builderCommand.updateTimeStamp();
        builderCommand.setToBeBuiltId(itemTypeToBuild.getId());
        builderCommand.setPositionToBeBuilt(positionToBeBuild);
        SimplePath path = pathingService.setupPathToDestination(builder, builder.getBaseItemType().getBuilderType().getRange(), positionToBeBuild, itemTypeToBuild.getPhysicalAreaConfig().getRadius());
        if (moveIfPathTargetUnreachable(builder, path)) {
            return;
        }
        builderCommand.setSimplePath(path);
        executeCommand(builderCommand);
    }

    public void finalizeBuild(SyncBaseItem builder, SyncBaseItem building) {
        BuilderFinalizeCommand builderFinalizeCommand = new BuilderFinalizeCommand();
        builderFinalizeCommand.setId(builder.getId());
        builderFinalizeCommand.updateTimeStamp();
        builderFinalizeCommand.setBuildingId(building.getId());
        SimplePath path = pathingService.setupPathToDestination(builder, builder.getBaseItemType().getBuilderType().getRange(), building);
        if (moveIfPathTargetUnreachable(builder, path)) {
            return;
        }
        builderFinalizeCommand.setSimplePath(path);
        executeCommand(builderFinalizeCommand);
    }

    public void finalizeBuild(Collection<Integer> builderIds, int buildingId) {
        SyncBaseItem building = syncItemContainerService.getSyncBaseItemSave(buildingId);
        for (int builderId : builderIds) {
            SyncBaseItem builder = syncItemContainerService.getSyncBaseItemSave(builderId);
            finalizeBuild(builder, building);
        }
    }

    public void fabricate(Collection<Integer> factoryIds, int itemTypeToBuildId) {
        BaseItemType toBuild = itemTypeService.getBaseItemType(itemTypeToBuildId);
        for (int factoryId : factoryIds) {
            SyncBaseItem factory = syncItemContainerService.getSyncBaseItemSave(factoryId);
            fabricate(factory, toBuild);
        }
    }

    public void fabricate(SyncBaseItem factory, BaseItemType itemTypeToBuild) {
        FactoryCommand factoryCommand = new FactoryCommand();
        factoryCommand.setId(factory.getId());
        factoryCommand.updateTimeStamp();
        factoryCommand.setToBeBuiltId(itemTypeToBuild.getId());
        executeCommand(factoryCommand);
    }

    public void harvest(Collection<Integer> harvesterIds, int resourceId) {
        SyncResourceItem resource = resourceService.getSyncResourceItem(resourceId);
        for (int harvesterId : harvesterIds) {
            SyncBaseItem harvester = syncItemContainerService.getSyncBaseItemSave(harvesterId);
            harvest(harvester, resource);
        }
    }

    public void harvest(SyncBaseItem harvester, SyncResourceItem resource) {
        HarvestCommand harvestCommand = new HarvestCommand();
        SimplePath path = pathingService.setupPathToDestination(harvester, harvester.getBaseItemType().getHarvesterType().getRange(), resource);
        if (moveIfPathTargetUnreachable(harvester, path)) {
            return;
        }
        harvestCommand.setSimplePath(path);
        harvestCommand.setId(harvester.getId());
        harvestCommand.updateTimeStamp();
        harvestCommand.setTarget(resource.getId());
        executeCommand(harvestCommand);
    }

    public void attack(Collection<Integer> attackerIds, int targetId) {
        SyncBaseItem target = syncItemContainerService.getSyncBaseItemSave(targetId);
        for (int attackerId : attackerIds) {
            SyncBaseItem attacker = syncItemContainerService.getSyncBaseItemSave(attackerId);
            attack(attacker, target, attacker.getSyncPhysicalArea().canMove());
        }
    }

    public void attack(SyncBaseItem syncBaseItem, SyncBaseItem target, boolean followTarget) {
        SimplePath path;
        AttackCommand attackCommand = new AttackCommand();
        if (followTarget) {
            path = pathingService.setupPathToDestination(syncBaseItem, syncBaseItem.getBaseItemType().getWeaponType().getRange(), target);
            if (moveIfPathTargetUnreachable(syncBaseItem, path)) {
                return;
            }
            attackCommand.setSimplePath(path);
        }
        attackCommand.setId(syncBaseItem.getId());
        attackCommand.updateTimeStamp();
        attackCommand.setTarget(target.getId());
        attackCommand.setFollowTarget(followTarget);
        executeCommand(attackCommand);
    }

    public void pickupBox(Collection<Integer> pickerIds, int boxId) {
        SyncBoxItem box = boxService.getSyncBoxItem(boxId);
        for (int pickerId : pickerIds) {
            SyncBaseItem picker = syncItemContainerService.getSyncBaseItemSave(pickerId);
            pickupBox(picker, box);
        }
    }

    public void pickupBox(SyncBaseItem picker, SyncBoxItem box) {
        PickupBoxCommand pickupBoxCommand = new PickupBoxCommand();
        SimplePath path = pathingService.setupPathToDestination(picker, picker.getBaseItemType().getBoxPickupRange(), box);
        if (moveIfPathTargetUnreachable(picker, path)) {
            return;
        }
        pickupBoxCommand.setSimplePath(path);
        pickupBoxCommand.setId(picker.getId());
        pickupBoxCommand.updateTimeStamp();
        pickupBoxCommand.setSynBoxItemId(box.getId());
        executeCommand(pickupBoxCommand);
    }

    public void defend(SyncBaseItem attacker, SyncBaseItem target) {
        attack(attacker, target, false);
    }

    public void executeCommand(BaseCommand baseCommand) {
        try {
            SyncBaseItem syncBaseItem = syncItemContainerService.getSyncBaseItemSave(baseCommand.getId());
            syncBaseItem.stop();
            syncBaseItem.executeCommand(baseCommand);
            baseItemService.addToActiveItemQueue(syncBaseItem);
            gameLogicService.onCommandSent(syncBaseItem, baseCommand);
        } catch (ItemDoesNotExistException e) {
            gameLogicService.onItemDoesNotExistException(e);
        } catch (InsufficientFundsException e) {
            gameLogicService.onInsufficientFundsException(e);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    protected boolean moveIfPathTargetUnreachable(SyncBaseItem syncBaseItem, SimplePath path) {
        return false;
// TODO       if (path.isDestinationReachable()) {
// TODO           return false;
// TODO       } else {
// TODO           move(syncBaseItem, path.getAlternativeDestination());
//  TODO          return true;
// TODO       }
    }
}
