package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.gameengine.datatypes.PlanetMode;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.exception.BaseDoesNotExistException;
import com.btxtech.shared.gameengine.datatypes.exception.PathCanNotBeFoundException;
import com.btxtech.shared.gameengine.datatypes.exception.PlaceCanNotBeFoundException;
import com.btxtech.shared.gameengine.datatypes.exception.PositionTakenException;
import com.btxtech.shared.gameengine.planet.condition.ConditionService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncTickItem;
import com.btxtech.shared.gameengine.planet.pathing.PathingService;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.SimpleScheduledFuture;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Beat
 * 13.07.2016.
 */
@Singleton
public class PlanetService implements Runnable {
    public static final PlanetMode MODE = PlanetMode.MASTER;
    public static final int TICK_TIME_MILLI_SECONDS = 100;
    public static final double TICK_FACTOR = (double) TICK_TIME_MILLI_SECONDS / 1000.0;
    // private Logger logger = Logger.getLogger(PlanetService.class.getName());
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private Event<PlanetActivationEvent> activationEvent;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private SimpleExecutorService simpleExecutorService;
    @Inject
    private PathingService pathingService;
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private ConditionService conditionService;
    @Inject
    private SyncItemContainerService syncItemContainerService;
    @Inject
    private Instance<ActivityService> activityServiceInstance;
    // @Inject
    // private CommandService commandService;
    private final HashSet<SyncBaseItem> guardingItems = new HashSet<>();
    private boolean pause;
    private SimpleScheduledFuture scheduledFuture;
    private PlanetConfig planetConfig;
    private long tickCount;

    @PostConstruct
    public void postConstruct() {
        scheduledFuture = simpleExecutorService.scheduleAtFixedRate(TICK_TIME_MILLI_SECONDS, false, this, SimpleExecutorService.Type.GAME_ENGINE);
    }

    public void initialise(PlanetConfig planetConfig) {
        tickCount = 0;
        this.planetConfig = planetConfig;
        activationEvent.fire(new PlanetActivationEvent(planetConfig));
    }

    public void start() {
        scheduledFuture.start();
    }

    @Override
    public void run() {
        if (pause) {
            return;
        }
        try {
            // TODO different sets for Active Items
            // TODO Moving (also pushed away items, ev targed reached)
            // TODO building, attacking,
            pathingService.tick();
            conditionService.checkPositionCondition();

            syncItemContainerService.iterateOverBaseItems(false, false,null, activeItem ->{
                if (!activeItem.isAlive()) {
                    return null;
                }
                if (activeItem.isIdle()) {
                    return null;
                }
                try {
                    if (!activeItem.tick()) {
                        try {
                            activeItem.stop();
                            addGuardingBaseItem(activeItem);
                            activityServiceInstance.get().onSyncItemDeactivated(activeItem);
                        } catch (Throwable t) {
                            exceptionHandler.handleException("Error during deactivation of active item: " + activeItem, t);
                        }
                    }
                } catch (BaseDoesNotExistException e) {
                    activeItem.stop();
                } catch (PositionTakenException e) {
                    activeItem.stop();
                    activityServiceInstance.get().onPositionTakenException(e);
                } catch (PathCanNotBeFoundException e) {
                    activeItem.stop();
                    activityServiceInstance.get().onPathCanNotBeFoundException(e);
                } catch (PlaceCanNotBeFoundException e) {
                    activeItem.stop();
                    activityServiceInstance.get().onPlaceCanNotBeFoundException(e);
                } catch (Throwable t) {
                    activeItem.stop();
                    activityServiceInstance.get().onThrowable(t);
                }
                return null;
            });
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
        tickCount++;
    }

    public long getTickCount() {
        return tickCount;
    }

    public void addGuardingBaseItem(SyncTickItem syncTickItem) {
        try {
            if (MODE != PlanetMode.MASTER) {
                return;
            }

            if (!(syncTickItem instanceof SyncBaseItem)) {
                return;
            }

            SyncBaseItem syncBaseItem = (SyncBaseItem) syncTickItem;
            if (!syncBaseItem.hasSyncWeapon() || !syncBaseItem.isAlive()) {
                return;
            }

            if (!syncBaseItem.isBuildup()) {
                return;
            }

            if (syncBaseItem.hasSyncConsumer() && !syncBaseItem.getSyncConsumer().isOperating()) {
                return;
            }

            if (!syncBaseItem.getSyncItemArea().hasPosition()) {
                return;
            }

            if (checkGuardingItemHasEnemiesInRange(syncBaseItem)) {
                return;
            }

            synchronized (guardingItems) {
                guardingItems.add(syncBaseItem);
            }
        } catch (Exception e) {
            exceptionHandler.handleException("ActionService.addGuardingBaseItem() " + syncTickItem, e);
        }
    }

    public void interactionGuardingItems(SyncBaseItem target) {
        try {
            if (MODE != PlanetMode.MASTER) {
                return;
            }
            if (target.isContainedIn()) {
                return;
            }
            if (!target.isAlive()) {
                return;
            }
            // Prevent ConcurrentModificationException
            List<SyncBaseItem> attackers = new ArrayList<SyncBaseItem>();
            synchronized (guardingItems) {
                for (SyncBaseItem attacker : guardingItems) {
                    if (attacker == target) {
                        continue;
                    }
                    if (!attacker.isAlive()) {
                        continue;
                    }
                    if (attacker.isEnemy(target)
                            && attacker.getSyncWeapon().isAttackAllowedWithoutMoving(target)
                            && !attacker.getSyncWeapon().isItemTypeDisallowed(target)) {
                        attackers.add(attacker);
                    }
                }
            }
            for (SyncBaseItem attacker : attackers) {
                throw new UnsupportedOperationException();
                // TODO commandService.defend(attacker, target);
            }
        } catch (Exception e) {
            exceptionHandler.handleException(e);
        }
    }

    public void removeGuardingBaseItem(SyncBaseItem syncItem) {
        if (MODE != PlanetMode.MASTER) {
            return;
        }
        if (!syncItem.hasSyncWeapon()) {
            return;
        }

        synchronized (guardingItems) {
            guardingItems.remove(syncItem);
        }
    }

    private boolean checkGuardingItemHasEnemiesInRange(SyncBaseItem guardingItem) {
        SyncBaseItem target = baseItemService.getFirstEnemyItemInRange(guardingItem);
        if (target != null) {
            throw new UnsupportedOperationException();
            // TODO commandService.defend(guardingItem, target);
            // TODO return true;
        } else {
            return false;
        }
    }

//    public void syncItemActivated(SyncTickItem syncTickItem) {
//        addToQueue(syncTickItem);
//        TODO addGuardingBaseItem(syncTickItem);
//    }
//
//    public void finalizeCommand(SyncBaseItem syncItem) {
//        addToQueue(syncItem);
//        TODO removeGuardingBaseItem(syncItem);
//    }

    public PlanetConfig getPlanetConfig() {
        return planetConfig;
    }

    public boolean isPause() {
        return pause;
    }

    public void setPause(boolean pause) {
        this.pause = pause;
    }

    public void stop() {
        scheduledFuture.cancel();
    }
}
