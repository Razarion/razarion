package com.btxtech.shared.gameengine.planet.bot;

import com.btxtech.shared.TestHelper;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotEnragementStateConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotSceneConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotSceneConflictConfig;
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
    private HumanBaseContext setupHumanAndBotScene() {
        setup();

        // Bot to watch
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().setBaseItemTypeId(FallbackConfig.BUILDER_ITEM_TYPE_ID).setCount(1).setPlace(new PlaceConfig().setPosition(new DecimalPosition(256, 248))).setCreateDirectly(true).setNoRebuild(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(FallbackConfig.BUILDER_ITEM_TYPE_ID).setCount(1).setPlace(new PlaceConfig().setPosition(new DecimalPosition(304, 232))).setCreateDirectly(true).setNoRebuild(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(FallbackConfig.BUILDER_ITEM_TYPE_ID).setCount(1).setPlace(new PlaceConfig().setPosition(new DecimalPosition(256, 200))).setCreateDirectly(true).setNoRebuild(true));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
        botConfigs.add(new BotConfig().setId(1).setAutoAttack(true).setRealm(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(256, 192, 40, 40))).setActionDelay(1).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Kenny"));
        // Bot scene
        List<BotSceneConfig> botSceneConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> sceneBotEnragementStateConfigs = new ArrayList<>();
        BotSceneConflictConfig botSceneConflictConfig = new BotSceneConflictConfig().setId(1).setEnterKills(2).setEnterDuration(100).setLeaveNoKillDuration(100).setMinDistance(100).setMaxDistance(200).setTargetBaseItemTypeId(FallbackConfig.FACTORY_ITEM_TYPE_ID).setStopKills(2);
        List<BotItemConfig> sceneBotItems = new ArrayList<>();
        sceneBotItems.add(new BotItemConfig().setBaseItemTypeId(FallbackConfig.ATTACKER_ITEM_TYPE_ID).setCount(2).setCreateDirectly(true));
        sceneBotEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(sceneBotItems));
        botSceneConflictConfig.setBotConfig(new BotConfig().setId(2).setAutoAttack(true).setRealm(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(-50, -50, 100, 100))).setActionDelay(1).setBotEnragementStateConfigs(sceneBotEnragementStateConfigs).setName("Scene Bot"));
        botSceneConfigs.add(new BotSceneConfig().setId(1).setScheduleTimeMillis(1).setBotIdsToWatch(Collections.singletonList(1)).setBotSceneConflictConfigs(Collections.singletonList(botSceneConflictConfig)));

        startBots(botConfigs, botSceneConfigs);
        // Setup human
        HumanBaseContext humanBaseContext = createHumanBaseBFA4(new DecimalPosition(72, 57), new DecimalPosition(71, 88));
        getCommandService().move(humanBaseContext.getBuilder(), new DecimalPosition(64, 48));
        tickPlanetServiceBaseServiceActive();
        getCommandService().move(humanBaseContext.getAttacker1(), new DecimalPosition(88, 80));
        getCommandService().move(humanBaseContext.getAttacker2(), new DecimalPosition(104, 80));
        getCommandService().move(humanBaseContext.getAttacker3(), new DecimalPosition(120, 80));
        getCommandService().move(humanBaseContext.getAttacker4(), new DecimalPosition(136, 80));
        tickPlanetServiceBaseServiceActive();
        return humanBaseContext;
    }

    private void tickAll() {
        for (int i = 0; i < 10; i++) {
            tickAllOnce();
        }
    }

    private void tickAllOnce() {
        tickPlanetServiceBaseServiceActive();
        tickBotSceneRunner();
        tickBotRunner();
    }

    @Test
    public void test() {
        HumanBaseContext humanBaseContext = setupHumanAndBotScene();

        // Human attacks bot
        // First attack -> no conflict
        getCommandService().attack(humanBaseContext.getAttacker1(), findFirstBotItemHighestId(1, FallbackConfig.BUILDER_ITEM_TYPE_ID), true);
        tickAll();
        Assert.assertTrue(getBotService().getBotSceneIndicationInfos(humanBaseContext.getPlayerBaseFull().getHumanPlayerId()).isEmpty());
        // Second attack -> conflict
        getCommandService().attack(humanBaseContext.getAttacker1(), findFirstBotItemHighestId(1, FallbackConfig.BUILDER_ITEM_TYPE_ID), true);
        tickAllOnce();
        Assert.assertEquals(1, getBotService().getBotSceneIndicationInfos(humanBaseContext.getPlayerBaseFull().getHumanPlayerId()).size());
        tickAll();
        TestHelper.sleep(100);
        tickAllOnce();

        // showDisplay();
        Assert.assertTrue(getBotService().getBotSceneIndicationInfos(humanBaseContext.getPlayerBaseFull().getHumanPlayerId()).isEmpty());
        Assert.fail("... BETTER ASSERT ...");

    }

}
