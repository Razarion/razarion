package com.btxtech.server.user;

import com.btxtech.server.ArquillianBaseTest;
import com.btxtech.server.web.SessionHolder;
import com.btxtech.shared.datatypes.UserContext;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Locale;

/**
 * Created by Beat
 * 05.05.2017.
 */
public class UserServiceTest extends ArquillianBaseTest {
    @Inject
    private UserService userService;
    @Inject
    private SessionHolder sessionHolder;

    @Test
    public void registeredUser() throws Exception {
        setupLevels();

        userService.handleFacebookUserLogin("0000001");

        UserEntity userEntity = userService.getUserForFacebookId("0000001");

        UserContext userContext = userService.getUserContextFromSession();
        Assert.assertEquals(LEVEL_1_ID, userContext.getLevelId());
        Assert.assertEquals(0, userContext.getXp());
        Assert.assertEquals(userEntity.getId(), userContext.getHumanPlayerId().getUserId());
        Assert.assertEquals("Registered User", userContext.getName());
        Assert.assertFalse(userContext.isAdmin());
        Assert.assertTrue(userContext.getUnlockedItemLimit().isEmpty());

        runInTransaction(em -> {
            UserEntity actualUserEntity = em.find(UserEntity.class, userEntity.getId());
            Assert.assertEquals(LEVEL_1_ID, (int) actualUserEntity.getLevel().getId());
            Assert.assertNull(actualUserEntity.getActiveQuest());
            Assert.assertEquals(Locale.US, actualUserEntity.getLocale());
            em.remove(actualUserEntity);
        });


        cleanLevels();
    }

    @Test
    public void unregisteredUser() throws Exception {
        setupLevels();

        UserContext userContext = userService.getUserContextFromSession();
        Assert.assertEquals(LEVEL_1_ID, userContext.getLevelId());
        Assert.assertEquals(0, userContext.getXp());
        Assert.assertNull(userContext.getHumanPlayerId().getUserId());
        Assert.assertEquals("Unregistered User", userContext.getName());
        Assert.assertFalse(userContext.isAdmin());
        Assert.assertTrue(userContext.getUnlockedItemLimit().isEmpty());

        cleanLevels();
    }

}