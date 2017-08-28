package com.btxtech.server.persistence.server;

import com.btxtech.server.ArquillianBaseTest;
import com.btxtech.server.ClientSystemConnectionServiceTestHelper;
import com.btxtech.server.SimpleTestEnvironment;
import com.btxtech.server.TestClientSystemConnection;
import com.btxtech.server.TestHelper;
import com.btxtech.server.gameengine.ServerGameEngineControl;
import com.btxtech.server.persistence.GameUiControlConfigPersistence;
import com.btxtech.server.persistence.history.LevelHistoryEntity;
import com.btxtech.server.persistence.history.QuestHistoryEntity;
import com.btxtech.server.persistence.quest.QuestConfigEntity;
import com.btxtech.server.user.UserEntity;
import com.btxtech.server.user.UserService;
import com.btxtech.server.web.SessionHolder;
import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.planet.quest.QuestService;
import com.btxtech.shared.utils.CollectionUtils;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Beat
 * on 10.08.2017.
 */
public class ServerLevelQuestServiceTest extends ArquillianBaseTest {
    @Inject
    private UserService userService;
    @Inject
    private SessionHolder sessionHolder;
    @Inject
    private ServerLevelQuestService serverLevelQuestService;
    @Inject
    private GameUiControlConfigPersistence gameUiControlConfigPersistence;
    @Inject
    private ServerGameEnginePersistence serverGameEnginePersistence;
    @Inject
    private ClientSystemConnectionServiceTestHelper systemConnectionService;
    @Resource(name = "DefaultManagedScheduledExecutorService")
    private ManagedScheduledExecutorService scheduleExecutor;

    @Before
    public void before() throws Exception {
        setupPlanets();
    }

    @After
    public void after() throws Exception {
        cleanTableNative("USER_COMPLETED_QUEST");
        cleanTable(UserEntity.class);
        cleanPlanets();
        cleanTable(LevelHistoryEntity.class);
        cleanTable(QuestHistoryEntity.class);
    }

    @Test
    public void onLevelUpUnregistered() throws Exception {
        String sessionId = sessionHolder.getPlayerSession().getHttpSessionId();
        UserContext userContext = userService.getUserContextFromSession(); // Simulate anonymous login

        serverLevelQuestService.onClientLevelUpdate(sessionId, LEVEL_2_ID);
        Assert.assertEquals(LEVEL_1_ID, userService.getUserContextFromSession().getLevelId());
        Assert.assertNull(gameUiControlConfigPersistence.loadWarm(Locale.US, userContext).getSlavePlanetConfig());

        serverLevelQuestService.onClientLevelUpdate(sessionId, LEVEL_3_ID);
        Assert.assertEquals(LEVEL_1_ID, userService.getUserContextFromSession().getLevelId());
        Assert.assertNull(gameUiControlConfigPersistence.loadWarm(Locale.US, userContext).getSlavePlanetConfig());

        serverLevelQuestService.onClientLevelUpdate(sessionId, LEVEL_4_ID);
        Assert.assertEquals(LEVEL_4_ID, userService.getUserContextFromSession().getLevelId());
        Assert.assertEquals(SERVER_QUEST_ID_L4_1, gameUiControlConfigPersistence.loadWarm(Locale.US, userContext).getSlaveQuestInfo().getActiveQuest().getId());

        assertCount(1, QuestHistoryEntity.class);
        assertCount(3, LevelHistoryEntity.class);
        assertCount(0, UserEntity.class);
    }

