package com.btxtech.gameengine.scenarios;

import com.btxtech.shared.datatypes.DecimalPosition;

/**
 * Created by Beat
 * 07.11.2016.
 */
public class MoveVsMovingScenarioSuite extends ScenarioSuite {
    public MoveVsMovingScenarioSuite() {
        super("Move vs moving");
    }

    @Override
    protected void setupScenarios() {
        addScenario(new Scenario("Crash sideways") {
            @Override
            public void createSyncItems() {
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-6, 10), new DecimalPosition(20, 10));
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(6, 0), new DecimalPosition(-20, 0));
            }
        });
        addScenario(new Scenario("Move mutual places") {
            @Override
            public void createSyncItems() {
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-3, 0), new DecimalPosition(10, 0));
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(3, 0), new DecimalPosition(-10, 0));
            }
        });
    }
}
