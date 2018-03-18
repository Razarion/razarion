package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.TestHelper;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.planet.GameTestContent;
import com.btxtech.shared.gameengine.planet.WeldSlaveEmulator;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import org.junit.Test;

/**
 * Created by Beat
 * on 18.03.2018.
 */
public class MoveSingleUnitTest extends AStarBaseTest {
    @Test
    public void moveEast() {
        UserContext userContext = createLevel1UserContext();
        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(64, 160), userContext);
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, GameTestContent.BUILDER_ITEM_TYPE_ID);

        WeldSlaveEmulator slave = new WeldSlaveEmulator();
        slave.connectToMater(userContext, this);
        slave.tickPlanetServiceBaseServiceActive();
        SyncBaseItem slaveBuilder = slave.findSyncBaseItem(playerBaseFull, GameTestContent.BUILDER_ITEM_TYPE_ID);

        getCommandService().move(builder, new DecimalPosition(200, 160));

        slave.tickPlanetServiceBaseServiceActive();
        tickPlanetServiceBaseServiceActive();

        TestHelper.assertDecimalPosition("Unexpected master position", new DecimalPosition(200, 160), builder.getSyncPhysicalMovable().getPosition2d(), 0.2);
        TestHelper.assertDecimalPosition("Unexpected slave position", new DecimalPosition(200, 160), slaveBuilder.getSyncPhysicalMovable().getPosition2d(), 0.2);
    }

}
