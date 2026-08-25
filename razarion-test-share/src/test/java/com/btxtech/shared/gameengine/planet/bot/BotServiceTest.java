package com.btxtech.shared.gameengine.planet.bot;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotEnragementStateConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
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

    /**
     * A build job that ends before the target is finished used to leave the shell standing forever:
     * Need was already satisfied when the shell was created, so nothing rebuilt it, and the bot
     * could not produce from it either because a building below buildup 1.0 is never idle. On PROD
     * this cost bot 1601 its whole Hydra supply for half an hour after its builder got pushed out
     * of range one second before the dockyard was done.
     */
    @Test
    public void abandonedShellIsFinishedByTheBot() {
        setup();
        // Give the factory a real build time - the fallback default of 0 finishes it in one tick,
        // which would leave no window in which the shell can be abandoned.
        getBaseItemType(FallbackConfig.FACTORY_ITEM_TYPE_ID).buildup(50);

        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().baseItemTypeId(FallbackConfig.BUILDER_ITEM_TYPE_ID).count(1).createDirectly(true).place(new PlaceConfig().polygon2D(Polygon2D.fromRectangle(150, 80, 150, 150))));
        botItems.add(new BotItemConfig().baseItemTypeId(FallbackConfig.FACTORY_ITEM_TYPE_ID).count(1).createDirectly(false).place(new PlaceConfig().polygon2D(Polygon2D.fromRectangle(150, 80, 150, 150))));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().name("Normal").botItems(botItems));
        botConfigs.add(new BotConfig().id(1).actionDelay(1).botEnragementStateConfigs(botEnragementStateConfigs).name("Kenny").npc(false));
        startBots(botConfigs);

        SyncBaseItem factory = null;
        for (int i = 0; i < 5000 && factory == null; i++) {
            tickBotRunner();
            tickPlanetService();
            factory = findBotItemOrNull(FallbackConfig.FACTORY_ITEM_TYPE_ID);
        }
        Assert.assertNotNull("Bot never started the factory", factory);
        Assert.assertFalse("Factory was expected to be an unfinished shell", factory.isBuildup());

        // Exactly what BaseItemService did to the builder when it was pushed out of range: the
        // build job is dropped while the shell stays behind.
        SyncBaseItem builder = findBotItemOrNull(FallbackConfig.BUILDER_ITEM_TYPE_ID);
        Assert.assertNotNull(builder);
        builder.stop(true);
        Assert.assertFalse(factory.isBuildup());

        for (int i = 0; i < 5000; i++) {
            tickBotRunner();
            tickPlanetService();
        }
        Assert.assertTrue("Bot left the abandoned shell unfinished", factory.isBuildup());
        Assert.assertEquals("The shell was to be finished, not replaced", 1, countBotItems(FallbackConfig.FACTORY_ITEM_TYPE_ID));
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

    /**
     * The camper case. An enemy parks inside the bot's realm and kills the same item over and over,
     * and the bot rebuilds it for ever - which is how a single ship left behind by an offline player
     * starved the water bot for a week and took a level 7 quest with it.
     *
     * The rage state is what is supposed to end that, and this pins the two conditions it needs: the
     * kills have to be counted against the attacker's base, and they must survive the ticks in
     * between. Note what is NOT required - the bot's base is never defeated here. Enrage counts
     * individual item kills, not base deaths.
     */
    @Test
    public void testEnrageUpOnRepeatedKills() {
        setup();

        List<BotConfig> botConfigs = new ArrayList<>();
        botConfigs.add(camperBotConfig(enragingStates()));
        startBots(botConfigs);
        tickBotRunner();

        Assert.assertEquals(1, countBotItems(FallbackConfig.FACTORY_ITEM_TYPE_ID));
        Assert.assertEquals(0, countBotItems(FallbackConfig.ATTACKER_ITEM_TYPE_ID));

        // The intruder has to stay inside the realm: handleIntruders() drops the kill count of every
        // base that has no item in there, so a hit-and-run attacker never builds anything up.
        PlayerBaseFull camper = createHumanBaseWithBaseItem(new DecimalPosition(60, 60), createLevel1UserContext());

        killBotItemOnce(camper);
        killBotItemOnce(camper);
        Assert.assertEquals("Two of three kills - still the normal roster", 1, countBotItems(FallbackConfig.FACTORY_ITEM_TYPE_ID));

        killBotItemOnce(camper);
        Assert.assertEquals("Third kill hits the threshold, the normal roster goes", 0, countBotItems(FallbackConfig.FACTORY_ITEM_TYPE_ID));
        Assert.assertEquals("...and the rage roster takes over", 3, countBotItems(FallbackConfig.ATTACKER_ITEM_TYPE_ID));
    }

    /**
     * The shape (Bot1) Water was actually in: a single enragement state. isEnragementActive is then
     * false whatever enrageUpKills says, so the kills are counted nowhere and the loop never ends.
     * A config check cannot tell this apart from a bot that is meant to have no rage - which is
     * exactly why it went unnoticed - so it is pinned here instead.
     */
    @Test
    public void testSingleEnragementStateNeverRages() {
        setup();

        List<BotEnragementStateConfig> singleState = new ArrayList<>();
        // enrageUpKills is set, as it is on the real (Bot1) Water, and means nothing without a successor.
        singleState.add(new BotEnragementStateConfig().name("Normal").enrageUpKills(3).botItems(normalItems()));

        List<BotConfig> botConfigs = new ArrayList<>();
        botConfigs.add(camperBotConfig(singleState));
        startBots(botConfigs);
        tickBotRunner();

        PlayerBaseFull camper = createHumanBaseWithBaseItem(new DecimalPosition(60, 60), createLevel1UserContext());

        for (int kill = 0; kill < 10; kill++) {
            killBotItemOnce(camper);
        }

        Assert.assertEquals("Rebuilds for ever", 1, countBotItems(FallbackConfig.FACTORY_ITEM_TYPE_ID));
        Assert.assertEquals("Nothing ever fights back", 0, countBotItems(FallbackConfig.ATTACKER_ITEM_TYPE_ID));
    }

    private List<BotEnragementStateConfig> enragingStates() {
        List<BotItemConfig> rageItems = new ArrayList<>();
        // createDirectly, or the rage roster would need the builder the camper is killing too.
        rageItems.add(new BotItemConfig().baseItemTypeId(FallbackConfig.ATTACKER_ITEM_TYPE_ID).count(3).createDirectly(true)
                .place(new PlaceConfig().polygon2D(Polygon2D.fromRectangle(30, 30, 20, 20))));

        List<BotEnragementStateConfig> states = new ArrayList<>();
        states.add(new BotEnragementStateConfig().name("Normal").enrageUpKills(3).botItems(normalItems()));
        states.add(new BotEnragementStateConfig().name("Rage 1").botItems(rageItems));
        return states;
    }

    private List<BotItemConfig> normalItems() {
        List<BotItemConfig> normalItems = new ArrayList<>();
        normalItems.add(new BotItemConfig().baseItemTypeId(FallbackConfig.FACTORY_ITEM_TYPE_ID).count(1).createDirectly(true)
                .place(new PlaceConfig().polygon2D(Polygon2D.fromRectangle(30, 30, 20, 20))));
        return normalItems;
    }

    private BotConfig camperBotConfig(List<BotEnragementStateConfig> states) {
        return new BotConfig().id(1).actionDelay(1).name("Kenny").npc(false)
                .realm(new PlaceConfig().polygon2D(Polygon2D.fromRectangle(20, 20, 100, 100)))
                .botEnragementStateConfigs(states);
    }

    /**
     * One turn of the loop: the camper kills the bot's item, and the bot ticks - which is where it
     * rebuilds and where the intruder handling runs.
     */
    private void killBotItemOnce(PlayerBaseFull camper) {
        SyncBaseItem victim = findFirstBotItemHighestId(1, FallbackConfig.FACTORY_ITEM_TYPE_ID);
        getBotService().onKill(victim, camper);
        tickBotRunner();
    }

    private SyncBaseItem findBotItemOrNull(int baseItemTypeId) {
        for (SyncBaseItem syncBaseItem : ((PlayerBaseFull) getBotBase(1)).getItems()) {
            if (syncBaseItem.getBaseItemType().getId() == baseItemTypeId) {
                return syncBaseItem;
            }
        }
        return null;
    }

    private int countBotItems(int baseItemTypeId) {
        int count = 0;
        for (SyncBaseItem syncBaseItem : ((PlayerBaseFull) getBotBase(1)).getItems()) {
            if (syncBaseItem.getBaseItemType().getId() == baseItemTypeId) {
                count++;
            }
        }
        return count;
    }
}