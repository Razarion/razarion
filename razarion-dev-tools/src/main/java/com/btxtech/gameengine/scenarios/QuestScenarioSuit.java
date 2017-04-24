package com.btxtech.gameengine.scenarios;

import com.btxtech.shared.datatypes.DecimalPosition;
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
 * 27.02.2017.
 */
public class QuestScenarioSuit extends ScenarioSuite {
    public QuestScenarioSuit() {
        super("Quest");
    }

    @Override
    protected void setupScenarios() {
        addScenario(new Scenario("Kill 1 base") {

            @Override
            protected void createSyncItems() {
                createSyncBaseItem(ScenarioService.ATTACKER_ITEM_TYPE, new DecimalPosition(0, 0), 0, null);
            }

            @Override
            protected void setupBots(Collection<BotConfig> botConfigs) {
                List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
                List<BotItemConfig> botItems = new ArrayList<>();
                botItems.add(new BotItemConfig().setBaseItemTypeId(ScenarioService.HARVESTER_ITEM_TYPE.getId()).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(50, 0))).setNoSpawn(true).setNoRebuild(true));
                botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
                botConfigs.add(new BotConfig().setId(1).setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Kenny").setNpc(false));
            }

            @Override
            public void executeCommands(CommandService commandService) {
                commandService.attack(getFirstCreatedSyncBaseItem(), getFirstBotItem(1), true);
            }

            @Override
            public QuestConfig setupQuest() {
                return new QuestConfig().setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.BASE_KILLED).setComparisonConfig(new ComparisonConfig().setCount(1)));
            }
        });
    }
}
