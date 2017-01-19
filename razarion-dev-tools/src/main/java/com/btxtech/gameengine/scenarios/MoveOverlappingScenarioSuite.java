package com.btxtech.gameengine.scenarios;

import com.btxtech.shared.datatypes.DecimalPosition;

/**
 * Created by Beat
 * 07.11.2016.
 */
public class MoveOverlappingScenarioSuite extends ScenarioSuite {
    public MoveOverlappingScenarioSuite() {
        super("Move overlapping");
    }

    @Override
    protected void setupScenarios() {
        addScenario(new Scenario("Two overlapping") {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(20, 0);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), 0, direction);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(1, 0), 0, direction);
            }
        });
    }
}
