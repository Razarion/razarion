package com.btxtech.gameengine.scenarios;

import com.btxtech.shared.datatypes.DecimalPosition;

/**
 * Created by Beat
 * 07.11.2016.
 */
public class MoveSingleOutOfGroupScenarioSuite extends ScenarioSuite {
    public MoveSingleOutOfGroupScenarioSuite() {
        super("Move single out of group");
    }

    @Override
    protected void setupScenarios() {
        addScenario(new Scenario("Middle move east") {
            @Override
            public void createSyncItems() {
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        DecimalPosition destination = null;
                        if (x == 0 && y == 0) {
                            destination = new DecimalPosition(50, 0);
                        }
                        createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(4 * x, 4 * y), 0, destination);
                    }
                }
            }
        });
        addScenario(new Scenario("Left move east") {
            @Override
            public void createSyncItems() {
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        DecimalPosition destination = null;
                        if (x == -2 && y == 0) {
                            destination = new DecimalPosition(50, 0);
                        }
                        createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(4 * x, 4 * y), 0, destination);
                    }
                }
            }
        });
    }
}
