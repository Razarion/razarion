package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.gameengine.datatypes.command.SimplePath;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.GameTestHelper;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.terrain.DaggerTerrainServiceTestBase;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import org.junit.Before;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * on 15.02.2018.
 */
public abstract class AStarBaseTest extends DaggerTerrainServiceTestBase {
    @Before
    public void before() {
        List<TerrainObjectConfig> terrainObjectConfigs = new ArrayList<>();
        terrainObjectConfigs.add(new TerrainObjectConfig().id(1).radius(1));
        terrainObjectConfigs.add(new TerrainObjectConfig().id(2).radius(5));
        terrainObjectConfigs.add(new TerrainObjectConfig().id(3).radius(10));

        PlanetConfig planetConfig = FallbackConfig.setupPlanetConfig();
        planetConfig.setSize(new DecimalPosition(320, 320));
        List<TerrainObjectPosition> terrainObjectPositions = new ArrayList<>();
        terrainObjectPositions.add((new TerrainObjectPosition().terrainObjectConfigId(3).position(new DecimalPosition(76, 30))));
        terrainObjectPositions.add((new TerrainObjectPosition().terrainObjectConfigId(3).position(new DecimalPosition(114, 28))));
        terrainObjectPositions.add((new TerrainObjectPosition().terrainObjectConfigId(3).position(new DecimalPosition(95, 11))));
        terrainObjectPositions.add((new TerrainObjectPosition().terrainObjectConfigId(3).position(new DecimalPosition(223, 95))));
        terrainObjectPositions.add((new TerrainObjectPosition().terrainObjectConfigId(3).position(new DecimalPosition(191, 116))));
        terrainObjectPositions.add((new TerrainObjectPosition().terrainObjectConfigId(3).position(new DecimalPosition(48, 124))));
        terrainObjectPositions.add((new TerrainObjectPosition().terrainObjectConfigId(3).position(new DecimalPosition(132, 131))));
        terrainObjectPositions.add((new TerrainObjectPosition().terrainObjectConfigId(3).position(new DecimalPosition(50, 280))));
        terrainObjectPositions.add((new TerrainObjectPosition().terrainObjectConfigId(3).position(new DecimalPosition(127, 290))));
        terrainObjectPositions.add((new TerrainObjectPosition().terrainObjectConfigId(3).position(new DecimalPosition(212, 325))));
        terrainObjectPositions.add((new TerrainObjectPosition().terrainObjectConfigId(3).position(new DecimalPosition(223, 290))));

        setupTerrainTypeService(null, terrainObjectConfigs, planetConfig, terrainObjectPositions, null);
    }

    protected SimplePath setupPath(double actorRadius, TerrainType actorTerrainType, DecimalPosition actorPosition, double range, double targetRadius, TerrainType targetTerrainType, DecimalPosition targetPosition) {
        SyncBaseItem actor = GameTestHelper.createMockSyncBaseItem(actorRadius, actorTerrainType, actorPosition, getSyncItemContainerService());
        SyncBaseItem target = GameTestHelper.createMockSyncBaseItem(targetRadius, targetTerrainType, targetPosition, getSyncItemContainerService());
        return getPathingService().setupPathToDestination(actor, range, target);
    }

    protected SimplePath setupPath(double radius, TerrainType land, DecimalPosition start, DecimalPosition destination) {
        SyncBaseItem syncBaseItem = GameTestHelper.createMockSyncBaseItem(radius, land, start, getSyncItemContainerService());
        return getPathingService().setupPathToDestination(syncBaseItem, destination);
    }

}
