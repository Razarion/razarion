package com.btxtech.shared.gameengine.planet.bot;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotEnragementStateConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 03.05.2017.
 */
public class BotServiceTest extends BaseBotServiceTest {

    @Test
    public void testSimpleBuildupDirectly() {
        setup();
        // Setup bot config
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().baseItemTypeId(FallbackConfig.FACTORY_ITEM_TYPE_ID).count(3).createDirectly(true).place(new PlaceConfig().polygon2D(Polygon2D.fromRectangle(150, 80, 150, 150))));
        botItems.add(new BotItemConfig().baseItemTypeId(FallbackConfig.ATTACKER_ITEM_TYPE_ID).count(6).createDirectly(true).place(new PlaceConfig().polygon2D(Polygon2D.fromRectangle(150, 80, 150, 150))));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().name("Normal").botItems(botItems));
        botConfigs.add(new BotConfig().id(1).actionDelay(1).botEnragementStateConfigs(botEnragementStateConfigs).name("Kenny").npc(false));
        // Start bot
        startBots(botConfigs);

        tickBotRunner();
        Assert.assertEquals(9, getSyncBaseItemInfos().size());
        for (int i = 0; i < 100; i++) {
            tickBotRunner();
        }
        Assert.assertEquals(9, getSyncBaseItemInfos().size());

    }

    @Test
    public void testSimpleBuildupIndirectly() {
        setup();
        // Setup bot config
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().baseItemTypeId(FallbackConfig.FACTORY_ITEM_TYPE_ID).count(3).createDirectly(true).place(new PlaceConfig().polygon2D(Polygon2D.fromRectangle(150, 80, 150, 150))));
        botItems.add(new BotItemConfig().baseItemTypeId(FallbackConfig.ATTACKER_ITEM_TYPE_ID).count(6).createDirectly(false).place(new PlaceConfig().polygon2D(Polygon2D.fromRectangle(150, 80, 150, 150))));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().name("Normal").botItems(botItems));
        botConfigs.add(new BotConfig().id(1).actionDelay(1).botEnragementStateConfigs(botEnragementStateConfigs).name("Kenny").npc(false));
        // Start bot
        startBots(botConfigs);

        tickBotRunner();
        Assert.assertEquals(3, getSyncBaseItemInfos().size());
        for (int i = 0; i < 1000; i++) {
            tickBotRunner();
            tickPlanetService();
        }
        Assert.assertEquals(9, getSyncBaseItemInfos().size());
    }

    @Test
    public void testAttack() {
        setup();

        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().baseItemTypeId(FallbackConfig.ATTACKER_ITEM_TYPE_ID).count(3).createDirectly(true).place(new PlaceConfig().polygon2D(Polygon2D.fromRectangle(20, 20, 10, 10))));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().name("Normal").botItems(botItems));
        botConfigs.add(new BotConfig().id(1).autoAttack(true).realm(new PlaceConfig().polygon2D(Polygon2D.fromRectangle(20, 20, 100, 100))).actionDelay(1).botEnragementStateConfigs(botEnragementStateConfigs).name("Kenny").npc(false));
        startBots(botConfigs);

        tickBotRunner();

        UserContext userContext = createLevel1UserContext();
        createHumanBaseWithBaseItem(new DecimalPosition(20, 60), userContext);

        for (int i = 0; i < 10000; i++) {
            tickBotRunner();
            tickPlanetServiceBaseServiceActive();
        }

        Assert.assertEquals(1, getTestGameLogicListener().getSyncBaseItemKilled().size());
    }

    @Test
    public void testAttackNoAutoAttack() {
        setup();

        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().baseItemTypeId(FallbackConfig.ATTACKER_ITEM_TYPE_ID).count(3).createDirectly(true).place(new PlaceConfig().polygon2D(Polygon2D.fromRectangle(20, 20, 10, 10))));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().name("Normal").botItems(botItems));
        botConfigs.add(new BotConfig().id(1).realm(new PlaceConfig().polygon2D(Polygon2D.fromRectangle(20, 20, 100, 100))).actionDelay(1).botEnragementStateConfigs(botEnragementStateConfigs).name("Kenny").npc(false));
        startBots(botConfigs);

        tickBotRunner();

        UserContext userContext = createLevel1UserContext();
        createHumanBaseWithBaseItem(new DecimalPosition(20, 60), userContext);

        for (int i = 0; i < 1000; i++) {
            tickBotRunner();
            tickPlanetServiceBaseServiceActive();
        }

        Assert.assertTrue(getTestGameLogicListener().getSyncBaseItemKilled().isEmpty());
    }
}