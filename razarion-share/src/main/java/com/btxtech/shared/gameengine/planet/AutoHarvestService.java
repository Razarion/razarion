package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.gameengine.planet.pathing.PathingService;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sends a human player's harvester to the nearest crystal on its own when it leaves the factory.
 * When that crystal runs dry the harvester stops - on purpose, to see in the recorder whether the
 * dropout just moves to that point (2026-11-06). A new harvester used to just stand at the rally
 * point until the player gave a harvest order, and many new players never did - "L2 Harvest 5
 * razarion" was the biggest dropout of the tutorial (recorder, 2026-11-05/06). Bots are left out,
 * they get their harvest orders from their bot config. Any order the player gives replaces this one.
 * Master only.
 */
@Singleton
public class AutoHarvestService {
    /**
     * How far around the harvester to look for a crystal. The crystals around the noob-island start
     * positions are 30-57 m from the base, so this finds them without sending a harvester across the map.
     */
    static final double SEARCH_RADIUS = 80;
    private final Logger logger = Logger.getLogger(AutoHarvestService.class.getName());
    private final Provider<CommandService> commandService;
    private final Provider<PathingService> pathingService;
    private final Provider<SyncItemContainerServiceImpl> syncItemContainerService;
    private final Provider<BaseItemService> baseItemService;

    @Inject
    public AutoHarvestService(Provider<CommandService> commandService,
                              Provider<PathingService> pathingService,
                              Provider<SyncItemContainerServiceImpl> syncItemContainerService,
                              Provider<BaseItemService> baseItemService) {
        this.commandService = commandService;
        this.pathingService = pathingService;
        this.syncItemContainerService = syncItemContainerService;
        this.baseItemService = baseItemService;
    }

    /** A unit came out of a factory. */
    public void onFactoryItemCreated(SyncBaseItem syncBaseItem) {
        harvestNearest(syncBaseItem);
    }

    private void harvestNearest(SyncBaseItem harvester) {
        try {
            if (baseItemService.get().getGameEngineMode() != GameEngineMode.MASTER) {
                return;
            }
            if (harvester.getSyncHarvester() == null
                    || !harvester.isAlive()
                    || harvester.isContainedIn()
                    || !harvester.getAbstractSyncPhysical().canMove()
                    || harvester.getBase() == null
                    || !harvester.getBase().getCharacter().isHuman()) {
                return;
            }
            SyncResourceItem resource = findNearestReachable(harvester);
            if (resource != null) {
                commandService.get().harvest(harvester, resource);
            }
        } catch (Throwable t) {
            logger.log(Level.WARNING, "AutoHarvestService: harvester " + harvester.getId(), t);
        }
    }

    private SyncResourceItem findNearestReachable(SyncBaseItem harvester) {
        SyncResourceItem[] found = new SyncResourceItem[1];
        // iterateCellRadiusItem hands the items over nearest first
        syncItemContainerService.get().iterateCellRadiusItem(harvester.getAbstractSyncPhysical().getPosition(), SEARCH_RADIUS, (SyncItem syncItem) -> {
            if (found[0] != null || !(syncItem instanceof SyncResourceItem) || !syncItem.isAlive()) {
                return;
            }
            SyncResourceItem resource = (SyncResourceItem) syncItem;
            // A crystal on the other side of the water is close but not reachable
            if (pathingService.get().setupPathToDestination(harvester, 0, resource).isDestinationReachable()) {
                found[0] = resource;
            }
        });
        return found[0];
    }
}
