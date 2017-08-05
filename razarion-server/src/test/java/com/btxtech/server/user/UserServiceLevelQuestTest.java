package com.btxtech.server.user;

import com.btxtech.server.ArquillianBaseTest;
import com.btxtech.server.TestHelper;
import com.btxtech.server.persistence.history.LevelHistoryEntity;
import com.btxtech.server.persistence.quest.ComparisonConfigEntity;
import com.btxtech.server.persistence.quest.ConditionConfigEntity;
import com.btxtech.server.persistence.quest.QuestConfigEntity;
import com.btxtech.server.persistence.server.ServerChildListCrudePersistence;
import com.btxtech.server.persistence.server.ServerGameEngineConfigEntity;
import com.btxtech.server.persistence.server.ServerGameEnginePersistence;
import com.btxtech.server.persistence.server.ServerLevelQuestEntity;
import com.btxtech.server.web.SessionHolder;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ServerLevelQuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.ComparisonConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;

import javax.inject.Inject;
import java.util.Locale;

/**
 * Created by Beat
 * 05.05.2017.
 */
public class UserServiceLevelQuestTest extends ArquillianBaseTest {
    @Inject
    private UserService userService;
    @Inject
    private SessionHolder sessionHolder;
    @Inject
    private ServerGameEnginePersistence serverGameEnginePersistence;

    @Before
    public void before() throws Exception {
        setupPlanets();
    }

    @After
    public void after() throws Exception {
        cleanTable(LevelHistoryEntity.class);
        cleanTable(UserEntity.class);
        cleanPlanets();
    }

    @Test
    public void onLevelUpUnregistered() throws Exception {
        setupQuests();
        String sessionId = sessionHolder.getPlayerSession().getHttpSessionId();
        UnregisteredUser unregisteredUser = sessionHolder.getPlayerSession().getUnregisteredUser();
        userService.getUserContext(); // Simulate anonymous login

        userService.onLevelUpdate(sessionId, LEVEL_2_ID);
        Assert.assertEquals(LEVEL_1_ID, userService.getUserContext().getLevelId());

        userService.onLevelUpdate(sessionId, LEVEL_3_ID);
        Assert.assertEquals(LEVEL_1_ID, userService.getUserContext().getLevelId());

        userService.onLevelUpdate(sessionId, LEVEL_4_ID);
        Assert.assertEquals(LEVEL_4_ID, userService.getUserContext().getLevelId());

        assertCount(3, LevelHistoryEntity.class);
        assertCount(0, UserEntity.class);
        cleanQuests();
    }

    @Test
    public void onLevelUpRegister() throws Exception {
        QuestConfig questConfig11 = setupQuests();
        String sessionId = sessionHolder.getPlayerSession().getHttpSessionId();
        userService.handleFacebookUserLogin("0000001");

        assertUser("0000001", LEVEL_1_ID, null);

        userService.onLevelUpdate(sessionId, LEVEL_2_ID);
        Assert.assertEquals(LEVEL_1_ID, userService.getUserContext().getLevelId());
        assertUser("0000001", LEVEL_1_ID, null);

        userService.onLevelUpdate(sessionId, LEVEL_3_ID);
        Assert.assertEquals(LEVEL_1_ID, userService.getUserContext().getLevelId());
        assertUser("0000001", LEVEL_1_ID, null);

        userService.onLevelUpdate(sessionId, LEVEL_4_ID);
        Assert.assertEquals(LEVEL_4_ID, userService.getUserContext().getLevelId());
        assertUser("0000001", LEVEL_4_ID, questConfig11.getId());

        assertCount(3, LevelHistoryEntity.class);
        assertCount(1, UserEntity.class);
        cleanQuests();
    }

