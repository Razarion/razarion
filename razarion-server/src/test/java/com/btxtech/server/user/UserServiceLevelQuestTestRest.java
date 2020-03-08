package com.btxtech.server.user;

import com.btxtech.server.IgnoreOldArquillianTest;
import com.btxtech.server.TestHelper;
import com.btxtech.server.persistence.quest.ComparisonConfigEntity;
import com.btxtech.server.persistence.quest.ConditionConfigEntity;
import com.btxtech.server.persistence.quest.QuestConfigEntity;
import com.btxtech.server.persistence.server.ServerGameEngineCrudPersistence;
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
public class UserServiceLevelQuestTestRest extends IgnoreOldArquillianTest {
    @Inject
    private ServerGameEngineCrudPersistence serverGameEngineCrudPersistence;

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
        Assert.assertTrue(serverGameEngineCrudPersistence.getServerLevelQuestCrud().readObjectNameIds().isEmpty());
        TestHelper.assertIds(serverGameEngineCrudPersistence.readAllQuestIds());

        // Create first
        ServerLevelQuestConfig expectedLevelQuestConfig1 = serverGameEngineCrudPersistence.getServerLevelQuestCrud().create();
        expectedLevelQuestConfig1.setMinimalLevelId(LEVEL_4_ID).setInternalName("landnfas 1");
        serverGameEngineCrudPersistence.getServerLevelQuestCrud().update(expectedLevelQuestConfig1);
        // Verify
        TestHelper.assertObjectNameIds(serverGameEngineCrudPersistence.getServerLevelQuestCrud().readObjectNameIds(), "landnfas 1");
        int serverLevelQuestConfigId1 = TestHelper.findIdForName(serverGameEngineCrudPersistence.getServerLevelQuestCrud().readObjectNameIds(), "landnfas 1");
        ServerLevelQuestConfig actualLevelQuestConfig1 = serverGameEngineCrudPersistence.getServerLevelQuestCrud().read(serverLevelQuestConfigId1);
        ReflectionAssert.assertReflectionEquals(expectedLevelQuestConfig1, actualLevelQuestConfig1);
        TestHelper.assertIds(serverGameEngineCrudPersistence.readAllQuestIds());

        // Create second
        ServerLevelQuestConfig expectedLevelQuestConfig2 = serverGameEngineCrudPersistence.getServerLevelQuestCrud().create();
        expectedLevelQuestConfig2.setMinimalLevelId(LEVEL_4_ID).setInternalName("landnfas 2");
        serverGameEngineCrudPersistence.getServerLevelQuestCrud().update(expectedLevelQuestConfig2);
        // Verify
        TestHelper.assertObjectNameIds(serverGameEngineCrudPersistence.getServerLevelQuestCrud().readObjectNameIds(), "landnfas 1", "landnfas 2");
        serverLevelQuestConfigId1 = TestHelper.findIdForName(serverGameEngineCrudPersistence.getServerLevelQuestCrud().readObjectNameIds(), "landnfas 1");
        actualLevelQuestConfig1 = serverGameEngineCrudPersistence.getServerLevelQuestCrud().read(serverLevelQuestConfigId1);
        ReflectionAssert.assertReflectionEquals(expectedLevelQuestConfig1, actualLevelQuestConfig1);
        int serverLevelQuestConfigId2 = TestHelper.findIdForName(serverGameEngineCrudPersistence.getServerLevelQuestCrud().readObjectNameIds(), "landnfas 2");
        ServerLevelQuestConfig actualLevelQuestConfig2 = serverGameEngineCrudPersistence.getServerLevelQuestCrud().read(serverLevelQuestConfigId2);
        ReflectionAssert.assertReflectionEquals(expectedLevelQuestConfig2, actualLevelQuestConfig2);
        TestHelper.assertIds(serverGameEngineCrudPersistence.readAllQuestIds());

