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
        addScenario(new Scenario("Move West") {

            @Override
            public void createSyncItems() {
                // createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), new DecimalPosition(-100, 0));
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), new DecimalPosition(0, 7));
                // createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), new DecimalPosition(20, 0));
            }
        });


    }


}
