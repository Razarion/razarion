package com.btxtech.shared.gameengine.planet.bot;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotEnragementStateConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotSceneConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotSceneConflictConfig;
import com.btxtech.shared.gameengine.planet.GameTestContent;
import com.btxtech.shared.gameengine.planet.basic.HumanBaseContext;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Beat
 * on 20.03.2018.
 */
public class BotSceneTest extends BaseBotServiceTest {

    @Test
    public void test() throws InterruptedException {
        setup();

        // Bot to watch
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().setBaseItemTypeId(GameTestContent.BUILDER_ITEM_TYPE_ID).setCount(3).setCreateDirectly(true));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
        botConfigs.add(new BotConfig().setId(1).setAutoAttack(true).setRealm(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(256, 192, 40, 40))).setActionDelay(1).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Kenny").setNpc(false));
        // Bot scene
        List<BotSceneConfig> botSceneConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> sceneBotEnragementStateConfigs = new ArrayList<>();
        BotSceneConflictConfig botSceneConflictConfig = new BotSceneConflictConfig().setMinDistance(100).setMaxDistance(200).setTargetBaseItemTypeId(GameTestContent.FACTORY_ITEM_TYPE_ID);
        List<BotItemConfig> sceneBotItems = new ArrayList<>();
        sceneBotItems.add(new BotItemConfig().setBaseItemTypeId(GameTestContent.ATTACKER_ITEM_TYPE_ID).setCount(3).setCreateDirectly(true));
        sceneBotEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(sceneBotItems));
        botSceneConflictConfig.setBotConfig(new BotConfig().setId(2).setAutoAttack(true).setRealm(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(-50, -50, 100, 100))).setActionDelay(1).setBotEnragementStateConfigs(sceneBotEnragementStateConfigs).setName("Scene Bot"));
        botSceneConfigs.add(new BotSceneConfig().setId(1).setScheduleTimeMillis(1).setBotIdsToWatch(Collections.singletonList(1)).setKillThreshold(2).setBotSceneConflictConfig(botSceneConflictConfig));

        startBots(botConfigs, botSceneConfigs);
        // Setup human
        HumanBaseContext humanBaseContext = createHumanBaseBFA(new DecimalPosition(72, 57), new DecimalPosition(71, 88));
        tickPlanetServiceBaseServiceActive();

        // Human attacks bot
        getCommandService().attack(humanBaseContext.getAttacker(), findFirstBotItemHighestId(1, GameTestContent.BUILDER_ITEM_TYPE_ID), true);
        tickPlanetServiceBaseServiceActive();
        getCommandService().attack(humanBaseContext.getAttacker(), findFirstBotItemHighestId(1, GameTestContent.BUILDER_ITEM_TYPE_ID), true);
        tickPlanetServiceBaseServiceActive();

        for (int i = 0; i < 10; i++) {
            tickBotSceneRunner();
            tickBotRunner();
            tickPlanetServiceBaseServiceActive();
        }

        // TODO assert
        Assert.fail("... TODO ...");
        // showDisplay();
    }

}
