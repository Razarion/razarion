package com.btxtech.server.user;

import com.btxtech.server.ArquillianBaseTest;
import com.btxtech.server.persistence.history.LevelHistoryEntity;
import com.btxtech.server.web.SessionHolder;
import com.btxtech.server.web.SessionService;
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
    @Inject
    private SessionService sessionService;
    @Inject
    private SessionHolder sessionHolder;

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
    public void onLevelUpUnregistered() throws Exception {
        setupPlanets();

        String sessionId = sessionHolder.getPlayerSession().getHttpSessionId();
        userService.getUserContext(); // Simulate anonymous login

        userService.onLevelUpdate(sessionId, LEVEL_2_ID);
        Assert.assertEquals(LEVEL_1_ID, userService.getUserContext().getLevelId());
        userService.onLevelUpdate(sessionId, LEVEL_3_ID);
        Assert.assertEquals(LEVEL_1_ID, userService.getUserContext().getLevelId());
        userService.onLevelUpdate(sessionId, LEVEL_4_ID);
        Assert.assertEquals(LEVEL_4_ID, userService.getUserContext().getLevelId());

        assertCount(3, LevelHistoryEntity.class);
        assertCount(0, UserEntity.class);

        cleanTable(LevelHistoryEntity.class);

        cleanPlanets();
    }

    @Test
    public void onLevelUpRegister() throws Exception {
        setupPlanets();

        String sessionId = sessionHolder.getPlayerSession().getHttpSessionId();
        userService.handleFacebookUserLogin("0000001");

        assertUser("0000001", LEVEL_1_ID);

        userService.onLevelUpdate(sessionId, LEVEL_2_ID);
        Assert.assertEquals(LEVEL_1_ID, userService.getUserContext().getLevelId());
        assertUser("0000001", LEVEL_1_ID);

        userService.onLevelUpdate(sessionId, LEVEL_3_ID);
        Assert.assertEquals(LEVEL_1_ID, userService.getUserContext().getLevelId());
        assertUser("0000001", LEVEL_1_ID);

        userService.onLevelUpdate(sessionId, LEVEL_4_ID);
        Assert.assertEquals(LEVEL_4_ID, userService.getUserContext().getLevelId());
        assertUser("0000001", LEVEL_4_ID);

        assertCount(3, LevelHistoryEntity.class);
        assertCount(1, UserEntity.class);

        cleanTable(LevelHistoryEntity.class);
        cleanTable(UserEntity.class);

        cleanPlanets();
    }

    private void assertUser(String facebookUserId, int levelId) throws Exception {
        UserEntity userEntity = userService.getUserForFacebookId(facebookUserId);

        runInTransaction(em -> {
            UserEntity actualUserEntity = em.find(UserEntity.class, userEntity.getId());
            Assert.assertEquals(levelId, (int)actualUserEntity.getLevel().getId());
        });
    }

}