    @Test
    public void onLevelUpRegister() throws Exception {
        String sessionId = sessionHolder.getPlayerSession().getHttpSessionId();
        UserContext userContext = userService.handleFacebookUserLogin("0000001");

        assertUser("0000001", LEVEL_1_ID, null);
        Assert.assertTrue(userService.findActiveQuests4Users(serverGameEnginePersistence.readAllQuestIds()).entrySet().isEmpty());

        serverLevelQuestService.onClientLevelUpdate(sessionId, LEVEL_2_ID);
        Assert.assertEquals(LEVEL_1_ID, userService.getUserContextFromSession().getLevelId());
        assertUser("0000001", LEVEL_1_ID, null);
        Assert.assertTrue(userService.findActiveQuests4Users(serverGameEnginePersistence.readAllQuestIds()).entrySet().isEmpty());
        Assert.assertNull(gameUiControlConfigPersistence.loadWarm(Locale.US, userContext).getSlavePlanetConfig());

        serverLevelQuestService.onClientLevelUpdate(sessionId, LEVEL_3_ID);
        Assert.assertEquals(LEVEL_1_ID, userService.getUserContextFromSession().getLevelId());
        assertUser("0000001", LEVEL_1_ID, null);
        Assert.assertTrue(userService.findActiveQuests4Users(serverGameEnginePersistence.readAllQuestIds()).entrySet().isEmpty());
        Assert.assertNull(gameUiControlConfigPersistence.loadWarm(Locale.US, userContext).getSlavePlanetConfig());

        serverLevelQuestService.onClientLevelUpdate(sessionId, LEVEL_4_ID);
        Assert.assertEquals(LEVEL_4_ID, userService.getUserContextFromSession().getLevelId());
        assertUser("0000001", LEVEL_4_ID, SERVER_QUEST_ID_L4_1);
        Assert.assertEquals(SERVER_QUEST_ID_L4_1, gameUiControlConfigPersistence.loadWarm(Locale.US, userContext).getSlaveQuestInfo().getActiveQuest().getId());

        Map<HumanPlayerId, QuestConfig> map = userService.findActiveQuests4Users(serverGameEnginePersistence.readAllQuestIds());
        Assert.assertEquals(1, map.size());
        Map.Entry<HumanPlayerId, QuestConfig> entry = CollectionUtils.getFirst(map.entrySet());
        UserEntity userEntity = userService.getUserForFacebookId("0000001");
        Assert.assertEquals(userEntity.getId(), entry.getKey().getUserId());
        Assert.assertEquals(SERVER_QUEST_ID_L4_1, entry.getValue().getId());

        assertCount(1, QuestHistoryEntity.class);
        assertCount(3, LevelHistoryEntity.class);
        assertCount(1, UserEntity.class);
    }

