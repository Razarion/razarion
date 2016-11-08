package com.btxtech.gameengine.scenarios;

import com.btxtech.shared.datatypes.DecimalPosition;

/**
 * Created by Beat
 * 07.11.2016.
 */
public class MoveSamePositionScenarioSuite extends ScenarioSuite {
    public MoveSamePositionScenarioSuite() {
        super("Move to same position");
    }

    @Override
    protected void setupScenarios() {
        addScenario(new Scenario("Two middle one wrong direction") {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(0, 0);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-3, 0), direction);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(3, 0), direction);
            }
        });
        addScenario(new Scenario("One position reached 1 moving") {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(0, 0);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), direction);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(4, 0), direction);
            }
        });
        addScenario(new Scenario("One position reached 2 moving") {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(0, 0);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), direction);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(4, 0), direction);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-4, 0), direction);
            }
        });
        addScenario(new Scenario("One position reached 3 moving") {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(0, 0);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(2, 0), direction);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(6, 0), direction);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-6, 0), direction);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-2, 0), direction);
            }
        });
        addScenario(new Scenario("One position reached (middle) 8 moving") {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(0, 0);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-16, 0), direction);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-12, 0), direction);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-8, 0), direction);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-4, 0), direction);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), direction);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(4, 0), direction);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(8, 0), direction);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(12, 0), direction);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(16, 0), direction);
            }
        });
        addScenario(new Scenario("Two moving to middle same direction") {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(3, 0);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 10), direction);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, -10), direction);
            }
        });
        addScenario(new Scenario("Group moving") {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(50, 0);
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(4 * x, 4 * y), direction);
                    }
                }
            }
        });

    }
}
