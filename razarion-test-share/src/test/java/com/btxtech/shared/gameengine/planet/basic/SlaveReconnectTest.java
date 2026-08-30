package com.btxtech.shared.gameengine.planet.basic;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.planet.DaggerSlaveEmulator;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import org.junit.Assert;
import org.junit.Test;

/**
 * A game socket that closes and comes back.
 * <p>
 * On PROD, 2026-08-30, a phone's socket closed with 1006 five seconds after the player had placed
 * their factory. The server sent the full snapshot on reconnect, the client applied it onto the
 * world it already had, and sixteen ids collided. Each collision replaced a positioned item with a
 * positionless one, so from then on {@code onPostTick} threw for every item on every tick: the
 * factory never finished building and every unit disappeared from the screen. Reloading the browser
 * was the only way out, and a new player does not reload - they leave.
 * <p>
 * Two independent defects had to line up for that, and this covers both: the container replaced
 * before it refused, and a snapshot applied additively onto a populated world.
 */
public class SlaveReconnectTest extends BaseBasicTest {

    @Test
    public void theWorldSurvivesAReconnect() {
        setup();
        setupBot("Kenny", FallbackConfig.HARVESTER_ITEM_TYPE_ID, new DecimalPosition(230, 90), 1);

        UserContext userContext = createLevel1UserContext();
        DaggerSlaveEmulator slave = new DaggerSlaveEmulator();
        slave.connectToMaster(userContext, this);
        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(167, 136), userContext);
        tickPlanetServiceBaseServiceActive();
        slave.tickPlanetServiceBaseServiceActive();

        // The state the player was in: a builder putting up a factory, and a bot on the map.
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, FallbackConfig.BUILDER_ITEM_TYPE_ID);
        getCommandService().build(builder, new DecimalPosition(104, 144), getBaseItemType(FallbackConfig.FACTORY_ITEM_TYPE_ID));
        tickPlanetServiceBaseServiceActive();
        slave.tickPlanetServiceBaseServiceActive();
        slave.assertSyncItemCount(3, 0, 0);

        slave.reconnectToMaster(userContext);

        // Same world, not a doubled one and not an emptied one.
        slave.assertSyncItemCount(3, 0, 0);
        // The actual damage: an item that lost its physical area has no position, cannot be drawn,
        // and makes every following tick throw. Before the fix all three items ended up like this.
        assertEveryItemStillHasAPosition(slave);

        // And the tick that used to throw for ever must simply run.
        slave.tickPlanetServiceBaseServiceActive();
        tickPlanetServiceBaseServiceActive();
        slave.tickPlanetServiceBaseServiceActive();
        slave.assertSyncItemCount(3, 0, 0);
        assertEveryItemStillHasAPosition(slave);
    }

    /**
     * Reconnecting twice in a row is not a special case - a phone on a weak signal does exactly
     * that. Neither pass may leave anything behind.
     */
    @Test
    public void twoReconnectsInARowAreStillTheSameWorld() {
        setup();
        UserContext userContext = createLevel1UserContext();
        DaggerSlaveEmulator slave = new DaggerSlaveEmulator();
        slave.connectToMaster(userContext, this);
        createHumanBaseWithBaseItem(new DecimalPosition(167, 136), userContext);
        tickPlanetServiceBaseServiceActive();
        slave.tickPlanetServiceBaseServiceActive();
        slave.assertSyncItemCount(1, 0, 0);

        slave.reconnectToMaster(userContext);
        slave.reconnectToMaster(userContext);

        slave.assertSyncItemCount(1, 0, 0);
        assertEveryItemStillHasAPosition(slave);
    }

    private void assertEveryItemStillHasAPosition(DaggerSlaveEmulator slave) {
        slave.getSyncItemContainerService().iterateOverItems(true, true, null, syncItem -> {
            Assert.assertNotNull("No physical area after the snapshot: " + syncItem,
                    syncItem.getAbstractSyncPhysical());
            Assert.assertTrue("No position after the snapshot: " + syncItem,
                    syncItem.getAbstractSyncPhysical().hasPosition());
            return null;
        });
    }
}
