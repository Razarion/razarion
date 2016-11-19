package com.btxtech.gameengine.scenarios;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.config.ComparisonConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.planet.CommandService;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beat
 * 11.11.2016.
 */
public class FabricateScenarioSuite extends ScenarioSuite {
    public FabricateScenarioSuite() {
        super("Fabricate");
    }

    @Override
    protected void setupScenarios() {
        addScenario(new Scenario("Fabricate") {
            @Override
            protected void createSyncItems() {
                createSyncBaseItem(ScenarioService.FACTORY_ITEM_TYPE, new DecimalPosition(0, 0), null);
            }

            @Override
            public void executeCommands(CommandService commandService) {
                commandService.fabricate(getFirstCreatedSyncBaseItem(), ScenarioService.HARVESTER_ITEM_TYPE);
            }

            @Override
            public QuestConfig setupQuest() {
                Map<Integer, Integer> buildupItemTypeCount = new HashMap<>();
                buildupItemTypeCount.put(ScenarioService.HARVESTER_ITEM_TYPE.getId(), 1);
                ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setTypeCount(buildupItemTypeCount));
                return new QuestConfig().setConditionConfig(conditionConfig);
            }
        });
    }
}
