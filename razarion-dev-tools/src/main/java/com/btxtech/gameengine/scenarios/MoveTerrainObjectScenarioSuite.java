package com.btxtech.gameengine.scenarios;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;

import java.util.List;

/**
 * Created by Beat
 * 07.11.2016.
 */
public class MoveTerrainObjectScenarioSuite extends ScenarioSuite {
    public MoveTerrainObjectScenarioSuite() {
        super("Move terrain object");
    }

    @Override
    protected void setupScenarios() {
        addScenario(new Scenario("Frontal") {
            @Override
            public void createSyncItems() {
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), 0, new DecimalPosition(20, 0));
            }

            @Override
            public void setupTerrain(List<TerrainSlopePosition> slopePositions, List<TerrainObjectPosition> terrainObjectPositions) {
                terrainObjectPositions.add(new TerrainObjectPosition().setId(1).setTerrainObjectId(ScenarioService.TERRAIN_OBJECT_ID).setPosition(new DecimalPosition(10, 0)));
            }
        });
        addScenario(new Scenario("Not frontal") {
            @Override
            public void createSyncItems() {
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), 0, new DecimalPosition(20, 0));
            }

            @Override
            public void setupTerrain(List<TerrainSlopePosition> slopePositions, List<TerrainObjectPosition> terrainObjectPositions) {
                terrainObjectPositions.add(new TerrainObjectPosition().setId(1).setTerrainObjectId(ScenarioService.TERRAIN_OBJECT_ID).setPosition(new DecimalPosition(10, 5)));
            }
        });
    }
}
