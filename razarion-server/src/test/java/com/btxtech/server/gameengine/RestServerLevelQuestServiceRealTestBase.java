package com.btxtech.server.gameengine;

import com.btxtech.server.ClientSystemConnectionServiceTestHelper;
import com.btxtech.server.FakeEmailServer;
import com.btxtech.server.systemtests.RestServerTestBase;
import com.btxtech.server.SimpleTestEnvironment;
import com.btxtech.server.TestClientSystemConnection;
import com.btxtech.server.persistence.history.QuestHistoryEntity;
import com.btxtech.server.persistence.itemtype.ItemTypePersistence;
import com.btxtech.server.persistence.level.LevelEntity;
import com.btxtech.server.persistence.quest.QuestConfigEntity;
import com.btxtech.server.persistence.server.ServerLevelQuestEntity;
import com.btxtech.server.user.UnregisteredUser;
import com.btxtech.server.user.UserEntity;
import com.btxtech.server.user.UserService;
import com.btxtech.server.web.SessionHolder;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.config.ComparisonConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.CommandService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.utils.CollectionUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Beat
 * on 11.02.2018.
 */
public class RestServerLevelQuestServiceRealTestBase extends RestServerTestBase {
    private static int FULFILLED_QUEST;
    private static int BEFORE_FULFILLED_QUEST;
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private CommandService commandService;
    @Inject
    private ServerLevelQuestService serverLevelQuestService;
    @Inject
    private SessionHolder sessionHolder;
    @Inject
    private FakeEmailServer fakeEmailServer;
    @Inject
    private ItemTypePersistence itemTypePersistence;
    @Inject
    private ClientSystemConnectionServiceTestHelper systemConnectionService;
    @Inject
    private UserService userService;

    @Before
    public void before() throws Exception {
        clearMongoDb(); // Remove user quest progress
        fakeEmailServer.startFakeMailServer();
        setupPlanetFastTickGameEngine();
    }

    @After
    public void after() throws Exception {
        fakeEmailServer.stopFakeMailServer();
        cleanUsers();
        cleanTable(UserEntity.class);
        cleanTable(QuestHistoryEntity.class);
        cleanPlanetFastTickGameEngine();
        clearMongoDb(); // Remove user quest progress
    }

    @Test
    public void testManualActivateFulfilledQuestRegistered() throws Exception {
        UserContext userContext = handleNewUnverifiedUser("test", "test");

        manualActivateFulfilledQuest(userContext);

        runInTransaction(entityManager -> {
            UserEntity userEntity = userService.getUserEntity4Email("test");
            Assert.assertEquals(SERVER_QUEST_ID_L4_1, (int) userEntity.getActiveQuest().getId());
            Assert.assertTrue(userEntity.getCompletedQuestIds().contains(FULFILLED_QUEST));
        });
    }


    @Test
    public void testManualActivateFulfilledQuestUnregistered() throws Exception {
        UserContext userContext = handleUnregisteredLogin();

        manualActivateFulfilledQuest(userContext);

        UnregisteredUser unregisteredUser = sessionHolder.getPlayerSession().getUnregisteredUser();
        Assert.assertEquals(SERVER_QUEST_ID_L4_1, (int) unregisteredUser.getActiveQuest().getId());
        Assert.assertTrue(unregisteredUser.getCompletedQuestIds().contains(FULFILLED_QUEST));
    }

    @Test
    public void testFirstQuestActivateFulfilledQuestRegistered() throws Exception {
        UserContext userContext = handleNewUnverifiedUser("test", "test");

        firstQuestActivateFulfilledQuest(userContext);

        runInTransaction(entityManager -> {
            UserEntity userEntity = userService.getUserEntity4Email("test");
            Assert.assertEquals(SERVER_QUEST_ID_L4_1, (int) userEntity.getActiveQuest().getId());
            Assert.assertTrue(userEntity.getCompletedQuestIds().contains(FULFILLED_QUEST));
        });
    }

