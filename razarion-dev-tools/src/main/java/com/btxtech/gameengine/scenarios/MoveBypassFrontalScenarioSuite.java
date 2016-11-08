package com.btxtech.gameengine.scenarios;

import com.btxtech.shared.datatypes.DecimalPosition;

/**
 * Created by Beat
 * 07.11.2016.
 */
public class MoveBypassFrontalScenarioSuite extends ScenarioSuite {
    public MoveBypassFrontalScenarioSuite() {
        super("Move bypass frontal");
    }

    @Override
    protected void setupScenarios() {
        addScenario(new Scenario("Movable vs fix") {
            @Override
            public void createSyncItems() {
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), new DecimalPosition(10, 0));
                createSyncBaseItem(ScenarioService.SIMPLE_FIX_ITEM_TYPE, new DecimalPosition(5, 0), null);
            }
        });
        addScenario(new Scenario("Movable vs two standing and 3 fix") {
            @Override
            public void createSyncItems() {
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-6, 0), new DecimalPosition(20, 0));
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), null);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(4, 0), null);
                createSyncBaseItem(ScenarioService.SIMPLE_FIX_ITEM_TYPE, new DecimalPosition(8, 0), null);
            }
        });
    }
}
