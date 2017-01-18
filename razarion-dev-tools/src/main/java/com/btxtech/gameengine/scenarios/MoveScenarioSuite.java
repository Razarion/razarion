package com.btxtech.gameengine.scenarios;

import com.btxtech.shared.datatypes.DecimalPosition;

/**
 * Created by Beat
 * 07.11.2016.
 */
public class MoveScenarioSuite extends ScenarioSuite {
    public MoveScenarioSuite() {
        super("Move");
    }

    @Override
    protected void setupScenarios() {
        addScenario(new Scenario("Move east") {
            @Override
            public void createSyncItems() {
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), new DecimalPosition(50, 0));
            }

            @Override
            public boolean isStart() {
                return true;
            }

        });
        addScenario(new Scenario("Move east fast") {
            @Override
            public void createSyncItems() {
                createSyncBaseItem(ScenarioService.SIMPLE_FAST_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), new DecimalPosition(50, 0));
            }
        });
        addScenario(new Scenario("Move South") {
            @Override
            public void createSyncItems() {
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), new DecimalPosition(0, 10));
            }
        });
        addScenario(new Scenario("Move Fast South") {
            @Override
            public void createSyncItems() {
                createSyncBaseItem(ScenarioService.SIMPLE_FAST_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), new DecimalPosition(0, 10));
            }
        });
    }
}
