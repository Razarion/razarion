package com.btxtech.server.user;

import com.btxtech.server.ArquillianBaseTest;
import com.btxtech.shared.datatypes.UserContext;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;

/**
 * Created by Beat
 * 05.05.2017.
 */
public class UserServiceTest extends ArquillianBaseTest {
    @Inject
    private UserService userService;

    @Test
    public void unregisteredUser() throws Exception {
        setupLevels();

        UserContext userContext = userService.getUserContext();
        Assert.assertEquals(LEVEL_1_ID, userContext.getLevelId());
        Assert.assertEquals(0, userContext.getCrystals());
        Assert.assertEquals(0, userContext.getXp());
        Assert.assertNull(userContext.getHumanPlayerId().getUserId());
        Assert.assertEquals("Unregistered User", userContext.getName());
        Assert.assertFalse(userContext.isAdmin());
        Assert.assertTrue(userContext.getInventoryItemIds().isEmpty());
        Assert.assertTrue(userContext.getInventoryArtifactIds().isEmpty());
        Assert.assertTrue(userContext.getUnlockedItemTypes().isEmpty());
        Assert.assertTrue(userContext.getUnlockedQuests().isEmpty());
        Assert.assertTrue(userContext.getUnlockedPlanets().isEmpty());

        cleanLevels();
    }

    @Test
    public void handleFacebookUserLogin() throws Exception {
        setupLevels();
        System.out.println("userService: " + userService);
        userService.handleFacebookUserLogin("0000001");
        cleanLevels();
    }

}