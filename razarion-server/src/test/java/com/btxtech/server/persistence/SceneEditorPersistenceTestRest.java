package com.btxtech.server.persistence;

import com.btxtech.server.RestServerTestBase;
import com.btxtech.server.TestHelper;
import com.btxtech.server.persistence.quest.ComparisonConfigEntity;
import com.btxtech.server.persistence.quest.ConditionConfigEntity;
import com.btxtech.server.persistence.quest.QuestConfigEntity;
import com.btxtech.server.persistence.server.ServerLevelQuestEntity;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.dto.BaseItemPlacerConfig;
import com.btxtech.shared.dto.BotAttackCommandConfig;
import com.btxtech.shared.dto.BotHarvestCommandConfig;
import com.btxtech.shared.dto.BotKillHumanCommandConfig;
import com.btxtech.shared.dto.BotKillOtherBotCommandConfig;
import com.btxtech.shared.dto.BotMoveCommandConfig;
import com.btxtech.shared.dto.BoxItemPosition;
import com.btxtech.shared.dto.GameTipConfig;
import com.btxtech.shared.dto.KillBotCommandConfig;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.ResourceItemPosition;
import com.btxtech.shared.dto.SceneConfig;
import com.btxtech.shared.dto.ScrollUiQuest;
import com.btxtech.shared.dto.ViewFieldConfig;
import com.btxtech.shared.gameengine.datatypes.config.ComparisonConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestDescriptionConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotEnragementStateConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;
import org.unitils.reflectionassert.comparator.impl.ObjectComparatorIgnore;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 16.05.2017.
 */
public class SceneEditorPersistenceTestRest extends RestServerTestBase {
    private static final int NPC_BOT_OUTPOST_AUX = 11;
    private static final int NPC_BOT_OUTPOST_2_AUX = 22;
    private static final int ENEMY_BOT_AUX = 33;
    private static final int NPC_BOT_INSTRUCTOR_AUX = 44;
    @Inject
    private SceneEditorPersistence sceneEditorPersistence;
    @Inject
    private GameUiControlConfigPersistence gameUiControlConfigPersistence;

    @Before
    public void before() throws Exception {
        setupPlanets();
    }

    @After
    public void after() throws Exception {
        cleanPlanets();
    }

    @Test
    public void testSceneConfigCrud() throws Exception {
        ObjectComparatorIgnore.add(BotConfig.class, "id");
        ObjectComparatorIgnore.add(QuestDescriptionConfig.class, "id");

        // Cleanup
        cleanTable(ServerLevelQuestEntity.class);
        cleanTableNative("SERVER_QUEST");
        cleanTable(QuestConfigEntity.class);
        cleanTable(ConditionConfigEntity.class);
        cleanTable(ComparisonConfigEntity.class);
        cleanTableNative("QUEST_COMPARISON_BASE_ITEM");
        List<ObjectNameId> objectNameIds = sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds();
        for (ObjectNameId objectNameId : objectNameIds) {
            sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).delete(objectNameId.getId());
        }

