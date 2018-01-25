package com.btxtech.server.user;

import com.btxtech.server.ArquillianBaseTest;
import com.btxtech.server.ClientGameConnectionServiceTestHelper;
import com.btxtech.server.TestClientGameConnection;
import com.btxtech.server.gameengine.ServerLevelQuestService;
import com.btxtech.server.gameengine.ServerUnlockService;
import com.btxtech.server.persistence.history.UserHistoryEntity;
import com.btxtech.server.persistence.inventory.InventoryItemEntity;
import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.server.persistence.level.LevelUnlockEntity;
import com.btxtech.server.persistence.quest.QuestConfigEntity;
import com.btxtech.server.web.SessionHolder;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.ErrorResult;
import com.btxtech.shared.datatypes.FbAuthResponse;
import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.datatypes.RegisterInfo;
import com.btxtech.shared.datatypes.SetNameResult;
import com.btxtech.shared.datatypes.SingleHolder;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.InventoryInfo;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.packets.PlayerBaseInfo;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
    @Inject
    private ServerUnlockService serverUnlockService;

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
        Assert.assertEquals("USER 1 NAME", setNameResult.getUserName());
        Assert.assertEquals("USER 1 NAME", playerBase.getName());
        Assert.assertEquals("USER 1 NAME", userService.getUserContextFromSession().getName());
        runInTransaction(entityManager -> Assert.assertEquals("USER 1 NAME", entityManager.find(UserEntity.class, userEntity.getId()).toUserContext().getName()));
        testClientGameConnection.assertPacketStringSent("BASE_NAME_CHANGED", 1);
        int index = testClientGameConnection.findFirstPacketStringSentIndex("BASE_NAME_CHANGED");
        testClientGameConnection.assertMessageSent(index, "BASE_NAME_CHANGED", PlayerBaseInfo.class, new PlayerBaseInfo().setBaseId(playerBase.getBaseId()).setName("USER 1 NAME"));
        // Set again -> fail
        try {
            userService.setName("USER xxx NAME");
            Assert.fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
            Assert.assertTrue(e.getMessage().startsWith("The name has already been set"));
        }
        // Verify
        Assert.assertNull("USER 1 NAME", setNameResult.getErrorResult());
        Assert.assertEquals("USER 1 NAME", setNameResult.getUserName());
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

    @Test
    public void handleInGameFacebookUserLogin() throws Exception {
        setupPlanetWithSlopes();

        // Prepare
        userService.handleUnregisteredLogin();

        String sessionId = sessionHolder.getPlayerSession().getHttpSessionId();
        sessionHolder.getPlayerSession().getUserContext().setXp(32);
        UnregisteredUser unregisteredUser = sessionHolder.getPlayerSession().getUnregisteredUser();
        unregisteredUser.setCrystals(99);
        unregisteredUser.addInventoryItemId(INVENTORY_ITEM_1_ID);
        unregisteredUser.addLevelUnlockEntityId(LEVEL_UNLOCK_ID_L4_1);
        unregisteredUser.addLevelUnlockEntityId(LEVEL_UNLOCK_ID_L5_1);
        unregisteredUser.addCompletedQuestId(SERVER_QUEST_ID_L5_1);
        unregisteredUser.addCompletedQuestId(SERVER_QUEST_ID_L5_2);
        unregisteredUser.addCompletedQuestId(SERVER_QUEST_ID_L5_3);
        HumanPlayerId oldHumanPlayerId = sessionHolder.getPlayerSession().getUserContext().getHumanPlayerId();

        UserContext userContext = sessionHolder.getPlayerSession().getUserContext();
        serverLevelQuestService.onClientLevelUpdate(sessionId, LEVEL_4_ID);
        TestClientGameConnection testClientGameConnection = clientGameConnectionServiceTestHelper.connectClient(sessionHolder.getPlayerSession());

        baseItemService.createHumanBaseWithBaseItem(userContext.getLevelId(), userContext.getUnlockedItemLimit(), userContext.getHumanPlayerId(), userContext.getName(), new DecimalPosition(1000, 1000));
        Thread.sleep(5000);

        testClientGameConnection.clearMessages();
        // Actual test
        RegisterInfo registerInfo = userService.handleInGameFacebookUserLogin(new FbAuthResponse().setUserID("0123456789"));

        // Verify UserContext from session
        int userEntityId = userService.getUserForFacebookId("0123456789").getId();
        Assert.assertEquals(oldHumanPlayerId.getPlayerId(), registerInfo.getHumanPlayerId().getPlayerId());
        Assert.assertEquals(userEntityId, (int) registerInfo.getHumanPlayerId().getUserId());
        Assert.assertFalse(registerInfo.isUserAlreadyExits());
        UserContext newUserContext = sessionHolder.getPlayerSession().getUserContext();
        Assert.assertEquals(32, newUserContext.getXp());
        Assert.assertEquals(LEVEL_4_ID, newUserContext.getLevelId());
        Assert.assertEquals(2, newUserContext.getUnlockedItemLimit().size());
        Assert.assertEquals(1, (int) newUserContext.getUnlockedItemLimit().get(BASE_ITEM_TYPE_BULLDOZER_ID));
        Assert.assertEquals(2, (int) newUserContext.getUnlockedItemLimit().get(BASE_ITEM_TYPE_ATTACKER_ID));
        HumanPlayerId newHumanPlayerId = newUserContext.getHumanPlayerId();
        Assert.assertEquals(oldHumanPlayerId.getPlayerId(), newHumanPlayerId.getPlayerId());
        Assert.assertEquals(userEntityId, (int) newHumanPlayerId.getUserId());
        // Verify usr in DB
        runInTransaction(entityManager -> {
            UserEntity userEntity = entityManager.find(UserEntity.class, userEntityId);
            Assert.assertEquals(99, userEntity.getCrystals());
            Assert.assertEquals(32, userEntity.getXp());
            Assert.assertEquals(oldHumanPlayerId.getPlayerId(), (int) userEntity.getHumanPlayerIdEntity().getId());
            Assert.assertEquals(LEVEL_4_ID, (int) userEntity.getLevel().getId());
            Assert.assertEquals(SERVER_QUEST_ID_L4_1, (int) userEntity.getActiveQuest().getId());
            List<Integer> completedQuestIds = userEntity.getCompletedQuestIds();
            Assert.assertEquals(3, completedQuestIds.size());
            Assert.assertTrue(completedQuestIds.contains(SERVER_QUEST_ID_L5_1));
            Assert.assertTrue(completedQuestIds.contains(SERVER_QUEST_ID_L5_2));
            Assert.assertTrue(completedQuestIds.contains(SERVER_QUEST_ID_L5_3));
            InventoryInfo inventoryInfo = userEntity.toInventoryInfo();
            Assert.assertEquals(1, inventoryInfo.getInventoryItemIds().size());
            Assert.assertTrue(inventoryInfo.getInventoryItemIds().contains(INVENTORY_ITEM_1_ID));
            List<Integer> unlockedLevelEntityIds = userEntity.getLevelUnlockEntities().stream().map(LevelUnlockEntity::getId).collect(Collectors.toList());
            Assert.assertEquals(2, unlockedLevelEntityIds.size());
            Assert.assertTrue(unlockedLevelEntityIds.contains(LEVEL_UNLOCK_ID_L4_1));
            Assert.assertTrue(unlockedLevelEntityIds.contains(LEVEL_UNLOCK_ID_L5_1));
        });
        // Game engine
        PlayerBase playerBase = baseItemService.getPlayerBase4HumanPlayerId(newUserContext.getHumanPlayerId());
        Assert.assertEquals(newUserContext.getHumanPlayerId().getPlayerId(), playerBase.getHumanPlayerId().getPlayerId());
        Assert.assertEquals(userEntityId, (int) playerBase.getHumanPlayerId().getUserId());
        // Assert connection
        testClientGameConnection.assertMessageSentCount(1);
        testClientGameConnection.assertMessageSent(0, "BASE_HUMAN_PLAYER_ID_CHANGED", PlayerBaseInfo.class, new PlayerBaseInfo().setBaseId(playerBase.getBaseId()).setHumanPlayerId(newHumanPlayerId));

        cleanUsers();
        cleanPlanetWithSlopes();
    }

    @Test
    public void handleInGameFacebookUserLoginExisting() throws Exception {
        setupPlanetWithSlopes();

        // Prepare
        SingleHolder<HumanPlayerId> holder = new SingleHolder<>();
        runInTransaction(entityManager -> {
                    HumanPlayerIdEntity humanPlayerIdEntity = userService.createHumanPlayerId();
                    UserEntity existingUser = new UserEntity();
                    existingUser.fromFacebookUserLoginInfo("0123456789", humanPlayerIdEntity, Locale.ENGLISH);
                    existingUser.setXp(123);
                    existingUser.setName("gegel");
                    existingUser.setCrystals(346);
                    existingUser.setLevel(entityManager.find(LevelEntity.class, LEVEL_4_ID));
                    existingUser.setActiveQuest(entityManager.find(QuestConfigEntity.class, SERVER_QUEST_ID_L4_1));
                    existingUser.addLevelUnlockEntity(entityManager.find(LevelUnlockEntity.class, LEVEL_UNLOCK_ID_L4_1));
                    existingUser.addLevelUnlockEntity(entityManager.find(LevelUnlockEntity.class, LEVEL_UNLOCK_ID_L5_1));
                    existingUser.addCompletedQuest(entityManager.find(QuestConfigEntity.class, SERVER_QUEST_ID_L5_1));
                    existingUser.addCompletedQuest(entityManager.find(QuestConfigEntity.class, SERVER_QUEST_ID_L5_2));
                    existingUser.addCompletedQuest(entityManager.find(QuestConfigEntity.class, SERVER_QUEST_ID_L5_3));
                    existingUser.addCompletedQuest(entityManager.find(QuestConfigEntity.class, SERVER_QUEST_ID_L5_3));
                    existingUser.addInventoryItem(entityManager.find(InventoryItemEntity.class, INVENTORY_ITEM_1_ID));
                    entityManager.persist(existingUser);
                    holder.setO(existingUser.createHumanPlayerId());
                    existingUser.createHumanPlayerId();
                }
        );
        HumanPlayerId expectedHumanPlayerId = holder.getO();
        userService.handleUnregisteredLogin();

        // Actual test
        RegisterInfo registerInfo = userService.handleInGameFacebookUserLogin(new FbAuthResponse().setUserID("0123456789"));

        // Verify return value
        Assert.assertTrue(registerInfo.isUserAlreadyExits());
        Assert.assertNull(registerInfo.getHumanPlayerId());
        // Verify session
        Assert.assertNull(sessionHolder.getPlayerSession().getUnregisteredUser());
        UserContext userContext = sessionHolder.getPlayerSession().getUserContext();
        Assert.assertEquals(expectedHumanPlayerId.getPlayerId(), userContext.getHumanPlayerId().getPlayerId());
        Assert.assertEquals(expectedHumanPlayerId.getUserId(), userContext.getHumanPlayerId().getUserId());
        Assert.assertEquals("gegel", userContext.getName());
        Assert.assertEquals(LEVEL_4_ID, userContext.getLevelId());
        Assert.assertEquals(123, userContext.getXp());
        Assert.assertEquals(2, userContext.getUnlockedItemLimit().size());
        Assert.assertEquals(1, (int) userContext.getUnlockedItemLimit().get(BASE_ITEM_TYPE_BULLDOZER_ID));
        Assert.assertEquals(2, (int) userContext.getUnlockedItemLimit().get(BASE_ITEM_TYPE_ATTACKER_ID));

        cleanUsers();
        cleanPlanetWithSlopes();
    }
}