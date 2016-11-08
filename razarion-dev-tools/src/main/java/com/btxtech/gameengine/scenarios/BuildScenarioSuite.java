package com.btxtech.gameengine.scenarios;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.CommandService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;

/**
 * Created by Beat
 * 07.11.2016.
 */
public class BuildScenarioSuite extends ScenarioSuite {
    public BuildScenarioSuite() {
        super("Build");
    }

    @Override
    protected void setupScenarios() {
        addScenario(new Scenario("Build") {
            @Override
            protected void createSyncItems() {
                createSyncBaseItem(ScenarioService.BUILDER_ITEM_TYPE, new DecimalPosition(0, 0), null);
            }

            @Override
            public void executeCommands(CommandService commandService) {
                commandService.build(getFirstCreatedSyncBaseItem(), new DecimalPosition(20, 0), ScenarioService.FACTORY_ITEM_TYPE);
            }
        });
        addScenario(new Scenario("Finalize build") {
            @Override
            protected void createSyncItems() {
                createSyncBaseItem(ScenarioService.BUILDER_ITEM_TYPE, new DecimalPosition(0, 0), null);
                SyncBaseItem factory = createSyncBaseItem(ScenarioService.FACTORY_ITEM_TYPE, new DecimalPosition(0, 20), null);
                factory.setBuildup(0.5);
            }

            @Override
            public void executeCommands(CommandService commandService) {
                commandService.finalizeBuild(getFirstCreatedSyncBaseItem(), getSecondCreatedSyncBaseItem());
            }
        });

    }
}
