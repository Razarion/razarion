package com.btxtech.shared.gameengine.planet.bot;

import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotEnragementStateConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;
import com.btxtech.shared.gameengine.planet.BaseItemServiceBase;
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
        setupMasterEnvironment();
        // Setup bot config
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().setBaseItemTypeId(BaseItemServiceBase.FACTORY_ITEM_TYPE_ID).setCount(3).setCreateDirectly(true).setPlace(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(150, 80, 150, 150))));
        botItems.add(new BotItemConfig().setBaseItemTypeId(BaseItemServiceBase.ATTACKER_ITEM_TYPE_ID).setCount(6).setCreateDirectly(true).setPlace(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(150, 80, 150, 150))));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
        botConfigs.add(new BotConfig().setId(1).setActionDelay(1).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Kenny").setNpc(false));
        // Start bot
        startBot(botConfigs);

        tickBotRunner();
        Assert.assertEquals(9, getSyncBaseItemInfos().size());
        for (int i = 0; i < 100; i++) {
            tickBotRunner();
        }
        Assert.assertEquals(9, getSyncBaseItemInfos().size());

    }

    @Test
    public void testSimpleBuildupIndirectly() {
        setupMasterEnvironment();
        // Setup bot config
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().setBaseItemTypeId(BaseItemServiceBase.FACTORY_ITEM_TYPE_ID).setCount(3).setCreateDirectly(true).setPlace(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(150, 80, 150, 150))));
        botItems.add(new BotItemConfig().setBaseItemTypeId(BaseItemServiceBase.ATTACKER_ITEM_TYPE_ID).setCount(6).setCreateDirectly(false).setPlace(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(150, 80, 150, 150))));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
        botConfigs.add(new BotConfig().setId(1).setActionDelay(1).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Kenny").setNpc(false));
        // Start bot
        startBot(botConfigs);

        tickBotRunner();
        Assert.assertEquals(3, getSyncBaseItemInfos().size());
        for (int i = 0; i < 1000; i++) {
            tickBotRunner();
            tickPlanetService();
        }
        Assert.assertEquals(9, getSyncBaseItemInfos().size());

    }
}