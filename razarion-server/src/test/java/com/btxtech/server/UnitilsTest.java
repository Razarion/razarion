package com.btxtech.server;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.BotHarvestCommandConfig;
import com.btxtech.shared.dto.SceneConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestDescriptionConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotEnragementStateConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;
import org.unitils.reflectionassert.comparator.impl.ObjectComparatorIgnore;

import java.util.ArrayList;
import java.util.List;


// unitils unitils-core patched
//

// Patched org.unitils.reflectionassert.comparator.impl.ObjectComparator
//
// In compareFields line 102
//     if (ObjectComparatorIgnore.contains(clazz, field)) {
//         continue;
//     }
//

// Added this ugly hack class
///**
// * Created by Beat
// * 18.05.2017.
// */
//public class ObjectComparatorIgnore {
//    private static Map<Class<?>, Set<String>> ignores = new HashMap<Class<?>, Set<String>>();
//
//    public static boolean add(Class<?> clazz, String fieldName) {
//        Set<String> fieldNames = ignores.get(clazz);
//        if (fieldNames == null) {
//            fieldNames = new HashSet<String>();
//            ignores.put(clazz, fieldNames);
//        }
//
//        fieldNames.add(fieldName);
//        return false;
//    }
//
//    public static void clear() {
//        ignores.clear();
//    }
//
//    public static boolean contains(Class<?> clazz, Field field) {
//        Set<String> fieldNames = ignores.get(clazz);
//        return fieldNames != null && fieldNames.contains(field.getName());
//    }
//}


/**
 * Created by Beat
 * 18.05.2017.
 */
public class UnitilsTest {

    @Test
    public void testWithIgnoring() {
        List<SceneConfig> expectedSceneConfigs = setupTestGraph(1, 11);
        List<SceneConfig> actualSceneConfigs = setupTestGraph(2, 12);


        ObjectComparatorIgnore.add(BotConfig.class, "id");
        ObjectComparatorIgnore.add(QuestDescriptionConfig.class, "id");
        ReflectionAssert.assertReflectionEquals(expectedSceneConfigs, actualSceneConfigs);
    }

    private List<SceneConfig> setupTestGraph(int botId, int questId) {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().baseItemTypeId(1).count(1).createDirectly(true).place(new PlaceConfig().position(new DecimalPosition(223, 130))).angle(Math.toRadians(110)).noSpawn(true).noRebuild(true));
        botItems.add(new BotItemConfig().baseItemTypeId(22).count(1).createDirectly(true).place(new PlaceConfig().position(new DecimalPosition(220, 109))).noSpawn(true).noRebuild(true));
        botItems.add(new BotItemConfig().baseItemTypeId(22).count(1).createDirectly(true).place(new PlaceConfig().position(new DecimalPosition(213, 92))).noSpawn(true).noRebuild(true));
        botItems.add(new BotItemConfig().baseItemTypeId(33).count(1).createDirectly(true).place(new PlaceConfig().position(new DecimalPosition(207, 111))).angle(Math.toRadians(30)).noSpawn(true).noRebuild(true));
        botItems.add(new BotItemConfig().baseItemTypeId(55).count(1).createDirectly(true).place(new PlaceConfig().position(new DecimalPosition(201, 94))).angle(Math.toRadians(175)).noSpawn(true).noRebuild(true));
        botItems.add(new BotItemConfig().baseItemTypeId(1).count(1).createDirectly(true).place(new PlaceConfig().position(new DecimalPosition(201, 88))).angle(Math.toRadians(310)).noSpawn(true).noRebuild(true));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().name("Normal").botItems(botItems));
        botConfigs.add(new BotConfig().id(botId).actionDelay(3000).botEnragementStateConfigs(botEnragementStateConfigs).name("Roger").npc(true));
        List<BotHarvestCommandConfig> botHarvestCommandConfigs = new ArrayList<>();
        botHarvestCommandConfigs.add(new BotHarvestCommandConfig().setBotAuxiliaryId(23).setResourceItemTypeId(54).setResourceSelection(new PlaceConfig().position(new DecimalPosition(212, 144))).setHarvesterItemTypeId(17));
        sceneConfigs.add(new SceneConfig().setInternalName("user: spawn 1").setWait4QuestPassedDialog(true).setQuestConfig(new QuestConfig().setId(questId).setTitle("Platzieren").setDescription("Wähle deinen Startpunkt um deine Starteinheit zu platzieren").setXp(1).setPassedMessage("Gratuliere, du hast soeben deinen ersten Quest bestanden. Quests geben Erfahrungspunkte (Ep). Hast du genügend Erfahrungspunkte, erreichst du den nächsten Level. Im oberen linken Bereich siehst du deine Erfahrungspunkte.")));
        sceneConfigs.add(new SceneConfig().setInternalName("setup: add NPC bot").setBotConfigs(botConfigs).setBotHarvestCommandConfigs(botHarvestCommandConfigs));
        return sceneConfigs;
    }

}