        // Create first scene
        SceneConfig expectedScene1 = sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).create();
        setResources(expectedScene1);
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).update(expectedScene1);
        // Verify
        TestHelper.assertOrderedObjectNameIds(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "setup: add resources");
        int id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "setup: add resources");
        ReflectionAssert.assertReflectionEquals(expectedScene1, sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).read(id));
        // Create second scene
        SceneConfig expectedScene2 = sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).create();
        setUserSpawnScene(expectedScene2);
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).update(expectedScene2);
        // Verify
        TestHelper.assertOrderedObjectNameIds(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "setup: add resources", "user: spawn 1");
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "setup: add resources");
        ReflectionAssert.assertReflectionEquals(expectedScene1, sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).read(id));
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "user: spawn 1");
        ReflectionAssert.assertReflectionEquals(expectedScene2, sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).read(id));
        // Delete first
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "setup: add resources");
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).delete(id);
        // Verify
        TestHelper.assertOrderedObjectNameIds(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "user: spawn 1");
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "user: spawn 1");
        ReflectionAssert.assertReflectionEquals(expectedScene2, sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).read(id));
        // Delete second
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "user: spawn 1");
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).delete(id);
        // Verify
        TestHelper.assertOrderedObjectNameIds(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds());
        // Create new
        SceneConfig expectedScene3 = sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).create();
        setScrollOverTerrain(expectedScene3);
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).update(expectedScene3);
        // Verify
        TestHelper.assertOrderedObjectNameIds(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "script: scroll over terrain");
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "script: scroll over terrain");
        ReflectionAssert.assertReflectionEquals(expectedScene3, sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).read(id));
        // Create Box
        SceneConfig expectedScene4 = sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).create();
        setPickBoxTask(expectedScene4);
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).update(expectedScene4);
        // Verify
        TestHelper.assertOrderedObjectNameIds(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "script: scroll over terrain", "user: pick box");
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "script: scroll over terrain");
        ReflectionAssert.assertReflectionEquals(expectedScene3, sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).read(id));
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "user: pick box");
        ReflectionAssert.assertReflectionEquals(expectedScene4, sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).read(id));
        // Swap
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).swap(0, 1);
        // Verify
        TestHelper.assertOrderedObjectNameIds(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "user: pick box", "script: scroll over terrain");
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "script: scroll over terrain");
        ReflectionAssert.assertReflectionEquals(expectedScene3, sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).read(id));
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "user: pick box");
        ReflectionAssert.assertReflectionEquals(expectedScene4, sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).read(id));
        // Delete
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "script: scroll over terrain");
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).delete(id);
        // Verify
        TestHelper.assertOrderedObjectNameIds(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "user: pick box");
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "user: pick box");
        ReflectionAssert.assertReflectionEquals(expectedScene4, sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).read(id));
        // Delete
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "user: pick box");
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).delete(id);
        // Create
        SceneConfig expectedScene5 = sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).create();
        setUserMoveScene(expectedScene5);
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).update(expectedScene5);
        SceneConfig expectedScene6 = sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).create();
        setFindEnemyBase(expectedScene6);
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).update(expectedScene6);
        SceneConfig expectedScene7 = sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).create();
        setBoxSpawnTask(expectedScene7);
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).update(expectedScene7);
        SceneConfig expectedScene8 = sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).create();
        setAttackTask(expectedScene8);
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).update(expectedScene8);
        SceneConfig expectedScene9 = sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).create();
        setUserSpawnScene2(expectedScene9);
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).update(expectedScene9);
        SceneConfig expectedScene10 = sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).create();
        setBuildFactoryTask(expectedScene10);
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).update(expectedScene10);
        SceneConfig expectedScene11 = sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).create();
        setFactorizeHarvesterTask(expectedScene11);
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).update(expectedScene11);
        SceneConfig expectedScene12 = sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).create();
        setHarvestTask(expectedScene12);
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).update(expectedScene12);
        SceneConfig expectedScene13 = sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).create();
        setBuildViperTask(expectedScene13);
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).update(expectedScene13);
        SceneConfig expectedScene14 = sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).create();
        setBuildViperTask2(expectedScene14);
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).update(expectedScene14);
        SceneConfig expectedScene15 = sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).create();
        setKillTower(expectedScene15);
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).update(expectedScene15);
        // Verify
        TestHelper.assertOrderedObjectNameIds(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(),
                "user: move", "user: find enemy base", "user: box spawn", "user: kill bot harvester", "user: spawn 2", "user: build factory", "user: fabricate harvester",
                "user: harvest", "user: build viper 1", "user: build viper 2", "user: kill tower");
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "user: move");
        ReflectionAssert.assertReflectionEquals(expectedScene5, sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).read(id));
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "user: find enemy base");
        ReflectionAssert.assertReflectionEquals(expectedScene6, sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).read(id));
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "user: box spawn");
        ReflectionAssert.assertReflectionEquals(expectedScene7, sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).read(id));
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "user: kill bot harvester");
        ReflectionAssert.assertReflectionEquals(expectedScene8, sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).read(id));
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "user: spawn 2");
        ReflectionAssert.assertReflectionEquals(expectedScene9, sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).read(id));
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "user: build factory");
        ReflectionAssert.assertReflectionEquals(expectedScene10, sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).read(id));
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "user: fabricate harvester");
        ReflectionAssert.assertReflectionEquals(expectedScene11, sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).read(id));
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "user: harvest");
        ReflectionAssert.assertReflectionEquals(expectedScene12, sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).read(id));
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "user: build viper 1");
        ReflectionAssert.assertReflectionEquals(expectedScene13, sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).read(id));
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "user: build viper 2");
        ReflectionAssert.assertReflectionEquals(expectedScene14, sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).read(id));
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "user: kill tower");
        ReflectionAssert.assertReflectionEquals(expectedScene15, sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).read(id));
        // Delete
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "user: move");
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).delete(id);
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "user: find enemy base");
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).delete(id);
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "user: box spawn");
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).delete(id);
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "user: kill bot harvester");
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).delete(id);
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "user: spawn 2");
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).delete(id);
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "user: build factory");
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).delete(id);
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "user: fabricate harvester");
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).delete(id);
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "user: harvest");
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).delete(id);
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "user: build viper 1");
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).delete(id);
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "user: build viper 2");
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).delete(id);
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "user: kill tower");
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).delete(id);
        // Verify
        TestHelper.assertOrderedObjectNameIds(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds());
        // Create
        SceneConfig expectedScene16 = sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).create();
        setNpcBot(expectedScene16);
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).update(expectedScene16);
        SceneConfig expectedScene17 = sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).create();
        setEnemyBot(expectedScene17);
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).update(expectedScene17);
        SceneConfig expectedScene18 = sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).create();
        setBotSpawnScene(expectedScene18);
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).update(expectedScene18);
        SceneConfig expectedScene19 = sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).create();
        setBotMoveScene(expectedScene19);
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).update(expectedScene19);
        SceneConfig expectedScene20 = sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).create();
        setNpcHarvestAttack(expectedScene20);
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).update(expectedScene20);
        SceneConfig expectedScene21 = sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).create();
        setEnemyKillTask(expectedScene21);
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).update(expectedScene21);
        SceneConfig expectedScene22 = sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).create();
        setNpcEscapeTask(expectedScene22);
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).update(expectedScene22);
        SceneConfig expectedScene23 = sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).create();
        setNpcAttackTowerCommand(expectedScene23);
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).update(expectedScene23);
        // Verify
        TestHelper.assertOrderedObjectNameIds(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "setup: add NPC bot", "setup: add enemy bot"
                , "script: npc bot spawn", "script: npc bot move", "script: bot harvest attack", "script: enemy bot destroy user", "script: escape npc bot", "script: npc bot attacks tower");
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "setup: add NPC bot");
        ReflectionAssert.assertReflectionEquals(expectedScene16, sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).read(id));
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "setup: add enemy bot");
        ReflectionAssert.assertReflectionEquals(expectedScene17, sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).read(id));
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "script: npc bot spawn");
        ReflectionAssert.assertReflectionEquals(expectedScene18, sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).read(id));
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "script: npc bot move");
        ReflectionAssert.assertReflectionEquals(expectedScene19, sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).read(id));
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "script: bot harvest attack");
        ReflectionAssert.assertReflectionEquals(expectedScene20, sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).read(id));
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "script: enemy bot destroy user");
        ReflectionAssert.assertReflectionEquals(expectedScene21, sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).read(id));
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "script: escape npc bot");
        ReflectionAssert.assertReflectionEquals(expectedScene22, sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).read(id));
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "script: npc bot attacks tower");
        ReflectionAssert.assertReflectionEquals(expectedScene23, sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).read(id));
        // Delete
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "setup: add NPC bot");
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).delete(id);
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "setup: add enemy bot");
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).delete(id);
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "script: npc bot spawn");
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).delete(id);
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "script: npc bot move");
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).delete(id);
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "script: bot harvest attack");
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).delete(id);
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "script: enemy bot destroy user");
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).delete(id);
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "script: escape npc bot");
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).delete(id);
        id = TestHelper.findIdForName(sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).readObjectNameIds(), "script: npc bot attacks tower");
        sceneEditorPersistence.getSceneConfigCrud(GAME_UI_CONTROL_CONFIG_1_ID).delete(id);

        ObjectComparatorIgnore.clear();

        Assert.assertEquals(0, ((Number) getEntityManager().createNativeQuery("SELECT COUNT(*) FROM SCENE_BOT").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) getEntityManager().createQuery("SELECT COUNT(r) FROM ResourceItemPositionEntity r").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) getEntityManager().createQuery("SELECT COUNT(b) FROM BoxItemPositionEntity b").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) getEntityManager().createQuery("SELECT COUNT(b) FROM BoxItemPositionEntity b").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) getEntityManager().createQuery("SELECT COUNT(b) FROM BotAttackCommandEntity b").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) getEntityManager().createQuery("SELECT COUNT(b) FROM BotHarvestCommandEntity b").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) getEntityManager().createQuery("SELECT COUNT(b) FROM BotKillBotCommandEntity b").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) getEntityManager().createQuery("SELECT COUNT(b) FROM BotKillHumanCommandEntity b").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) getEntityManager().createQuery("SELECT COUNT(b) FROM BotKillOtherBotCommandEntity b").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) getEntityManager().createQuery("SELECT COUNT(b) FROM BotMoveCommandEntity b").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) getEntityManager().createQuery("SELECT COUNT(s) FROM SceneEntity s").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) getEntityManager().createQuery("SELECT COUNT(b) FROM BotRemoveOwnItemCommandEntity b").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) getEntityManager().createQuery("SELECT COUNT(g) FROM GameTipConfigEntity g").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) getEntityManager().createNativeQuery("SELECT COUNT(*) FROM SCENE_START_PLACE_ALLOWED_AREA").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) getEntityManager().createNativeQuery("SELECT COUNT(*) FROM QUEST_COMPARISON_BASE_ITEM").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) getEntityManager().createQuery("SELECT COUNT(q) FROM QuestConfigEntity q").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) getEntityManager().createQuery("SELECT COUNT(c) FROM ConditionConfigEntity c").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) getEntityManager().createQuery("SELECT COUNT(c) FROM ComparisonConfigEntity c").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) getEntityManager().createNativeQuery("SELECT COUNT(*) FROM SCENE_START_PLACE_ALLOWED_AREA").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) getEntityManager().createQuery("SELECT COUNT(s) FROM StartPointPlacerEntity s").getSingleResult()).intValue());
        // Bots
        Assert.assertEquals(0, ((Number) getEntityManager().createNativeQuery("SELECT COUNT(*) FROM BOT_CONFIG_BOT_ITEM").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) getEntityManager().createNativeQuery("SELECT COUNT(*) FROM BOT_CONFIG_ENRAGEMENT_STATE_CONFIG").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) getEntityManager().createQuery("SELECT COUNT(b) FROM BotConfigEntity b").getSingleResult()).intValue());
        // I18n bundles
        Assert.assertEquals(0, ((Number) getEntityManager().createNativeQuery("SELECT COUNT(*) FROM I18N_BUNDLE_STRING").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) getEntityManager().createQuery("SELECT COUNT(i) FROM I18N_BUNDLE i").getSingleResult()).intValue());
        // Place
        Assert.assertEquals(0, ((Number) getEntityManager().createNativeQuery("SELECT COUNT(*) FROM PLACE_CONFIG_POSITION_POLYGON").getSingleResult()).intValue());
        Assert.assertEquals(0, ((Number) getEntityManager().createQuery("SELECT COUNT(p) FROM PlaceConfigEntity p").getSingleResult()).intValue());
    }

    private void setResources(SceneConfig sceneConfig) {
        sceneConfig.setInternalName("setup: add resources");
        List<ResourceItemPosition> resourceItemTypePositions = new ArrayList<>();
        // Outpost
        resourceItemTypePositions.add(new ResourceItemPosition().setResourceItemTypeId(RESOURCE_ITEM_TYPE_ID).setPosition(new DecimalPosition(212, 144)).setRotationZ(Math.toRadians(0)));
        resourceItemTypePositions.add(new ResourceItemPosition().setResourceItemTypeId(RESOURCE_ITEM_TYPE_ID).setPosition(new DecimalPosition(233, 164)).setRotationZ(Math.toRadians(80)));
        // Outpost 2
        resourceItemTypePositions.add(new ResourceItemPosition().setResourceItemTypeId(RESOURCE_ITEM_TYPE_ID).setPosition(new DecimalPosition(96, 254)).setRotationZ(Math.toRadians(0)));
        resourceItemTypePositions.add(new ResourceItemPosition().setResourceItemTypeId(RESOURCE_ITEM_TYPE_ID).setPosition(new DecimalPosition(108, 254)).setRotationZ(Math.toRadians(80)));
        resourceItemTypePositions.add(new ResourceItemPosition().setResourceItemTypeId(RESOURCE_ITEM_TYPE_ID).setPosition(new DecimalPosition(120, 252)).setRotationZ(Math.toRadians(160)));

        sceneConfig.setResourceItemTypePositions(resourceItemTypePositions);
    }

    private void setNpcBot(SceneConfig sceneConfig) {
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_HARVESTER_ID).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(223, 130))).setAngle(Math.toRadians(110)).setNoSpawn(true).setNoRebuild(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_FACTORY_ID).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(220, 109))).setNoSpawn(true).setNoRebuild(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_FACTORY_ID).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(213, 92))).setNoSpawn(true).setNoRebuild(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_BULLDOZER_ID).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(207, 111))).setAngle(Math.toRadians(30)).setNoSpawn(true).setNoRebuild(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_ATTACKER_ID).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(201, 94))).setAngle(Math.toRadians(175)).setNoSpawn(true).setNoRebuild(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_HARVESTER_ID).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(201, 88))).setAngle(Math.toRadians(310)).setNoSpawn(true).setNoRebuild(true));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
        botConfigs.add(new BotConfig().setAuxiliaryId(NPC_BOT_OUTPOST_AUX).setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Roger").setNpc(true));
        List<BotHarvestCommandConfig> botHarvestCommandConfigs = new ArrayList<>();
        botHarvestCommandConfigs.add(new BotHarvestCommandConfig().setBotAuxiliaryId(NPC_BOT_OUTPOST_AUX).setResourceItemTypeId(RESOURCE_ITEM_TYPE_ID).setResourceSelection(new PlaceConfig().setPosition(new DecimalPosition(212, 144))).setHarvesterItemTypeId(BASE_ITEM_TYPE_HARVESTER_ID));
        sceneConfig.setInternalName("setup: add NPC bot").setBotConfigs(botConfigs).setBotHarvestCommandConfigs(botHarvestCommandConfigs);
    }

    private void setEnemyBot(SceneConfig sceneConfig) {
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_TOWER_ID).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(190, 242))).setNoSpawn(true).setNoRebuild(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_FACTORY_ID).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(248, 283))).setNoSpawn(true).setNoRebuild(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_FACTORY_ID).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(277, 296))).setNoSpawn(true).setNoRebuild(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_FACTORY_ID).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(299, 261))).setNoSpawn(true).setNoRebuild(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_BULLDOZER_ID).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(240, 255))).setAngle(Math.toRadians(100)).setNoSpawn(true).setNoRebuild(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_BULLDOZER_ID).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(277, 252))).setAngle(Math.toRadians(200)).setNoSpawn(true).setNoRebuild(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_BULLDOZER_ID).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(260, 227))).setAngle(Math.toRadians(333)).setNoSpawn(true).setNoRebuild(true));
        // Attackers 4 harvester
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_ATTACKER_ID).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(230, 187))).setAngle(Math.toRadians(260)).setNoSpawn(true).setNoRebuild(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_ATTACKER_ID).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(234, 187))).setAngle(Math.toRadians(260)).setNoSpawn(true).setNoRebuild(true));
        // Harvester to harvest after attack
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_HARVESTER_ID).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(253, 200))).setAngle(Math.toRadians(240)).setNoSpawn(true).setNoRebuild(true));

        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
        botConfigs.add(new BotConfig().setAuxiliaryId(ENEMY_BOT_AUX).setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Razar Industries").setNpc(false));
        sceneConfig.setInternalName("setup: add enemy bot").setBotConfigs(botConfigs).setViewFieldConfig(new ViewFieldConfig().setToPosition(new DecimalPosition(270, 275)).setCameraLocked(true).setBottomWidth(120.0));
    }

    private void addFadeOutLoadingCover(List<SceneConfig> sceneConfigs) {
        sceneConfigs.add(new SceneConfig().setInternalName("script: fade out").setRemoveLoadingCover(true));
    }

    private void setScrollOverTerrain(SceneConfig sceneConfig) {
        sceneConfig.setInternalName("script: scroll over terrain").setIntroText("Willkommen Kommandant, Razarion Industries betreibt Raubbau auf diesem Planeten. Ihre Aufgabe ist es, Razarion Industries von diesem Planeten zu vertreiben.");
        sceneConfig.setViewFieldConfig(new ViewFieldConfig().setFromPosition(new DecimalPosition(270, 275)).setToPosition(new DecimalPosition(116, 84)).setSpeed(50.0).setCameraLocked(true).setBottomWidth(120.0));
    }

    private void setBotSpawnScene(SceneConfig sceneConfigs) {
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_BULLDOZER_ID).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(116, 100))).setNoRebuild(true));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
        botConfigs.add(new BotConfig().setAuxiliaryId(NPC_BOT_INSTRUCTOR_AUX).setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Kenny").setNpc(true));
        sceneConfigs.setInternalName("script: npc bot spawn").setBotConfigs(botConfigs).setIntroText("Kenny unterstützt Dich dabei. Er wird sich gleich auf die Planetenoberfläche beamen.").setDuration(3000);
    }

    private void setUserSpawnScene(SceneConfig sceneConfigs) {
        BaseItemPlacerConfig baseItemPlacerConfig = new BaseItemPlacerConfig().setEnemyFreeRadius(10.0).setSuggestedPosition(new DecimalPosition(135, 85));
        Map<Integer, Integer> buildupItemTypeCount = new HashMap<>();
        buildupItemTypeCount.put(BASE_ITEM_TYPE_BULLDOZER_ID, 1);
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setTypeCount(buildupItemTypeCount));
        // Tip
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.START_PLACER);
        gameTipConfig.setToCreatedItemTypeId(BASE_ITEM_TYPE_BULLDOZER_ID);
        gameTipConfig.setTerrainPositionHint(new DecimalPosition(135, 85));

        sceneConfigs.setInternalName("user: spawn 1").setGameTipConfig(gameTipConfig).setWait4QuestPassedDialog(true).setStartPointPlacerConfig(baseItemPlacerConfig).setQuestConfig(new QuestConfig().setTitle("Platzieren").setDescription("Wähle deinen Startpunkt um deine Starteinheit zu platzieren").setConditionConfig(conditionConfig).setXp(1).setPassedMessage("Gratuliere, du hast soeben deinen ersten Quest bestanden. Quests geben Erfahrungspunkte (Ep). Hast du genügend Erfahrungspunkte, erreichst du den nächsten Level. Im oberen linken Bereich siehst du deine Erfahrungspunkte."));
    }

    private void setBotMoveScene(SceneConfig sceneConfig) {
        ViewFieldConfig viewFieldConfig = new ViewFieldConfig().setToPosition(new DecimalPosition(205, 102)).setSpeed(50.0).setCameraLocked(true);
        List<BotMoveCommandConfig> botMoveCommandConfigs = new ArrayList<>();
        botMoveCommandConfigs.add(new BotMoveCommandConfig().setBotAuxiliaryId(NPC_BOT_INSTRUCTOR_AUX).setBaseItemTypeId(BASE_ITEM_TYPE_BULLDOZER_ID).setTargetPosition(new DecimalPosition(188, 90)));
        sceneConfig.setInternalName("script: npc bot move").setViewFieldConfig(viewFieldConfig).setBotMoveCommandConfigs(botMoveCommandConfigs).setIntroText("Folge mir zum Vorposten");
    }

    private void addScrollToOwnScene(List<SceneConfig> sceneConfigs) {
        SceneConfig sceneConfig = new SceneConfig().setInternalName("script: scroll to user").setIntroText("Fahre deine Einheit zum Vorposten");
        sceneConfig.setViewFieldConfig(new ViewFieldConfig().setToPosition(new DecimalPosition(160, 100)).setSpeed(50.0).setCameraLocked(true));
        sceneConfigs.add(sceneConfig);
    }

    private void setUserMoveScene(SceneConfig sceneConfig) {
        Map<Integer, Integer> itemTypeCount = new HashMap<>();
        itemTypeCount.put(BASE_ITEM_TYPE_BULLDOZER_ID, 1);
        ComparisonConfig comparisonConfig = new ComparisonConfig().setTypeCount(itemTypeCount).setPlaceConfig(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(175, 103, 10, 10)));
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_POSITION).setComparisonConfig(comparisonConfig);
        // Tip
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.MOVE);
        gameTipConfig.setActor(BASE_ITEM_TYPE_BULLDOZER_ID);
        gameTipConfig.setTerrainPositionHint(new DecimalPosition(180, 108));

        sceneConfig.setInternalName("user: move").setQuestConfig(new QuestConfig().setTitle("Fahre zu Vorposten").setDescription("Folge Kenny und Fahre zum Vorposten. Bewege deine Einheit zum markierten Bereich").setXp(1).setConditionConfig(conditionConfig)).setGameTipConfig(gameTipConfig).setWait4LevelUpDialog(true);
    }

    private void setNpcHarvestAttack(SceneConfig sceneConfig) {
        sceneConfig.setInternalName("script: bot harvest attack");
        sceneConfig.setViewFieldConfig(new ViewFieldConfig().setToPosition(new DecimalPosition(212, 144)).setSpeed(50.0).setCameraLocked(true));
        List<BotAttackCommandConfig> botAttackCommandConfigs = new ArrayList<>();
        botAttackCommandConfigs.add(new BotAttackCommandConfig().setBotAuxiliaryId(ENEMY_BOT_AUX).setTargetItemTypeId(BASE_ITEM_TYPE_HARVESTER_ID).setActorItemTypeId(BASE_ITEM_TYPE_ATTACKER_ID).setTargetSelection(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(190, 124, 40, 40))));
        botAttackCommandConfigs.add(new BotAttackCommandConfig().setBotAuxiliaryId(ENEMY_BOT_AUX).setTargetItemTypeId(BASE_ITEM_TYPE_HARVESTER_ID).setActorItemTypeId(BASE_ITEM_TYPE_ATTACKER_ID).setTargetSelection(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(190, 124, 40, 40))));
        sceneConfig.setBotAttackCommandConfigs(botAttackCommandConfigs).setDuration(7000).setIntroText("Hilfe wir werden angegriffen");
    }

    private void setFindEnemyBase(SceneConfig sceneConfig) {
        // Scroll Quest
        ScrollUiQuest scrollUiQuest = new ScrollUiQuest().setXp(1).setTitle("Finde Gegenerbasis").setDescription("Scrolle und such die gegenrische Basis").setScrollTargetRectangle(new Rectangle2D(250, 290, 10, 10)).setXp(1).setPassedMessage("Gratuliere, du hast die gegnerische Basis gefunden");
        // div
        ViewFieldConfig viewFieldConfig = new ViewFieldConfig().setCameraLocked(false);
        List<BotHarvestCommandConfig> botHarvestCommandConfigs = new ArrayList<>();
        botHarvestCommandConfigs.add(new BotHarvestCommandConfig().setBotAuxiliaryId(ENEMY_BOT_AUX).setResourceItemTypeId(RESOURCE_ITEM_TYPE_ID).setResourceSelection(new PlaceConfig().setPosition(new DecimalPosition(212, 144))).setHarvesterItemTypeId(BASE_ITEM_TYPE_HARVESTER_ID));
        // Tip
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.SCROLL);
        gameTipConfig.setTerrainPositionHint(new DecimalPosition(270, 310));

        sceneConfig.setInternalName("user: find enemy base").setGameTipConfig(gameTipConfig).setViewFieldConfig(viewFieldConfig).setScrollUiQuest(scrollUiQuest).setWait4QuestPassedDialog(true).setBotHarvestCommandConfigs(botHarvestCommandConfigs);
    }

    private void setPickBoxTask(SceneConfig sceneConfig) {
        // Drop box
        List<BoxItemPosition> boxItemPositions = new ArrayList<>();
        boxItemPositions.add(new BoxItemPosition().setBoxItemTypeId(BOX_ITEM_TYPE_ID).setPosition(new DecimalPosition(180, 120)));
        // Pick box quest
        QuestConfig questConfig = new QuestConfig().setXp(1).setTitle("Nimm die Box").setDescription("Eine Box wurde gesichtet. Sammle sie auf").setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.BOX_PICKED).setComparisonConfig(new ComparisonConfig().setCount(1)));
        // Tip
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.PICK_BOX);
        gameTipConfig.setActor(BASE_ITEM_TYPE_BULLDOZER_ID);
        gameTipConfig.setBoxItemTypeId(BOX_ITEM_TYPE_ID);

        sceneConfig.setInternalName("user: pick box").setGameTipConfig(gameTipConfig).setBoxItemPositions(boxItemPositions).setQuestConfig(questConfig).setWait4QuestPassedDialog(true);
    }

    private void setBoxSpawnTask(SceneConfig sceneConfig) {
        // Use inventory item quest
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setCount(1));
        // Move attackers away
        List<BotMoveCommandConfig> botMoveCommandConfigs = new ArrayList<>();
        botMoveCommandConfigs.add(new BotMoveCommandConfig().setBotAuxiliaryId(ENEMY_BOT_AUX).setBaseItemTypeId(BASE_ITEM_TYPE_ATTACKER_ID).setTargetPosition(new DecimalPosition(255, 244)));
        botMoveCommandConfigs.add(new BotMoveCommandConfig().setBotAuxiliaryId(ENEMY_BOT_AUX).setBaseItemTypeId(BASE_ITEM_TYPE_ATTACKER_ID).setTargetPosition(new DecimalPosition(257, 246)));
        // Tip
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.SPAN_INVENTORY_ITEM);
        gameTipConfig.setInventoryItemId(INVENTORY_ITEM_1_ID);
        gameTipConfig.setTerrainPositionHint(new DecimalPosition(216, 125));
        sceneConfig.setInternalName("user: box spawn").setGameTipConfig(gameTipConfig).setQuestConfig(new QuestConfig().setXp(1).setTitle("Benutze Inventar").setDescription("Platziere die Militäreinheiten vom Inventar").setConditionConfig(conditionConfig)).setWait4QuestPassedDialog(true).setBotMoveCommandConfigs(botMoveCommandConfigs);
    }

    private void setAttackTask(SceneConfig sceneConfig) {
        // Attack quest
        Map<Integer, Integer> attackItemTypeCount = new HashMap<>();
        attackItemTypeCount.put(BASE_ITEM_TYPE_HARVESTER_ID, 1);
        QuestConfig questConfig = new QuestConfig().setXp(10).setTitle("Zerstöre die Abbaufahrzeuge").setDescription("Greiffe Razarion insudtries an und zerstöre die Abbaufahrzeuge").setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_KILLED).setComparisonConfig(new ComparisonConfig().setTypeCount(attackItemTypeCount)));
        // Tip
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.ATTACK);
        gameTipConfig.setActor(BASE_ITEM_TYPE_ATTACKER_ID);
        gameTipConfig.setPlaceConfig(new PlaceConfig().setPolygon2D(new Rectangle2D(194, 133, 50, 50).toPolygon()));

        sceneConfig.setInternalName("user: kill bot harvester").setQuestConfig(questConfig).setGameTipConfig(gameTipConfig).setWait4LevelUpDialog(true);
    }

    private void setEnemyKillTask(SceneConfig sceneConfig) {
        // Kill bot command
        List<BotKillOtherBotCommandConfig> botKillOtherBotCommandConfigs = new ArrayList<>();
        botKillOtherBotCommandConfigs.add(new BotKillOtherBotCommandConfig().setBotAuxiliaryId(ENEMY_BOT_AUX).setTargetBotAuxiliaryId(NPC_BOT_OUTPOST_AUX).setDominanceFactor(1).setAttackerBaseItemTypeId(BASE_ITEM_TYPE_ATTACKER_ID).setSpawnPoint(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(212, 162, 51, 87))));
        // Kill human command
        List<BotKillHumanCommandConfig> botKillHumanCommandConfigs = new ArrayList<>();
        botKillHumanCommandConfigs.add(new BotKillHumanCommandConfig().setBotAuxiliaryId(ENEMY_BOT_AUX).setDominanceFactor(2).setAttackerBaseItemTypeId(BASE_ITEM_TYPE_ATTACKER_ID).setSpawnPoint(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(213, 209, 80, 70))));
        sceneConfig.setInternalName("script: enemy bot destroy user").setBotKillHumanCommandConfigs(botKillHumanCommandConfigs).setBotKillOtherBotCommandConfigs(botKillOtherBotCommandConfigs).setIntroText("Hilfe, Razar Industries greift uns an").setDuration(4000);
    }

    private void addWaitForDeadTask(List<SceneConfig> sceneConfigs) {
        sceneConfigs.add(new SceneConfig().setInternalName("script wait for dead dialog").setWaitForBaseLostDialog(true));
    }

    private void setNpcEscapeTask(SceneConfig sceneConfig) {
        List<BotMoveCommandConfig> botMoveCommandConfigs = new ArrayList<>();
        botMoveCommandConfigs.add(new BotMoveCommandConfig().setBotAuxiliaryId(NPC_BOT_INSTRUCTOR_AUX).setBaseItemTypeId(BASE_ITEM_TYPE_BULLDOZER_ID).setTargetPosition(new DecimalPosition(48, 110)));
        sceneConfig.setInternalName("script: escape npc bot").setBotMoveCommandConfigs(botMoveCommandConfigs).setIntroText("Baue dich neu auf und zerstöre Razar Industries.").setDuration(3000);
    }

    private void setUserSpawnScene2(SceneConfig sceneConfig) {
        // Bot NPC_BOT_OUTPOST_2_AUX
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_ATTACKER_ID).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(145, 260))).setNoSpawn(true).setNoRebuild(true));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
        botConfigs.add(new BotConfig().setAuxiliaryId(NPC_BOT_OUTPOST_2_AUX).setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Roger").setNpc(true));
        // User Spawn
        BaseItemPlacerConfig baseItemPlacerConfig = new BaseItemPlacerConfig().setEnemyFreeRadius(10.0).setAllowedArea(Polygon2D.fromRectangle(80, 260, 50, 50));
        Map<Integer, Integer> buildupItemTypeCount = new HashMap<>();
        buildupItemTypeCount.put(BASE_ITEM_TYPE_BULLDOZER_ID, 1);
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setTypeCount(buildupItemTypeCount));
        // Tip
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.START_PLACER);
        gameTipConfig.setToCreatedItemTypeId(BASE_ITEM_TYPE_BULLDOZER_ID);
        gameTipConfig.setTerrainPositionHint(new DecimalPosition(100, 280));

        // Kill NPC_BOT_INSTRUCTOR_AUX
        List<KillBotCommandConfig> killBotCommandConfigs = new ArrayList<>();
        killBotCommandConfigs.add(new KillBotCommandConfig().setBotAuxiliaryId(NPC_BOT_INSTRUCTOR_AUX));
        // Build factory Quest
        sceneConfig.setInternalName("user: spawn 2").setGameTipConfig(gameTipConfig).setStartPointPlacerConfig(baseItemPlacerConfig).setQuestConfig(new QuestConfig().setTitle("Baue eine Basis").setDescription("Platziere deinen Bulldozer und baue eine Basis auf um Razarion Industries zu besiegen.").setHidePassedDialog(true).setConditionConfig(conditionConfig).setXp(0)).setKillBotCommandConfigs(killBotCommandConfigs).setBotConfigs(botConfigs);
    }

    private void setBuildFactoryTask(SceneConfig sceneConfig) {
        // Build factory Quest
        Map<Integer, Integer> buildupItemTypeCount = new HashMap<>();
        buildupItemTypeCount.put(BASE_ITEM_TYPE_FACTORY_ID, 1);
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setTypeCount(buildupItemTypeCount));
        // Tip
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.BUILD);
        gameTipConfig.setActor(BASE_ITEM_TYPE_BULLDOZER_ID);
        gameTipConfig.setToCreatedItemTypeId(BASE_ITEM_TYPE_FACTORY_ID);
        gameTipConfig.setTerrainPositionHint(new DecimalPosition(112, 285));

        sceneConfig.setInternalName("user: build factory").setGameTipConfig(gameTipConfig).setQuestConfig(new QuestConfig().setTitle("Baue eine Fabrik").setDescription("Baue eine Fabrik mit deinem Bulldozer").setConditionConfig(conditionConfig).setXp(10)).setWait4QuestPassedDialog(true);
    }

    private void setFactorizeHarvesterTask(SceneConfig sceneConfig) {
        // Build Harvester Quest
        Map<Integer, Integer> buildupItemTypeCount = new HashMap<>();
        buildupItemTypeCount.put(BASE_ITEM_TYPE_HARVESTER_ID, 1);
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setTypeCount(buildupItemTypeCount));
        // Kill NPC_BOT_OUTPOST_AUX
        List<KillBotCommandConfig> killBotCommandConfigs = new ArrayList<>();
        killBotCommandConfigs.add(new KillBotCommandConfig().setBotAuxiliaryId(NPC_BOT_OUTPOST_AUX));
        // Tip
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.FABRICATE);
        gameTipConfig.setActor(BASE_ITEM_TYPE_FACTORY_ID);
        gameTipConfig.setToCreatedItemTypeId(BASE_ITEM_TYPE_HARVESTER_ID);

        sceneConfig.setInternalName("user: fabricate harvester").setGameTipConfig(gameTipConfig).setQuestConfig(new QuestConfig().setTitle("Baue ein Harvester").setDescription("Baue ein Harvester in deiner Fabrik").setConditionConfig(conditionConfig).setXp(10)).setWait4QuestPassedDialog(true).setKillBotCommandConfigs(killBotCommandConfigs);
    }

    private void setHarvestTask(SceneConfig sceneConfig) {
        // Harvest quest
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.HARVEST).setComparisonConfig(new ComparisonConfig().setCount(30));
        // Tip
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.HARVEST);
        gameTipConfig.setActor(BASE_ITEM_TYPE_HARVESTER_ID);
        gameTipConfig.setResourceItemTypeId(RESOURCE_ITEM_TYPE_ID);
        gameTipConfig.setPlaceConfig(new PlaceConfig().setPosition(new DecimalPosition(108, 254)));
        sceneConfig.setInternalName("user: harvest").setGameTipConfig(gameTipConfig).setQuestConfig(new QuestConfig().setTitle("Sammle Razarion").setDescription("Sammle Razarion um eine Armee zu bauen").setConditionConfig(conditionConfig).setXp(10)).setWait4LevelUpDialog(true);
    }

    private void addHarvestExplanationTask(List<SceneConfig> sceneConfigs) {
        // Harvest explanation
        sceneConfigs.add(new SceneConfig().setInternalName("script: explain harvest").setIntroText("Du brauchst viel Razarion um eine Armee zu bauen").setDuration(3000));
    }

    private void setBuildViperTask(SceneConfig sceneConfig) {
        // Build viper
        Map<Integer, Integer> buildupItemTypeCount = new HashMap<>();
        buildupItemTypeCount.put(BASE_ITEM_TYPE_ATTACKER_ID, 1);
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setTypeCount(buildupItemTypeCount));
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.FABRICATE);
        gameTipConfig.setActor(BASE_ITEM_TYPE_FACTORY_ID);
        gameTipConfig.setToCreatedItemTypeId(BASE_ITEM_TYPE_ATTACKER_ID);

        sceneConfig.setInternalName("user: build viper 1").setGameTipConfig(gameTipConfig).setQuestConfig(new QuestConfig().setTitle("Bauen").setDescription("Baue ein Viper in deiner Fabrik").setConditionConfig(conditionConfig).setXp(10)).setWait4QuestPassedDialog(true);
    }

    private void setNpcAttackTowerCommand(SceneConfig sceneConfig) {
        // Attack command
        List<BotAttackCommandConfig> botAttackCommandConfigs = new ArrayList<>();
        botAttackCommandConfigs.add(new BotAttackCommandConfig().setBotAuxiliaryId(NPC_BOT_OUTPOST_2_AUX).setTargetItemTypeId(BASE_ITEM_TYPE_TOWER_ID).setTargetSelection(new PlaceConfig().setPosition(new DecimalPosition(190, 242))).setActorItemTypeId(BASE_ITEM_TYPE_ATTACKER_ID));
        sceneConfig.setInternalName("script: npc bot attacks tower").setIntroText("Komm, greiffen wir an!").setBotAttackCommandConfigs(botAttackCommandConfigs).setDuration(5000).setViewFieldConfig(new ViewFieldConfig().setToPosition(new DecimalPosition(190, 242)).setSpeed(50.0));
    }

    private void addNpcTooWeakCommand(List<SceneConfig> sceneConfigs) {
        // Attack command
        sceneConfigs.add(new SceneConfig().setInternalName("script: npc too weak").setIntroText("Der Turm ist zu stark, wir brauchen eine grössere Armee").setDuration(2000));
    }

    private void setBuildViperTask2(SceneConfig sceneConfig) {
        // Build viper
        Map<Integer, Integer> buildupItemTypeCount = new HashMap<>();
        buildupItemTypeCount.put(BASE_ITEM_TYPE_ATTACKER_ID, 2);
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setTypeCount(buildupItemTypeCount));
        // Tip
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.FABRICATE);
        gameTipConfig.setActor(BASE_ITEM_TYPE_FACTORY_ID);
        gameTipConfig.setToCreatedItemTypeId(BASE_ITEM_TYPE_ATTACKER_ID);

        sceneConfig.setInternalName("user: build viper 2").setGameTipConfig(gameTipConfig).setQuestConfig(new QuestConfig().setTitle("Bauen").setDescription("Baue zwei Vipers in deiner Fabrik").setConditionConfig(conditionConfig).setXp(10)).setWait4QuestPassedDialog(true);
    }

    private void setKillTower(SceneConfig sceneConfig) {
        Map<Integer, Integer> buildupItemTypeCount = new HashMap<>();
        buildupItemTypeCount.put(BASE_ITEM_TYPE_TOWER_ID, 1);
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_KILLED).setComparisonConfig(new ComparisonConfig().setTypeCount(buildupItemTypeCount));
        // Tip
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.ATTACK);
        gameTipConfig.setActor(BASE_ITEM_TYPE_ATTACKER_ID);
        gameTipConfig.setPlaceConfig(new PlaceConfig().setPosition(new DecimalPosition(190, 242)));

        sceneConfig.setInternalName("user: kill tower").setGameTipConfig(gameTipConfig).setQuestConfig(new QuestConfig().setTitle("Zerstöre Turm").setDescription("Nimm deine 3 Vipers und zerstöre den Turm").setConditionConfig(conditionConfig).setXp(10)).setWait4QuestPassedDialog(true);
    }

    private void addKillBotEndForward(List<SceneConfig> sceneConfigs) {
        // Kill bot base quest
        sceneConfigs.add(new SceneConfig().setInternalName("user: kill bot").setQuestConfig(new QuestConfig().setXp(20).setTitle("Kill Razar Industries").setDescription("Vertreibe Razar Industries von diesem Planeten").setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.BASE_KILLED).setComparisonConfig(new ComparisonConfig().setCount(1)))).setWait4QuestPassedDialog(true));
    }
}