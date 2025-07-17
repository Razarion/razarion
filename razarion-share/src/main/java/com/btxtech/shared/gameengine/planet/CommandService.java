package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.command.AttackCommand;
import com.btxtech.shared.gameengine.datatypes.command.BaseCommand;
import com.btxtech.shared.gameengine.datatypes.command.BuilderCommand;
import com.btxtech.shared.gameengine.datatypes.command.BuilderFinalizeCommand;
import com.btxtech.shared.gameengine.datatypes.command.FactoryCommand;
import com.btxtech.shared.gameengine.datatypes.command.HarvestCommand;
import com.btxtech.shared.gameengine.datatypes.command.LoadContainerCommand;
import com.btxtech.shared.gameengine.datatypes.command.MoveCommand;
import com.btxtech.shared.gameengine.datatypes.command.PickupBoxCommand;
import com.btxtech.shared.gameengine.datatypes.command.SimplePath;
import com.btxtech.shared.gameengine.datatypes.command.UnloadContainerCommand;
import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.workerdto.IdsDto;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.gameengine.planet.pathing.PathingService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 18.07.2016.
 */
@Singleton
public class CommandService {
    // Is part of the Base service
    private final Logger logger = Logger.getLogger(CommandService.class.getName());
    private final PathingService pathingService;
    private final GameLogicService gameLogicService;
    private final BaseItemService baseItemService;
    private final ResourceService resourceService;
    private final BoxService boxService;
    private final SyncItemContainerServiceImpl syncItemContainerService;
    private final ItemTypeService itemTypeService;
    private final PlanetService planetService;
    private final GuardingItemService guardingItemService;

    @Inject
    public CommandService(GuardingItemService guardingItemService,
                          PlanetService planetService,
                          ItemTypeService itemTypeService,
                          SyncItemContainerServiceImpl syncItemContainerService,
                          BoxService boxService,
                          ResourceService resourceService,
                          BaseItemService baseItemService,
                          GameLogicService gameLogicService,
                          PathingService pathingService) {
        this.guardingItemService = guardingItemService;
        this.planetService = planetService;
        this.itemTypeService = itemTypeService;
        this.syncItemContainerService = syncItemContainerService;
        this.boxService = boxService;
        this.resourceService = resourceService;
        this.baseItemService = baseItemService;
        this.gameLogicService = gameLogicService;
        this.pathingService = pathingService;
    }

    public void move(IdsDto syncBaseItemIds, DecimalPosition destination) {
        for (int syncBaseItemId : syncBaseItemIds.getIds()) {
            SyncBaseItem syncBaseItem = syncItemContainerService.getSyncBaseItemSave(syncBaseItemId);
            move(syncBaseItem, destination);
        }
    }

    public void move(SyncBaseItem syncBaseItem, DecimalPosition destination) {
        checkSyncBaseItem(syncBaseItem);
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
        checkSyncBaseItem(builder);
        BuilderCommand builderCommand = new BuilderCommand();
        builderCommand.setId(builder.getId());
        builderCommand.updateTimeStamp();
        builderCommand.setToBeBuiltId(itemTypeToBuild.getId());
        builderCommand.setPositionToBeBuilt(positionToBeBuild);
        SimplePath path = pathingService.setupPathToDestination(builder,
                builder.getBaseItemType().getBuilderType().getRangeOtherTerrain(),
                itemTypeToBuild.getPhysicalAreaConfig().getTerrainType(),
                positionToBeBuild,
                itemTypeToBuild.getPhysicalAreaConfig().getRadius());
        if (moveIfPathTargetUnreachable(builder, path)) {
            return;
        }
        builderCommand.setSimplePath(path);
        executeCommand(builderCommand);
    }

    public void finalizeBuild(SyncBaseItem builder, SyncBaseItem building) {
        checkSyncBaseItem(builder);
        checkSyncBaseItem(building);
        BuilderFinalizeCommand builderFinalizeCommand = new BuilderFinalizeCommand();
        builderFinalizeCommand.setId(builder.getId());
        builderFinalizeCommand.updateTimeStamp();
        builderFinalizeCommand.setBuildingId(building.getId());
        SimplePath path = pathingService.setupPathToDestination(builder, builder.getBaseItemType().getBuilderType().getRangeOtherTerrain(), building);
        if (moveIfPathTargetUnreachable(builder, path)) {
            return;
        }
        builderFinalizeCommand.setSimplePath(path);
        executeCommand(builderFinalizeCommand);
    }

    public void finalizeBuild(IdsDto builderIds, int buildingId) {
        SyncBaseItem building = syncItemContainerService.getSyncBaseItemSave(buildingId);
        for (int builderId : builderIds.getIds()) {
            SyncBaseItem builder = syncItemContainerService.getSyncBaseItemSave(builderId);
            finalizeBuild(builder, building);
        }
    }

    public void fabricate(int factoryId, int itemTypeToBuildId) {
        BaseItemType toBuild = itemTypeService.getBaseItemType(itemTypeToBuildId);
        fabricate(syncItemContainerService.getSyncBaseItemSave(factoryId), toBuild);
    }

    public void fabricate(SyncBaseItem factory, BaseItemType itemTypeToBuild) {
        checkSyncBaseItem(factory);
        FactoryCommand factoryCommand = new FactoryCommand();
        factoryCommand.setId(factory.getId());
        factoryCommand.updateTimeStamp();
        factoryCommand.setToBeBuiltId(itemTypeToBuild.getId());
        executeCommand(factoryCommand);
    }

    public void harvest(IdsDto harvesterIds, int resourceId) {
        SyncResourceItem resource = resourceService.getSyncResourceItem(resourceId);
        for (int harvesterId : harvesterIds.getIds()) {
            SyncBaseItem harvester = syncItemContainerService.getSyncBaseItemSave(harvesterId);
            harvest(harvester, resource);
        }
    }

