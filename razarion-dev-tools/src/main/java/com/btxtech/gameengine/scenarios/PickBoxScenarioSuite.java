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
public class PickBoxScenarioSuite extends ScenarioSuite {
    public PickBoxScenarioSuite() {
        super("Pick box");
    }

    @Override
    protected void setupScenarios() {
        addScenario(new Scenario("Pick box quest east") {
            @Override
            protected void createSyncItems() {
                createSyncBaseItem(ScenarioService.ATTACKER_ITEM_TYPE, new DecimalPosition(0, 0), null);
                createBoxItem(ScenarioService.BOX_ITEM_TYPE, new DecimalPosition(15, 0));
            }

            @Override
            public void executeCommands(CommandService commandService) {
                commandService.pickupBox(getFirstCreatedSyncBaseItem(), getFirstCreatedSyncBoxItem());
            }

            @Override
            public QuestConfig setupQuest() {
                return new QuestConfig().setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.BOX_PICKED).setComparisonConfig(new ComparisonConfig().setCount(1)));
            }

            @Override
            public boolean isStart() {
                return true;
            }
        });
        addScenario(new Scenario("Pick box quest north") {
            @Override
            protected void createSyncItems() {
                createSyncBaseItem(ScenarioService.ATTACKER_ITEM_TYPE, new DecimalPosition(0, 0), null);
                createBoxItem(ScenarioService.BOX_ITEM_TYPE, new DecimalPosition(0, 15));
            }

            @Override
            public void executeCommands(CommandService commandService) {
                commandService.pickupBox(getFirstCreatedSyncBaseItem(), getFirstCreatedSyncBoxItem());
            }

            @Override
            public QuestConfig setupQuest() {
                return new QuestConfig().setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.BOX_PICKED).setComparisonConfig(new ComparisonConfig().setCount(1)));
            }
        });
        addScenario(new Scenario("Pick box quest south") {
            @Override
            protected void createSyncItems() {
                createSyncBaseItem(ScenarioService.ATTACKER_ITEM_TYPE, new DecimalPosition(0, 0), null);
                createBoxItem(ScenarioService.BOX_ITEM_TYPE, new DecimalPosition(0, -80));
            }

            @Override
            public void executeCommands(CommandService commandService) {
                commandService.pickupBox(getFirstCreatedSyncBaseItem(), getFirstCreatedSyncBoxItem());
            }

            @Override
            public QuestConfig setupQuest() {
                return new QuestConfig().setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.BOX_PICKED).setComparisonConfig(new ComparisonConfig().setCount(1)));
            }
        });
    }
}