    @Test
    public void testFirstQuestActivateFulfilledQuestUnregistered() throws Exception {
        UserContext userContext = handleUnregisteredLogin();

        firstQuestActivateFulfilledQuest(userContext);

        UnregisteredUser unregisteredUser = sessionHolder.getPlayerSession().getUnregisteredUser();
        Assert.assertEquals(SERVER_QUEST_ID_L4_1, (int) unregisteredUser.getActiveQuest().getId());
        Assert.assertTrue(unregisteredUser.getCompletedQuestIds().contains(FULFILLED_QUEST));
    }

    @Test
    public void testGameEngineQuestActivateFulfilledQuestRegistered() throws Exception {
        UserContext userContext = handleNewUnverifiedUser("test", "test");

        gameEngineActivateFulfilledQuest(userContext);

        runInTransaction(entityManager -> {
            UserEntity userEntity = userService.getUserEntity4Email("test");
            Assert.assertEquals(SERVER_QUEST_ID_L4_1, (int) userEntity.getActiveQuest().getId());
            Assert.assertTrue(userEntity.getCompletedQuestIds().contains(FULFILLED_QUEST));
            Assert.assertTrue(userEntity.getCompletedQuestIds().contains(BEFORE_FULFILLED_QUEST));
        });
    }

    @Test
    public void testGameEngineQuestActivateFulfilledQuestUnregistered() throws Exception {
        UserContext userContext = handleUnregisteredLogin();

        gameEngineActivateFulfilledQuest(userContext);

        UnregisteredUser unregisteredUser = sessionHolder.getPlayerSession().getUnregisteredUser();
        Assert.assertEquals(SERVER_QUEST_ID_L4_1, (int) unregisteredUser.getActiveQuest().getId());
        Assert.assertTrue(unregisteredUser.getCompletedQuestIds().contains(FULFILLED_QUEST));
        Assert.assertTrue(unregisteredUser.getCompletedQuestIds().contains(BEFORE_FULFILLED_QUEST));
    }

