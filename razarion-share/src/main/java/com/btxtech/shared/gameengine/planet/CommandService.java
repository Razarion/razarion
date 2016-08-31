package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.gameengine.UserTrackingService;
import com.btxtech.shared.gameengine.datatypes.Path;
import com.btxtech.shared.gameengine.datatypes.command.AttackCommand;
import com.btxtech.shared.gameengine.datatypes.command.BaseCommand;
import com.btxtech.shared.gameengine.datatypes.command.MoveCommand;
import com.btxtech.shared.gameengine.datatypes.command.PathToDestinationCommand;
import com.btxtech.shared.gameengine.datatypes.exception.InsufficientFundsException;
import com.btxtech.shared.gameengine.datatypes.exception.ItemDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.exception.NotYourBaseException;
import com.btxtech.shared.gameengine.datatypes.exception.PathCanNotBeFoundException;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.system.ExceptionHandler;

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
    private Logger logger = Logger.getLogger(CommandService.class.getName());
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private CollisionService collisionService;
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

    public void move(SyncBaseItem syncBaseItem, DecimalPosition destination) {
        syncBaseItem.stop();
        MoveCommand moveCommand = new MoveCommand();
        moveCommand.setId(syncBaseItem.getId());
        moveCommand.setTimeStamp();
        moveCommand.setPathToDestination(collisionService.setupPathToDestination(syncBaseItem, destination));
        try {
            executeCommand(moveCommand, syncBaseItem.getBase().getCharacter().isBot());
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

    public void finalizeBuild(SyncBaseItem builder, SyncBaseItem building, Index destinationHint, double destinationAngel) {
        throw new UnsupportedOperationException();
    }

    public void fabricate(SyncBaseItem builder, BaseItemType itemTypeToBuild) {
        throw new UnsupportedOperationException();
    }

    public void collect(SyncBaseItem harvester, SyncResourceItem moneyItem, Index destinationHint, double destinationAngel) {
        throw new UnsupportedOperationException();
    }

    public void attack(SyncBaseItem syncBaseItem, SyncBaseItem target, DecimalPosition destinationHint, double destinationAngel, boolean followTarget) {
        syncBaseItem.stop();
        Path path;
        AttackCommand attackCommand = new AttackCommand();
        if (followTarget) {
            path = collisionService.setupPathToDestination(syncBaseItem, destinationHint);
            if (moveIfPathTargetUnreachable(syncBaseItem, path)) {
                return;
            }
            path.setDestinationAngel(destinationAngel);
            attackCommand.setPathToDestination(path);
        }
        attackCommand.setId(syncBaseItem.getId());
        attackCommand.setTimeStamp();
        attackCommand.setTarget(target.getId());
        attackCommand.setFollowTarget(followTarget);

        try {
            executeCommand(attackCommand, syncBaseItem.getBase().getCharacter().isBot());
        } catch (Exception e) {
            exceptionHandler.handleException(e);
        }
    }

    public void pickupBox(SyncBaseItem picker, SyncBoxItem box, Index destinationHint, double destinationAngel) {
        throw new UnsupportedOperationException();
    }

    public void defend(SyncBaseItem attacker, SyncBaseItem target) {
        throw new UnsupportedOperationException();
    }

    public void loadContainer(SyncBaseItem container, SyncBaseItem item, Index destinationHint, double destinationAngel) {
        throw new UnsupportedOperationException();
    }

    public void unloadContainer(SyncBaseItem container, Index unloadPos) {
        throw new UnsupportedOperationException();
    }

    private void executeCommand(BaseCommand baseCommand, boolean cmdFromSystem) throws ItemDoesNotExistException, NotYourBaseException {
        SyncBaseItem syncItem;
        try {
            syncItem = (SyncBaseItem) baseItemService.getItem(baseCommand.getId());
        } catch (ItemDoesNotExistException e) {
            if (logger.isLoggable(Level.INFO)) {
                logger.info("Can not execute command. Item does no longer exist " + baseCommand);
            }
            return;
        }
        if (!cmdFromSystem) {
            baseService.checkBaseAccess(syncItem);
            userTrackingService.saveUserCommand(baseCommand);
            if (baseCommand instanceof PathToDestinationCommand) {
                if (!collisionService.checkIfPathValid(((PathToDestinationCommand) baseCommand).getPathToDestination())) {
                    activityService.onInvalidPath(baseCommand);
                    return;
                }
            }
        }
        try {
            syncItem.stop();
            syncItem.executeCommand(baseCommand);
            planetService.finalizeCommand(syncItem);
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
        if (path.isDestinationReachable()) {
            return false;
        } else {
            move(syncBaseItem, path.getAlternativeDestination());
            return true;
        }
    }
}
