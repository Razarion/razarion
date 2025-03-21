package com.btxtech.server;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.BotHarvestCommandConfig;
import com.btxtech.shared.dto.SceneConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotEnragementStateConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;
import org.junit.Test;

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
        sceneConfigs.add(new SceneConfig().internalName("user: spawn 1").wait4QuestPassedDialog(true).questConfig(new QuestConfig().id(questId).title("Platzieren").description("Wähle deinen Startpunkt um deine Starteinheit zu platzieren").xp(1).passedMessage("Gratuliere, du hast soeben deinen ersten Quest bestanden. Quests geben Erfahrungspunkte (Ep). Hast du genügend Erfahrungspunkte, erreichst du den nächsten Level. Im oberen linken Bereich siehst du deine Erfahrungspunkte.")));
        sceneConfigs.add(new SceneConfig().internalName("setup: add NPC bot").botConfigs(botConfigs).botHarvestCommandConfigs(botHarvestCommandConfigs));
        return sceneConfigs;
    }

}
