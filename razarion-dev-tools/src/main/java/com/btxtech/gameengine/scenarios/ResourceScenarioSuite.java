package com.btxtech.gameengine.scenarios;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.dto.ResourceRegionConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.planet.CommandService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by Beat
 * 07.11.2016.
 */
public class ResourceScenarioSuite extends ScenarioSuite {
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public ResourceScenarioSuite() {
        super("Resource");
    }

    @Override
    protected void setupScenarios() {
        addScenario(new Scenario("Resource Region") {
            private ScheduledFuture backgroundWorker;

            @Override
            protected void createSyncItems() {
                createSyncBaseItem(ScenarioService.HARVESTER_ITEM_TYPE, new DecimalPosition(0, 0), 0, null);
            }

            @Override
            public void setupResourceRegionConfig(List<ResourceRegionConfig> resourceRegionConfigs) {
                ResourceRegionConfig resourceRegionConfig = new ResourceRegionConfig();
                resourceRegionConfig.setCount(5);
                resourceRegionConfig.setMinDistanceToItems(3);
                resourceRegionConfig.setResourceItemTypeId(ScenarioService.RESOURCE_LITTLE_ITEM_TYPE.getId());
                resourceRegionConfig.setRegion(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(20, 20, 40, 40)));
                resourceRegionConfigs.add(resourceRegionConfig);
            }

            @Override
            public void executeCommands(CommandService commandService) {
                backgroundWorker = scheduler.scheduleAtFixedRate(() -> {
                    try {
                        SyncBaseItem harvester = getFirstCreatedSyncBaseItem();
                        if(!harvester.isIdle()) {
                            return;
                        }
                        commandService.harvest(harvester, getSyncResourceItem(ScenarioService.RESOURCE_LITTLE_ITEM_TYPE.getId()));
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }, 300, 300, TimeUnit.MILLISECONDS);

            }

            @Override
            public boolean isStart() {
                return true;
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