        // Create third
        ServerLevelQuestConfig expectedLevelQuestConfig3 = serverGameEngineCrudPersistence.getServerLevelQuestCrud().create();
        expectedLevelQuestConfig3.setMinimalLevelId(LEVEL_4_ID).setInternalName("landnfas 33");
        serverGameEngineCrudPersistence.getServerLevelQuestCrud().update(expectedLevelQuestConfig3);
        // Verify
        TestHelper.assertObjectNameIds(serverGameEngineCrudPersistence.getServerLevelQuestCrud().readObjectNameIds(), "landnfas 1", "landnfas 2", "landnfas 33");
        serverLevelQuestConfigId1 = TestHelper.findIdForName(serverGameEngineCrudPersistence.getServerLevelQuestCrud().readObjectNameIds(), "landnfas 1");
        actualLevelQuestConfig1 = serverGameEngineCrudPersistence.getServerLevelQuestCrud().read(serverLevelQuestConfigId1);
        ReflectionAssert.assertReflectionEquals(expectedLevelQuestConfig1, actualLevelQuestConfig1);
        serverLevelQuestConfigId2 = TestHelper.findIdForName(serverGameEngineCrudPersistence.getServerLevelQuestCrud().readObjectNameIds(), "landnfas 2");
        actualLevelQuestConfig2 = serverGameEngineCrudPersistence.getServerLevelQuestCrud().read(serverLevelQuestConfigId2);
        ReflectionAssert.assertReflectionEquals(expectedLevelQuestConfig2, actualLevelQuestConfig2);
        int serverLevelQuestConfigId3 = TestHelper.findIdForName(serverGameEngineCrudPersistence.getServerLevelQuestCrud().readObjectNameIds(), "landnfas 33");
        ServerLevelQuestConfig actualLevelQuestConfig3 = serverGameEngineCrudPersistence.getServerLevelQuestCrud().read(serverLevelQuestConfigId3);
        ReflectionAssert.assertReflectionEquals(expectedLevelQuestConfig3, actualLevelQuestConfig3);
        TestHelper.assertIds(serverGameEngineCrudPersistence.readAllQuestIds());

        // Create third
        ServerLevelQuestConfig expectedLevelQuestConfig4 = serverGameEngineCrudPersistence.getServerLevelQuestCrud().create();
        expectedLevelQuestConfig4.setMinimalLevelId(LEVEL_5_ID).setInternalName("landnfas 444");
        serverGameEngineCrudPersistence.getServerLevelQuestCrud().update(expectedLevelQuestConfig4);
        // Verify
        TestHelper.assertObjectNameIds(serverGameEngineCrudPersistence.getServerLevelQuestCrud().readObjectNameIds(), "landnfas 1", "landnfas 2", "landnfas 33", "landnfas 444");
        serverLevelQuestConfigId1 = TestHelper.findIdForName(serverGameEngineCrudPersistence.getServerLevelQuestCrud().readObjectNameIds(), "landnfas 1");
        actualLevelQuestConfig1 = serverGameEngineCrudPersistence.getServerLevelQuestCrud().read(serverLevelQuestConfigId1);
        ReflectionAssert.assertReflectionEquals(expectedLevelQuestConfig1, actualLevelQuestConfig1);
        serverLevelQuestConfigId2 = TestHelper.findIdForName(serverGameEngineCrudPersistence.getServerLevelQuestCrud().readObjectNameIds(), "landnfas 2");
        actualLevelQuestConfig2 = serverGameEngineCrudPersistence.getServerLevelQuestCrud().read(serverLevelQuestConfigId2);
        ReflectionAssert.assertReflectionEquals(expectedLevelQuestConfig2, actualLevelQuestConfig2);
        serverLevelQuestConfigId3 = TestHelper.findIdForName(serverGameEngineCrudPersistence.getServerLevelQuestCrud().readObjectNameIds(), "landnfas 33");
        actualLevelQuestConfig3 = serverGameEngineCrudPersistence.getServerLevelQuestCrud().read(serverLevelQuestConfigId3);
        ReflectionAssert.assertReflectionEquals(expectedLevelQuestConfig3, actualLevelQuestConfig3);
        int serverLevelQuestConfigId4 = TestHelper.findIdForName(serverGameEngineCrudPersistence.getServerLevelQuestCrud().readObjectNameIds(), "landnfas 444");
        ServerLevelQuestConfig actualLevelQuestConfig4 = serverGameEngineCrudPersistence.getServerLevelQuestCrud().read(serverLevelQuestConfigId4);
        ReflectionAssert.assertReflectionEquals(expectedLevelQuestConfig4, actualLevelQuestConfig4);