    public void harvest(SyncBaseItem harvester, SyncResourceItem resource) {
        checkSyncBaseItem(harvester);
        HarvestCommand harvestCommand = new HarvestCommand();
        SimplePath path = pathingService.setupPathToDestination(harvester, 0, resource);
        if (moveIfPathTargetUnreachable(harvester, path)) {
            return;
        }
        harvestCommand.setSimplePath(path);
        harvestCommand.setId(harvester.getId());
        harvestCommand.updateTimeStamp();
        harvestCommand.setTarget(resource.getId());
        executeCommand(harvestCommand);
    }

    public void attack(IdsDto attackerIds, int targetId) {
        SyncBaseItem target = syncItemContainerService.getSyncBaseItemSave(targetId);
        for (int attackerId : attackerIds.getIds()) {
            SyncBaseItem attacker = syncItemContainerService.getSyncBaseItemSave(attackerId);
            attack(attacker, target, attacker.getAbstractSyncPhysical().canMove());
        }
    }

    public void attack(SyncBaseItem syncBaseItem, SyncBaseItem target, boolean followTarget) {
        checkSyncBaseItem(syncBaseItem);
        checkSyncBaseItem(target);
        SimplePath path;
        AttackCommand attackCommand = new AttackCommand();
        if (followTarget) {
            path = pathingService.setupPathToDestination(syncBaseItem, 0, target);
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

    public void pickupBox(IdsDto pickerIds, int boxId) {
        SyncBoxItem box = boxService.getSyncBoxItem(boxId);
        for (int pickerId : pickerIds.getIds()) {
            SyncBaseItem picker = syncItemContainerService.getSyncBaseItemSave(pickerId);
            pickupBox(picker, box);
        }
    }

    public void pickupBox(SyncBaseItem picker, SyncBoxItem box) {
        checkSyncBaseItem(picker);
        PickupBoxCommand pickupBoxCommand = new PickupBoxCommand();
        SimplePath path = pathingService.setupPathToDestination(picker, 0, box);
        if (moveIfPathTargetUnreachable(picker, path)) {
            return;
        }
        pickupBoxCommand.setSimplePath(path);
        pickupBoxCommand.setId(picker.getId());
        pickupBoxCommand.updateTimeStamp();
        pickupBoxCommand.setSynBoxItemId(box.getId());
        executeCommand(pickupBoxCommand);
    }

    public void loadContainer(IdsDto containedIds, int containerId) {
        SyncBaseItem container = syncItemContainerService.getSyncBaseItemSave(containerId);
        for (int containedId : containedIds.getIds()) {
            SyncBaseItem contained = syncItemContainerService.getSyncBaseItemSave(containedId);
            loadContainer(contained, container);
        }
    }

    public void loadContainer(SyncBaseItem contained, SyncBaseItem container) {
        checkSyncBaseItem(contained);
        checkSyncBaseItem(container);
        LoadContainerCommand loadContainerCommand = new LoadContainerCommand();
        SimplePath path = pathingService.setupPathToDestination(contained, 0, container);
        if (moveIfPathTargetUnreachable(contained, path)) {
            return;
        }
        loadContainerCommand.setSimplePath(path);
        loadContainerCommand.setId(contained.getId());
        loadContainerCommand.setItemContainer(container.getId());
        loadContainerCommand.updateTimeStamp();
        executeCommand(loadContainerCommand);
    }

    public void unloadContainer(int container, DecimalPosition unloadPos) {
        unloadContainer(syncItemContainerService.getSyncBaseItemSave(container), unloadPos);
    }

    public void unloadContainer(SyncBaseItem container, DecimalPosition unloadPos) {
        checkSyncBaseItem(container);

        UnloadContainerCommand unloadContainerCommand = new UnloadContainerCommand();
//        SimplePath path = pathingService.setupPathToDestination(container, container.getSyncItemContainer().getRange(), TerrainType.LAND, unloadPos, 0);
//        if (moveIfPathTargetUnreachable(container, path)) {
//            return;
//        }
//        unloadContainerCommand.setSimplePath(path);
        unloadContainerCommand.setId(container.getId());
        unloadContainerCommand.updateTimeStamp();
        unloadContainerCommand.setUnloadPos(unloadPos);
        executeCommand(unloadContainerCommand);
    }


    public void defend(SyncBaseItem attacker, SyncBaseItem target) {
        attack(attacker, target, false);
    }

    private void checkSyncBaseItem(SyncBaseItem syncBaseItem) {
        if (syncBaseItem.isContainedIn()) {
            throw new IllegalStateException("CommandService.checkSyncBaseItem() Item is inside a item container: " + syncBaseItem);
        }
    }

    public void executeCommand(BaseCommand baseCommand) {
        try {
            if (planetService.getGameEngineMode() == GameEngineMode.MASTER) {
                baseItemService.queueCommand(baseCommand);
            } else if (planetService.getGameEngineMode() == GameEngineMode.SLAVE) {
                gameLogicService.onSlaveCommandSent(syncItemContainerService.getSyncBaseItemSave(baseCommand.getId()), baseCommand);
            }
        } catch (ItemDoesNotExistException e) {
            gameLogicService.onItemDoesNotExistException(e);
        } catch (Throwable t) {
            logger.log(Level.SEVERE, t.getMessage(), t);
        }
    }

    protected boolean moveIfPathTargetUnreachable(SyncBaseItem syncBaseItem, SimplePath path) {
        if (path.isDestinationReachable()) {
            return false;
        } else {
            move(syncBaseItem, path.getAlternativeDestination());
            return true;
        }
    }
}
