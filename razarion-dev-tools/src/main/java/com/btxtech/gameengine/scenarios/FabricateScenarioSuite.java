package com.btxtech.gameengine.scenarios;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.CommandService;

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
                commandService.fabricate(getFirstCreatedSyncBaseItem(), ScenarioService.BUILDER_ITEM_TYPE);
            }
        });
    }
}
