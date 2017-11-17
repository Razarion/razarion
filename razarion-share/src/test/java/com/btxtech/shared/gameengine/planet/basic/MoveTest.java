package com.btxtech.shared.gameengine.planet.basic;

import com.btxtech.shared.TestHelper;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.planet.GameTestContent;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import org.junit.Test;

/**
 * Created by Beat
 * on 21.10.2017.
 */
public class MoveTest extends BaseBasicTest {
    @Test
    public void land() {
        setup();

        UserContext userContext = createLevel1UserContext();
        PlayerBaseFull playerBaseFull = createHumanBaseWithBaseItem(new DecimalPosition(40, 144), userContext);
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, GameTestContent.BUILDER_ITEM_TYPE_ID);
        getCommandService().move(builder, new DecimalPosition(152, 32));
        tickPlanetServiceBaseServiceActive();
        TestHelper.assertDecimalPosition(null, new DecimalPosition(152, 32), builder.getSyncPhysicalArea().getPosition2d(), 0.5);
        TestHelper.assertDecimalPosition(null, new DecimalPosition(0, 0), builder.getSyncPhysicalMovable().getVelocity(), 1);
        // showDisplay();
    }

}
