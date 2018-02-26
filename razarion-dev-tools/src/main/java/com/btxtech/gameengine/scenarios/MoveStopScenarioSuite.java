package com.btxtech.gameengine.scenarios;

import com.btxtech.shared.datatypes.DecimalPosition;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by Beat
 * 07.11.2016.
 */
public class MoveStopScenarioSuite extends ScenarioSuite {
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public MoveStopScenarioSuite() {
        super("Move stop");
    }

    @Override
    protected void setupScenarios() {
        addScenario(new Scenario("Simple stop") {
            @Override
            public void createSyncItems() {
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), 0, new DecimalPosition(10, 0));
            }
        });
        addScenario(new Scenario("Destination occupied") {
            @Override
            public void createSyncItems() {
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-10, 0), 0, new DecimalPosition(0, 0));
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-2, 0), 0, null);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), 0, null);
            }
        });
        addScenario(new Scenario("Tow move to same destination with same angle") {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(5, 0);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, -10), 0, direction);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 10), 0, direction);
            }
        });
        addScenario(new Scenario("Destination inside group") {
            @Override
            public void createSyncItems() {
                DecimalPosition destination = new DecimalPosition(0, 0);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-30, 0), 0, destination);
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(4 * x, 4 * y), 0, destination);
                    }
                }
            }
        });
        addScenario(new Scenario("Group move to same position") {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(50, 0);
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(6 * x, 6 * y), 0, direction);
                    }
                }
            }
        });
        addScenario(new Scenario("Factory") {
            private ScheduledFuture backgroundWorker;

            @Override
            public void createSyncItems() {
                backgroundWorker = scheduler.scheduleAtFixedRate((Runnable) () -> {
                    try {
                        createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-20, 0), 0, new DecimalPosition(20, 0));
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }, 1000, 1000, TimeUnit.MILLISECONDS);
            }

            @Override
            public void stop() {
                if (backgroundWorker != null) {
                    backgroundWorker.cancel(true);
                    backgroundWorker = null;
                }
            }
        });
    }
}
