package com.btxtech.gameengine.scenarios;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.utils.MathHelper;

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
        addScenario(new Scenario("Move strait") {
            @Override
            public void createSyncItems() {
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), 0, new DecimalPosition(50, 0));
            }

        });
        addScenario(new Scenario("Move strait down") {
            @Override
            public void createSyncItems() {
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), MathHelper.THREE_QUARTER_RADIANT, new DecimalPosition(0, -30));
            }
        });
        addScenario(new Scenario("Move strait fast") {
            @Override
            public void createSyncItems() {
                createSyncBaseItem(ScenarioService.SIMPLE_FAST_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), 0, new DecimalPosition(50, 0));
            }
        });
        addScenario(new Scenario("Move 90dec") {
            @Override
            public void createSyncItems() {
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), 0, new DecimalPosition(0, 10));
            }
        });
        addScenario(new Scenario("Move 90dec") {
            @Override
            public void createSyncItems() {
                createSyncBaseItem(ScenarioService.SIMPLE_FAST_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), 0, new DecimalPosition(0, 10));
            }
        });
        addScenario(new Scenario("Move 180dec") {
            @Override
            public void createSyncItems() {
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), MathHelper.THREE_QUARTER_RADIANT, new DecimalPosition(0, 30));
            }
        });
    }
}
