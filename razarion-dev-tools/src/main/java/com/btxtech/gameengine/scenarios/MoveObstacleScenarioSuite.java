package com.btxtech.gameengine.scenarios;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;

import java.util.List;

/**
 * Created by Beat
 * 07.11.2016.
 */
public class MoveObstacleScenarioSuite extends ScenarioSuite {
    public MoveObstacleScenarioSuite() {
        super("Move obstacle");
    }

    @Override
    protected void setupScenarios() {
        addScenario(new Scenario("Frontal") {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(10, 0);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), 0, direction);
            }

            @Override
            public void setupTerrain(List<TerrainSlopePosition> slopePositions, List<TerrainObjectPosition> terrainObjectPositions) {
                slopePositions.add(createRectangleSlope(ScenarioService.SLOPE_ID, 5, -20, 20, 40));
            }
        });
        addScenario(new Scenario("Not frontal") {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(40, 0);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), 0, direction);
            }

            @Override
            public void setupTerrain(List<TerrainSlopePosition> slopePositions, List<TerrainObjectPosition> terrainObjectPositions) {
                slopePositions.add(createRectangleSlope(ScenarioService.SLOPE_ID, 5, 1, 20, 40));
            }
        });
        addScenario(new Scenario("1") {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(20, 0);
                // createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-10, 0), direction);
                // createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-8, 0), direction);
                // createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-6, 0), direction);
                // createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-4, 0), direction);
                // createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-2, 0), direction);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), 0, direction);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(2, 0), 0, direction);
            }

            @Override
            public void setupTerrain(List<TerrainSlopePosition> slopePositions, List<TerrainObjectPosition> terrainObjectPositions) {
                slopePositions.add(createRectangleSlope(ScenarioService.SLOPE_ID, 10, -20, 20, 40));
            }
        });
        addScenario(new Scenario("Group vs obstacle") {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(60, 0);
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(4 * x, 4 * y), 0, direction);
                    }
                }
            }

            @Override
            public void setupTerrain(List<TerrainSlopePosition> slopePositions, List<TerrainObjectPosition> terrainObjectPositions) {
                slopePositions.add(createRectangleSlope(ScenarioService.SLOPE_ID, 20, -5, 20, 10));
            }
        });
        addScenario(new Scenario("Group vs bottle neck") {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(50, 0);
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(4 * x, 4 * y), 0, direction);
                    }
                }
            }

            @Override
            public void setupTerrain(List<TerrainSlopePosition> slopePositions, List<TerrainObjectPosition> terrainObjectPositions) {
                slopePositions.add(createRectangleSlope(ScenarioService.SLOPE_ID, 15, 5, 20, 15));
                slopePositions.add(createRectangleSlope(ScenarioService.SLOPE_ID, 15, -20, 20, 15));
            }
        });
    }
}
