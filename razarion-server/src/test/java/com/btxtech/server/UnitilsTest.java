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
        botItems.add(new BotItemConfig().setBaseItemTypeId(1).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().position(new DecimalPosition(223, 130))).setAngle(Math.toRadians(110)).setNoSpawn(true).setNoRebuild(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(22).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().position(new DecimalPosition(220, 109))).setNoSpawn(true).setNoRebuild(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(22).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().position(new DecimalPosition(213, 92))).setNoSpawn(true).setNoRebuild(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(33).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().position(new DecimalPosition(207, 111))).setAngle(Math.toRadians(30)).setNoSpawn(true).setNoRebuild(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(55).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().position(new DecimalPosition(201, 94))).setAngle(Math.toRadians(175)).setNoSpawn(true).setNoRebuild(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(1).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().position(new DecimalPosition(201, 88))).setAngle(Math.toRadians(310)).setNoSpawn(true).setNoRebuild(true));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
        botConfigs.add(new BotConfig().setId(botId).setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Roger").setNpc(true));
        List<BotHarvestCommandConfig> botHarvestCommandConfigs = new ArrayList<>();
        botHarvestCommandConfigs.add(new BotHarvestCommandConfig().setBotAuxiliaryId(23).setResourceItemTypeId(54).setResourceSelection(new PlaceConfig().position(new DecimalPosition(212, 144))).setHarvesterItemTypeId(17));
        sceneConfigs.add(new SceneConfig().setInternalName("user: spawn 1").setWait4QuestPassedDialog(true).setQuestConfig(new QuestConfig().setId(questId).setTitle("Platzieren").setDescription("Wähle deinen Startpunkt um deine Starteinheit zu platzieren").setXp(1).setPassedMessage("Gratuliere, du hast soeben deinen ersten Quest bestanden. Quests geben Erfahrungspunkte (Ep). Hast du genügend Erfahrungspunkte, erreichst du den nächsten Level. Im oberen linken Bereich siehst du deine Erfahrungspunkte.")));
        sceneConfigs.add(new SceneConfig().setInternalName("setup: add NPC bot").setBotConfigs(botConfigs).setBotHarvestCommandConfigs(botHarvestCommandConfigs));
        return sceneConfigs;
    }

}
