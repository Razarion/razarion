package com.btxtech.server.persistence.item;

import com.btxtech.server.ArquillianBaseTest;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * on 09.01.2018.
 */
public class ItemTrackerPersistenceTest extends ArquillianBaseTest {
    @Inject
    private UserService userService;
    @Inject
    private BaseItemService baseItemService;

    @Before
    public void before() throws Exception {
        clearMongoDb();
        setupPlanetWithSlopes();
    }

    @After
    public void after() throws Exception {
        cleanUsers();
        cleanPlanetWithSlopes();
        clearMongoDb();
    }

    @Test
    public void test() {
        UserContext userContext = userService.getUserContextFromSession(); // Simulate anonymous login
        // Create base
        clearMongoDb();
        PlayerBaseFull playerBaseFull = baseItemService.createHumanBaseWithBaseItem(userContext.getLevelId(), userContext.getUnlockedItemLimit(), userContext.getHumanPlayerId(), userContext.getName(), new DecimalPosition(1000, 1000));
        tickPlanetServiceBaseServiceActive();
        // Verify
        Assert.assertEquals(1, playerBaseFull.getItemCount());
        List<ItemTracking> itemTrackings = readMongoDb("server_item_tracking", ItemTracking.class);
        Assert.assertEquals(2, itemTrackings.size());
        Assert.assertEquals(ItemTracking.Type.BASE_CREATED,itemTrackings.get(0).getType());
        Assert.assertEquals(ItemTracking.Type.BASE_ITEM_SPAWN,itemTrackings.get(1).getType());
    }

}