package com.btxtech.server.gameengine;

import com.btxtech.server.ClientSystemConnectionServiceTestHelper;
import com.btxtech.server.IgnoreOldArquillianTest;
import com.btxtech.server.SimpleTestEnvironment;
import com.btxtech.server.TestClientSystemConnection;
import com.btxtech.server.TestHelper;
import com.btxtech.server.persistence.GameUiContextCrudPersistence;
import com.btxtech.server.persistence.history.LevelHistoryEntity;
import com.btxtech.server.persistence.history.QuestHistoryEntity;
import com.btxtech.server.persistence.quest.QuestConfigEntity;
import com.btxtech.server.persistence.server.ServerGameEngineCrudPersistence;
import com.btxtech.server.user.UserEntity;
import com.btxtech.server.user.UserService;
import com.btxtech.server.web.SessionHolder;
import com.btxtech.shared.datatypes.LevelUpPacket;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.config.LevelUnlockConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.planet.quest.QuestService;
import com.btxtech.shared.utils.CollectionUtils;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Beat
 * on 10.08.2017.
 */
@Ignore
public class RestServerLevelQuestServiceTestBase extends IgnoreOldArquillianTest {

    private UserService userService;

    private SessionHolder sessionHolder;

    private ServerLevelQuestService serverLevelQuestService;

    private GameUiContextCrudPersistence gameUiContextCrudPersistence;

    private ServerGameEngineCrudPersistence serverGameEngineCrudPersistence;

    private ClientSystemConnectionServiceTestHelper systemConnectionService;

    private ManagedScheduledExecutorService scheduleExecutor;

    @Inject
    public RestServerLevelQuestServiceTestBase(ClientSystemConnectionServiceTestHelper systemConnectionService, ServerGameEngineCrudPersistence serverGameEngineCrudPersistence, GameUiContextCrudPersistence gameUiContextCrudPersistence, ServerLevelQuestService serverLevelQuestService, SessionHolder sessionHolder, UserService userService) {
        this.systemConnectionService = systemConnectionService;
        this.serverGameEngineCrudPersistence = serverGameEngineCrudPersistence;
        this.gameUiContextCrudPersistence = gameUiContextCrudPersistence;
        this.serverLevelQuestService = serverLevelQuestService;
        this.sessionHolder = sessionHolder;
        this.userService = userService;
    }

    @Before
    public void before() throws Exception {
        setupPlanetDb();
    }

    @After
    public void after() throws Exception {
        cleanTableNative("USER_COMPLETED_QUEST");
        cleanTable(UserEntity.class);
        cleanPlanets();
        cleanTable(LevelHistoryEntity.class);
        cleanTable(QuestHistoryEntity.class);
    }

    // Fails due to complex mocking. Use setupPlanetWithSlopes() and real
    // @Test
    public void onLevelUpUnregistered() throws Exception {
        String sessionId = sessionHolder.getPlayerSession().getHttpSessionId();
        UserContext userContext = userService.getUserContextFromSession(); // Simulate anonymous login

        serverLevelQuestService.onClientLevelUpdate(sessionId, LEVEL_2_ID);
        Assert.assertEquals(LEVEL_1_ID, userService.getUserContextFromSession().getLevelId().intValue());
        Assert.assertNull(gameUiContextCrudPersistence.loadWarm(Locale.US, userContext).getSlavePlanetConfig());

        serverLevelQuestService.onClientLevelUpdate(sessionId, LEVEL_3_ID);
        Assert.assertEquals(LEVEL_1_ID, userService.getUserContextFromSession().getLevelId().intValue());
        Assert.assertNull(gameUiContextCrudPersistence.loadWarm(Locale.US, userContext).getSlavePlanetConfig());

        serverLevelQuestService.onClientLevelUpdate(sessionId, LEVEL_4_ID);
        Assert.assertEquals(LEVEL_4_ID, userService.getUserContextFromSession().getLevelId().intValue());
        Assert.assertEquals(SERVER_QUEST_ID_L4_1, gameUiContextCrudPersistence.loadWarm(Locale.US, userContext).getSlaveQuestInfo().getActiveQuest().getId().intValue());

        assertCount(1, QuestHistoryEntity.class);
        assertCount(3, LevelHistoryEntity.class);
        assertCount(0, UserEntity.class);
    }

    // @Test
    // Fails due to complex mocking. Use setupPlanetWithSlopes() and real
    public void onLevelUpRegister() throws Exception {
        String sessionId = sessionHolder.getPlayerSession().getHttpSessionId();
        UserContext userContext = handleFacebookUserLogin("0000001");

        assertUser("0000001", LEVEL_1_ID, null);
        Assert.assertTrue(userService.findActiveQuests4Users(serverGameEngineCrudPersistence.readAllQuestIds()).entrySet().isEmpty());

        serverLevelQuestService.onClientLevelUpdate(sessionId, LEVEL_2_ID);
        Assert.assertEquals(LEVEL_1_ID, userService.getUserContextFromSession().getLevelId().intValue());
        assertUser("0000001", LEVEL_1_ID, null);
        Assert.assertTrue(userService.findActiveQuests4Users(serverGameEngineCrudPersistence.readAllQuestIds()).entrySet().isEmpty());
        Assert.assertNull(gameUiContextCrudPersistence.loadWarm(Locale.US, userContext).getSlavePlanetConfig());

        serverLevelQuestService.onClientLevelUpdate(sessionId, LEVEL_3_ID);
        Assert.assertEquals(LEVEL_1_ID, userService.getUserContextFromSession().getLevelId().intValue());
        assertUser("0000001", LEVEL_1_ID, null);
        Assert.assertTrue(userService.findActiveQuests4Users(serverGameEngineCrudPersistence.readAllQuestIds()).entrySet().isEmpty());
        Assert.assertNull(gameUiContextCrudPersistence.loadWarm(Locale.US, userContext).getSlavePlanetConfig());

        serverLevelQuestService.onClientLevelUpdate(sessionId, LEVEL_4_ID);
        Assert.assertEquals(LEVEL_4_ID, userService.getUserContextFromSession().getLevelId().intValue());
        assertUser("0000001", LEVEL_4_ID, SERVER_QUEST_ID_L4_1);
        Assert.assertEquals(SERVER_QUEST_ID_L4_1, gameUiContextCrudPersistence.loadWarm(Locale.US, userContext).getSlaveQuestInfo().getActiveQuest().getId().intValue());

        Map<Integer, QuestConfig> map = userService.findActiveQuests4Users(serverGameEngineCrudPersistence.readAllQuestIds());
        Assert.assertEquals(1, map.size());
        Map.Entry<Integer, QuestConfig> entry = CollectionUtils.getFirst(map.entrySet());
        UserEntity userEntity = userService.getUserForFacebookId("0000001");
        Assert.assertEquals(userEntity.getId(), entry.getKey());
        Assert.assertEquals(SERVER_QUEST_ID_L4_1, entry.getValue().getId().intValue());

        assertCount(1, QuestHistoryEntity.class);
        assertCount(3, LevelHistoryEntity.class);
        assertCount(1, UserEntity.class);
    }

