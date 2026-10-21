package com.btxtech.shared.gameengine.planet.basic;

import com.btxtech.shared.TestHelper;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.planet.DaggerSlaveEmulator;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Beat
 * on 21.10.2017.
 */
public class MoveTest extends BaseBasicTest {
    @Test
    public void landSimpleStraight() {
        setup();

        UserContext userContext = createLevel1UserContext();
        DaggerSlaveEmulator permSlave = new DaggerSlaveEmulator();
        permSlave.connectToMaster(userContext, this);
        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(17, 20), userContext);
        tickPlanetServiceBaseServiceActive();
        permSlave.tickPlanetServiceBaseServiceActive();
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, FallbackConfig.BUILDER_ITEM_TYPE_ID);
        getCommandService().move(builder, new DecimalPosition(55, 20));
        // showDisplay();
        tickPlanetServiceBaseServiceActive();
        permSlave.tickPlanetServiceBaseServiceActive();
        TestHelper.assertDecimalPosition(null, new DecimalPosition(55, 20), builder.getAbstractSyncPhysical().getPosition(), 0.5);
        Assert.assertNull(builder.getSyncPhysicalMovable().getVelocity());

        SyncBaseItem slaveBuilder = permSlave.getSyncItemContainerService().getSyncBaseItemSave(builder.getId());
        TestHelper.assertDecimalPosition(null, new DecimalPosition(55, 20), slaveBuilder.getAbstractSyncPhysical().getPosition(), 0.5);
        Assert.assertNull(slaveBuilder.getSyncPhysicalMovable().getVelocity());
    }

    @Test
    public void landAroundTerrain() {
        setup();

        UserContext userContext = createLevel1UserContext();
        DaggerSlaveEmulator permSlave = new DaggerSlaveEmulator();
        permSlave.connectToMaster(userContext, this);
        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(184, 95), userContext);
        tickPlanetServiceBaseServiceActive();
        permSlave.tickPlanetServiceBaseServiceActive();
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, FallbackConfig.BUILDER_ITEM_TYPE_ID);
        getCommandService().move(builder, new DecimalPosition(175, 72));
        // showDisplay();
        tickPlanetServiceBaseServiceActive();
        permSlave.tickPlanetServiceBaseServiceActive();
        TestHelper.assertDecimalPosition(null, new DecimalPosition(55, 20), builder.getAbstractSyncPhysical().getPosition(), 0.5);
        Assert.assertNull(builder.getSyncPhysicalMovable().getVelocity());

        SyncBaseItem slaveBuilder = permSlave.getSyncItemContainerService().getSyncBaseItemSave(builder.getId());
        TestHelper.assertDecimalPosition(null, new DecimalPosition(55, 20), slaveBuilder.getAbstractSyncPhysical().getPosition(), 0.5);
        Assert.assertNull(slaveBuilder.getSyncPhysicalMovable().getVelocity());
    }

    @Test
    public void land() {
        setup();

        UserContext userContext = createLevel1UserContext();
        DaggerSlaveEmulator permSlave = new DaggerSlaveEmulator();
        permSlave.connectToMaster(userContext, this);
        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(40, 144), userContext);
        tickPlanetServiceBaseServiceActive();
        permSlave.tickPlanetServiceBaseServiceActive();
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, FallbackConfig.BUILDER_ITEM_TYPE_ID);
        getCommandService().move(builder, new DecimalPosition(152, 32));
        // showDisplay();
        tickPlanetServiceBaseServiceActive();
        permSlave.tickPlanetServiceBaseServiceActive();
        TestHelper.assertDecimalPosition(null, new DecimalPosition(152, 32), builder.getAbstractSyncPhysical().getPosition(), 0.5);
        Assert.assertNull(builder.getSyncPhysicalMovable().getVelocity());

        SyncBaseItem slaveBuilder = permSlave.getSyncItemContainerService().getSyncBaseItemSave(builder.getId());
        TestHelper.assertDecimalPosition(null, new DecimalPosition(152, 32), slaveBuilder.getAbstractSyncPhysical().getPosition(), 0.5);
        Assert.assertNull(slaveBuilder.getSyncPhysicalMovable().getVelocity());
    }

    @Test
    public void landSecondCommand() {
        setup();

        UserContext userContext = createLevel1UserContext();
        DaggerSlaveEmulator permSlave = new DaggerSlaveEmulator();
        permSlave.connectToMaster(userContext, this);
        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(132, 144), userContext);
        tickPlanetServiceBaseServiceActive();
        permSlave.tickPlanetServiceBaseServiceActive();
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, FallbackConfig.BUILDER_ITEM_TYPE_ID);
        getCommandService().move(builder, new DecimalPosition(168, 144));
        tickPlanetService(75);
        permSlave.tickPlanetService(75);
        getCommandService().move(builder, new DecimalPosition(184, 120));
        tickPlanetServiceBaseServiceActive();
        permSlave.tickPlanetServiceBaseServiceActive();

        // showDisplay();
        TestHelper.assertDecimalPosition(null, new DecimalPosition(184, 120), builder.getAbstractSyncPhysical().getPosition(), 0.5);
        Assert.assertNull(builder.getSyncPhysicalMovable().getVelocity());

        SyncBaseItem slaveBuilder = permSlave.getSyncItemContainerService().getSyncBaseItemSave(builder.getId());
        TestHelper.assertDecimalPosition(null, new DecimalPosition(184, 120), slaveBuilder.getAbstractSyncPhysical().getPosition(), 0.5);
        Assert.assertNull(slaveBuilder.getSyncPhysicalMovable().getVelocity());
    }

    /**
     * Redirected while still under way, a few metres short of the first goal, on to a goal 20 m
     * further. The slave kept the closest distance it had reached to the first goal, measured the new
     * approach against it, "made no progress" and gave up after 2.5 s - standing metres short while
     * the master drove on, until the master's stop made it jump (local test, 2026-10-11: TELEPORTING
     * 3.7-32 m). Fails without the reset in SyncPhysicalMovable.synchronize() at tick 24.
     * <p>
     * Not a U-turn: turning on the spot gets no closer either, and there the master replans (which
     * resets its watch) while the slave, which never replans, gives up until the replanned path
     * arrives. That is a separate, shorter stall.
     */
    @Test
    public void landRedirectedShortlyBeforeArrival() {
        setup();

        UserContext userContext = createLevel1UserContext();
        DaggerSlaveEmulator permSlave = new DaggerSlaveEmulator();
        permSlave.connectToMaster(userContext, this);
        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(17, 20), userContext);
        tickPlanetServiceBaseServiceActive();
        permSlave.tickPlanetServiceBaseServiceActive();
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, FallbackConfig.BUILDER_ITEM_TYPE_ID);
        SyncBaseItem slaveBuilder = permSlave.getSyncItemContainerService().getSyncBaseItemSave(builder.getId());
        DecimalPosition firstGoal = new DecimalPosition(55, 20);
        DecimalPosition secondGoal = new DecimalPosition(75, 20);

        getCommandService().move(builder, firstGoal);
        for (int tick = 0; tick < 300 && builder.getAbstractSyncPhysical().getPosition().getDistance(firstGoal) > 3; tick++) {
            tickPlanetService(1);
            permSlave.tickPlanetService(1);
        }
        Assert.assertTrue("still under way to the first goal", builder.getSyncPhysicalMovable().hasDestination());

        getCommandService().move(builder, secondGoal);
        for (int tick = 0; tick < 300 && builder.getSyncPhysicalMovable().hasDestination(); tick++) {
            tickPlanetService(1);
            permSlave.tickPlanetService(1);
            if (builder.getSyncPhysicalMovable().hasDestination()) {
                Assert.assertTrue("the slave stopped at " + slaveBuilder.getAbstractSyncPhysical().getPosition()
                                + " while the master drove on at " + builder.getAbstractSyncPhysical().getPosition() + " (tick " + tick + ")",
                        slaveBuilder.getSyncPhysicalMovable().hasDestination()
                                || slaveBuilder.getAbstractSyncPhysical().getPosition().getDistance(secondGoal) < 1);
            }
        }
        tickPlanetService(2);
        permSlave.tickPlanetService(2);

        TestHelper.assertDecimalPosition(null, secondGoal, builder.getAbstractSyncPhysical().getPosition(), 0.5);
        TestHelper.assertDecimalPosition(null, secondGoal, slaveBuilder.getAbstractSyncPhysical().getPosition(), 0.5);
    }

    /**
     * Turned round under way, on to a goal just behind the unit. Turning on the spot gets no closer to
     * it for a while; the master keeps going, the slave used to judge that on its own, give up after
     * 2.5 s and stand there until the master's next sync. Only the master may give up now
     * (SyncPhysicalMovable.stopIfDestinationReached(mayGiveUp)) - in a fight the slave's give-up also
     * ended the attack and left whole Viper groups standing while the server drove on (PROD
     * [SyncSnap] 11.-18.10.2026: jumps of 30-113 m, localIdle=true).
     */
    @Test
    public void slaveDoesNotGiveUpOnAUTurn() {
        setup();

        UserContext userContext = createLevel1UserContext();
        DaggerSlaveEmulator permSlave = new DaggerSlaveEmulator();
        permSlave.connectToMaster(userContext, this);
        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(17, 20), userContext);
        tickPlanetServiceBaseServiceActive();
        permSlave.tickPlanetServiceBaseServiceActive();
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, FallbackConfig.BUILDER_ITEM_TYPE_ID);
        SyncBaseItem slaveBuilder = permSlave.getSyncItemContainerService().getSyncBaseItemSave(builder.getId());
        DecimalPosition firstGoal = new DecimalPosition(75, 20);
        DecimalPosition behind = new DecimalPosition(40, 20);

        getCommandService().move(builder, firstGoal);
        for (int tick = 0; tick < 300 && builder.getAbstractSyncPhysical().getPosition().getX() < 45; tick++) {
            tickPlanetService(1);
            permSlave.tickPlanetService(1);
        }
        Assert.assertTrue("at full speed past the turning goal", builder.getSyncPhysicalMovable().hasDestination());

        getCommandService().move(builder, behind);
        for (int tick = 0; tick < 300 && builder.getSyncPhysicalMovable().hasDestination(); tick++) {
            tickPlanetService(1);
            permSlave.tickPlanetService(1);
            if (builder.getSyncPhysicalMovable().hasDestination()) {
                Assert.assertTrue("the slave stopped at " + slaveBuilder.getAbstractSyncPhysical().getPosition()
                                + " while the master drove on at " + builder.getAbstractSyncPhysical().getPosition() + " (tick " + tick + ")",
                        slaveBuilder.getSyncPhysicalMovable().hasDestination()
                                || slaveBuilder.getAbstractSyncPhysical().getPosition().getDistance(behind) < 1);
            }
        }
        tickPlanetService(2);
        permSlave.tickPlanetService(2);

        TestHelper.assertDecimalPosition(null, builder.getAbstractSyncPhysical().getPosition(), slaveBuilder.getAbstractSyncPhysical().getPosition(), 0.5);
    }

}
