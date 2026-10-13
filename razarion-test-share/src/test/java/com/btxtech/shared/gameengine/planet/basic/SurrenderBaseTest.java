package com.btxtech.shared.gameengine.planet.basic;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import org.junit.Assert;
import org.junit.Test;

/**
 * Surrendering a base reports it gone once. It used to be twice - once for the surrender and once
 * more when its last item was removed - so every client got two BASE_DELETED packets, the second for
 * a base it had already dropped ("deleteBaseSlave: Base with Id does not exits"), and the history
 * two BASE_DEFEATED records (local test, 2026-10-11).
 */
public class SurrenderBaseTest extends BaseBasicTest {

    @Test
    public void surrenderReportsTheBaseGoneOnce() {
        setup();
        HumanBaseContext context = createHumanBaseBFA();
        PlayerBaseFull base = context.getPlayerBaseFull();
        Assert.assertTrue(base.getItemCount() > 1);
        getTestGameLogicListener().clearAll();

        getBaseItemService().surrenderHumanBase(context.getUserContext().getUserId());

        Assert.assertEquals(1, getTestGameLogicListener().getBaseDeleted().size());
        Assert.assertSame(base, getTestGameLogicListener().getBaseDeleted().get(0));
        Assert.assertEquals(0, base.getItemCount());
        Assert.assertFalse(baseExists(base.getBaseId()));
    }

    @Test
    public void surrenderedBaseIsReplacedByTheNewOne() {
        setup();
        UserContext userContext = createLevel1UserContext();
        PlayerBaseFull first = createHumanBaseWithBaseItem(new DecimalPosition(167, 136), userContext);
        tickPlanetServiceBaseServiceActive();
        getTestGameLogicListener().clearAll();

        PlayerBaseFull second = createHumanBaseWithBaseItem(new DecimalPosition(167, 136), userContext);
        tickPlanetServiceBaseServiceActive();

        Assert.assertEquals(1, getTestGameLogicListener().getBaseDeleted().size());
        Assert.assertSame(first, getTestGameLogicListener().getBaseDeleted().get(0));
        Assert.assertNotEquals(first.getBaseId(), second.getBaseId());
        Assert.assertFalse(baseExists(first.getBaseId()));
    }

    private boolean baseExists(int baseId) {
        return getBaseItemService().getPlayerBaseInfos().stream().anyMatch(info -> info.getBaseId() == baseId);
    }
}
