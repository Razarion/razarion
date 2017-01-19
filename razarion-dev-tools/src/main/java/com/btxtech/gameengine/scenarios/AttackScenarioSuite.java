package com.btxtech.gameengine.scenarios;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.AbstractBotCommandConfig;
import com.btxtech.shared.dto.BotMoveCommandConfig;
import com.btxtech.shared.gameengine.datatypes.config.ComparisonConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotEnragementStateConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;
import com.btxtech.shared.gameengine.planet.CommandService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 07.11.2016.
 */
public class AttackScenarioSuite extends ScenarioSuite {
    public AttackScenarioSuite() {
        super("Attack");
    }

    @Override
    protected void setupScenarios() {
        addScenario(new Scenario("Attacker vs harvester") {
            @Override
            protected void createSyncItems() {
                createSyncBaseItem(ScenarioService.ATTACKER_ITEM_TYPE, new DecimalPosition(0, 0), 0, null);
            }

            @Override
            public void setupBots(Collection<BotConfig> botConfigs) {
                List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
                List<BotItemConfig> botItems = new ArrayList<>();
                botItems.add(new BotItemConfig().setBaseItemTypeId(ScenarioService.HARVESTER_ITEM_TYPE.getId()).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(0, 20))).setNoSpawn(true).setNoRebuild(true));
                botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
                botConfigs.add(new BotConfig().setId(1).setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Kenny").setNpc(false));
            }

            @Override
            public void executeCommands(CommandService commandService) {
                commandService.attack(getFirstCreatedSyncBaseItem(), getFirstBotItem(1), true);
            }

            @Override
            public QuestConfig setupQuest() {
                return new QuestConfig().setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_KILLED).setComparisonConfig(new ComparisonConfig().setCount(1)));
            }
        });

        addScenario(new Scenario("Tower vs harvester") {
            @Override
            protected void createSyncItems() {
                createSyncBaseItem(ScenarioService.TOWER_ITEM_TYPE, new DecimalPosition(10, 5), 0, null);
            }

            @Override
            public void setupBots(Collection<BotConfig> botConfigs) {
                List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
                List<BotItemConfig> botItems = new ArrayList<>();
                botItems.add(new BotItemConfig().setBaseItemTypeId(ScenarioService.HARVESTER_ITEM_TYPE.getId()).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(50, 0))).setNoSpawn(true).setNoRebuild(true));
                botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
                botConfigs.add(new BotConfig().setId(1).setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Kenny").setNpc(false));
            }


            @Override
            public void setupBotCommands(Collection<AbstractBotCommandConfig> botCommandConfigs) {
                botCommandConfigs.add(new BotMoveCommandConfig().setBotId(1).setTargetPosition(new DecimalPosition(0, 10)).setBaseItemTypeId(ScenarioService.HARVESTER_ITEM_TYPE.getId()));
            }

            @Override
            public QuestConfig setupQuest() {
                return new QuestConfig().setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_KILLED).setComparisonConfig(new ComparisonConfig().setCount(1)));
            }
        });
    }
}
