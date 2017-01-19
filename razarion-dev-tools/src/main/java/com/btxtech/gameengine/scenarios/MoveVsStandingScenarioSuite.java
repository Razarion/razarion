package com.btxtech.gameengine.scenarios;

import com.btxtech.shared.datatypes.DecimalPosition;

/**
 * Created by Beat
 * 07.11.2016.
 */
public class MoveVsStandingScenarioSuite extends ScenarioSuite {
    public MoveVsStandingScenarioSuite() {
        super("Move vs standing");
    }

    @Override
    protected void setupScenarios() {
        addScenario(new Scenario("Not frontal") {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(20, 0);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), 0, direction);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(10, 1), 0, null);
            }
        });
        addScenario(new Scenario("Frontal") {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(20, 0);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), 0, direction);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(10, 0), 0, null);
            }
        });
        addScenario(new Scenario("Moving around 2 standing") {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(30, 0);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), 0, direction);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(6, 0), 0, null);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(10, 0), 0, null);
            }
        });
        addScenario(new Scenario("Move around group") {
            @Override
            public void createSyncItems() {
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-30, 0), 0, new DecimalPosition(50, 0));
                // createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-10, 10), new DecimalPosition(20, 10));
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(4 * x, 4 * y), 0, null);
                    }
                }
            }
        });
        addScenario(new Scenario("Group moving vs standing") {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(50, 0);
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(4 * x, 4 * y), 0, direction);
                    }
                }
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(20, 0), 0, null);

            }
        });
        addScenario(new Scenario("Group moving vs group standing") {
            @Override
            public void createSyncItems() {
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(4 * x + 10, 4 * y), 0, null);
                    }
                }
                DecimalPosition direction = new DecimalPosition(50, 0);
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(4 * x - 20, 4 * y), 0, direction);
                    }
                }
            }
        });
    }
}