    private QuestConfig setupQuests() {
        ServerChildListCrudePersistence<ServerGameEngineConfigEntity, ServerGameEngineConfigEntity, ServerLevelQuestEntity, ServerLevelQuestConfig> crud = serverGameEnginePersistence.getServerLevelQuestCrud();
        ServerLevelQuestConfig serverLevelQuestConfig1 = crud.create();
        serverLevelQuestConfig1.setMinimalLevelId(LEVEL_4_ID);
        serverLevelQuestConfig1.setInternalName("xxxx 1");
        crud.update(serverLevelQuestConfig1);
        ServerChildListCrudePersistence<ServerGameEngineConfigEntity, ServerLevelQuestEntity, QuestConfigEntity, QuestConfig> questCrud = serverGameEnginePersistence.getServerQuestCrud(serverLevelQuestConfig1.getId(), Locale.ENGLISH);
        QuestConfig questConfig11 = questCrud.create();
        questConfig11.setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_KILLED).setComparisonConfig(new ComparisonConfig().setCount(1)));
        questCrud.update(questConfig11);
        questCrud.create();
        questCrud.create();

        ServerLevelQuestConfig serverLevelQuestConfig2 = crud.create();
        serverLevelQuestConfig2.setMinimalLevelId(LEVEL_5_ID);
        serverLevelQuestConfig2.setInternalName("xxxx 2");
        crud.update(serverLevelQuestConfig2);
        questCrud = serverGameEnginePersistence.getServerQuestCrud(serverLevelQuestConfig2.getId(), Locale.ENGLISH);
        questCrud.create();
        questCrud.create();
        return questConfig11;
    }

    private void cleanQuests() {
        ServerChildListCrudePersistence<ServerGameEngineConfigEntity, ServerGameEngineConfigEntity, ServerLevelQuestEntity, ServerLevelQuestConfig> crud = serverGameEnginePersistence.getServerLevelQuestCrud();
        for (ObjectNameId objectNameId : crud.readObjectNameIds()) {
            crud.delete(objectNameId.getId());
        }
        assertEmptyCount(ServerLevelQuestEntity.class);
        assertEmptyCountNative("SERVER_QUEST");
        assertEmptyCount(QuestConfigEntity.class);
        assertEmptyCount(ConditionConfigEntity.class);
        assertEmptyCount(ComparisonConfigEntity.class);
        assertEmptyCountNative("QUEST_COMPARISON_BASE_ITEM");
    }

    @Test
    public void crud() throws Exception {
        // Verify
        Assert.assertTrue(serverGameEnginePersistence.getServerLevelQuestCrud().readObjectNameIds().isEmpty());
        // Create first
        ServerLevelQuestConfig expectedLevelQuestConfig1 = serverGameEnginePersistence.getServerLevelQuestCrud().create();
        expectedLevelQuestConfig1.setMinimalLevelId(LEVEL_4_ID).setInternalName("landnfas 1");
        serverGameEnginePersistence.getServerLevelQuestCrud().update(expectedLevelQuestConfig1);
        // Verify
        TestHelper.assertObjectNameIds(serverGameEnginePersistence.getServerLevelQuestCrud().readObjectNameIds(), "landnfas 1");
        int serverLevelQuestConfigId1 = TestHelper.findIdForName(serverGameEnginePersistence.getServerLevelQuestCrud().readObjectNameIds(), "landnfas 1");
        ServerLevelQuestConfig actualLevelQuestConfig1 = serverGameEnginePersistence.getServerLevelQuestCrud().read(serverLevelQuestConfigId1);
        ReflectionAssert.assertReflectionEquals(expectedLevelQuestConfig1, actualLevelQuestConfig1);

        // Create second
        ServerLevelQuestConfig expectedLevelQuestConfig2 = serverGameEnginePersistence.getServerLevelQuestCrud().create();
        expectedLevelQuestConfig2.setMinimalLevelId(LEVEL_4_ID).setInternalName("landnfas 2");
        serverGameEnginePersistence.getServerLevelQuestCrud().update(expectedLevelQuestConfig2);
        // Verify
        TestHelper.assertObjectNameIds(serverGameEnginePersistence.getServerLevelQuestCrud().readObjectNameIds(), "landnfas 1", "landnfas 2");
        serverLevelQuestConfigId1 = TestHelper.findIdForName(serverGameEnginePersistence.getServerLevelQuestCrud().readObjectNameIds(), "landnfas 1");
        actualLevelQuestConfig1 = serverGameEnginePersistence.getServerLevelQuestCrud().read(serverLevelQuestConfigId1);
        ReflectionAssert.assertReflectionEquals(expectedLevelQuestConfig1, actualLevelQuestConfig1);
        int serverLevelQuestConfigId2 = TestHelper.findIdForName(serverGameEnginePersistence.getServerLevelQuestCrud().readObjectNameIds(), "landnfas 2");
        ServerLevelQuestConfig actualLevelQuestConfig2 = serverGameEnginePersistence.getServerLevelQuestCrud().read(serverLevelQuestConfigId2);
        ReflectionAssert.assertReflectionEquals(expectedLevelQuestConfig2, actualLevelQuestConfig2);

        // Create third
        ServerLevelQuestConfig expectedLevelQuestConfig3 = serverGameEnginePersistence.getServerLevelQuestCrud().create();
        expectedLevelQuestConfig3.setMinimalLevelId(LEVEL_4_ID).setInternalName("landnfas 33");
        serverGameEnginePersistence.getServerLevelQuestCrud().update(expectedLevelQuestConfig3);
        // Verify
        TestHelper.assertObjectNameIds(serverGameEnginePersistence.getServerLevelQuestCrud().readObjectNameIds(), "landnfas 1", "landnfas 2", "landnfas 33");
        serverLevelQuestConfigId1 = TestHelper.findIdForName(serverGameEnginePersistence.getServerLevelQuestCrud().readObjectNameIds(), "landnfas 1");
        actualLevelQuestConfig1 = serverGameEnginePersistence.getServerLevelQuestCrud().read(serverLevelQuestConfigId1);
        ReflectionAssert.assertReflectionEquals(expectedLevelQuestConfig1, actualLevelQuestConfig1);
        serverLevelQuestConfigId2 = TestHelper.findIdForName(serverGameEnginePersistence.getServerLevelQuestCrud().readObjectNameIds(), "landnfas 2");
        actualLevelQuestConfig2 = serverGameEnginePersistence.getServerLevelQuestCrud().read(serverLevelQuestConfigId2);
        ReflectionAssert.assertReflectionEquals(expectedLevelQuestConfig2, actualLevelQuestConfig2);
        int serverLevelQuestConfigId3 = TestHelper.findIdForName(serverGameEnginePersistence.getServerLevelQuestCrud().readObjectNameIds(), "landnfas 33");
        ServerLevelQuestConfig actualLevelQuestConfig3 = serverGameEnginePersistence.getServerLevelQuestCrud().read(serverLevelQuestConfigId3);
        ReflectionAssert.assertReflectionEquals(expectedLevelQuestConfig3, actualLevelQuestConfig3);

        // Create third
        ServerLevelQuestConfig expectedLevelQuestConfig4 = serverGameEnginePersistence.getServerLevelQuestCrud().create();
        expectedLevelQuestConfig4.setMinimalLevelId(LEVEL_5_ID).setInternalName("landnfas 444");
        serverGameEnginePersistence.getServerLevelQuestCrud().update(expectedLevelQuestConfig4);
        // Verify
        TestHelper.assertObjectNameIds(serverGameEnginePersistence.getServerLevelQuestCrud().readObjectNameIds(), "landnfas 1", "landnfas 2", "landnfas 33", "landnfas 444");
        serverLevelQuestConfigId1 = TestHelper.findIdForName(serverGameEnginePersistence.getServerLevelQuestCrud().readObjectNameIds(), "landnfas 1");
        actualLevelQuestConfig1 = serverGameEnginePersistence.getServerLevelQuestCrud().read(serverLevelQuestConfigId1);
        ReflectionAssert.assertReflectionEquals(expectedLevelQuestConfig1, actualLevelQuestConfig1);
        serverLevelQuestConfigId2 = TestHelper.findIdForName(serverGameEnginePersistence.getServerLevelQuestCrud().readObjectNameIds(), "landnfas 2");
        actualLevelQuestConfig2 = serverGameEnginePersistence.getServerLevelQuestCrud().read(serverLevelQuestConfigId2);
        ReflectionAssert.assertReflectionEquals(expectedLevelQuestConfig2, actualLevelQuestConfig2);
        serverLevelQuestConfigId3 = TestHelper.findIdForName(serverGameEnginePersistence.getServerLevelQuestCrud().readObjectNameIds(), "landnfas 33");
        actualLevelQuestConfig3 = serverGameEnginePersistence.getServerLevelQuestCrud().read(serverLevelQuestConfigId3);
        ReflectionAssert.assertReflectionEquals(expectedLevelQuestConfig3, actualLevelQuestConfig3);
        int serverLevelQuestConfigId4 = TestHelper.findIdForName(serverGameEnginePersistence.getServerLevelQuestCrud().readObjectNameIds(), "landnfas 444");
        ServerLevelQuestConfig actualLevelQuestConfig4 = serverGameEnginePersistence.getServerLevelQuestCrud().read(serverLevelQuestConfigId4);
        ReflectionAssert.assertReflectionEquals(expectedLevelQuestConfig4, actualLevelQuestConfig4);

        // Add quests
        QuestConfig expectedQuestConfig1 = serverGameEnginePersistence.getServerQuestCrud(actualLevelQuestConfig1.getId(), Locale.ENGLISH).create();
        expectedQuestConfig1.setInternalName("dsfdsf 1");
        serverGameEnginePersistence.getServerQuestCrud(actualLevelQuestConfig1.getId(), Locale.ENGLISH).update(expectedQuestConfig1);
        // Verify quest
        TestHelper.assertObjectNameIds(serverGameEnginePersistence.getServerQuestCrud(actualLevelQuestConfig1.getId(), Locale.ENGLISH).readObjectNameIds(), "dsfdsf 1");
        int questConfigId1 = TestHelper.findIdForName(serverGameEnginePersistence.getServerQuestCrud(actualLevelQuestConfig1.getId(), Locale.ENGLISH).readObjectNameIds(), "dsfdsf 1");
        QuestConfig actualQuestConfig1 = serverGameEnginePersistence.getServerQuestCrud(actualLevelQuestConfig1.getId(), Locale.ENGLISH).read(questConfigId1);
        ReflectionAssert.assertReflectionEquals(expectedQuestConfig1, actualQuestConfig1);

        // Remove last and first
        serverGameEnginePersistence.getServerLevelQuestCrud().delete(serverLevelQuestConfigId4);
        serverGameEnginePersistence.getServerLevelQuestCrud().delete(serverLevelQuestConfigId1);
        // Verify
        try {
            TestHelper.assertObjectNameIds(serverGameEnginePersistence.getServerQuestCrud(actualLevelQuestConfig1.getId(), Locale.ENGLISH).readObjectNameIds());
            Assert.fail("IllegalStateException expected");
        } catch(IllegalStateException e) {
            // Expected
        }
        TestHelper.assertObjectNameIds(serverGameEnginePersistence.getServerLevelQuestCrud().readObjectNameIds(), "landnfas 2", "landnfas 33");
        serverLevelQuestConfigId2 = TestHelper.findIdForName(serverGameEnginePersistence.getServerLevelQuestCrud().readObjectNameIds(), "landnfas 2");
        actualLevelQuestConfig2 = serverGameEnginePersistence.getServerLevelQuestCrud().read(serverLevelQuestConfigId2);
        ReflectionAssert.assertReflectionEquals(expectedLevelQuestConfig2, actualLevelQuestConfig2);
        serverLevelQuestConfigId3 = TestHelper.findIdForName(serverGameEnginePersistence.getServerLevelQuestCrud().readObjectNameIds(), "landnfas 33");
        actualLevelQuestConfig3 = serverGameEnginePersistence.getServerLevelQuestCrud().read(serverLevelQuestConfigId3);
        ReflectionAssert.assertReflectionEquals(expectedLevelQuestConfig3, actualLevelQuestConfig3);

        // Remove second and third
        serverGameEnginePersistence.getServerLevelQuestCrud().delete(serverLevelQuestConfigId2);
        serverGameEnginePersistence.getServerLevelQuestCrud().delete(serverLevelQuestConfigId3);
        TestHelper.assertObjectNameIds(serverGameEnginePersistence.getServerLevelQuestCrud().readObjectNameIds());

        assertEmptyCount(ServerLevelQuestEntity.class);
        assertEmptyCountNative("SERVER_QUEST");
        assertEmptyCount(QuestConfigEntity.class);
        assertEmptyCount(ConditionConfigEntity.class);
        assertEmptyCount(ComparisonConfigEntity.class);
        assertEmptyCountNative("QUEST_COMPARISON_BASE_ITEM");
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

}