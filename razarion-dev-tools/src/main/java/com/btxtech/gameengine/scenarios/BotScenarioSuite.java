package com.btxtech.gameengine.scenarios;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.dto.AbstractBotCommandConfig;
import com.btxtech.shared.dto.BotHarvestCommandConfig;
import com.btxtech.shared.dto.BotKillHumanCommandConfig;
import com.btxtech.shared.dto.BotKillOtherBotCommandConfig;
import com.btxtech.shared.dto.BotMoveCommandConfig;
import com.btxtech.shared.dto.BotRemoveOwnItemCommandConfig;
import com.btxtech.shared.dto.KillBotCommandConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotEnragementStateConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 07.11.2016.
 */
public class BotScenarioSuite extends ScenarioSuite {
    public BotScenarioSuite() {
        super("Bot");
    }

    @Override
    protected void setupScenarios() {
        addScenario(new Scenario("Move command") {
            @Override
            public void setupBots(Collection<BotConfig> botConfigs) {
                List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
                List<BotItemConfig> botItems = new ArrayList<>();
                botItems.add(new BotItemConfig().setBaseItemTypeId(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE.getId()).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(0, 0))).setNoSpawn(true));
                botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
                botConfigs.add(new BotConfig().setId(1).setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Kenny").setNpc(true));
            }

            @Override
            public void setupBotCommands(Collection<AbstractBotCommandConfig> botCommandConfigs) {
                botCommandConfigs.add(new BotMoveCommandConfig().setBotId(1).setTargetPosition(new DecimalPosition(0, 20)).setBaseItemTypeId(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE.getId()));
            }
        });
        // 39
        addScenario(new Scenario("Harvest command") {
            @Override
            protected void createSyncItems() {
                createSyncResourceItem(ScenarioService.RESOURCE_ITEM_TYPE, new DecimalPosition(20, 0));
            }

            @Override
            public void setupBots(Collection<BotConfig> botConfigs) {
                List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
                List<BotItemConfig> botItems = new ArrayList<>();
                botItems.add(new BotItemConfig().setBaseItemTypeId(ScenarioService.HARVESTER_ITEM_TYPE.getId()).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(0, 0))).setNoSpawn(true));
                botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
                botConfigs.add(new BotConfig().setId(1).setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Kenny").setNpc(true));
            }

            @Override
            public void setupBotCommands(Collection<AbstractBotCommandConfig> botCommandConfigs) {
                botCommandConfigs.add(new BotHarvestCommandConfig().setBotId(1).setResourceItemTypeId(ScenarioService.RESOURCE_ITEM_TYPE.getId()).setResourceSelection(new PlaceConfig().setPosition(new DecimalPosition(20, 0))).setHarvesterItemTypeId(ScenarioService.HARVESTER_ITEM_TYPE.getId()));
            }
        });
        addScenario(new Scenario("Kill other bot command") {
            @Override
            public void setupBots(Collection<BotConfig> botConfigs) {
                // Target bot
                List<BotEnragementStateConfig> targetEnragementStates = new ArrayList<>();
                List<BotItemConfig> targetItems = new ArrayList<>();
                targetItems.add(new BotItemConfig().setBaseItemTypeId(ScenarioService.HARVESTER_ITEM_TYPE.getId()).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(20, 20))).setNoSpawn(true).setNoRebuild(true));
                targetItems.add(new BotItemConfig().setBaseItemTypeId(ScenarioService.HARVESTER_ITEM_TYPE.getId()).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(20, 25))).setNoSpawn(true).setNoRebuild(true));
                targetItems.add(new BotItemConfig().setBaseItemTypeId(ScenarioService.HARVESTER_ITEM_TYPE.getId()).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(25, 20))).setNoSpawn(true).setNoRebuild(true));
                targetItems.add(new BotItemConfig().setBaseItemTypeId(ScenarioService.HARVESTER_ITEM_TYPE.getId()).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(25, 25))).setNoSpawn(true).setNoRebuild(true));
                targetItems.add(new BotItemConfig().setBaseItemTypeId(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE.getId()).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(30, 30))).setNoSpawn(true).setNoRebuild(true));
                targetItems.add(new BotItemConfig().setBaseItemTypeId(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE.getId()).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(30, 35))).setNoSpawn(true).setNoRebuild(true));
                targetItems.add(new BotItemConfig().setBaseItemTypeId(ScenarioService.ATTACKER_ITEM_TYPE.getId()).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(35, 30))).setNoSpawn(true).setNoRebuild(true));
                targetItems.add(new BotItemConfig().setBaseItemTypeId(ScenarioService.ATTACKER_ITEM_TYPE.getId()).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(35, 35))).setNoSpawn(true).setNoRebuild(true));
                targetEnragementStates.add(new BotEnragementStateConfig().setName("Normal").setBotItems(targetItems));
                botConfigs.add(new BotConfig().setId(1).setActionDelay(3000).setBotEnragementStateConfigs(targetEnragementStates).setName("Kenny").setNpc(true));
                // Attacker bot
                List<BotEnragementStateConfig> attackerEnragementStates = new ArrayList<>();
                List<BotItemConfig> attackerItems = new ArrayList<>();
                attackerItems.add(new BotItemConfig().setBaseItemTypeId(ScenarioService.ATTACKER_ITEM_TYPE.getId()).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(0, 0))).setNoSpawn(true).setNoRebuild(true));
                attackerEnragementStates.add(new BotEnragementStateConfig().setName("Normal").setBotItems(attackerItems));
                botConfigs.add(new BotConfig().setId(2).setActionDelay(3000).setBotEnragementStateConfigs(attackerEnragementStates).setName("Kenny").setNpc(false));
            }

            @Override
            public void setupBotCommands(Collection<AbstractBotCommandConfig> botCommandConfigs) {
                BotKillOtherBotCommandConfig commandConfig = new BotKillOtherBotCommandConfig().setBotId(2).setTargetBotId(1).setAttackerBaseItemTypeId(ScenarioService.ATTACKER_ITEM_TYPE.getId());
                commandConfig.setDominanceFactor(2).setSpawnPoint(new PlaceConfig().setPolygon2D(new Rectangle2D(0, 0, 15, 15).toPolygon()));
                botCommandConfigs.add(commandConfig);
            }
        });
        addScenario(new Scenario("Kill human command") {
            @Override
            protected void createSyncItems() {
                createSyncBaseItem(ScenarioService.ATTACKER_ITEM_TYPE, new DecimalPosition(20, 20), null);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(20, 25), null);
                createSyncBaseItem(ScenarioService.HARVESTER_ITEM_TYPE, new DecimalPosition(25, 20), null);
                createSyncBaseItem(ScenarioService.HARVESTER_ITEM_TYPE, new DecimalPosition(25, 25), null);
            }

            @Override
            public void setupBots(Collection<BotConfig> botConfigs) {
                // Attacker bot
                List<BotEnragementStateConfig> attackerEnragementStates = new ArrayList<>();
                List<BotItemConfig> attackerItems = new ArrayList<>();
                attackerItems.add(new BotItemConfig().setBaseItemTypeId(ScenarioService.ATTACKER_ITEM_TYPE.getId()).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(0, 0))).setNoSpawn(true).setNoRebuild(true));
                attackerEnragementStates.add(new BotEnragementStateConfig().setName("Normal").setBotItems(attackerItems));
                botConfigs.add(new BotConfig().setId(2).setActionDelay(3000).setBotEnragementStateConfigs(attackerEnragementStates).setName("Kenny").setNpc(false));
            }

            @Override
            public void setupBotCommands(Collection<AbstractBotCommandConfig> botCommandConfigs) {
                BotKillHumanCommandConfig commandConfig = new BotKillHumanCommandConfig().setBotId(2).setAttackerBaseItemTypeId(ScenarioService.ATTACKER_ITEM_TYPE.getId());
                commandConfig.setDominanceFactor(2).setSpawnPoint(new PlaceConfig().setPolygon2D(new Rectangle2D(0, 0, 15, 15).toPolygon()));
                botCommandConfigs.add(commandConfig);
            }
        });
        addScenario(new Scenario("Remove Own Item Command Configs") {

            @Override
            public void setupBots(Collection<BotConfig> botConfigs) {
                // Attacker bot
                List<BotEnragementStateConfig> attackerEnragementStates = new ArrayList<>();
                List<BotItemConfig> botItemConfigs = new ArrayList<>();
                botItemConfigs.add(new BotItemConfig().setBaseItemTypeId(ScenarioService.ATTACKER_ITEM_TYPE.getId()).setCount(10).setCreateDirectly(true).setPlace(new PlaceConfig().setPolygon2D(new Rectangle2D(0,0, 20,20).toPolygon())).setNoSpawn(true).setNoRebuild(true));
                botItemConfigs.add(new BotItemConfig().setBaseItemTypeId(ScenarioService.HARVESTER_ITEM_TYPE.getId()).setCount(5).setCreateDirectly(true).setPlace(new PlaceConfig().setPolygon2D(new Rectangle2D(0,30, 20,20).toPolygon())).setNoSpawn(true).setNoRebuild(true));
                attackerEnragementStates.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItemConfigs));
                botConfigs.add(new BotConfig().setId(1).setActionDelay(3000).setBotEnragementStateConfigs(attackerEnragementStates).setName("Kenny").setNpc(false));
            }

            @Override
            public void setupBotCommands(Collection<AbstractBotCommandConfig> botCommandConfigs) {
                botCommandConfigs.add(new BotRemoveOwnItemCommandConfig().setBotId(1).setBaseItemType2RemoveId(ScenarioService.ATTACKER_ITEM_TYPE.getId()));
            }
        });
        addScenario(new Scenario("Kill Bot Command Configs") {

            @Override
            public void setupBots(Collection<BotConfig> botConfigs) {
                // Attacker bot
                List<BotEnragementStateConfig> attackerEnragementStates = new ArrayList<>();
                List<BotItemConfig> botItemConfigs = new ArrayList<>();
                botItemConfigs.add(new BotItemConfig().setBaseItemTypeId(ScenarioService.ATTACKER_ITEM_TYPE.getId()).setCount(10).setCreateDirectly(true).setPlace(new PlaceConfig().setPolygon2D(new Rectangle2D(0,0, 20,20).toPolygon())).setNoSpawn(true).setNoRebuild(true));
                botItemConfigs.add(new BotItemConfig().setBaseItemTypeId(ScenarioService.HARVESTER_ITEM_TYPE.getId()).setCount(5).setCreateDirectly(true).setPlace(new PlaceConfig().setPolygon2D(new Rectangle2D(0,30, 20,20).toPolygon())).setNoSpawn(true).setNoRebuild(true));
                attackerEnragementStates.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItemConfigs));
                botConfigs.add(new BotConfig().setId(1).setActionDelay(3000).setBotEnragementStateConfigs(attackerEnragementStates).setName("Kenny").setNpc(false));
            }

            @Override
            public void setupBotCommands(Collection<AbstractBotCommandConfig> botCommandConfigs) {
                botCommandConfigs.add(new KillBotCommandConfig().setBotId(1));
            }

            @Override
            public boolean isStart() {
                return true;
            }
        });
    }
}
