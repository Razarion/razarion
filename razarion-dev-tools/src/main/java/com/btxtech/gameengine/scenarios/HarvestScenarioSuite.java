package com.btxtech.gameengine.scenarios;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.config.ComparisonConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.planet.CommandService;

/**
 * Created by Beat
 * 07.11.2016.
 */
public class HarvestScenarioSuite extends ScenarioSuite {
    public HarvestScenarioSuite() {
        super("Harvest");
    }

    @Override
    protected void setupScenarios() {
        addScenario(new Scenario("Move around resource") {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(20, 0);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), direction);
                createSyncResourceItem(ScenarioService.RESOURCE_ITEM_TYPE, new DecimalPosition(10, 0));
            }
        });

        addScenario(new Scenario("Harvest command") {
            @Override
            public void createSyncItems() {
                createSyncBaseItem(ScenarioService.HARVESTER_ITEM_TYPE, new DecimalPosition(0, 0), null);
                createSyncResourceItem(ScenarioService.RESOURCE_ITEM_TYPE, new DecimalPosition(0, 20));
            }

            @Override
            public void executeCommands(CommandService commandService) {
                commandService.harvest(getFirstCreatedSyncBaseItem(), getFirstCreatedSyncResourceItem());
            }

            @Override
            public QuestConfig setupQuest() {
                return new QuestConfig().setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.HARVEST).setComparisonConfig(new ComparisonConfig().setCount(100)));
            }

            @Override
            public boolean isStart() {
                return true;
            }
        });

    }
}