    @Test
    public void registerDuringQuest() throws Exception {
        runInTransaction(em -> {
            ServerLevelQuestEntity serverLevelQuestEntityL4 = em.createQuery("select s from ServerLevelQuestEntity s where s.minimalLevel=:minLevel", ServerLevelQuestEntity.class).setParameter("minLevel", em.find(LevelEntity.class, LEVEL_4_ID)).getSingleResult();
            QuestConfigEntity questConfigEntity = new QuestConfigEntity();
            Map<Integer, Integer> typeCount = new HashMap<>();
            typeCount.put(BASE_ITEM_TYPE_FACTORY_ID, 1);
            questConfigEntity.fromQuestConfig(itemTypePersistence, new QuestConfig().setInternalName("Start quest").setXp(100).setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setTypeCount(typeCount))), Locale.US);
            serverLevelQuestEntityL4.getQuestConfigs().add(0, questConfigEntity);
            em.merge(serverLevelQuestEntityL4);
            BEFORE_FULFILLED_QUEST = serverLevelQuestEntityL4.getQuestConfigs().get(0).getId();
        });
        UserContext userContext = handleUnregisteredLogin();

        serverLevelQuestService.onClientLevelUpdate(sessionHolder.getPlayerSession().getHttpSessionId(), LEVEL_4_ID);
        PlayerBaseFull playerBaseFull = baseItemService.createHumanBaseWithBaseItem(userContext.getLevelId(), userContext.getUnlockedItemLimit(), userContext.getHumanPlayerId(), userContext.getName(), new DecimalPosition(1000, 1000));
        userService.createUnverifiedUserAndLogin("test", "test");
        waitForBaseServiceIdle();
        systemConnectionService.connectClient(sessionHolder.getPlayerSession());
        commandService.build(CollectionUtils.getFirst(playerBaseFull.getItems()).getId(), new DecimalPosition(1020, 1000), BASE_ITEM_TYPE_FACTORY_ID);
        waitForBaseServiceIdle();

        // Verify
        runInTransaction(entityManager -> {
            UserEntity userEntity = userService.getUserEntity4Email("test");
            Assert.assertEquals(SERVER_QUEST_ID_L4_1, (int) userEntity.getActiveQuest().getId());
            Assert.assertTrue(userEntity.getCompletedQuestIds().contains(BEFORE_FULFILLED_QUEST));
        });
    }


    private void manualActivateFulfilledQuest(UserContext userContext) throws Exception {
        runInTransaction(em -> {
            ServerLevelQuestEntity serverLevelQuestEntityL5 = em.createQuery("select s from ServerLevelQuestEntity s where s.minimalLevel=:minLevel", ServerLevelQuestEntity.class).setParameter("minLevel", em.find(LevelEntity.class, LEVEL_5_ID)).getSingleResult();
            QuestConfigEntity questConfigEntity = new QuestConfigEntity();
            Map<Integer, Integer> typeCount = new HashMap<>();
            typeCount.put(BASE_ITEM_TYPE_BULLDOZER_ID, 1);
            questConfigEntity.fromQuestConfig(itemTypePersistence, new QuestConfig().setInternalName("Test Server Quest L5").setXp(100).setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_POSITION).setComparisonConfig(new ComparisonConfig().setTypeCount(typeCount))), Locale.US);
            serverLevelQuestEntityL5.getQuestConfigs().add(questConfigEntity);
            em.merge(serverLevelQuestEntityL5);
            FULFILLED_QUEST = serverLevelQuestEntityL5.getQuestConfigs().get(3).getId();
        });
        serverLevelQuestService.onClientLevelUpdate(sessionHolder.getPlayerSession().getHttpSessionId(), LEVEL_5_ID);
        baseItemService.createHumanBaseWithBaseItem(userContext.getLevelId(), userContext.getUnlockedItemLimit(), userContext.getHumanPlayerId(), userContext.getName(), new DecimalPosition(1000, 1000));
        waitForBaseServiceIdle();
        TestClientSystemConnection systemConnection = systemConnectionService.connectClient(sessionHolder.getPlayerSession());
        serverLevelQuestService.activateQuest(userContext, FULFILLED_QUEST, Locale.ENGLISH);

        // Verify
        systemConnection.getWebsocketMessageHelper().assertMessageSentCount(7);
        systemConnection.getWebsocketMessageHelper().assertMessageSent(0, "QUEST_ACTIVATED", QuestConfig.class, null);
        systemConnection.getWebsocketMessageHelper().assertMessageSent(1, "QUEST_ACTIVATED", QuestConfig.class, readQuestConfig(FULFILLED_QUEST));
        systemConnection.getWebsocketMessageHelper().assertMessageSent(2, "QUEST_PASSED", QuestConfig.class, readQuestConfig(FULFILLED_QUEST));
        systemConnection.getWebsocketMessageHelper().assertMessageSent(3, "XP_CHANGED", Integer.class, 100);
        systemConnection.getWebsocketMessageHelper().assertMessageSent(4, "QUEST_ACTIVATED", QuestConfig.class, readQuestConfig(SERVER_QUEST_ID_L4_1));
        systemConnection.getWebsocketMessageHelper().assertMessageSent(5, "QUEST_PROGRESS_CHANGED", QuestProgressInfo.class, new QuestProgressInfo().setCount(0));
        systemConnection.getWebsocketMessageHelper().assertMessageSent(6, "QUEST_PROGRESS_CHANGED", QuestProgressInfo.class, new QuestProgressInfo().setCount(0));

        assertCount(5, QuestHistoryEntity.class);
    }

    private void firstQuestActivateFulfilledQuest(UserContext userContext) throws Exception {
        runInTransaction(em -> {
            ServerLevelQuestEntity serverLevelQuestEntityL5 = em.createQuery("select s from ServerLevelQuestEntity s where s.minimalLevel=:minLevel", ServerLevelQuestEntity.class).setParameter("minLevel", em.find(LevelEntity.class, LEVEL_4_ID)).getSingleResult();
            QuestConfigEntity questConfigEntity = new QuestConfigEntity();
            Map<Integer, Integer> typeCount = new HashMap<>();
            typeCount.put(BASE_ITEM_TYPE_BULLDOZER_ID, 1);
            questConfigEntity.fromQuestConfig(itemTypePersistence, new QuestConfig().setInternalName("Test Server Quest L5").setXp(100).setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_POSITION).setComparisonConfig(new ComparisonConfig().setTypeCount(typeCount))), Locale.US);
            serverLevelQuestEntityL5.getQuestConfigs().add(0, questConfigEntity);
            em.merge(serverLevelQuestEntityL5);
            FULFILLED_QUEST = serverLevelQuestEntityL5.getQuestConfigs().get(0).getId();
        });
        baseItemService.createHumanBaseWithBaseItem(userContext.getLevelId(), userContext.getUnlockedItemLimit(), userContext.getHumanPlayerId(), userContext.getName(), new DecimalPosition(1000, 1000));
        waitForBaseServiceIdle();
        TestClientSystemConnection systemConnection = systemConnectionService.connectClient(sessionHolder.getPlayerSession());
        serverLevelQuestService.onClientLevelUpdate(sessionHolder.getPlayerSession().getHttpSessionId(), LEVEL_4_ID);

        // Verify
        systemConnection.getWebsocketMessageHelper().assertMessageSentCount(6);
        systemConnection.getWebsocketMessageHelper().assertMessageSent(0, "QUEST_ACTIVATED", QuestConfig.class, readQuestConfig(FULFILLED_QUEST));
        systemConnection.getWebsocketMessageHelper().assertMessageSent(1, "QUEST_PASSED", QuestConfig.class, readQuestConfig(FULFILLED_QUEST));
        systemConnection.getWebsocketMessageHelper().assertMessageSent(2, "XP_CHANGED", Integer.class, 100);
        systemConnection.getWebsocketMessageHelper().assertMessageSent(3, "QUEST_ACTIVATED", QuestConfig.class, readQuestConfig(SERVER_QUEST_ID_L4_1));
        systemConnection.getWebsocketMessageHelper().assertMessageSent(4, "QUEST_PROGRESS_CHANGED", QuestProgressInfo.class, new QuestProgressInfo().setCount(0));
        systemConnection.getWebsocketMessageHelper().assertMessageSent(5, "QUEST_PROGRESS_CHANGED", QuestProgressInfo.class, new QuestProgressInfo().setCount(0));

        assertCount(3, QuestHistoryEntity.class);
    }

    private void gameEngineActivateFulfilledQuest(UserContext userContext) throws Exception {
        runInTransaction(em -> {
            ServerLevelQuestEntity serverLevelQuestEntityL4 = em.createQuery("select s from ServerLevelQuestEntity s where s.minimalLevel=:minLevel", ServerLevelQuestEntity.class).setParameter("minLevel", em.find(LevelEntity.class, LEVEL_4_ID)).getSingleResult();
            QuestConfigEntity questConfigEntity = new QuestConfigEntity();
            Map<Integer, Integer> typeCount = new HashMap<>();
            typeCount.put(BASE_ITEM_TYPE_BULLDOZER_ID, 1);
            questConfigEntity.fromQuestConfig(itemTypePersistence, new QuestConfig().setInternalName("Auto filfill quest").setXp(100).setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_POSITION).setComparisonConfig(new ComparisonConfig().setTypeCount(typeCount))), Locale.US);
            serverLevelQuestEntityL4.getQuestConfigs().add(0, questConfigEntity);
            questConfigEntity = new QuestConfigEntity();
            typeCount = new HashMap<>();
            typeCount.put(BASE_ITEM_TYPE_FACTORY_ID, 1);
            questConfigEntity.fromQuestConfig(itemTypePersistence, new QuestConfig().setInternalName("Start quest").setXp(100).setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setTypeCount(typeCount))), Locale.US);
            serverLevelQuestEntityL4.getQuestConfigs().add(0, questConfigEntity);
            em.merge(serverLevelQuestEntityL4);
            FULFILLED_QUEST = serverLevelQuestEntityL4.getQuestConfigs().get(1).getId();
            BEFORE_FULFILLED_QUEST = serverLevelQuestEntityL4.getQuestConfigs().get(0).getId();
        });
        serverLevelQuestService.onClientLevelUpdate(sessionHolder.getPlayerSession().getHttpSessionId(), LEVEL_4_ID);
        PlayerBaseFull playerBaseFull = baseItemService.createHumanBaseWithBaseItem(userContext.getLevelId(), userContext.getUnlockedItemLimit(), userContext.getHumanPlayerId(), userContext.getName(), new DecimalPosition(1000, 1000));
        waitForBaseServiceIdle();
        TestClientSystemConnection systemConnection = systemConnectionService.connectClient(sessionHolder.getPlayerSession());
        commandService.build(CollectionUtils.getFirst(playerBaseFull.getItems()).getId(), new DecimalPosition(1020, 1000), BASE_ITEM_TYPE_FACTORY_ID);
        waitForBaseServiceIdle();

        // Verify
        systemConnection.getWebsocketMessageHelper().assertMessageSentCount(9);
        systemConnection.getWebsocketMessageHelper().assertMessageSent(0, "QUEST_PROGRESS_CHANGED", QuestProgressInfo.class, new QuestProgressInfo().setTypeCount(setupMap(BASE_ITEM_TYPE_FACTORY_ID, 1)));
        systemConnection.getWebsocketMessageHelper().assertMessageSent(1, "QUEST_PASSED", QuestConfig.class, readQuestConfig(BEFORE_FULFILLED_QUEST));
        systemConnection.getWebsocketMessageHelper().assertMessageSent(2, "XP_CHANGED", Integer.class, 100);
        systemConnection.getWebsocketMessageHelper().assertMessageSent(3, "QUEST_ACTIVATED", QuestConfig.class, readQuestConfig(FULFILLED_QUEST));
        systemConnection.getWebsocketMessageHelper().assertMessageSent(4, "QUEST_PASSED", QuestConfig.class, readQuestConfig(FULFILLED_QUEST));
        systemConnection.getWebsocketMessageHelper().assertMessageSent(5, "XP_CHANGED", Integer.class, 200);
        systemConnection.getWebsocketMessageHelper().assertMessageSent(6, "QUEST_ACTIVATED", QuestConfig.class, readQuestConfig(SERVER_QUEST_ID_L4_1));
        systemConnection.getWebsocketMessageHelper().assertMessageSent(7, "QUEST_PROGRESS_CHANGED", QuestProgressInfo.class, new QuestProgressInfo().setCount(0));
        systemConnection.getWebsocketMessageHelper().assertMessageSent(8, "QUEST_PROGRESS_CHANGED", QuestProgressInfo.class, new QuestProgressInfo().setCount(0));

        printSqlStatement("select * from HISTORY_QUEST");

        assertCount(5, QuestHistoryEntity.class);
    }

    private void waitForBaseServiceIdle() {
        Collection<SyncBaseItem> activeItems = (Collection<SyncBaseItem>) SimpleTestEnvironment.readField("activeItems", baseItemService);
        Collection<SyncBaseItem> activeItemQueue = (Collection<SyncBaseItem>) SimpleTestEnvironment.readField("activeItemQueue", baseItemService);

        while (!activeItems.isEmpty() || !activeItemQueue.isEmpty()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Map<Integer, Integer> setupMap(int itemTypeId, int count) {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(itemTypeId, count);
        return map;
    }
}
