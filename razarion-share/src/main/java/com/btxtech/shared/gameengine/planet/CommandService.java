package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.UserTrackingService;
import com.btxtech.shared.gameengine.datatypes.Path;
import com.btxtech.shared.gameengine.datatypes.command.AttackCommand;
import com.btxtech.shared.gameengine.datatypes.command.BaseCommand;
import com.btxtech.shared.gameengine.datatypes.command.HarvestCommand;
import com.btxtech.shared.gameengine.datatypes.command.MoveCommand;
import com.btxtech.shared.gameengine.datatypes.exception.InsufficientFundsException;
import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.exception.NotYourBaseException;
import com.btxtech.shared.gameengine.datatypes.exception.PathCanNotBeFoundException;
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
public class CommandService {
    private Logger logger = Logger.getLogger(CommandService.class.getName());
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private CollisionService collisionService;
    @Inject
    private PathingService pathingService;
    @Inject
    private ActivityService activityService;
    @Inject
    private BaseService baseService;
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private UserTrackingService userTrackingService;
    @Inject
    private PlanetService planetService;
    @Inject
    private SyncItemContainerService syncItemContainerService;

    public void move(Collection<SyncBaseItem> syncBaseItems, DecimalPosition destination) {
        for (SyncBaseItem syncBaseItem : syncBaseItems) {
            move(syncBaseItem, destination);
        }
    }

    public void move(SyncBaseItem syncBaseItem, DecimalPosition destination) {
        syncBaseItem.stop();
        MoveCommand moveCommand = new MoveCommand();
        moveCommand.setId(syncBaseItem.getId());
        moveCommand.setTimeStamp();
        moveCommand.setPathToDestination(pathingService.setupPathToDestination(syncBaseItem, destination));
        try {
            executeCommand(moveCommand);
        } catch (PathCanNotBeFoundException e) {
            logger.warning("PathCanNotBeFoundException: " + e.getMessage());
        } catch (Exception e) {
            exceptionHandler.handleException(e);
        }
    }

    public void build(SyncBaseItem builder, DecimalPosition position, BaseItemType itemTypeToBuild) {
        throw new UnsupportedOperationException();
    }

    public void build(SyncBaseItem syncItem, Index positionToBeBuild, BaseItemType toBeBuilt, Index destinationHint, double destinationAngel) {
        throw new UnsupportedOperationException();
    }

    public void finalizeBuild(Collection<SyncBaseItem> syncBaseItems, SyncBaseItem building) {
        throw new UnsupportedOperationException();
    }

    public void fabricate(Collection<SyncBaseItem> factories, BaseItemType itemTypeToBuild) {
        for (SyncBaseItem factory : factories) {
            fabricate(factory, itemTypeToBuild);
        }
    }

    public void fabricate(SyncBaseItem factory, BaseItemType itemTypeToBuild) {
        throw new UnsupportedOperationException();
    }

    public void harvest(Collection<SyncBaseItem> syncBaseItems, SyncResourceItem resource) {
        for (SyncBaseItem syncBaseItem : syncBaseItems) {
            harvest(syncBaseItem, resource);
        }
    }

    public void harvest(SyncBaseItem harvester, SyncResourceItem resource) {
        harvester.stop();
        HarvestCommand harvestCommand = new HarvestCommand();
        Path path = pathingService.setupPathToDestination(harvester, resource, harvester.getBaseItemType().getHarvesterType().getRange());
        if (moveIfPathTargetUnreachable(harvester, path)) {
            return;
        }
        harvestCommand.setPathToDestination(path);
        harvestCommand.setId(harvester.getId());
        harvestCommand.setTimeStamp();
        harvestCommand.setTarget(resource.getId());

        try {
            executeCommand(harvestCommand);
        } catch (Exception e) {
            exceptionHandler.handleException(e);
        }
    }

    public void attack(Collection<SyncBaseItem> syncBaseItems, SyncBaseItem target) {
        for (SyncBaseItem syncBaseItem : syncBaseItems) {
            attack(syncBaseItem, target, syncBaseItem.getSyncPhysicalArea().canMove());
        }
    }

    public void attack(SyncBaseItem syncBaseItem, SyncBaseItem target, boolean followTarget) {
        syncBaseItem.stop();
        Path path;
        AttackCommand attackCommand = new AttackCommand();
        if (followTarget) {
            path = pathingService.setupPathToDestination(syncBaseItem, target, syncBaseItem.getBaseItemType().getWeaponType().getRange());
            if (moveIfPathTargetUnreachable(syncBaseItem, path)) {
                return;
            }
            attackCommand.setPathToDestination(path);
        }
        attackCommand.setId(syncBaseItem.getId());
        attackCommand.setTimeStamp();
        attackCommand.setTarget(target.getId());
        attackCommand.setFollowTarget(followTarget);

        try {
            executeCommand(attackCommand);
        } catch (Exception e) {
            exceptionHandler.handleException(e);
        }
    }

    public void pickupBox(Collection<SyncBaseItem> syncBaseItems, SyncBoxItem box) {
        for (SyncBaseItem syncBaseItem : syncBaseItems) {
            pickupBox(syncBaseItem, box);
        }
    }

    public void pickupBox(SyncBaseItem picker, SyncBoxItem box) {
        throw new UnsupportedOperationException();
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

    private void executeCommand(BaseCommand baseCommand) throws ItemDoesNotExistException, NotYourBaseException {
        SyncBaseItem syncItem = (SyncBaseItem) syncItemContainerService.getSyncItem(baseCommand.getId());
        try {
            syncItem.stop();
            syncItem.executeCommand(baseCommand);
            activityService.onCommandSent(syncItem, baseCommand);
        } catch (PathCanNotBeFoundException e) {
            activityService.onPathCanNotBeFoundException(e);
        } catch (ItemDoesNotExistException e) {
            activityService.onItemDoesNotExistException(e);
        } catch (InsufficientFundsException e) {
            activityService.onInsufficientFundsException(e);
        } catch (Throwable t) {
            activityService.onThrowable(t);
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
