package com.btxtech.shared.gameengine.planet.basic;

import com.btxtech.shared.TestHelper;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
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
        SyncBaseItem builder = findSyncBaseItem(playerBaseFull, FallbackConfig.BUILDER_ITEM_TYPE_ID);
        getCommandService().move(builder, new DecimalPosition(152, 32));
        tickPlanetServiceBaseServiceActive();
        // TODO this test fails. That's bad. Has maybe something to do with in sight only one beam in the middle
        TestHelper.assertDecimalPosition(null, new DecimalPosition(152, 32), builder.getSyncPhysicalArea().getPosition2d(), 0.5);
        TestHelper.assertDecimalPosition(null, new DecimalPosition(0, 0), builder.getSyncPhysicalMovable().getVelocity(), 1);
    }

}