    // @Test
    // Fails due to complex mocking. Use setupPlanetWithSlopes() and real
    public void onQuestPassedUnregistered() throws Exception {
        // Setup mocks
        ServerGameEngineControl serverGameEngineControlMock = EasyMock.createStrictMock(ServerGameEngineControl.class);
        serverGameEngineControlMock.onLevelChanged(EasyMock.anyObject(), EasyMock.eq(LEVEL_5_ID));
        QuestService questServiceMock = EasyMock.createStrictMock(QuestService.class);
        EasyMock.expect(questServiceMock.hasActiveQuest(EasyMock.anyObject())).andReturn(false);
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
        SimpleTestEnvironment.Ejector ejectorGameEngine = SimpleTestEnvironment.injectInstance("serverGameEngineControlInstance", serverLevelQuestService, () -> serverGameEngineControlMock);
        SimpleTestEnvironment.Ejector ejectorQuestService = SimpleTestEnvironment.injectService("questService", serverLevelQuestService, questServiceMock);

        UserContext userContext = userService.getUserContextFromSession(); // Simulate anonymous loginn
        String sessionId = sessionHolder.getPlayerSession().getHttpSessionId();
        TestClientSystemConnection systemConnection = systemConnectionService.connectClient(sessionHolder.getPlayerSession());
        int userId = userContext.getUserId();

        serverLevelQuestService.onClientLevelUpdate(sessionId, LEVEL_4_ID);
        userContext = userService.getUserContextFromSession();
        Assert.assertEquals(0, userContext.getXp());
        Assert.assertEquals(LEVEL_4_ID, userContext.getLevelId().intValue());
        QuestConfig actualQuestConfigL41 = gameUiContextCrudPersistence.loadWarm(Locale.US, userContext).getSlaveQuestInfo().getActiveQuest();
        Assert.assertEquals(SERVER_QUEST_ID_L4_1, actualQuestConfigL41.getId().intValue());
        TestHelper.assertCollection(sessionHolder.getPlayerSession().getUnregisteredUser().getCompletedQuestIds());
        Assert.assertEquals(SERVER_QUEST_ID_L4_1, sessionHolder.getPlayerSession().getUnregisteredUser().getActiveQuest().getId().intValue());
        // L4 first quest passed
        executeInGameEngineThread(() -> serverLevelQuestService.onQuestPassed(userId, actualQuestConfigL41));
        userContext = userService.getUserContextFromSession();
        Assert.assertEquals(100, userContext.getXp());
        Assert.assertEquals(LEVEL_4_ID, userContext.getLevelId().intValue());
        QuestConfig actualQuestConfigL42 = gameUiContextCrudPersistence.loadWarm(Locale.US, userContext).getSlaveQuestInfo().getActiveQuest();
        Assert.assertEquals(SERVER_QUEST_ID_L4_2, actualQuestConfigL42.getId().intValue());
        TestHelper.assertCollection(sessionHolder.getPlayerSession().getUnregisteredUser().getCompletedQuestIds(), SERVER_QUEST_ID_L4_1);
        Assert.assertEquals(SERVER_QUEST_ID_L4_2, sessionHolder.getPlayerSession().getUnregisteredUser().getActiveQuest().getId().intValue());
        // L4 Second quest passed
        executeInGameEngineThread(() -> serverLevelQuestService.onQuestPassed(userId, actualQuestConfigL42));
        userContext = userService.getUserContextFromSession();
        Assert.assertEquals(0, userContext.getXp());
        Assert.assertEquals(LEVEL_5_ID, userContext.getLevelId().intValue());
        QuestConfig actualQuestConfigL51 = gameUiContextCrudPersistence.loadWarm(Locale.US, userContext).getSlaveQuestInfo().getActiveQuest();
        Assert.assertEquals(SERVER_QUEST_ID_L5_1, actualQuestConfigL51.getId().intValue());
        TestHelper.assertCollection(sessionHolder.getPlayerSession().getUnregisteredUser().getCompletedQuestIds(), SERVER_QUEST_ID_L4_1, SERVER_QUEST_ID_L4_2);
        Assert.assertEquals(SERVER_QUEST_ID_L5_1, sessionHolder.getPlayerSession().getUnregisteredUser().getActiveQuest().getId().intValue());
        // L5 First quest passed
        executeInGameEngineThread(() -> serverLevelQuestService.onQuestPassed(userId, actualQuestConfigL51));
        userContext = userService.getUserContextFromSession();
        Assert.assertEquals(100, userContext.getXp());
        Assert.assertEquals(LEVEL_5_ID, userContext.getLevelId().intValue());
        QuestConfig actualQuestConfigL52 = gameUiContextCrudPersistence.loadWarm(Locale.US, userContext).getSlaveQuestInfo().getActiveQuest();
        Assert.assertEquals(SERVER_QUEST_ID_L5_2, actualQuestConfigL52.getId().intValue());
        TestHelper.assertCollection(sessionHolder.getPlayerSession().getUnregisteredUser().getCompletedQuestIds(), SERVER_QUEST_ID_L4_1, SERVER_QUEST_ID_L4_2, SERVER_QUEST_ID_L5_1);
        Assert.assertEquals(SERVER_QUEST_ID_L5_2, sessionHolder.getPlayerSession().getUnregisteredUser().getActiveQuest().getId().intValue());
        // L5 Second quest passed
        executeInGameEngineThread(() -> serverLevelQuestService.onQuestPassed(userId, actualQuestConfigL52));
        userContext = userService.getUserContextFromSession();
        Assert.assertEquals(300, userContext.getXp());
        Assert.assertEquals(LEVEL_5_ID, userContext.getLevelId().intValue());
        QuestConfig actualQuestConfigL53 = gameUiContextCrudPersistence.loadWarm(Locale.US, userContext).getSlaveQuestInfo().getActiveQuest();
        Assert.assertEquals(SERVER_QUEST_ID_L5_3, actualQuestConfigL53.getId().intValue());
        TestHelper.assertCollection(sessionHolder.getPlayerSession().getUnregisteredUser().getCompletedQuestIds(), SERVER_QUEST_ID_L4_1, SERVER_QUEST_ID_L4_2, SERVER_QUEST_ID_L5_1, SERVER_QUEST_ID_L5_2);
        Assert.assertEquals(SERVER_QUEST_ID_L5_3, sessionHolder.getPlayerSession().getUnregisteredUser().getActiveQuest().getId().intValue());
        // L5 Third quest passed
        executeInGameEngineThread(() -> serverLevelQuestService.onQuestPassed(userId, actualQuestConfigL53));
        userContext = userService.getUserContextFromSession();
        Assert.assertEquals(350, userContext.getXp());
        Assert.assertEquals(LEVEL_5_ID, userContext.getLevelId().intValue());
        Assert.assertNull(gameUiContextCrudPersistence.loadWarm(Locale.US, userContext).getSlaveQuestInfo().getActiveQuest());
        TestHelper.assertCollection(sessionHolder.getPlayerSession().getUnregisteredUser().getCompletedQuestIds(), SERVER_QUEST_ID_L4_1, SERVER_QUEST_ID_L4_2, SERVER_QUEST_ID_L5_1, SERVER_QUEST_ID_L5_2, SERVER_QUEST_ID_L5_3);
        Assert.assertNull(sessionHolder.getPlayerSession().getUnregisteredUser().getActiveQuest());

        systemConnection.getWebsocketMessageHelper().printMessagesSent();

        systemConnection.getWebsocketMessageHelper().assertMessageSentCount(15);
        systemConnection.getWebsocketMessageHelper().assertMessageSent(0, "QUEST_ACTIVATED#{\"id\":" + SERVER_QUEST_ID_L4_1 + ",\"internalName\":\"Test Server Quest L4 1\",\"title\":null,\"description\":null,\"xp\":100,\"razarion\":0,\"crystal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"SYNC_ITEM_CREATED\",\"comparisonConfig\":{\"count\":1,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        systemConnection.getWebsocketMessageHelper().assertMessageSent(1, "QUEST_PASSED#{\"id\":" + SERVER_QUEST_ID_L4_1 + ",\"internalName\":\"Test Server Quest L4 1\",\"title\":null,\"description\":null,\"xp\":100,\"razarion\":0,\"crystal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"SYNC_ITEM_CREATED\",\"comparisonConfig\":{\"count\":1,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        systemConnection.getWebsocketMessageHelper().assertMessageSent(2, "XP_CHANGED#100");
        systemConnection.getWebsocketMessageHelper().assertMessageSent(3, "QUEST_ACTIVATED#{\"id\":" + SERVER_QUEST_ID_L4_2 + ",\"internalName\":\"Test Server Quest L4 2\",\"title\":null,\"description\":null,\"xp\":200,\"razarion\":0,\"crystal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"SYNC_ITEM_KILLED\",\"comparisonConfig\":{\"count\":2,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        systemConnection.getWebsocketMessageHelper().assertMessageSent(4, "QUEST_PASSED#{\"id\":" + SERVER_QUEST_ID_L4_2 + ",\"internalName\":\"Test Server Quest L4 2\",\"title\":null,\"description\":null,\"xp\":200,\"razarion\":0,\"crystal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"SYNC_ITEM_KILLED\",\"comparisonConfig\":{\"count\":2,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        UserContext expectedUserContext = new UserContext().userId(userId).levelId(LEVEL_5_ID).name("Unregistered User").unlockedItemLimit(Collections.emptyMap());
        List<LevelUnlockConfig> expectedLevelUnlockConfigs = new ArrayList<>();
        expectedLevelUnlockConfigs.add(new LevelUnlockConfig().id(LEVEL_UNLOCK_ID_L4_1).baseItemType(BASE_ITEM_TYPE_BULLDOZER_ID).baseItemTypeCount(1).internalName("levelUnlockEntity4_1"));
        expectedLevelUnlockConfigs.add(new LevelUnlockConfig().id(LEVEL_UNLOCK_ID_L5_1).baseItemType(BASE_ITEM_TYPE_ATTACKER_ID).baseItemTypeCount(2).crystalCost(10).internalName("levelUnlockEntity5_1"));
        expectedLevelUnlockConfigs.add(new LevelUnlockConfig().id(LEVEL_UNLOCK_ID_L5_2).baseItemType(BASE_ITEM_TYPE_HARVESTER_ID).baseItemTypeCount(1).crystalCost(20).internalName("levelUnlockEntity5_2"));
        systemConnection.getWebsocketMessageHelper().assertMessageSent(5, "LEVEL_UPDATE_SERVER", LevelUpPacket.class, new LevelUpPacket().userContext(expectedUserContext).availableUnlocks(!expectedLevelUnlockConfigs.isEmpty()));
        systemConnection.getWebsocketMessageHelper().assertMessageSent(6, "QUEST_ACTIVATED#{\"id\":" + SERVER_QUEST_ID_L5_1 + ",\"internalName\":\"Test Server Quest L5 1\",\"title\":null,\"description\":null,\"xp\":100,\"razarion\":0,\"crystal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"BOX_PICKED\",\"comparisonConfig\":{\"count\":1,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        systemConnection.getWebsocketMessageHelper().assertMessageSent(7, "QUEST_PASSED#{\"id\":" + SERVER_QUEST_ID_L5_1 + ",\"internalName\":\"Test Server Quest L5 1\",\"title\":null,\"description\":null,\"xp\":100,\"razarion\":0,\"crystal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"BOX_PICKED\",\"comparisonConfig\":{\"count\":1,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        systemConnection.getWebsocketMessageHelper().assertMessageSent(8, "XP_CHANGED#100");
        systemConnection.getWebsocketMessageHelper().assertMessageSent(9, "QUEST_ACTIVATED#{\"id\":" + SERVER_QUEST_ID_L5_2 + ",\"internalName\":\"Test Server Quest L5 2\",\"title\":null,\"description\":null,\"xp\":200,\"razarion\":0,\"crystal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"BASE_KILLED\",\"comparisonConfig\":{\"count\":2,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        systemConnection.getWebsocketMessageHelper().assertMessageSent(10, "QUEST_PASSED#{\"id\":" + SERVER_QUEST_ID_L5_2 + ",\"internalName\":\"Test Server Quest L5 2\",\"title\":null,\"description\":null,\"xp\":200,\"razarion\":0,\"crystal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"BASE_KILLED\",\"comparisonConfig\":{\"count\":2,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        systemConnection.getWebsocketMessageHelper().assertMessageSent(11, "XP_CHANGED#300");
        systemConnection.getWebsocketMessageHelper().assertMessageSent(12, "QUEST_ACTIVATED#{\"id\":" + SERVER_QUEST_ID_L5_3 + ",\"internalName\":\"Test Server Quest L5 3\",\"title\":null,\"description\":null,\"xp\":50,\"razarion\":0,\"crystal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"HARVEST\",\"comparisonConfig\":{\"count\":100,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        systemConnection.getWebsocketMessageHelper().assertMessageSent(13, "QUEST_PASSED#{\"id\":" + SERVER_QUEST_ID_L5_3 + ",\"internalName\":\"Test Server Quest L5 3\",\"title\":null,\"description\":null,\"xp\":50,\"razarion\":0,\"crystal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"HARVEST\",\"comparisonConfig\":{\"count\":100,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        systemConnection.getWebsocketMessageHelper().assertMessageSent(14, "XP_CHANGED#350");

        assertCount(10, QuestHistoryEntity.class);
        assertCount(2, LevelHistoryEntity.class);
        assertCount(0, UserEntity.class);

        EasyMock.verify(serverGameEngineControlMock, questServiceMock);

        ejectorGameEngine.eject();
        ejectorQuestService.eject();
    }

    // @Test
    // Fails due to complex mocking. Use setupPlanetWithSlopes() and real
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
        UserContext userContext = handleFacebookUserLogin("0000001");
        // Setup mocks
        ServerGameEngineControl serverGameEngineControlMock = EasyMock.createStrictMock(ServerGameEngineControl.class);
        serverGameEngineControlMock.onLevelChanged(EasyMock.anyObject(), EasyMock.eq(LEVEL_5_ID));
        QuestService questServiceMock = EasyMock.createStrictMock(QuestService.class);
        EasyMock.expect(questServiceMock.hasActiveQuest(EasyMock.anyObject())).andReturn(false);
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
        SimpleTestEnvironment.Ejector ejectorServerLevelQuestService = SimpleTestEnvironment.injectInstance("serverGameEngineControlInstance", serverLevelQuestService, () -> serverGameEngineControlMock);
        SimpleTestEnvironment.Ejector ejectorQuestService = SimpleTestEnvironment.injectService("questService", serverLevelQuestService, questServiceMock);

        TestClientSystemConnection systemConnection = systemConnectionService.connectClient(sessionHolder.getPlayerSession());
        userService.getUserContextFromSession(); // Simulate anonymous login
        int userId = userContext.getLevelId();

        serverLevelQuestService.onClientLevelUpdate(sessionId, LEVEL_4_ID);
        userContext = userService.getUserContextFromSession();
        Assert.assertEquals(0, userContext.getXp());
        Assert.assertEquals(LEVEL_4_ID, userContext.getLevelId().intValue());
        QuestConfig actualQuestConfigL41 = gameUiContextCrudPersistence.loadWarm(Locale.US, userContext).getSlaveQuestInfo().getActiveQuest();
        assertDbUserLevelXp(userContext.getUserId(), LEVEL_4_ID, SERVER_QUEST_ID_L4_1, 0);
        Assert.assertEquals(SERVER_QUEST_ID_L4_1, actualQuestConfigL41.getId().intValue());
        // L4 first quest passed
        executeInGameEngineThread(() -> serverLevelQuestService.onQuestPassed(userId, actualQuestConfigL41));
        userContext = userService.getUserContextFromSession();
        Assert.assertEquals(100, userContext.getXp());
        Assert.assertEquals(LEVEL_4_ID, userContext.getLevelId().intValue());
        QuestConfig actualQuestConfigL42 = gameUiContextCrudPersistence.loadWarm(Locale.US, userContext).getSlaveQuestInfo().getActiveQuest();
        assertDbUserLevelXp(userContext.getUserId(), LEVEL_4_ID, SERVER_QUEST_ID_L4_2, 100, SERVER_QUEST_ID_L4_1);
        Assert.assertEquals(SERVER_QUEST_ID_L4_2, actualQuestConfigL42.getId().intValue());
        // L4 Second quest passed
        executeInGameEngineThread(() -> serverLevelQuestService.onQuestPassed(userId, actualQuestConfigL42));
        userContext = userService.getUserContextFromSession();
        Assert.assertEquals(0, userContext.getXp());
        Assert.assertEquals(LEVEL_5_ID, userContext.getLevelId().intValue());
        QuestConfig actualQuestConfigL51 = gameUiContextCrudPersistence.loadWarm(Locale.US, userContext).getSlaveQuestInfo().getActiveQuest();
        assertDbUserLevelXp(userContext.getUserId(), LEVEL_5_ID, SERVER_QUEST_ID_L5_1, 0, SERVER_QUEST_ID_L4_1, SERVER_QUEST_ID_L4_2);
        Assert.assertEquals(SERVER_QUEST_ID_L5_1, actualQuestConfigL51.getId().intValue());
        // L5 First quest passed
        executeInGameEngineThread(() -> serverLevelQuestService.onQuestPassed(userId, actualQuestConfigL51));
        userContext = userService.getUserContextFromSession();
        Assert.assertEquals(100, userContext.getXp());
        Assert.assertEquals(LEVEL_5_ID, userContext.getLevelId().intValue());
        QuestConfig actualQuestConfigL52 = gameUiContextCrudPersistence.loadWarm(Locale.US, userContext).getSlaveQuestInfo().getActiveQuest();
        assertDbUserLevelXp(userContext.getUserId(), LEVEL_5_ID, SERVER_QUEST_ID_L5_2, 100, SERVER_QUEST_ID_L4_1, SERVER_QUEST_ID_L4_2, SERVER_QUEST_ID_L5_1);
        Assert.assertEquals(SERVER_QUEST_ID_L5_2, actualQuestConfigL52.getId().intValue());
        // L5 Second quest passed
        executeInGameEngineThread(() -> serverLevelQuestService.onQuestPassed(userId, actualQuestConfigL52));
        userContext = userService.getUserContextFromSession();
        Assert.assertEquals(300, userContext.getXp());
        Assert.assertEquals(LEVEL_5_ID, userContext.getLevelId().intValue());
        QuestConfig actualQuestConfigL53 = gameUiContextCrudPersistence.loadWarm(Locale.US, userContext).getSlaveQuestInfo().getActiveQuest();
        assertDbUserLevelXp(userContext.getUserId(), LEVEL_5_ID, SERVER_QUEST_ID_L5_3, 300, SERVER_QUEST_ID_L4_1, SERVER_QUEST_ID_L4_2, SERVER_QUEST_ID_L5_1, SERVER_QUEST_ID_L5_2);
        Assert.assertEquals(SERVER_QUEST_ID_L5_3, actualQuestConfigL53.getId().intValue());
        // L5 Third quest passed
        executeInGameEngineThread(() -> serverLevelQuestService.onQuestPassed(userId, actualQuestConfigL53));
        userContext = userService.getUserContextFromSession();
        Assert.assertEquals(350, userContext.getXp());
        Assert.assertNull(gameUiContextCrudPersistence.loadWarm(Locale.US, userContext).getSlaveQuestInfo().getActiveQuest());
        assertDbUserLevelXp(userId, LEVEL_5_ID, null, 350, SERVER_QUEST_ID_L4_1, SERVER_QUEST_ID_L4_2, SERVER_QUEST_ID_L5_1, SERVER_QUEST_ID_L5_2, SERVER_QUEST_ID_L5_3);
        Assert.assertEquals(LEVEL_5_ID, userContext.getLevelId().intValue());

        systemConnection.getWebsocketMessageHelper().assertMessageSentCount(15);
        systemConnection.getWebsocketMessageHelper().assertMessageSent(0, "QUEST_ACTIVATED#{\"id\":" + SERVER_QUEST_ID_L4_1 + ",\"internalName\":\"Test Server Quest L4 1\",\"title\":null,\"description\":null,\"xp\":100,\"razarion\":0,\"crystal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"SYNC_ITEM_CREATED\",\"comparisonConfig\":{\"count\":1,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        systemConnection.getWebsocketMessageHelper().assertMessageSent(1, "QUEST_PASSED#{\"id\":" + SERVER_QUEST_ID_L4_1 + ",\"internalName\":\"Test Server Quest L4 1\",\"title\":null,\"description\":null,\"xp\":100,\"razarion\":0,\"crystal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"SYNC_ITEM_CREATED\",\"comparisonConfig\":{\"count\":1,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        systemConnection.getWebsocketMessageHelper().assertMessageSent(2, "XP_CHANGED#100");
        systemConnection.getWebsocketMessageHelper().assertMessageSent(3, "QUEST_ACTIVATED#{\"id\":" + SERVER_QUEST_ID_L4_2 + ",\"internalName\":\"Test Server Quest L4 2\",\"title\":null,\"description\":null,\"xp\":200,\"razarion\":0,\"crystal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"SYNC_ITEM_KILLED\",\"comparisonConfig\":{\"count\":2,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        systemConnection.getWebsocketMessageHelper().assertMessageSent(4, "QUEST_PASSED#{\"id\":" + SERVER_QUEST_ID_L4_2 + ",\"internalName\":\"Test Server Quest L4 2\",\"title\":null,\"description\":null,\"xp\":200,\"razarion\":0,\"crystal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"SYNC_ITEM_KILLED\",\"comparisonConfig\":{\"count\":2,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        UserContext expectedUserContext = new UserContext().userId(userId).levelId(LEVEL_5_ID).name("Registered User").unlockedItemLimit(Collections.emptyMap());
        List<LevelUnlockConfig> expectedLevelUnlockConfigs = new ArrayList<>();
        expectedLevelUnlockConfigs.add(new LevelUnlockConfig().id(LEVEL_UNLOCK_ID_L4_1).baseItemType(BASE_ITEM_TYPE_BULLDOZER_ID).baseItemTypeCount(1).internalName("levelUnlockEntity4_1"));
        expectedLevelUnlockConfigs.add(new LevelUnlockConfig().id(LEVEL_UNLOCK_ID_L5_1).baseItemType(BASE_ITEM_TYPE_ATTACKER_ID).baseItemTypeCount(2).crystalCost(10).internalName("levelUnlockEntity5_1"));
        expectedLevelUnlockConfigs.add(new LevelUnlockConfig().id(LEVEL_UNLOCK_ID_L5_2).baseItemType(BASE_ITEM_TYPE_HARVESTER_ID).baseItemTypeCount(1).crystalCost(20).internalName("levelUnlockEntity5_2"));
        systemConnection.getWebsocketMessageHelper().assertMessageSent(5, "LEVEL_UPDATE_SERVER", LevelUpPacket.class, new LevelUpPacket().userContext(expectedUserContext).availableUnlocks(!expectedLevelUnlockConfigs.isEmpty()));
        systemConnection.getWebsocketMessageHelper().assertMessageSent(6, "QUEST_ACTIVATED#{\"id\":" + SERVER_QUEST_ID_L5_1 + ",\"internalName\":\"Test Server Quest L5 1\",\"title\":null,\"description\":null,\"xp\":100,\"razarion\":0,\"crystal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"BOX_PICKED\",\"comparisonConfig\":{\"count\":1,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        systemConnection.getWebsocketMessageHelper().assertMessageSent(7, "QUEST_PASSED#{\"id\":" + SERVER_QUEST_ID_L5_1 + ",\"internalName\":\"Test Server Quest L5 1\",\"title\":null,\"description\":null,\"xp\":100,\"razarion\":0,\"crystal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"BOX_PICKED\",\"comparisonConfig\":{\"count\":1,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        systemConnection.getWebsocketMessageHelper().assertMessageSent(8, "XP_CHANGED#100");
        systemConnection.getWebsocketMessageHelper().assertMessageSent(9, "QUEST_ACTIVATED#{\"id\":" + SERVER_QUEST_ID_L5_2 + ",\"internalName\":\"Test Server Quest L5 2\",\"title\":null,\"description\":null,\"xp\":200,\"razarion\":0,\"crystal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"BASE_KILLED\",\"comparisonConfig\":{\"count\":2,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        systemConnection.getWebsocketMessageHelper().assertMessageSent(10, "QUEST_PASSED#{\"id\":" + SERVER_QUEST_ID_L5_2 + ",\"internalName\":\"Test Server Quest L5 2\",\"title\":null,\"description\":null,\"xp\":200,\"razarion\":0,\"crystal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"BASE_KILLED\",\"comparisonConfig\":{\"count\":2,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        systemConnection.getWebsocketMessageHelper().assertMessageSent(11, "XP_CHANGED#300");
        systemConnection.getWebsocketMessageHelper().assertMessageSent(12, "QUEST_ACTIVATED#{\"id\":" + SERVER_QUEST_ID_L5_3 + ",\"internalName\":\"Test Server Quest L5 3\",\"title\":null,\"description\":null,\"xp\":50,\"razarion\":0,\"crystal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"HARVEST\",\"comparisonConfig\":{\"count\":100,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        systemConnection.getWebsocketMessageHelper().assertMessageSent(13, "QUEST_PASSED#{\"id\":" + SERVER_QUEST_ID_L5_3 + ",\"internalName\":\"Test Server Quest L5 3\",\"title\":null,\"description\":null,\"xp\":50,\"razarion\":0,\"crystal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"HARVEST\",\"comparisonConfig\":{\"count\":100,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        systemConnection.getWebsocketMessageHelper().assertMessageSent(14, "XP_CHANGED#350");

        assertCount(10, QuestHistoryEntity.class);
        assertCount(2, LevelHistoryEntity.class);
        assertCount(2, UserEntity.class);

        EasyMock.verify(serverGameEngineControlMock, questServiceMock);

        ejectorServerLevelQuestService.eject();
        ejectorQuestService.eject();
    }

    // @Test
    // Fails due to complex mocking. Use setupPlanetWithSlopes() and real
    public void activateQuestRegistered() throws Exception {
        // Setup session
        String sessionId = sessionHolder.getPlayerSession().getHttpSessionId();
        handleFacebookUserLogin("0000001");

        TestClientSystemConnection systemConnection = activateQuest(sessionId);

        systemConnection.getWebsocketMessageHelper().assertMessageSentCount(8);
        systemConnection.getWebsocketMessageHelper().assertMessageSent(0, "QUEST_ACTIVATED#{\"id\":" + SERVER_QUEST_ID_L4_1 + ",\"internalName\":\"Test Server Quest L4 1\",\"title\":null,\"description\":null,\"xp\":100,\"razarion\":0,\"crystal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"SYNC_ITEM_CREATED\",\"comparisonConfig\":{\"count\":1,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        systemConnection.getWebsocketMessageHelper().assertMessageSent(1, "QUEST_ACTIVATED#null");
        systemConnection.getWebsocketMessageHelper().assertMessageSent(2, "QUEST_ACTIVATED#{\"id\":" + SERVER_QUEST_ID_L4_2 + ",\"internalName\":\"Test Server Quest L4 2\",\"title\":null,\"description\":null,\"xp\":200,\"razarion\":0,\"crystal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"SYNC_ITEM_KILLED\",\"comparisonConfig\":{\"count\":2,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        systemConnection.getWebsocketMessageHelper().assertMessageSent(3, "QUEST_PASSED#{\"id\":" + SERVER_QUEST_ID_L4_2 + ",\"internalName\":\"Test Server Quest L4 2\",\"title\":null,\"description\":null,\"xp\":200,\"razarion\":0,\"crystal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"SYNC_ITEM_KILLED\",\"comparisonConfig\":{\"count\":2,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        systemConnection.getWebsocketMessageHelper().assertMessageSent(4, "XP_CHANGED#200");
        systemConnection.getWebsocketMessageHelper().assertMessageSent(5, "QUEST_ACTIVATED#{\"id\":" + SERVER_QUEST_ID_L4_1 + ",\"internalName\":\"Test Server Quest L4 1\",\"title\":null,\"description\":null,\"xp\":100,\"razarion\":0,\"crystal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"SYNC_ITEM_CREATED\",\"comparisonConfig\":{\"count\":1,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");
        systemConnection.getWebsocketMessageHelper().assertMessageSent(6, "QUEST_ACTIVATED#null");
        systemConnection.getWebsocketMessageHelper().assertMessageSent(7, "QUEST_ACTIVATED#{\"id\":" + SERVER_QUEST_ID_L4_1 + ",\"internalName\":\"Test Server Quest L4 1\",\"title\":null,\"description\":null,\"xp\":100,\"razarion\":0,\"crystal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"SYNC_ITEM_CREATED\",\"comparisonConfig\":{\"count\":1,\"typeCount\":null,\"time\":null,\"addExisting\":null,\"placeConfig\":null}}}");

        assertCount(7, QuestHistoryEntity.class);
        assertCount(2, LevelHistoryEntity.class);

    }

    @Test
    // Fails due to complex mocking. Use setupPlanetWithSlopes() and real
    public void activateQuestUnRegistered() throws Exception {
        // Setup session
        handleUnregisteredLogin();

        String sessionId = sessionHolder.getPlayerSession().getHttpSessionId();

        TestClientSystemConnection systemConnection = activateQuest(sessionId);
        systemConnection.getWebsocketMessageHelper().printMessagesSent();

        systemConnection.getWebsocketMessageHelper().assertMessageSentCount(12);
        systemConnection.getWebsocketMessageHelper().assertMessageSent(0, "QUEST_ACTIVATED#{\"id\":" + SERVER_QUEST_ID_L4_1 + ",\"internalName\":\"Test Server Quest L4 1\",\"title\":null,\"description\":null,\"xp\":100,\"razarion\":0,\"crystal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"SYNC_ITEM_KILLED\",\"comparisonConfig\":{\"count\":1,\"typeCount\":null,\"time\":null,\"placeConfig\":null,\"botIds\":null}}}");
        systemConnection.getWebsocketMessageHelper().assertMessageSent(1, "QUEST_PROGRESS_CHANGED#null");
        systemConnection.getWebsocketMessageHelper().assertMessageSent(2, "QUEST_ACTIVATED#null");
        systemConnection.getWebsocketMessageHelper().assertMessageSent(3, "QUEST_ACTIVATED#{\"id\":" + SERVER_QUEST_ID_L4_2 + ",\"internalName\":\"Test Server Quest L4 2\",\"title\":null,\"description\":null,\"xp\":200,\"razarion\":0,\"crystal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"SYNC_ITEM_CREATED\",\"comparisonConfig\":{\"count\":2,\"typeCount\":null,\"time\":null,\"placeConfig\":null,\"botIds\":null}}}");
        systemConnection.getWebsocketMessageHelper().assertMessageSent(4, "QUEST_PROGRESS_CHANGED#null");
        systemConnection.getWebsocketMessageHelper().assertMessageSent(5, "QUEST_PASSED#{\"id\":" + SERVER_QUEST_ID_L4_2 + ",\"internalName\":\"Test Server Quest L4 2\",\"title\":null,\"description\":null,\"xp\":200,\"razarion\":0,\"crystal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"SYNC_ITEM_CREATED\",\"comparisonConfig\":{\"count\":2,\"typeCount\":null,\"time\":null,\"placeConfig\":null,\"botIds\":null}}}");
        systemConnection.getWebsocketMessageHelper().assertMessageSent(6, "XP_CHANGED#200");
        systemConnection.getWebsocketMessageHelper().assertMessageSent(7, "QUEST_ACTIVATED#{\"id\":" + SERVER_QUEST_ID_L4_1 + ",\"internalName\":\"Test Server Quest L4 1\",\"title\":null,\"description\":null,\"xp\":100,\"razarion\":0,\"crystal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"SYNC_ITEM_KILLED\",\"comparisonConfig\":{\"count\":1,\"typeCount\":null,\"time\":null,\"placeConfig\":null,\"botIds\":null}}}");
        systemConnection.getWebsocketMessageHelper().assertMessageSent(8, "QUEST_PROGRESS_CHANGED#null");
        systemConnection.getWebsocketMessageHelper().assertMessageSent(9, "QUEST_ACTIVATED#null");
        systemConnection.getWebsocketMessageHelper().assertMessageSent(10, "QUEST_ACTIVATED#{\"id\":" + SERVER_QUEST_ID_L4_1 + ",\"internalName\":\"Test Server Quest L4 1\",\"title\":null,\"description\":null,\"xp\":100,\"razarion\":0,\"crystal\":0,\"passedMessage\":null,\"hidePassedDialog\":false,\"conditionConfig\":{\"conditionTrigger\":\"SYNC_ITEM_KILLED\",\"comparisonConfig\":{\"count\":1,\"typeCount\":null,\"time\":null,\"placeConfig\":null,\"botIds\":null}}}");
        systemConnection.getWebsocketMessageHelper().assertMessageSent(11, "QUEST_PROGRESS_CHANGED#null");

        assertCount(7, QuestHistoryEntity.class);
        assertCount(2, LevelHistoryEntity.class);

    }

    private TestClientSystemConnection activateQuest(String sessionId) throws Exception {
        // Setup mocks
        QuestService questServiceMock = EasyMock.createStrictMock(QuestService.class);
        // Level up LEVEL_4_ID
        EasyMock.expect(questServiceMock.hasActiveQuest(EasyMock.anyObject())).andReturn(false);
        questServiceMock.activateCondition(EasyMock.anyObject(), EasyMock.anyObject());
        EasyMock.expect(questServiceMock.getQuestProgressInfo(EasyMock.anyObject())).andReturn(null);
        // Activate quest SERVER_QUEST_ID_L4_2
        EasyMock.expect(questServiceMock.hasActiveQuest(EasyMock.anyObject())).andReturn(true);
        questServiceMock.deactivateActorCondition(EasyMock.anyObject());
        questServiceMock.activateCondition(EasyMock.anyObject(), EasyMock.anyObject());
        EasyMock.expect(questServiceMock.getQuestProgressInfo(EasyMock.anyObject())).andReturn(null);
        // Pass Quest SERVER_QUEST_ID_L4_2
        questServiceMock.activateCondition(EasyMock.anyObject(), EasyMock.anyObject());
        EasyMock.expect(questServiceMock.getQuestProgressInfo(EasyMock.anyObject())).andReturn(null);
        // Activate quest wrong level
        EasyMock.expect(questServiceMock.hasActiveQuest(EasyMock.anyObject())).andReturn(true);
        questServiceMock.deactivateActorCondition(EasyMock.anyObject());
        // Activate passed quest
        EasyMock.expect(questServiceMock.hasActiveQuest(EasyMock.anyObject())).andReturn(false);
        // Level up LEVEL_5_ID
        EasyMock.expect(questServiceMock.hasActiveQuest(EasyMock.anyObject())).andReturn(false);
        questServiceMock.activateCondition(EasyMock.anyObject(), EasyMock.anyObject());
        EasyMock.expect(questServiceMock.getQuestProgressInfo(EasyMock.anyObject())).andReturn(null);

        EasyMock.replay(questServiceMock);
        SimpleTestEnvironment.Ejector ejector = SimpleTestEnvironment.injectService("questService", serverLevelQuestService, questServiceMock);
        TestClientSystemConnection systemConnection = systemConnectionService.connectClient(sessionHolder.getPlayerSession());
        // Simulate anonymous login
        UserContext userContext = userService.getUserContextFromSession();
        int userId = userContext.getUserId();
        // Level 1: no server quests
        List<QuestConfig> actualQuestConfigs = serverLevelQuestService.readOpenQuestForDialog(userContext, Locale.US);
        TestHelper.assertObjectNameIdProviders(actualQuestConfigs);
        // Level 4: 2 server quests one quest active
        serverLevelQuestService.onClientLevelUpdate(sessionId, LEVEL_4_ID);
        actualQuestConfigs = serverLevelQuestService.readOpenQuestForDialog(userContext, Locale.US);
        TestHelper.assertObjectNameIdProviders(actualQuestConfigs, SERVER_QUEST_ID_L4_2);
        // Activate quest
        serverLevelQuestService.activateQuest(userContext, SERVER_QUEST_ID_L4_2, Locale.US);
        actualQuestConfigs = serverLevelQuestService.readOpenQuestForDialog(userContext, Locale.US);
        TestHelper.assertObjectNameIdProviders(actualQuestConfigs, SERVER_QUEST_ID_L4_1);
        // Pass Quest
        serverLevelQuestService.onQuestPassed(userId, readQuestConfig(SERVER_QUEST_ID_L4_2));
        actualQuestConfigs = serverLevelQuestService.readOpenQuestForDialog(userContext, Locale.US);
        TestHelper.assertObjectNameIdProviders(actualQuestConfigs);
        // Activate quest wrong level
        try {
            serverLevelQuestService.activateQuest(userContext, SERVER_QUEST_ID_L5_1, Locale.US);
            Assert.fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            // Expected
        }
        // Activate passed quest
        try {
            serverLevelQuestService.activateQuest(userContext, SERVER_QUEST_ID_L4_2, Locale.US);
            Assert.fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            // Expected
        }
        // Level up LEVEL_5_ID
        serverLevelQuestService.onClientLevelUpdate(sessionId, LEVEL_5_ID);
        actualQuestConfigs = serverLevelQuestService.readOpenQuestForDialog(userContext, Locale.US);
        TestHelper.assertObjectNameIdProviders(actualQuestConfigs, SERVER_QUEST_ID_L5_1, SERVER_QUEST_ID_L5_2, SERVER_QUEST_ID_L5_3);

        EasyMock.verify(questServiceMock);

        assertCount(7, QuestHistoryEntity.class);
        assertCount(2, LevelHistoryEntity.class);

        ejector.eject();

        return systemConnection;
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