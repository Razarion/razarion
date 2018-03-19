package com.btxtech.gameengine.scenarios;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;

import java.util.List;

/**
 * Created by Beat
 * 07.11.2016.
 */
public class MoveComplexScenarioSuite extends ScenarioSuite {
    public MoveComplexScenarioSuite() {
        super("Move complex");
    }

    @Override
    protected void setupScenarios() {
        addScenario(new Scenario("Group") {
            @Override
            public void createSyncItems() {
                for (int x = -3; x < 4; x++) {
                    for (int y = -3; y < 4; y++) {
                        createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(6.1 * x, 6.1 * y), 0, new DecimalPosition(100, 100));
                    }
                }
            }


            @Override
            public void setupTerrain(List<TerrainSlopePosition> slopePositions, List<TerrainObjectPosition> terrainObjectPositions) {
                terrainObjectPositions.add(new TerrainObjectPosition().setId(1).setTerrainObjectId(ScenarioService.TERRAIN_OBJECT_ID).setScale(1).setPosition(new DecimalPosition(50, 20)));
                terrainObjectPositions.add(new TerrainObjectPosition().setId(2).setTerrainObjectId(ScenarioService.TERRAIN_OBJECT_ID).setScale(2).setPosition(new DecimalPosition(40, 20)));
                terrainObjectPositions.add(new TerrainObjectPosition().setId(3).setTerrainObjectId(ScenarioService.TERRAIN_OBJECT_ID).setScale(1).setPosition(new DecimalPosition(50, 50)));
                terrainObjectPositions.add(new TerrainObjectPosition().setId(4).setTerrainObjectId(ScenarioService.TERRAIN_OBJECT_ID).setScale(2).setPosition(new DecimalPosition(30, 56)));
                terrainObjectPositions.add(new TerrainObjectPosition().setId(4).setTerrainObjectId(ScenarioService.TERRAIN_OBJECT_ID).setScale(2).setPosition(new DecimalPosition(50, 70)));
                terrainObjectPositions.add(new TerrainObjectPosition().setId(4).setTerrainObjectId(ScenarioService.TERRAIN_OBJECT_ID).setScale(2).setPosition(new DecimalPosition(65, 38)));
            }

            @Override
            public boolean isStart() {
                return true;
            }
        });
    }
}