        // Add first quests
        QuestConfig expectedQuestConfig1 = serverGameEngineCrudPersistence.getServerQuestCrud(actualLevelQuestConfig1.getId(), Locale.ENGLISH).create();
        expectedQuestConfig1.setInternalName("dsfdsf 1");
        serverGameEngineCrudPersistence.getServerQuestCrud(actualLevelQuestConfig1.getId(), Locale.ENGLISH).update(expectedQuestConfig1);
        // Verify quest
        TestHelper.assertObjectNameIds(serverGameEngineCrudPersistence.getServerQuestCrud(actualLevelQuestConfig1.getId(), Locale.ENGLISH).readObjectNameIds(), "dsfdsf 1");
        int questConfigId1 = TestHelper.findIdForName(serverGameEngineCrudPersistence.getServerQuestCrud(actualLevelQuestConfig1.getId(), Locale.ENGLISH).readObjectNameIds(), "dsfdsf 1");
        QuestConfig actualQuestConfig1 = serverGameEngineCrudPersistence.getServerQuestCrud(actualLevelQuestConfig1.getId(), Locale.ENGLISH).read(questConfigId1);
        ReflectionAssert.assertReflectionEquals(expectedQuestConfig1, actualQuestConfig1);
        TestHelper.assertIds(serverGameEngineCrudPersistence.readAllQuestIds(), expectedQuestConfig1.getId());
        // Add second quests
        QuestConfig expectedQuestConfig2 = serverGameEngineCrudPersistence.getServerQuestCrud(actualLevelQuestConfig1.getId(), Locale.ENGLISH).create();
        expectedQuestConfig2.setInternalName("dsfdsf 2");
        serverGameEngineCrudPersistence.getServerQuestCrud(actualLevelQuestConfig1.getId(), Locale.ENGLISH).update(expectedQuestConfig2);
        // Verify quest
        TestHelper.assertOrderedObjectNameIds(serverGameEngineCrudPersistence.getServerQuestCrud(actualLevelQuestConfig1.getId(), Locale.ENGLISH).readObjectNameIds(), "dsfdsf 1", "dsfdsf 2");
        // Swap
        serverGameEngineCrudPersistence.getServerQuestCrud(actualLevelQuestConfig1.getId(), Locale.ENGLISH).swap(0, 1);
        TestHelper.assertOrderedObjectNameIds(serverGameEngineCrudPersistence.getServerQuestCrud(actualLevelQuestConfig1.getId(), Locale.ENGLISH).readObjectNameIds(), "dsfdsf 2", "dsfdsf 1");
        // Delete
        serverGameEngineCrudPersistence.getServerQuestCrud(actualLevelQuestConfig1.getId(), Locale.ENGLISH).delete(expectedQuestConfig2.getId());
        // Verify
        TestHelper.assertOrderedObjectNameIds(serverGameEngineCrudPersistence.getServerQuestCrud(actualLevelQuestConfig1.getId(), Locale.ENGLISH).readObjectNameIds(), "dsfdsf 1");

        // Remove last and first
        serverGameEngineCrudPersistence.getServerLevelQuestCrud().delete(serverLevelQuestConfigId4);
        serverGameEngineCrudPersistence.getServerLevelQuestCrud().delete(serverLevelQuestConfigId1);
        // Verify
        try {
            TestHelper.assertObjectNameIds(serverGameEngineCrudPersistence.getServerQuestCrud(actualLevelQuestConfig1.getId(), Locale.ENGLISH).readObjectNameIds());
            Assert.fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
            // Expected
        }
        TestHelper.assertObjectNameIds(serverGameEngineCrudPersistence.getServerLevelQuestCrud().readObjectNameIds(), "landnfas 2", "landnfas 33");
        serverLevelQuestConfigId2 = TestHelper.findIdForName(serverGameEngineCrudPersistence.getServerLevelQuestCrud().readObjectNameIds(), "landnfas 2");
        actualLevelQuestConfig2 = serverGameEngineCrudPersistence.getServerLevelQuestCrud().read(serverLevelQuestConfigId2);
        ReflectionAssert.assertReflectionEquals(expectedLevelQuestConfig2, actualLevelQuestConfig2);
        serverLevelQuestConfigId3 = TestHelper.findIdForName(serverGameEngineCrudPersistence.getServerLevelQuestCrud().readObjectNameIds(), "landnfas 33");
        actualLevelQuestConfig3 = serverGameEngineCrudPersistence.getServerLevelQuestCrud().read(serverLevelQuestConfigId3);
        ReflectionAssert.assertReflectionEquals(expectedLevelQuestConfig3, actualLevelQuestConfig3);
        TestHelper.assertIds(serverGameEngineCrudPersistence.readAllQuestIds());

        // Remove second and third
        serverGameEngineCrudPersistence.getServerLevelQuestCrud().delete(serverLevelQuestConfigId2);
        serverGameEngineCrudPersistence.getServerLevelQuestCrud().delete(serverLevelQuestConfigId3);
        TestHelper.assertObjectNameIds(serverGameEngineCrudPersistence.getServerLevelQuestCrud().readObjectNameIds());

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
        ServerLevelQuestConfig levelQuestConfig1 = serverGameEngineCrudPersistence.getServerLevelQuestCrud().create();
        levelQuestConfig1.setMinimalLevelId(LEVEL_4_ID).setInternalName("landnfas 1");
        serverGameEngineCrudPersistence.getServerLevelQuestCrud().update(levelQuestConfig1);

