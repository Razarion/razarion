package com.btxtech.server.user;

import com.btxtech.server.ArquillianBaseTest;
import com.btxtech.server.ClientGameConnectionServiceTestHelper;
import com.btxtech.server.TestClientGameConnection;
import com.btxtech.server.gameengine.ServerLevelQuestService;
import com.btxtech.server.persistence.history.UserHistoryEntity;
import com.btxtech.server.web.SessionHolder;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.ErrorResult;
import com.btxtech.shared.datatypes.SetNameResult;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 05.05.2017.
 */
public class UserServiceTest extends ArquillianBaseTest {
    @Inject
    private Logger logger;
    @Inject
    private UserService userService;
    @Inject
    private SessionHolder sessionHolder;
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private ServerLevelQuestService serverLevelQuestService;
    @Inject
    private ClientGameConnectionServiceTestHelper clientGameConnectionServiceTestHelper;
    @Resource(name = "DefaultManagedScheduledExecutorService")
    private ManagedScheduledExecutorService scheduleExecutor;
    @Inject
    private HttpSession session;

    @Test
    public void registeredUser() throws Exception {
        setupLevels();

        userService.handleFacebookUserLogin("0000001");

        UserEntity userEntity = userService.getUserForFacebookId("0000001");

        UserContext userContext = userService.getUserContextFromSession();
        Assert.assertEquals(LEVEL_1_ID, userContext.getLevelId());
        Assert.assertEquals(0, userContext.getXp());
        Assert.assertEquals(userEntity.getId(), userContext.getHumanPlayerId().getUserId());
        Assert.assertNull(userContext.getName());
        Assert.assertFalse(userContext.isAdmin());
        Assert.assertTrue(userContext.getUnlockedItemLimit().isEmpty());
        runInTransaction(em -> {
            UserEntity actualUserEntity = em.find(UserEntity.class, userEntity.getId());
            Assert.assertEquals(LEVEL_1_ID, (int) actualUserEntity.getLevel().getId());
            Assert.assertNull(actualUserEntity.getActiveQuest());
            Assert.assertEquals(Locale.US, actualUserEntity.getLocale());
            em.remove(actualUserEntity);
        });

        // Verify history entry
        runInTransaction(em -> {
            em.createQuery("SELECT uhe FROM UserHistoryEntity uhe where uhe.id =:userId AND uhe.loggedIn is not null AND uhe.sessionId =:sessionId", UserHistoryEntity.class).setParameter("userId", userContext.getHumanPlayerId().getUserId()).setParameter("sessionId", sessionHolder.getPlayerSession().getHttpSessionId()).getFirstResult();
        });
        session.invalidate();
        // Verify history entry
        runInTransaction(em -> {
            em.createQuery("SELECT uhe FROM UserHistoryEntity uhe where uhe.id =:userId AND uhe.loggedOut is not null AND uhe.sessionId =:sessionId", UserHistoryEntity.class).setParameter("userId", userContext.getHumanPlayerId().getUserId()).setParameter("sessionId", sessionHolder.getPlayerSession().getHttpSessionId()).getFirstResult();
        });
        cleanTable(UserHistoryEntity.class);
        cleanLevels();
    }

    @Test
    public void unregisteredUser() throws Exception {
        setupLevels();

        UserContext userContext = userService.getUserContextFromSession();
        Assert.assertEquals(LEVEL_1_ID, userContext.getLevelId());
        Assert.assertEquals(0, userContext.getXp());
        Assert.assertNull(userContext.getHumanPlayerId().getUserId());
        Assert.assertNull(userContext.getName());
        Assert.assertFalse(userContext.isAdmin());
        Assert.assertTrue(userContext.getUnlockedItemLimit().isEmpty());

        cleanLevels();
        session.invalidate();
        assertEmptyCount(UserHistoryEntity.class);
    }

