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
import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ServerLevelQuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.ComparisonConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.utils.CollectionUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

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
    }

    @Test
    public void onLevelUpRegister() throws Exception {
        String sessionId = sessionHolder.getPlayerSession().getHttpSessionId();
        userService.handleFacebookUserLogin("0000001");

        assertUser("0000001", LEVEL_1_ID, null);
        Assert.assertTrue(userService.findUserQuestForPlanet(serverGameEnginePersistence.readAllQuestIds()).entrySet().isEmpty());

        userService.onLevelUpdate(sessionId, LEVEL_2_ID);
        Assert.assertEquals(LEVEL_1_ID, userService.getUserContext().getLevelId());
        assertUser("0000001", LEVEL_1_ID, null);
        Assert.assertTrue(userService.findUserQuestForPlanet(serverGameEnginePersistence.readAllQuestIds()).entrySet().isEmpty());

        userService.onLevelUpdate(sessionId, LEVEL_3_ID);
        Assert.assertEquals(LEVEL_1_ID, userService.getUserContext().getLevelId());
        assertUser("0000001", LEVEL_1_ID, null);
        Assert.assertTrue(userService.findUserQuestForPlanet(serverGameEnginePersistence.readAllQuestIds()).entrySet().isEmpty());

        userService.onLevelUpdate(sessionId, LEVEL_4_ID);
        Assert.assertEquals(LEVEL_4_ID, userService.getUserContext().getLevelId());
        assertUser("0000001", LEVEL_4_ID, SERVER_QUEST_ID_1);

        Map<HumanPlayerId, QuestConfig> map = userService.findUserQuestForPlanet(serverGameEnginePersistence.readAllQuestIds());
        Assert.assertEquals(1, map.size());
        Map.Entry<HumanPlayerId, QuestConfig> entry = CollectionUtils.getFirst(map.entrySet());
        UserEntity userEntity = userService.getUserForFacebookId("0000001");
        Assert.assertEquals(userEntity.getId(), entry.getKey().getUserId());
        Assert.assertEquals(SERVER_QUEST_ID_1, entry.getValue().getId());

        assertCount(3, LevelHistoryEntity.class);
        assertCount(1, UserEntity.class);
    }

    @Test
    public void crud() throws Exception {
        cleanTable(ServerLevelQuestEntity.class);
        cleanTableNative("SERVER_QUEST");
        cleanTable(QuestConfigEntity.class);
        cleanTable(ConditionConfigEntity.class);
        cleanTable(ComparisonConfigEntity.class);
        cleanTableNative("QUEST_COMPARISON_BASE_ITEM");

        // Verify
        Assert.assertTrue(serverGameEnginePersistence.getServerLevelQuestCrud().readObjectNameIds().isEmpty());
        TestHelper.assertIds(serverGameEnginePersistence.readAllQuestIds());

        // Create first
        ServerLevelQuestConfig expectedLevelQuestConfig1 = serverGameEnginePersistence.getServerLevelQuestCrud().create();
        expectedLevelQuestConfig1.setMinimalLevelId(LEVEL_4_ID).setInternalName("landnfas 1");
        serverGameEnginePersistence.getServerLevelQuestCrud().update(expectedLevelQuestConfig1);
        // Verify
        TestHelper.assertObjectNameIds(serverGameEnginePersistence.getServerLevelQuestCrud().readObjectNameIds(), "landnfas 1");
        int serverLevelQuestConfigId1 = TestHelper.findIdForName(serverGameEnginePersistence.getServerLevelQuestCrud().readObjectNameIds(), "landnfas 1");
        ServerLevelQuestConfig actualLevelQuestConfig1 = serverGameEnginePersistence.getServerLevelQuestCrud().read(serverLevelQuestConfigId1);
        ReflectionAssert.assertReflectionEquals(expectedLevelQuestConfig1, actualLevelQuestConfig1);
        TestHelper.assertIds(serverGameEnginePersistence.readAllQuestIds());

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
        TestHelper.assertIds(serverGameEnginePersistence.readAllQuestIds());

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
        TestHelper.assertIds(serverGameEnginePersistence.readAllQuestIds());

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
        TestHelper.assertIds(serverGameEnginePersistence.readAllQuestIds(), expectedQuestConfig1.getId());

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
        TestHelper.assertIds(serverGameEnginePersistence.readAllQuestIds());

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