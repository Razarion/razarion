package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Provider;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * on 21.11.2017.
 */
@Singleton
public class GuardingItemService {

    private SyncItemContainerServiceImpl syncItemContainerService;

    private ExceptionHandler exceptionHandler;

    private Provider<CommandService> commandService;
    private final Collection<SyncBaseItem> guardingItems = new ArrayList<>();
    private GameEngineMode gameEngineMode;

    @Inject
    public GuardingItemService(Provider<com.btxtech.shared.gameengine.planet.CommandService> commandService, ExceptionHandler exceptionHandler, SyncItemContainerServiceImpl syncItemContainerService) {
        this.commandService = commandService;
        this.exceptionHandler = exceptionHandler;
        this.syncItemContainerService = syncItemContainerService;
    }

    public void init(GameEngineMode gameEngineMode) {
        this.gameEngineMode = gameEngineMode;
        synchronized (guardingItems) {
            guardingItems.clear();
        }
    }

    public void tick() {
        if (gameEngineMode != GameEngineMode.MASTER) {
            return;
        }

        // Prevent ConcurrentModificationException
        List<SyncBaseItem> attackers;
        synchronized (guardingItems) {
            attackers = new ArrayList<>(guardingItems);
        }
        while (!attackers.isEmpty()) {
            handleGuardingItemHasEnemiesInRange(attackers.remove(0));
        }
    }

    public boolean add(SyncBaseItem syncBaseItem) {
        try {
            if (gameEngineMode != GameEngineMode.MASTER) {
                return false;
            }

            if (syncBaseItem.getSyncWeapon() == null || !syncBaseItem.isAlive()) {
                return false;
            }

            if (!syncBaseItem.isBuildup()) {
                return false;
            }

            if (syncBaseItem.isContainedIn()) {
                return false;
            }

            if (handleGuardingItemHasEnemiesInRange(syncBaseItem)) {
                return true;
            }

            synchronized (guardingItems) {
                guardingItems.add(syncBaseItem);
            }
        } catch (Exception e) {
            exceptionHandler.handleException("GuardingItemService.add(): " + syncBaseItem, e);
        }
        return false;
    }

    public void remove(SyncBaseItem syncBaseItem) {
        if (gameEngineMode != GameEngineMode.MASTER) {
            return;
        }
        if (syncBaseItem.getSyncWeapon() == null) {
            return;
        }

        synchronized (guardingItems) {
            guardingItems.remove(syncBaseItem);
        }
    }

    private boolean handleGuardingItemHasEnemiesInRange(SyncBaseItem guardingItem) {
        try {
            SyncBaseItem target = findNearestEnemy(guardingItem);
            if (target != null) {
                commandService.get().defend(guardingItem, target);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            exceptionHandler.handleException("GuardingItemService.handleGuardingItemHasEnemiesInRange(): " + guardingItem, e);
        }
        return false;
    }

    private SyncBaseItem findNearestEnemy(SyncBaseItem guardingItem) {
        Collection<SyncBaseItem> enemyItems = syncItemContainerService.findEnemyItems(guardingItem.getBase(), new PlaceConfig().position(guardingItem.getAbstractSyncPhysical().getPosition()).radius(guardingItem.getBaseItemType().getWeaponType().getRange() + guardingItem.getAbstractSyncPhysical().getRadius()));
        double distance = Double.MAX_VALUE;
        SyncBaseItem nearest = null;
        for (SyncBaseItem enemyItem : enemyItems) {
            double tmpDistance = enemyItem.getAbstractSyncPhysical().getDistance(guardingItem);
            if (distance > tmpDistance) {
                distance = tmpDistance;
                nearest = enemyItem;
            }
        }
        return nearest;
    }


}
