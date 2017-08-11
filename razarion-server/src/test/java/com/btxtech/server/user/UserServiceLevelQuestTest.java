package com.btxtech.server.user;

import com.btxtech.server.ArquillianBaseTest;
import com.btxtech.server.TestHelper;
import com.btxtech.server.persistence.quest.ComparisonConfigEntity;
import com.btxtech.server.persistence.quest.ConditionConfigEntity;
import com.btxtech.server.persistence.quest.QuestConfigEntity;
import com.btxtech.server.persistence.server.ServerGameEnginePersistence;
import com.btxtech.server.persistence.server.ServerLevelQuestEntity;
import com.btxtech.shared.dto.ServerLevelQuestConfig;
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
    private ServerGameEnginePersistence serverGameEnginePersistence;

    @Before
    public void before() throws Exception {
        setupPlanets();
    }

    @After
    public void after() throws Exception {
        cleanPlanets();
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

        // Add first quests
        QuestConfig expectedQuestConfig1 = serverGameEnginePersistence.getServerQuestCrud(actualLevelQuestConfig1.getId(), Locale.ENGLISH).create();
        expectedQuestConfig1.setInternalName("dsfdsf 1");
        serverGameEnginePersistence.getServerQuestCrud(actualLevelQuestConfig1.getId(), Locale.ENGLISH).update(expectedQuestConfig1);
        // Verify quest
        TestHelper.assertObjectNameIds(serverGameEnginePersistence.getServerQuestCrud(actualLevelQuestConfig1.getId(), Locale.ENGLISH).readObjectNameIds(), "dsfdsf 1");
        int questConfigId1 = TestHelper.findIdForName(serverGameEnginePersistence.getServerQuestCrud(actualLevelQuestConfig1.getId(), Locale.ENGLISH).readObjectNameIds(), "dsfdsf 1");
        QuestConfig actualQuestConfig1 = serverGameEnginePersistence.getServerQuestCrud(actualLevelQuestConfig1.getId(), Locale.ENGLISH).read(questConfigId1);
        ReflectionAssert.assertReflectionEquals(expectedQuestConfig1, actualQuestConfig1);
        TestHelper.assertIds(serverGameEnginePersistence.readAllQuestIds(), expectedQuestConfig1.getId());
        // Add second quests
        QuestConfig expectedQuestConfig2 = serverGameEnginePersistence.getServerQuestCrud(actualLevelQuestConfig1.getId(), Locale.ENGLISH).create();
        expectedQuestConfig2.setInternalName("dsfdsf 2");
        serverGameEnginePersistence.getServerQuestCrud(actualLevelQuestConfig1.getId(), Locale.ENGLISH).update(expectedQuestConfig2);
        // Verify quest
        TestHelper.assertOrderedObjectNameIds(serverGameEnginePersistence.getServerQuestCrud(actualLevelQuestConfig1.getId(), Locale.ENGLISH).readObjectNameIds(), "dsfdsf 1", "dsfdsf 2");
        // Swap
        serverGameEnginePersistence.getServerQuestCrud(actualLevelQuestConfig1.getId(), Locale.ENGLISH).swap(0, 1);
        TestHelper.assertOrderedObjectNameIds(serverGameEnginePersistence.getServerQuestCrud(actualLevelQuestConfig1.getId(), Locale.ENGLISH).readObjectNameIds(), "dsfdsf 2", "dsfdsf 1");
        // Delete
        serverGameEnginePersistence.getServerQuestCrud(actualLevelQuestConfig1.getId(), Locale.ENGLISH).delete(expectedQuestConfig2.getId());
        // Verify
        TestHelper.assertOrderedObjectNameIds(serverGameEnginePersistence.getServerQuestCrud(actualLevelQuestConfig1.getId(), Locale.ENGLISH).readObjectNameIds(), "dsfdsf 1");

        // Remove last and first
        serverGameEnginePersistence.getServerLevelQuestCrud().delete(serverLevelQuestConfigId4);
        serverGameEnginePersistence.getServerLevelQuestCrud().delete(serverLevelQuestConfigId1);
        // Verify
        try {
            TestHelper.assertObjectNameIds(serverGameEnginePersistence.getServerQuestCrud(actualLevelQuestConfig1.getId(), Locale.ENGLISH).readObjectNameIds());
            Assert.fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
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

    @Test
    public void crudQuests() throws Exception {
        cleanTableNative("SERVER_QUEST");
        cleanTable(ServerLevelQuestEntity.class);
        cleanTable(QuestConfigEntity.class);
        cleanTable(ConditionConfigEntity.class);
        cleanTable(ComparisonConfigEntity.class);
        cleanTableNative("QUEST_COMPARISON_BASE_ITEM");

        // Create first ServerLevel
        ServerLevelQuestConfig levelQuestConfig1 = serverGameEnginePersistence.getServerLevelQuestCrud().create();
        levelQuestConfig1.setMinimalLevelId(LEVEL_4_ID).setInternalName("landnfas 1");
        serverGameEnginePersistence.getServerLevelQuestCrud().update(levelQuestConfig1);

        // Add quests
        QuestConfig expectedQuestConfig1 = serverGameEnginePersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).create();
        expectedQuestConfig1.setInternalName("dsfdsf 1");
        serverGameEnginePersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).update(expectedQuestConfig1);
        QuestConfig expectedQuestConfig2 = serverGameEnginePersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).create();
        expectedQuestConfig2.setInternalName("dsfdsf 2");
        serverGameEnginePersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).update(expectedQuestConfig2);
        QuestConfig expectedQuestConfig3 = serverGameEnginePersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).create();
        expectedQuestConfig3.setInternalName("dsfdsf 3");
        serverGameEnginePersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).update(expectedQuestConfig3);
        QuestConfig expectedQuestConfig4 = serverGameEnginePersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).create();
        expectedQuestConfig4.setInternalName("dsfdsf 4");
        serverGameEnginePersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).update(expectedQuestConfig4);
        QuestConfig expectedQuestConfig5 = serverGameEnginePersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).create();
        expectedQuestConfig5.setInternalName("dsfdsf 5");
        serverGameEnginePersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).update(expectedQuestConfig5);

        // Verify
        TestHelper.assertOrderedObjectNameIds(serverGameEnginePersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).readObjectNameIds(), "dsfdsf 1", "dsfdsf 2", "dsfdsf 3", "dsfdsf 4", "dsfdsf 5");

        // Swap
        serverGameEnginePersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).swap(0, 4);

        // Verify
        TestHelper.assertOrderedObjectNameIds(serverGameEnginePersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).readObjectNameIds(), "dsfdsf 5", "dsfdsf 2", "dsfdsf 3", "dsfdsf 4", "dsfdsf 1");

        // Delete
        serverGameEnginePersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).delete(expectedQuestConfig1.getId());

        // Verify
        TestHelper.assertOrderedObjectNameIds(serverGameEnginePersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).readObjectNameIds(), "dsfdsf 5", "dsfdsf 2", "dsfdsf 3", "dsfdsf 4");

        // Swap
        serverGameEnginePersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).swap(1, 3);

        // Verify
        TestHelper.assertOrderedObjectNameIds(serverGameEnginePersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).readObjectNameIds(), "dsfdsf 5", "dsfdsf 4", "dsfdsf 3", "dsfdsf 2");

        // Delete
        serverGameEnginePersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).delete(expectedQuestConfig3.getId());

        // Verify
        TestHelper.assertOrderedObjectNameIds(serverGameEnginePersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).readObjectNameIds(), "dsfdsf 5", "dsfdsf 4", "dsfdsf 2");

        // Delete
        serverGameEnginePersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).delete(expectedQuestConfig5.getId());

        // Verify
        TestHelper.assertOrderedObjectNameIds(serverGameEnginePersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).readObjectNameIds(), "dsfdsf 4", "dsfdsf 2");

        // Swap
        serverGameEnginePersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).swap(0, 1);

        // Verify
        TestHelper.assertOrderedObjectNameIds(serverGameEnginePersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).readObjectNameIds(), "dsfdsf 2", "dsfdsf 4");

        // Delete
        serverGameEnginePersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).delete(expectedQuestConfig2.getId());

        // Verify
        TestHelper.assertOrderedObjectNameIds(serverGameEnginePersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).readObjectNameIds(), "dsfdsf 4");

        // Delete
        serverGameEnginePersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).delete(expectedQuestConfig4.getId());

        // Verify
        TestHelper.assertOrderedObjectNameIds(serverGameEnginePersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).readObjectNameIds());

        serverGameEnginePersistence.getServerLevelQuestCrud().delete(levelQuestConfig1.getId());

        assertEmptyCount(ServerLevelQuestEntity.class);
        assertEmptyCountNative("SERVER_QUEST");
        assertEmptyCount(QuestConfigEntity.class);
        assertEmptyCount(ConditionConfigEntity.class);
        assertEmptyCount(ComparisonConfigEntity.class);
        assertEmptyCountNative("QUEST_COMPARISON_BASE_ITEM");

    }
}