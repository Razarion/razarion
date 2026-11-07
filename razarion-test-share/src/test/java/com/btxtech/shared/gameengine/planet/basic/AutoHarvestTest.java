package com.btxtech.shared.gameengine.planet.basic;

import com.btxtech.shared.SimpleTestEnvironment;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.InitializeService;
import com.btxtech.shared.gameengine.datatypes.Character;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.command.SimplePath;
import com.btxtech.shared.gameengine.planet.AutoHarvestService;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.CommandService;
import com.btxtech.shared.gameengine.planet.SyncItemContainerServiceImpl;
import com.btxtech.shared.gameengine.planet.model.AbstractSyncPhysical;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncHarvester;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.gameengine.planet.pathing.PathingService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A harvester of a human player goes to the nearest crystal it can reach on its own. Stand-ins
 * rather than mocks, see BotEnragementStateTest.
 */
public class AutoHarvestTest {
    private final List<SyncResourceItem> resourcesNearestFirst = new ArrayList<>();
    // By identity: the stand-ins all have id 0, and SyncItem equality goes by id
    private final Set<SyncResourceItem> unreachable = Collections.newSetFromMap(new IdentityHashMap<>());
    private final List<SyncResourceItem> harvestOrders = new ArrayList<>();
    private BaseItemService baseItemService;
    private AutoHarvestService autoHarvestService;

    @Before
    public void setUp() {
        baseItemService = new BaseItemService(null, null, null, null, null, null, null, null, null, null,
                new InitializeService(), null);
        setMode(GameEngineMode.MASTER);
        CommandService commandService = new CommandService(null, null, null, null, null, null, null, null, null) {
            @Override
            public void harvest(SyncBaseItem harvester, SyncResourceItem resource) {
                harvestOrders.add(resource);
            }
        };
        PathingService pathingService = new PathingService(null, null, null) {
            @Override
            public SimplePath setupPathToDestination(SyncBaseItem syncBaseItem, double rangeOtherTerrain, SyncItem target) {
                SimplePath path = new SimplePath();
                path.setDestinationReachable(!unreachable.contains(target));
                return path;
            }
        };
        SyncItemContainerServiceImpl syncItemContainerService = new SyncItemContainerServiceImpl(null, null, null, null, null, null, null, null) {
            @Override
            public void iterateCellRadiusItem(DecimalPosition center, double radius, Consumer<SyncItem> callback) {
                resourcesNearestFirst.forEach(callback);
            }
        };
        autoHarvestService = new AutoHarvestService(() -> commandService, () -> pathingService, () -> syncItemContainerService, () -> baseItemService);
    }

    @Test
    public void newHarvesterGoesToNearestCrystal() {
        SyncResourceItem near = resource();
        SyncResourceItem far = resource();
        autoHarvestService.onFactoryItemCreated(harvester(Character.HUMAN));
        Assert.assertEquals(List.of(near), harvestOrders);
        Assert.assertNotSame(far, harvestOrders.get(0));
    }

    @Test
    public void skipsCrystalBeyondTheWater() {
        SyncResourceItem acrossTheWater = resource();
        SyncResourceItem reachable = resource();
        unreachable.add(acrossTheWater);
        autoHarvestService.onFactoryItemCreated(harvester(Character.HUMAN));
        Assert.assertEquals(List.of(reachable), harvestOrders);
    }

    @Test
    public void skipsEmptyCrystal() {
        SyncResourceItem empty = resource();
        SyncResourceItem full = resource();
        SimpleTestEnvironment.injectService("amount", empty, SyncResourceItem.class, 0.0);
        autoHarvestService.onFactoryItemCreated(harvester(Character.HUMAN));
        Assert.assertEquals(List.of(full), harvestOrders);
    }

    @Test
    public void noCrystalInReach() {
        autoHarvestService.onFactoryItemCreated(harvester(Character.HUMAN));
        Assert.assertTrue(harvestOrders.isEmpty());
    }

    @Test
    public void botHarvesterIsLeftToTheBot() {
        resource();
        autoHarvestService.onFactoryItemCreated(harvester(Character.BOT));
        Assert.assertTrue(harvestOrders.isEmpty());
    }

    @Test
    public void notAHarvester() {
        resource();
        autoHarvestService.onFactoryItemCreated(item(Character.HUMAN, null));
        Assert.assertTrue(harvestOrders.isEmpty());
    }

    @Test
    public void slaveDoesNothing() {
        resource();
        setMode(GameEngineMode.SLAVE);
        autoHarvestService.onFactoryItemCreated(harvester(Character.HUMAN));
        Assert.assertTrue(harvestOrders.isEmpty());
    }

    private void setMode(GameEngineMode mode) {
        SimpleTestEnvironment.injectService("gameEngineMode", baseItemService, BaseItemService.class, mode);
    }

    private SyncResourceItem resource() {
        SyncResourceItem resource = new SyncResourceItem(null);
        SimpleTestEnvironment.injectService("amount", resource, SyncResourceItem.class, 100.0);
        resourcesNearestFirst.add(resource);
        return resource;
    }

    private SyncBaseItem harvester(Character character) {
        return item(character, new SyncHarvester(null, null, null));
    }

    private SyncBaseItem item(Character character, SyncHarvester syncHarvester) {
        PlayerBase base = new PlayerBase(1, "Player", character, 0, 0, null, null, 0);
        AbstractSyncPhysical physical = new AbstractSyncPhysical(null) {
            @Override
            public DecimalPosition getPosition() {
                return new DecimalPosition(100, 100);
            }

            @Override
            public boolean canMove() {
                return true;
            }
        };
        return new SyncBaseItem(null, null, null, null, null, null, null, null, null, null, null, null, null) {
            @Override
            public SyncHarvester getSyncHarvester() {
                return syncHarvester;
            }

            @Override
            public boolean isAlive() {
                return true;
            }

            @Override
            public PlayerBase getBase() {
                return base;
            }

            @Override
            public AbstractSyncPhysical getAbstractSyncPhysical() {
                return physical;
            }
        };
    }
}
