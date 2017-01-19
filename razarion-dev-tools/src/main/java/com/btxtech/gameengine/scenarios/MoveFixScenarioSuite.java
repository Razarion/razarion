package com.btxtech.gameengine.scenarios;

import com.btxtech.shared.datatypes.DecimalPosition;

/**
 * Created by Beat
 * 07.11.2016.
 */
public class MoveFixScenarioSuite extends ScenarioSuite {
    public MoveFixScenarioSuite() {
        super("Move vs fix");
    }

    @Override
    protected void setupScenarios() {
        addScenario(new Scenario("Frontal") {
            @Override
            public void createSyncItems() {
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), 0, new DecimalPosition(20, 0));
                createSyncBaseItem(ScenarioService.SIMPLE_FIX_ITEM_TYPE, new DecimalPosition(6, 0), 0, null);
            }
        });
        addScenario(new Scenario("Not frontal") {
            @Override
            public void createSyncItems() {
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 1), 0, new DecimalPosition(20, 1));
                createSyncBaseItem(ScenarioService.SIMPLE_FIX_ITEM_TYPE, new DecimalPosition(6, 0), 0, null);
            }
        });
    }
}