    @Test
    public void onQuestPassedUnregistered() throws Exception {
        // Setup mocks
        ServerGameEngineControl serverGameEngineControlMock = EasyMock.createStrictMock(ServerGameEngineControl.class);
        serverGameEngineControlMock.onLevelChanged(EasyMock.anyObject(), EasyMock.eq(LEVEL_5_ID));
        QuestService questServiceMock = EasyMock.createStrictMock(QuestService.class);
        questServiceMock.activateCondition(EasyMock.anyObject(), EasyMock.anyObject());
        EasyMock.expect(questServiceMock.getQuestProgressInfo(EasyMock.anyObject())).andReturn(null);
        questServiceMock.activateCondition(EasyMock.anyObject(), EasyMock.anyObject());
        EasyMock.expect(questServiceMock.getQuestProgressInfo(EasyMock.anyObject())).andReturn(null);
        questServiceMock.activateCondition(EasyMock.anyObject(), EasyMock.anyObject());
        EasyMock.expect(questServiceMock.getQuestProgressInfo(EasyMock.anyObject())).andReturn(null);
        questServiceMock.activateCondition(EasyMock.anyObject(), EasyMock.anyObject());
        EasyMock.expect(questServiceMock.getQuestProgressInfo(EasyMock.anyObject())).andReturn(null);
        questServiceMock.activateCondition(EasyMock.anyObject(), EasyMock.anyObject());
        EasyMock.expect(questServiceMock.getQuestProgressInfo(EasyMock.anyObject())).andReturn(null);
        EasyMock.expect(questServiceMock.getQuestProgressInfo(EasyMock.anyObject())).andReturn(null);
        EasyMock.replay(serverGameEngineControlMock, questServiceMock);
        SimpleTestEnvironment.injectInstance("serverGameEngineControlInstance", serverLevelQuestService, () -> serverGameEngineControlMock);
        SimpleTestEnvironment.injectService("questService", serverLevelQuestService, questServiceMock);

        UserContext userContext = userService.getUserContextFromSession(); // Simulate anonymous login
        String sessionId = sessionHolder.getPlayerSession().getHttpSessionId();
        TestClientSystemConnection systemConnection = systemConnectionService.connectClient(sessionHolder.getPlayerSession());
        HumanPlayerId humanPlayerId = userContext.getHumanPlayerId();

        serverLevelQuestService.onClientLevelUpdate(sessionId, LEVEL_4_ID);
        userContext = userService.getUserContextFromSession();
        Assert.assertEquals(0, userContext.getXp());
        Assert.assertEquals(LEVEL_4_ID, userContext.getLevelId());
        QuestConfig actualQuestConfigL41 = gameUiControlConfigPersistence.loadWarm(Locale.US, userContext).getSlaveQuestInfo().getActiveQuest();
        Assert.assertEquals(SERVER_QUEST_ID_L4_1, actualQuestConfigL41.getId());
        TestHelper.assertCollection(sessionHolder.getPlayerSession().getUnregisteredUser().getCompletedQuestIds());
        Assert.assertEquals(SERVER_QUEST_ID_L4_1, sessionHolder.getPlayerSession().getUnregisteredUser().getActiveQuest().getId());
        // L4 first quest passed
        executeInGameEngineThread(() -> serverLevelQuestService.onQuestPassed(humanPlayerId, actualQuestConfigL41));
        userContext = userService.getUserContextFromSession();
        Assert.assertEquals(100, userContext.getXp());
        Assert.assertEquals(LEVEL_4_ID, userContext.getLevelId());
        QuestConfig actualQuestConfigL42 = gameUiControlConfigPersistence.loadWarm(Locale.US, userContext).getSlaveQuestInfo().getActiveQuest();
        Assert.assertEquals(SERVER_QUEST_ID_L4_2, actualQuestConfigL42.getId());
        TestHelper.assertCollection(sessionHolder.getPlayerSession().getUnregisteredUser().getCompletedQuestIds(), SERVER_QUEST_ID_L4_1);
        Assert.assertEquals(SERVER_QUEST_ID_L4_2, sessionHolder.getPlayerSession().getUnregisteredUser().getActiveQuest().getId());
        // L4 Second quest passed
        executeInGameEngineThread(() -> serverLevelQuestService.onQuestPassed(humanPlayerId, actualQuestConfigL42));
        userContext = userService.getUserContextFromSession();
        Assert.assertEquals(0, userContext.getXp());
        Assert.assertEquals(LEVEL_5_ID, userContext.getLevelId());
        QuestConfig actualQuestConfigL51 = gameUiControlConfigPersistence.loadWarm(Locale.US, userContext).getSlaveQuestInfo().getActiveQuest();
        Assert.assertEquals(SERVER_QUEST_ID_L5_1, actualQuestConfigL51.getId());
        TestHelper.assertCollection(sessionHolder.getPlayerSession().getUnregisteredUser().getCompletedQuestIds(), SERVER_QUEST_ID_L4_1, SERVER_QUEST_ID_L4_2);
        Assert.assertEquals(SERVER_QUEST_ID_L5_1, sessionHolder.getPlayerSession().getUnregisteredUser().getActiveQuest().getId());
        // L5 First quest passed
        executeInGameEngineThread(() -> serverLevelQuestService.onQuestPassed(humanPlayerId, actualQuestConfigL51));
        userContext = userService.getUserContextFromSession();
        Assert.assertEquals(100, userContext.getXp());
        Assert.assertEquals(LEVEL_5_ID, userContext.getLevelId());
        QuestConfig actualQuestConfigL52 = gameUiControlConfigPersistence.loadWarm(Locale.US, userContext).getSlaveQuestInfo().getActiveQuest();
        Assert.assertEquals(SERVER_QUEST_ID_L5_2, actualQuestConfigL52.getId());
        TestHelper.assertCollection(sessionHolder.getPlayerSession().getUnregisteredUser().getCompletedQuestIds(), SERVER_QUEST_ID_L4_1, SERVER_QUEST_ID_L4_2, SERVER_QUEST_ID_L5_1);
        Assert.assertEquals(SERVER_QUEST_ID_L5_2, sessionHolder.getPlayerSession().getUnregisteredUser().getActiveQuest().getId());
        // L5 Second quest passed
        executeInGameEngineThread(() -> serverLevelQuestService.onQuestPassed(humanPlayerId, actualQuestConfigL52));
        userContext = userService.getUserContextFromSession();
        Assert.assertEquals(300, userContext.getXp());
        Assert.assertEquals(LEVEL_5_ID, userContext.getLevelId());
        QuestConfig actualQuestConfigL53 = gameUiControlConfigPersistence.loadWarm(Locale.US, userContext).getSlaveQuestInfo().getActiveQuest();
        Assert.assertEquals(SERVER_QUEST_ID_L5_3, actualQuestConfigL53.getId());
        TestHelper.assertCollection(sessionHolder.getPlayerSession().getUnregisteredUser().getCompletedQuestIds(), SERVER_QUEST_ID_L4_1, SERVER_QUEST_ID_L4_2, SERVER_QUEST_ID_L5_1, SERVER_QUEST_ID_L5_2);
        Assert.assertEquals(SERVER_QUEST_ID_L5_3, sessionHolder.getPlayerSession().getUnregisteredUser().getActiveQuest().getId());
        // L5 Third quest passed
        executeInGameEngineThread(() -> serverLevelQuestService.onQuestPassed(humanPlayerId, actualQuestConfigL53));
        userContext = userService.getUserContextFromSession();
        Assert.assertEquals(350, userContext.getXp());
        Assert.assertEquals(LEVEL_5_ID, userContext.getLevelId());
        Assert.assertNull(gameUiControlConfigPersistence.loadWarm(Locale.US, userContext).getSlaveQuestInfo().getActiveQuest());
        TestHelper.assertCollection(sessionHolder.getPlayerSession().getUnregisteredUser().getCompletedQuestIds(), SERVER_QUEST_ID_L4_1, SERVER_QUEST_ID_L4_2, SERVER_QUEST_ID_L5_1, SERVER_QUEST_ID_L5_2, SERVER_QUEST_ID_L5_3);
        Assert.assertNull(sessionHolder.getPlayerSession().getUnregisteredUser().getActiveQuest());

        systemConnection.printMessagesSent();

        systemConnection.assertMessageSentCount(15);
        systemConnection.assertMessageSent(0, "QUEST_ACTIVATED#{\"id\":1,\"internalName\":\"Test Server Quest L4 1\",\"title\":null,\"description\":null,\"xp\":100,\"money\":0,\"cristal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"SYNC_ITEM_CREATED\",\"comparisonConfig\":{\"count\":1,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        systemConnection.assertMessageSent(1, "QUEST_PASSED#{\"id\":1,\"internalName\":\"Test Server Quest L4 1\",\"title\":null,\"description\":null,\"xp\":100,\"money\":0,\"cristal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"SYNC_ITEM_CREATED\",\"comparisonConfig\":{\"count\":1,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        systemConnection.assertMessageSent(2, "XP_CHANGED#100");
        systemConnection.assertMessageSent(3, "QUEST_ACTIVATED#{\"id\":2,\"internalName\":\"Test Server Quest L4 2\",\"title\":null,\"description\":null,\"xp\":200,\"money\":0,\"cristal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"SYNC_ITEM_KILLED\",\"comparisonConfig\":{\"count\":2,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        systemConnection.assertMessageSent(4, "QUEST_PASSED#{\"id\":2,\"internalName\":\"Test Server Quest L4 2\",\"title\":null,\"description\":null,\"xp\":200,\"money\":0,\"cristal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"SYNC_ITEM_KILLED\",\"comparisonConfig\":{\"count\":2,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        systemConnection.assertMessageSent(5, "LEVEL_UPDATE_SERVER#{\"humanPlayerId\":{\"playerId\":1,\"userId\":null},\"name\":\"Unregistered User\",\"admin\":false,\"levelId\":5,\"xp\":0,\"crystals\":0,\"inventoryItemIds\":[],\"inventoryArtifactIds\":[],\"unlockedItemTypes\":[],\"unlockedQuests\":[],\"unlockedPlanets\":[]}");
        systemConnection.assertMessageSent(6, "QUEST_ACTIVATED#{\"id\":3,\"internalName\":\"Test Server Quest L5 1\",\"title\":null,\"description\":null,\"xp\":100,\"money\":0,\"cristal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"BOX_PICKED\",\"comparisonConfig\":{\"count\":1,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        systemConnection.assertMessageSent(7, "QUEST_PASSED#{\"id\":3,\"internalName\":\"Test Server Quest L5 1\",\"title\":null,\"description\":null,\"xp\":100,\"money\":0,\"cristal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"BOX_PICKED\",\"comparisonConfig\":{\"count\":1,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        systemConnection.assertMessageSent(8, "XP_CHANGED#100");
        systemConnection.assertMessageSent(9, "QUEST_ACTIVATED#{\"id\":4,\"internalName\":\"Test Server Quest L5 2\",\"title\":null,\"description\":null,\"xp\":200,\"money\":0,\"cristal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"BASE_KILLED\",\"comparisonConfig\":{\"count\":2,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        systemConnection.assertMessageSent(10, "QUEST_PASSED#{\"id\":4,\"internalName\":\"Test Server Quest L5 2\",\"title\":null,\"description\":null,\"xp\":200,\"money\":0,\"cristal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"BASE_KILLED\",\"comparisonConfig\":{\"count\":2,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        systemConnection.assertMessageSent(11, "XP_CHANGED#300");
        systemConnection.assertMessageSent(12, "QUEST_ACTIVATED#{\"id\":5,\"internalName\":\"Test Server Quest L5 3\",\"title\":null,\"description\":null,\"xp\":50,\"money\":0,\"cristal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"HARVEST\",\"comparisonConfig\":{\"count\":100,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        systemConnection.assertMessageSent(13, "QUEST_PASSED#{\"id\":5,\"internalName\":\"Test Server Quest L5 3\",\"title\":null,\"description\":null,\"xp\":50,\"money\":0,\"cristal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"HARVEST\",\"comparisonConfig\":{\"count\":100,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        systemConnection.assertMessageSent(14, "XP_CHANGED#350");

        assertCount(10, QuestHistoryEntity.class);
        assertCount(2, LevelHistoryEntity.class);
        assertCount(0, UserEntity.class);

        EasyMock.verify(serverGameEngineControlMock, questServiceMock);
    }

    @Test
    public void onQuestPassedRegistered() throws Exception {
        // Simulate quest by previous users
        runInTransaction(entityManager -> {
            UserEntity userEntity = new UserEntity();
            userEntity.setActiveQuest(entityManager.find(QuestConfigEntity.class, SERVER_QUEST_ID_L5_3));
            userEntity.addCompletedQuest(entityManager.find(QuestConfigEntity.class, SERVER_QUEST_ID_L4_1));
            userEntity.addCompletedQuest(entityManager.find(QuestConfigEntity.class, SERVER_QUEST_ID_L4_2));
            userEntity.addCompletedQuest(entityManager.find(QuestConfigEntity.class, SERVER_QUEST_ID_L5_1));
            userEntity.addCompletedQuest(entityManager.find(QuestConfigEntity.class, SERVER_QUEST_ID_L5_2));
            userEntity.addCompletedQuest(entityManager.find(QuestConfigEntity.class, SERVER_QUEST_ID_L5_3));
            entityManager.persist(userEntity);
        });
        // Setup session
        String sessionId = sessionHolder.getPlayerSession().getHttpSessionId();
        UserContext userContext = userService.handleFacebookUserLogin("0000001");
        // Setup mocks
        ServerGameEngineControl serverGameEngineControlMock = EasyMock.createStrictMock(ServerGameEngineControl.class);
        serverGameEngineControlMock.onLevelChanged(EasyMock.anyObject(), EasyMock.eq(LEVEL_5_ID));
        QuestService questServiceMock = EasyMock.createStrictMock(QuestService.class);
        questServiceMock.activateCondition(EasyMock.anyObject(), EasyMock.anyObject());
        EasyMock.expect(questServiceMock.getQuestProgressInfo(EasyMock.anyObject())).andReturn(null);
        questServiceMock.activateCondition(EasyMock.anyObject(), EasyMock.anyObject());
        EasyMock.expect(questServiceMock.getQuestProgressInfo(EasyMock.anyObject())).andReturn(null);
        questServiceMock.activateCondition(EasyMock.anyObject(), EasyMock.anyObject());
        EasyMock.expect(questServiceMock.getQuestProgressInfo(EasyMock.anyObject())).andReturn(null);
        questServiceMock.activateCondition(EasyMock.anyObject(), EasyMock.anyObject());
        EasyMock.expect(questServiceMock.getQuestProgressInfo(EasyMock.anyObject())).andReturn(null);
        questServiceMock.activateCondition(EasyMock.anyObject(), EasyMock.anyObject());
        EasyMock.expect(questServiceMock.getQuestProgressInfo(EasyMock.anyObject())).andReturn(null);
        EasyMock.expect(questServiceMock.getQuestProgressInfo(EasyMock.anyObject())).andReturn(null);
        EasyMock.replay(serverGameEngineControlMock, questServiceMock);
        SimpleTestEnvironment.injectInstance("serverGameEngineControlInstance", serverLevelQuestService, () -> serverGameEngineControlMock);
        SimpleTestEnvironment.injectService("questService", serverLevelQuestService, questServiceMock);

        TestClientSystemConnection systemConnection = systemConnectionService.connectClient(sessionHolder.getPlayerSession());
        userService.getUserContextFromSession(); // Simulate anonymous login
        HumanPlayerId humanPlayerId = userContext.getHumanPlayerId();

        serverLevelQuestService.onClientLevelUpdate(sessionId, LEVEL_4_ID);
        userContext = userService.getUserContextFromSession();
        Assert.assertEquals(0, userContext.getXp());
        Assert.assertEquals(LEVEL_4_ID, userContext.getLevelId());
        QuestConfig actualQuestConfigL41 = gameUiControlConfigPersistence.loadWarm(Locale.US, userContext).getSlaveQuestInfo().getActiveQuest();
        assertDbUserLevelXp(humanPlayerId.getUserId(), LEVEL_4_ID, SERVER_QUEST_ID_L4_1, 0);
        Assert.assertEquals(SERVER_QUEST_ID_L4_1, actualQuestConfigL41.getId());
        // L4 first quest passed
        executeInGameEngineThread(() -> serverLevelQuestService.onQuestPassed(humanPlayerId, actualQuestConfigL41));
        userContext = userService.getUserContextFromSession();
        Assert.assertEquals(100, userContext.getXp());
        Assert.assertEquals(LEVEL_4_ID, userContext.getLevelId());
        QuestConfig actualQuestConfigL42 = gameUiControlConfigPersistence.loadWarm(Locale.US, userContext).getSlaveQuestInfo().getActiveQuest();
        assertDbUserLevelXp(humanPlayerId.getUserId(), LEVEL_4_ID, SERVER_QUEST_ID_L4_2, 100, SERVER_QUEST_ID_L4_1);
        Assert.assertEquals(SERVER_QUEST_ID_L4_2, actualQuestConfigL42.getId());
        // L4 Second quest passed
        executeInGameEngineThread(() -> serverLevelQuestService.onQuestPassed(humanPlayerId, actualQuestConfigL42));
        userContext = userService.getUserContextFromSession();
        Assert.assertEquals(0, userContext.getXp());
        Assert.assertEquals(LEVEL_5_ID, userContext.getLevelId());
        QuestConfig actualQuestConfigL51 = gameUiControlConfigPersistence.loadWarm(Locale.US, userContext).getSlaveQuestInfo().getActiveQuest();
        assertDbUserLevelXp(humanPlayerId.getUserId(), LEVEL_5_ID, SERVER_QUEST_ID_L5_1, 0, SERVER_QUEST_ID_L4_1, SERVER_QUEST_ID_L4_2);
        Assert.assertEquals(SERVER_QUEST_ID_L5_1, actualQuestConfigL51.getId());
        // L5 First quest passed
        executeInGameEngineThread(() -> serverLevelQuestService.onQuestPassed(humanPlayerId, actualQuestConfigL51));
        userContext = userService.getUserContextFromSession();
        Assert.assertEquals(100, userContext.getXp());
        Assert.assertEquals(LEVEL_5_ID, userContext.getLevelId());
        QuestConfig actualQuestConfigL52 = gameUiControlConfigPersistence.loadWarm(Locale.US, userContext).getSlaveQuestInfo().getActiveQuest();
        assertDbUserLevelXp(humanPlayerId.getUserId(), LEVEL_5_ID, SERVER_QUEST_ID_L5_2, 100, SERVER_QUEST_ID_L4_1, SERVER_QUEST_ID_L4_2, SERVER_QUEST_ID_L5_1);
        Assert.assertEquals(SERVER_QUEST_ID_L5_2, actualQuestConfigL52.getId());
        // L5 Second quest passed
        executeInGameEngineThread(() -> serverLevelQuestService.onQuestPassed(humanPlayerId, actualQuestConfigL52));
        userContext = userService.getUserContextFromSession();
        Assert.assertEquals(300, userContext.getXp());
        Assert.assertEquals(LEVEL_5_ID, userContext.getLevelId());
        QuestConfig actualQuestConfigL53 = gameUiControlConfigPersistence.loadWarm(Locale.US, userContext).getSlaveQuestInfo().getActiveQuest();
        assertDbUserLevelXp(humanPlayerId.getUserId(), LEVEL_5_ID, SERVER_QUEST_ID_L5_3, 300, SERVER_QUEST_ID_L4_1, SERVER_QUEST_ID_L4_2, SERVER_QUEST_ID_L5_1, SERVER_QUEST_ID_L5_2);
        Assert.assertEquals(SERVER_QUEST_ID_L5_3, actualQuestConfigL53.getId());
        // L5 Third quest passed
        executeInGameEngineThread(() -> serverLevelQuestService.onQuestPassed(humanPlayerId, actualQuestConfigL53));
        userContext = userService.getUserContextFromSession();
        Assert.assertEquals(350, userContext.getXp());
        Assert.assertNull(gameUiControlConfigPersistence.loadWarm(Locale.US, userContext).getSlaveQuestInfo().getActiveQuest());
        assertDbUserLevelXp(humanPlayerId.getUserId(), LEVEL_5_ID, null, 350, SERVER_QUEST_ID_L4_1, SERVER_QUEST_ID_L4_2, SERVER_QUEST_ID_L5_1, SERVER_QUEST_ID_L5_2, SERVER_QUEST_ID_L5_3);
        Assert.assertEquals(LEVEL_5_ID, userContext.getLevelId());

        int playerId = userContext.getHumanPlayerId().getPlayerId();
        int userId = userContext.getHumanPlayerId().getUserId();
        systemConnection.assertMessageSentCount(15);
        systemConnection.assertMessageSent(0, "QUEST_ACTIVATED#{\"id\":1,\"internalName\":\"Test Server Quest L4 1\",\"title\":null,\"description\":null,\"xp\":100,\"money\":0,\"cristal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"SYNC_ITEM_CREATED\",\"comparisonConfig\":{\"count\":1,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        systemConnection.assertMessageSent(1, "QUEST_PASSED#{\"id\":1,\"internalName\":\"Test Server Quest L4 1\",\"title\":null,\"description\":null,\"xp\":100,\"money\":0,\"cristal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"SYNC_ITEM_CREATED\",\"comparisonConfig\":{\"count\":1,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        systemConnection.assertMessageSent(2, "XP_CHANGED#100");
        systemConnection.assertMessageSent(3, "QUEST_ACTIVATED#{\"id\":2,\"internalName\":\"Test Server Quest L4 2\",\"title\":null,\"description\":null,\"xp\":200,\"money\":0,\"cristal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"SYNC_ITEM_KILLED\",\"comparisonConfig\":{\"count\":2,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        systemConnection.assertMessageSent(4, "QUEST_PASSED#{\"id\":2,\"internalName\":\"Test Server Quest L4 2\",\"title\":null,\"description\":null,\"xp\":200,\"money\":0,\"cristal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"SYNC_ITEM_KILLED\",\"comparisonConfig\":{\"count\":2,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        systemConnection.assertMessageSent(5, "LEVEL_UPDATE_SERVER#{\"humanPlayerId\":{\"playerId\":" + playerId + ",\"userId\":" + userId + "},\"name\":\"Registered User\",\"admin\":false,\"levelId\":5,\"xp\":0,\"crystals\":0,\"inventoryItemIds\":[],\"inventoryArtifactIds\":[],\"unlockedItemTypes\":[],\"unlockedQuests\":[],\"unlockedPlanets\":[]}");
        systemConnection.assertMessageSent(6, "QUEST_ACTIVATED#{\"id\":3,\"internalName\":\"Test Server Quest L5 1\",\"title\":null,\"description\":null,\"xp\":100,\"money\":0,\"cristal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"BOX_PICKED\",\"comparisonConfig\":{\"count\":1,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        systemConnection.assertMessageSent(7, "QUEST_PASSED#{\"id\":3,\"internalName\":\"Test Server Quest L5 1\",\"title\":null,\"description\":null,\"xp\":100,\"money\":0,\"cristal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"BOX_PICKED\",\"comparisonConfig\":{\"count\":1,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        systemConnection.assertMessageSent(8, "XP_CHANGED#100");
        systemConnection.assertMessageSent(9, "QUEST_ACTIVATED#{\"id\":4,\"internalName\":\"Test Server Quest L5 2\",\"title\":null,\"description\":null,\"xp\":200,\"money\":0,\"cristal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"BASE_KILLED\",\"comparisonConfig\":{\"count\":2,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        systemConnection.assertMessageSent(10, "QUEST_PASSED#{\"id\":4,\"internalName\":\"Test Server Quest L5 2\",\"title\":null,\"description\":null,\"xp\":200,\"money\":0,\"cristal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"BASE_KILLED\",\"comparisonConfig\":{\"count\":2,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        systemConnection.assertMessageSent(11, "XP_CHANGED#300");
        systemConnection.assertMessageSent(12, "QUEST_ACTIVATED#{\"id\":5,\"internalName\":\"Test Server Quest L5 3\",\"title\":null,\"description\":null,\"xp\":50,\"money\":0,\"cristal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"HARVEST\",\"comparisonConfig\":{\"count\":100,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        systemConnection.assertMessageSent(13, "QUEST_PASSED#{\"id\":5,\"internalName\":\"Test Server Quest L5 3\",\"title\":null,\"description\":null,\"xp\":50,\"money\":0,\"cristal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"HARVEST\",\"comparisonConfig\":{\"count\":100,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        systemConnection.assertMessageSent(14, "XP_CHANGED#350");

        assertCount(10, QuestHistoryEntity.class);
        assertCount(2, LevelHistoryEntity.class);
        assertCount(2, UserEntity.class);

        EasyMock.verify(serverGameEngineControlMock, questServiceMock);
    }

    private void assertDbUserLevelXp(int userId, int expectedLevel4Id, Integer expectedActiveQuestId, int expectedXp, Integer... expectedCompletedQuestIds) throws Exception {
        runInTransaction(entityManager -> {
            UserEntity userEntity = entityManager.find(UserEntity.class, userId);
            Assert.assertEquals(expectedLevel4Id, (int) userEntity.getLevel().getId());
            if (userEntity.getActiveQuest() == null) {
                if (expectedActiveQuestId != null) {
                    Assert.fail();
                }
            } else {
                Assert.assertEquals(expectedActiveQuestId, userEntity.getActiveQuest().getId());
            }
            Assert.assertEquals(expectedXp, userEntity.getXp());
            Collection<Integer> expectedCompletedQuestsIdsClone = new ArrayList<>(Arrays.asList(expectedCompletedQuestIds));
            Assert.assertEquals(expectedCompletedQuestIds.length, expectedCompletedQuestsIdsClone.size());
            expectedCompletedQuestsIdsClone.removeAll(userEntity.getCompletedQuestIds());
            Assert.assertTrue(expectedCompletedQuestsIdsClone.isEmpty());
        });
    }

    private void assertUser(String facebookUserId, int levelId, Integer activeQuestId) throws Exception {
        UserEntity userEntity = userService.getUserForFacebookId(facebookUserId);

        runInTransaction(em -> {
            UserEntity actualUserEntity = em.find(UserEntity.class, userEntity.getId());
            Assert.assertEquals(levelId, (int) actualUserEntity.getLevel().getId());
            if (activeQuestId == null) {
                Assert.assertNull(actualUserEntity.getActiveQuest());
            } else {
                Assert.assertNotNull(actualUserEntity.getActiveQuest());
                Assert.assertEquals(activeQuestId, actualUserEntity.getActiveQuest().getId());
            }
        });
    }

    private void executeInGameEngineThread(Runnable runnable) {
        CountDownLatch startSignal = new CountDownLatch(1);
        scheduleExecutor.execute(() -> {
            try {
                runnable.run();
            } catch (Throwable t) {
                t.printStackTrace();
            }
            startSignal.countDown();
        });
        try {
            startSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

}