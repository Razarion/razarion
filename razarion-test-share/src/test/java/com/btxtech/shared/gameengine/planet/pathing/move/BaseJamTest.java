package com.btxtech.shared.gameengine.planet.pathing.move;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.gameengine.planet.testframework.ScenarioBaseTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

/**
 * Units leaving their own base: the PROD "[PathingStuck] gave up" cases next to an own building
 * (docs/architecture/pathing-flowfield-analysis.md).
 * <p>
 * A unit touching a factory, sent to a point on the far side of it. A* routes around the building;
 * the way point follower used to test its look ahead against the terrain only, steered through the
 * building, and a second building with a gap narrower than the unit trapped it: three identical
 * replans, then gave up. Test content radii (factory 5, generator 2, unit 2) instead of PROD (factory
 * 2.55, unit 1.0) - the geometry is the same, scaled.
 */
public class BaseJamTest extends ScenarioBaseTest {
    private static final DecimalPosition FACTORY = new DecimalPosition(270, 90);
    /**
     * Terrain-only sight needed 45-51 ticks for the single-building scenes and 137 for the unit
     * beside the factory; with buildings in the sight test all scenes take 38-48.
     */
    private static final int MAX_TICKS = 80;

    // --- one building ---------------------------------------------------------------------------

    @Test
    public void single_touchingDestBehind() {
        assertArrives(new DecimalPosition(277, 90), new DecimalPosition(258, 90));
    }

    @Test
    public void single_touchingDestBehindOffAxis() {
        assertArrives(new DecimalPosition(277, 90.3), new DecimalPosition(258, 93));
    }

    @Test
    public void single_fromAfar() {
        assertArrives(new DecimalPosition(285, 90), new DecimalPosition(258, 90));
    }

    // --- factory + generator with a gap narrower than the unit: gave up with terrain-only sight ---

    @Test
    public void pair_touchingDestBehind() {
        // Generator north of the factory, surface gap 1.5 < unit diameter 4.
        assertArrives(new DecimalPosition(277, 90), new DecimalPosition(258, 90),
                FallbackConfig.GENERATOR_ITEM_TYPE_ID, new DecimalPosition(270, 98.5));
    }

    @Test
    public void pair_touchingDestBehindNorth() {
        assertArrives(new DecimalPosition(277, 91), new DecimalPosition(258, 95),
                FallbackConfig.GENERATOR_ITEM_TYPE_ID, new DecimalPosition(270, 98.5));
    }

    @Test
    public void pair_touchingDestNorthWest() {
        assertArrives(new DecimalPosition(277, 90), new DecimalPosition(262, 104),
                FallbackConfig.GENERATOR_ITEM_TYPE_ID, new DecimalPosition(275, 97));
    }

    @Test
    public void pair_fromAfarNorthWest() {
        assertArrives(new DecimalPosition(285, 90), new DecimalPosition(262, 104),
                FallbackConfig.GENERATOR_ITEM_TYPE_ID, new DecimalPosition(275, 97));
    }

    // --- factory + a unit standing next to it: cost a replan with terrain-only sight --------------

    @Test
    public void unitBeside_touchingDestBehind() {
        assertArrives(new DecimalPosition(277, 90), new DecimalPosition(258, 90),
                FallbackConfig.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(270, 97.2));
    }

    @Test
    public void unitBeside_touchingDestNorthWest() {
        assertArrives(new DecimalPosition(277, 90), new DecimalPosition(262, 104),
                FallbackConfig.MOVING_TEST_ITEM_TYPE_ID, new DecimalPosition(275.2, 96.2));
    }

    // --- the rally point --------------------------------------------------------------------------

    /**
     * The rally point is chosen with the factory, before the rest of the base exists. A building put
     * on it later must not make the factory spawn its units into that building.
     */
    @Test
    public void rallyPointTakenByALaterBuilding() {
        PlayerBaseFull playerBase = createBase();
        SyncBaseItem factory = spawn(FallbackConfig.FACTORY_ITEM_TYPE_ID, FACTORY, playerBase);
        DecimalPosition rallyPoint = factory.getSyncFactory().getRallyPoint();
        SyncBaseItem generator = spawn(FallbackConfig.GENERATOR_ITEM_TYPE_ID, rallyPoint, playerBase);

        getCommandService().fabricate(factory, getBaseItemType(FallbackConfig.ATTACKER_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();

        SyncBaseItem attacker = findSyncBaseItem(playerBase, FallbackConfig.ATTACKER_ITEM_TYPE_ID);
        double footprint = generator.getAbstractSyncPhysical().getRadius() + attacker.getAbstractSyncPhysical().getRadius();
        double distance = attacker.getAbstractSyncPhysical().getPosition().getDistance(generator.getAbstractSyncPhysical().getPosition());
        Assert.assertTrue("spawned " + distance + " from the generator, footprint " + footprint, distance >= footprint);
        Assert.assertNotEquals(rallyPoint, factory.getSyncFactory().getRallyPoint());
    }

    private void assertArrives(DecimalPosition start, DecimalPosition destination) {
        assertArrives(start, destination, 0, null);
    }

    private void assertArrives(DecimalPosition start, DecimalPosition destination, int neighbourTypeId, DecimalPosition neighbourPosition) {
        PlayerBaseFull playerBase = createBase();
        spawn(FallbackConfig.FACTORY_ITEM_TYPE_ID, FACTORY, playerBase);
        if (neighbourPosition != null) {
            spawn(neighbourTypeId, neighbourPosition, playerBase);
        }
        SyncBaseItem unit = spawn(FallbackConfig.MOVING_TEST_ITEM_TYPE_ID, start, playerBase);
        SyncPhysicalMovable movable = (SyncPhysicalMovable) unit.getAbstractSyncPhysical();
        movable.setPath(getPathingService().setupPathToDestination(unit, destination));

        int ticks = 0;
        int replans = 0;
        while (movable.hasDestination() && ticks < MAX_TICKS) {
            tickPlanetService();
            ticks++;
            replans = Math.max(replans, movable.getReplanCount());
        }

        Assert.assertFalse("still moving after " + ticks + " ticks at " + movable.getPosition(), movable.hasDestination());
        Assert.assertFalse("gave up at " + movable.getPosition(), movable.isDestinationUnreachable());
        Assert.assertEquals("replans", 0, replans);
    }

    private PlayerBaseFull createBase() {
        UserContext userContext = createLevel1UserContext();
        PlayerBaseFull playerBase = getBaseItemService().createHumanBase(0, userContext.getLevelId(), Collections.emptyMap(), userContext.getUserId(), userContext.getName());
        playerBase.setResources(Double.MAX_VALUE);
        return playerBase;
    }

    private SyncBaseItem spawn(int typeId, DecimalPosition position, PlayerBaseFull playerBase) {
        try {
            return getBaseItemService().spawnSyncBaseItem(getTestShareDagger().itemTypeService().getBaseItemType(typeId), position, 0, playerBase, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