        // Add quests
        QuestConfig expectedQuestConfig1 = serverGameEngineCrudPersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).create();
        expectedQuestConfig1.setInternalName("dsfdsf 1");
        serverGameEngineCrudPersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).update(expectedQuestConfig1);
        QuestConfig expectedQuestConfig2 = serverGameEngineCrudPersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).create();
        expectedQuestConfig2.setInternalName("dsfdsf 2");
        serverGameEngineCrudPersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).update(expectedQuestConfig2);
        QuestConfig expectedQuestConfig3 = serverGameEngineCrudPersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).create();
        expectedQuestConfig3.setInternalName("dsfdsf 3");
        serverGameEngineCrudPersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).update(expectedQuestConfig3);
        QuestConfig expectedQuestConfig4 = serverGameEngineCrudPersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).create();
        expectedQuestConfig4.setInternalName("dsfdsf 4");
        serverGameEngineCrudPersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).update(expectedQuestConfig4);
        QuestConfig expectedQuestConfig5 = serverGameEngineCrudPersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).create();
        expectedQuestConfig5.setInternalName("dsfdsf 5");
        serverGameEngineCrudPersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).update(expectedQuestConfig5);

        // Verify
        TestHelper.assertOrderedObjectNameIds(serverGameEngineCrudPersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).readObjectNameIds(), "dsfdsf 1", "dsfdsf 2", "dsfdsf 3", "dsfdsf 4", "dsfdsf 5");

        // Swap
        serverGameEngineCrudPersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).swap(0, 4);

        // Verify
        TestHelper.assertOrderedObjectNameIds(serverGameEngineCrudPersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).readObjectNameIds(), "dsfdsf 5", "dsfdsf 2", "dsfdsf 3", "dsfdsf 4", "dsfdsf 1");

        // Delete
        serverGameEngineCrudPersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).delete(expectedQuestConfig1.getId());

        // Verify
        TestHelper.assertOrderedObjectNameIds(serverGameEngineCrudPersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).readObjectNameIds(), "dsfdsf 5", "dsfdsf 2", "dsfdsf 3", "dsfdsf 4");

        // Swap
        serverGameEngineCrudPersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).swap(1, 3);

        // Verify
        TestHelper.assertOrderedObjectNameIds(serverGameEngineCrudPersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).readObjectNameIds(), "dsfdsf 5", "dsfdsf 4", "dsfdsf 3", "dsfdsf 2");

        // Delete
        serverGameEngineCrudPersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).delete(expectedQuestConfig3.getId());

        // Verify
        TestHelper.assertOrderedObjectNameIds(serverGameEngineCrudPersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).readObjectNameIds(), "dsfdsf 5", "dsfdsf 4", "dsfdsf 2");

        // Delete
        serverGameEngineCrudPersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).delete(expectedQuestConfig5.getId());

        // Verify
        TestHelper.assertOrderedObjectNameIds(serverGameEngineCrudPersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).readObjectNameIds(), "dsfdsf 4", "dsfdsf 2");

        // Swap
        serverGameEngineCrudPersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).swap(0, 1);

        // Verify
        TestHelper.assertOrderedObjectNameIds(serverGameEngineCrudPersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).readObjectNameIds(), "dsfdsf 2", "dsfdsf 4");

        // Delete
        serverGameEngineCrudPersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).delete(expectedQuestConfig2.getId());

        // Verify
        TestHelper.assertOrderedObjectNameIds(serverGameEngineCrudPersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).readObjectNameIds(), "dsfdsf 4");

        // Delete
        serverGameEngineCrudPersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).delete(expectedQuestConfig4.getId());

        // Verify
        TestHelper.assertOrderedObjectNameIds(serverGameEngineCrudPersistence.getServerQuestCrud(levelQuestConfig1.getId(), Locale.ENGLISH).readObjectNameIds());

        serverGameEngineCrudPersistence.getServerLevelQuestCrud().delete(levelQuestConfig1.getId());

        assertEmptyCount(ServerLevelQuestEntity.class);
        assertEmptyCountNative("SERVER_QUEST");
        assertEmptyCount(QuestConfigEntity.class);
        assertEmptyCount(ConditionConfigEntity.class);
        assertEmptyCount(ComparisonConfigEntity.class);
        assertEmptyCountNative("QUEST_COMPARISON_BASE_ITEM");

    }
}