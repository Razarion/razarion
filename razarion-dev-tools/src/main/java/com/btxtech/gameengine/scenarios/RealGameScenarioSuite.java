package com.btxtech.gameengine.scenarios;

import com.btxtech.persistence.JsonProviderEmulator;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.dto.AbstractBotCommandConfig;
import com.btxtech.shared.dto.BotKillHumanCommandConfig;
import com.btxtech.shared.dto.BotKillOtherBotCommandConfig;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotEnragementStateConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;

import javax.enterprise.inject.spi.CDI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 19.01.2017.
 */
public class RealGameScenarioSuite extends ScenarioSuite {
    private static final int NPC_BOT_OUTPOST = 1;
    private static final int NPC_BOT_OUTPOST_2 = 2;
    private static final int NPC_BOT_INSTRUCTOR = 3;
    private static final int ENEMY_BOT = 4;
    private static final int BASE_ITEM_TYPE_BULLDOZER = 180807;
    private static final int BASE_ITEM_TYPE_HARVESTER = 180830;
    private static final int BASE_ITEM_TYPE_ATTACKER = 180832;
    private static final int BASE_ITEM_TYPE_FACTORY = 272490;
    private static final int BASE_ITEM_TYPE_TOWER = 272495;
    private static final int RESOURCE_ITEM_TYPE = 180829;
    private static final int BOX_ITEM_TYPE = 272481;
    private static final int INVENTORY_ITEM = 1;

    public RealGameScenarioSuite() {
        super("Real Game");
    }

    @Override
    protected void setupScenarios() {
        addScenario(new Scenario("Bot attack") {
            @Override
            public StaticGameConfig setupGameEngineConfig() {
                throw new UnsupportedOperationException("readFromFile() Has been removed from JsonProviderEmulator");
                // return new JsonProviderEmulator().readFromFile(false).getStaticGameConfig();
            }

            @Override
            protected void createSyncItems() {
                getPlayerBase().setLevelId(4);
                // Player Items
                createSyncBaseItem(getBaseItemType(BASE_ITEM_TYPE_ATTACKER), new DecimalPosition(207, 140), 0, null);
                createSyncBaseItem(getBaseItemType(BASE_ITEM_TYPE_ATTACKER), new DecimalPosition(205, 146), 0, null);
                createSyncBaseItem(getBaseItemType(BASE_ITEM_TYPE_ATTACKER), new DecimalPosition(213, 140), 0, null);
                createSyncBaseItem(getBaseItemType(BASE_ITEM_TYPE_BULLDOZER), new DecimalPosition(187, 120), 0, null);
                // Resources
                createSyncResourceItem(getResourceItemType(RESOURCE_ITEM_TYPE), new DecimalPosition(212, 144));
                createSyncResourceItem(getResourceItemType(RESOURCE_ITEM_TYPE), new DecimalPosition(233, 164));
            }

            @Override
            protected void setupBots(Collection<BotConfig> botConfigs) {
                // NPC bot
                List<BotEnragementStateConfig> npcBotEnragementStateConfigs = new ArrayList<>();
                List<BotItemConfig> npcBotItems = new ArrayList<>();
                npcBotItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_HARVESTER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(223, 130))).setAngle(Math.toRadians(110)).setNoSpawn(true).setNoRebuild(true));
                npcBotItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_FACTORY).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(220, 109))).setNoSpawn(true).setNoRebuild(true));
                npcBotItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_FACTORY).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(213, 92))).setNoSpawn(true).setNoRebuild(true));
                npcBotItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_BULLDOZER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(207, 111))).setAngle(Math.toRadians(30)).setNoSpawn(true).setNoRebuild(true));
                npcBotItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_ATTACKER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(201, 94))).setAngle(Math.toRadians(175)).setNoSpawn(true).setNoRebuild(true));
                npcBotItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_HARVESTER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(201, 88))).setAngle(Math.toRadians(310)).setNoSpawn(true).setNoRebuild(true));
                npcBotEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(npcBotItems));
                botConfigs.add(new BotConfig().setId(NPC_BOT_OUTPOST).setActionDelay(3000).setBotEnragementStateConfigs(npcBotEnragementStateConfigs).setName("Roger").setNpc(true));
                // Enemy bot
                List<BotEnragementStateConfig> enemyBotEnragementStateConfigs = new ArrayList<>();
                List<BotItemConfig> enemyBotItems = new ArrayList<>();
                enemyBotItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_TOWER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(190, 242))).setNoSpawn(true).setNoRebuild(true));
                enemyBotItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_FACTORY).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(248, 283))).setNoSpawn(true).setNoRebuild(true));
                enemyBotItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_FACTORY).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(277, 296))).setNoSpawn(true).setNoRebuild(true));
                enemyBotItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_FACTORY).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(299, 261))).setNoSpawn(true).setNoRebuild(true));
                enemyBotItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_BULLDOZER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(240, 255))).setAngle(Math.toRadians(100)).setNoSpawn(true).setNoRebuild(true));
                enemyBotItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_BULLDOZER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(277, 252))).setAngle(Math.toRadians(200)).setNoSpawn(true).setNoRebuild(true));
                enemyBotItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_BULLDOZER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(260, 227))).setAngle(Math.toRadians(333)).setNoSpawn(true).setNoRebuild(true));
                // Attackers 4 harvester
                enemyBotItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_ATTACKER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(230, 187))).setAngle(Math.toRadians(260)).setNoSpawn(true).setNoRebuild(true));
                enemyBotItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_ATTACKER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(234, 187))).setAngle(Math.toRadians(260)).setNoSpawn(true).setNoRebuild(true));
                // Harvester to harvest after attack
                enemyBotItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_HARVESTER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(253, 200))).setAngle(Math.toRadians(240)).setNoSpawn(true).setNoRebuild(true));

                enemyBotEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(enemyBotItems));
                botConfigs.add(new BotConfig().setId(ENEMY_BOT).setActionDelay(3000).setBotEnragementStateConfigs(enemyBotEnragementStateConfigs).setName("Razar Industries").setNpc(false));
            }

            @Override
            public void setupBotCommands(Collection<AbstractBotCommandConfig> botCommandConfigs) {
                // Kill bot command
                botCommandConfigs.add(new BotKillOtherBotCommandConfig().setBotAuxiliaryId(ENEMY_BOT).setTargetBotAuxiliaryId(NPC_BOT_OUTPOST).setDominanceFactor(1).setAttackerBaseItemTypeId(BASE_ITEM_TYPE_ATTACKER).setSpawnPoint(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(213, 209 , 80, 70))));
                // Kill human command
                botCommandConfigs.add(new BotKillHumanCommandConfig().setBotAuxiliaryId(ENEMY_BOT).setDominanceFactor(2).setAttackerBaseItemTypeId(BASE_ITEM_TYPE_ATTACKER).setSpawnPoint(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(213, 209 , 80, 70))));
            }
        });
    }

    private BaseItemType getBaseItemType(int baseItemTypeId) {
        return CDI.current().select(ItemTypeService.class).get().getBaseItemType(baseItemTypeId);
    }

    private ResourceItemType getResourceItemType(int resourceItemTypeId) {
        return CDI.current().select(ItemTypeService.class).get().getResourceItemType(resourceItemTypeId);
    }
}