    @Test
    public void setNameTest() throws Exception {
        setupPlanetWithSlopes();

        runInTransaction(entityManager -> {
                    UserEntity existingUser = new UserEntity();
                    existingUser.setName("Existing User");
                    entityManager.persist(existingUser);
                }
        );


//        Collection<Callable<Void>> callables = new ArrayList<>();
//        callables.add(() -> {
//            try {
        userService.handleFacebookUserLogin("0000001");

        String sessionId = sessionHolder.getPlayerSession().getHttpSessionId();
        UserEntity userEntity = userService.getUserForFacebookId("0000001");
        UserContext userContext = sessionHolder.getPlayerSession().getUserContext();
        serverLevelQuestService.onClientLevelUpdate(sessionId, LEVEL_4_ID);
        TestClientGameConnection testClientGameConnection = clientGameConnectionServiceTestHelper.connectClient(sessionHolder.getPlayerSession());

        baseItemService.createHumanBaseWithBaseItem(userContext.getLevelId(), userContext.getUnlockedItemLimit(), userContext.getHumanPlayerId(), userContext.getName(), new DecimalPosition(1000, 1000));
        Thread.sleep(5000);
        PlayerBase playerBase = baseItemService.getPlayerBase4HumanPlayerId(userContext.getHumanPlayerId());
        Assert.assertNotNull(playerBase);
        Assert.assertNull(playerBase.getName());
        // Set wrong name
        Assert.assertEquals(ErrorResult.TO_SHORT, userService.setName("").getErrorResult());
        Assert.assertEquals(ErrorResult.TO_SHORT, userService.setName("x").getErrorResult());
        Assert.assertEquals(ErrorResult.TO_SHORT, userService.setName("bb").getErrorResult());
        Assert.assertEquals(ErrorResult.ALREADY_USED, userService.setName("Existing User").getErrorResult());
        Assert.assertNull(playerBase.getName());
        Assert.assertNull(userService.getUserContextFromSession().getName());
        runInTransaction(entityManager -> Assert.assertNull(entityManager.find(UserEntity.class, userEntity.getId()).toUserContext().getName()));
        testClientGameConnection.assertPacketStringSent("BASE_NAME_CHANGED", 0);
        // Set name
        SetNameResult setNameResult = userService.setName("USER 1 NAME");
        // Verify
        Assert.assertNull("USER 1 NAME", setNameResult.getErrorResult());
        Assert.assertEquals("USER 1 NAME", setNameResult.getUserContext().getName());
        Assert.assertEquals("USER 1 NAME", playerBase.getName());
        Assert.assertEquals("USER 1 NAME", userService.getUserContextFromSession().getName());
        runInTransaction(entityManager -> Assert.assertEquals("USER 1 NAME", entityManager.find(UserEntity.class, userEntity.getId()).toUserContext().getName()));
        testClientGameConnection.assertPacketStringSent("BASE_NAME_CHANGED", 1);
        int index = testClientGameConnection.findFirstPacketStringSentIndex("BASE_NAME_CHANGED");
        Assert.assertTrue(testClientGameConnection.assertAndExtractBody(index, "BASE_NAME_CHANGED").contains("\"name\":\"USER 1 NAME\""));
        // Set again -> fail
        try {
            userService.setName("USER xxx NAME");
            Assert.fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
            Assert.assertTrue(e.getMessage().startsWith("The name has already been set"));
        }
        // Verify
        Assert.assertNull("USER 1 NAME", setNameResult.getErrorResult());
        Assert.assertEquals("USER 1 NAME", setNameResult.getUserContext().getName());
        Assert.assertEquals("USER 1 NAME", playerBase.getName());
        Assert.assertEquals("USER 1 NAME", userService.getUserContextFromSession().getName());
        runInTransaction(entityManager -> Assert.assertEquals("USER 1 NAME", entityManager.find(UserEntity.class, userEntity.getId()).toUserContext().getName()));
        testClientGameConnection.assertPacketStringSent("BASE_NAME_CHANGED", 1);

//            } catch (Throwable t) {
//                logger.log(Level.SEVERE, "", t);
//            }
//            return null;
//        });
//
//        scheduleExecutor.invokeAll(callables, 10, TimeUnit.SECONDS);
        cleanUsers();
        cleanPlanetWithSlopes();
    }
}