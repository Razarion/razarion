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
                DecimalPosition destination = new DecimalPosition(40, 0);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), 0, destination);
            }

            @Override
            public void setupTerrain(List<TerrainSlopePosition> slopePositions, List<TerrainObjectPosition> terrainObjectPositions) {
                slopePositions.add(createRectangleSlope(ScenarioService.SLOPE_ID, 20, -20, 5, 40));
            }
        });
        addScenario(new Scenario("Frontal AStar") {
            @Override
            public void createSyncItems() {
                DecimalPosition destination = new DecimalPosition(40, 0);
                createSyncBaseItemAStar(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), 0, destination);
            }

            @Override
            public void setupTerrain(List<TerrainSlopePosition> slopePositions, List<TerrainObjectPosition> terrainObjectPositions) {
                slopePositions.add(createRectangleSlope(ScenarioService.SLOPE_ID, 20, -20, 5, 40));
            }
        });
        addScenario(new Scenario("Not frontal") {
            @Override
            public void createSyncItems() {
                DecimalPosition destination = new DecimalPosition(60, 0);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), 0, destination);
            }

            @Override
            public void setupTerrain(List<TerrainSlopePosition> slopePositions, List<TerrainObjectPosition> terrainObjectPositions) {
                slopePositions.add(createRectangleSlope(ScenarioService.SLOPE_ID, 20, 1, 20, 40));
            }
        });
        addScenario(new Scenario("1") {
            @Override
            public void createSyncItems() {
                DecimalPosition destination = new DecimalPosition(40, 0);
                // createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-10, 0), direction);
                // createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-8, 0), direction);
                // createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-6, 0), direction);
                // createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-4, 0), direction);
                // createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-2, 0), direction);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), 0, destination);
                createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(2, 0), 0, destination);
            }

            @Override
            public void setupTerrain(List<TerrainSlopePosition> slopePositions, List<TerrainObjectPosition> terrainObjectPositions) {
                slopePositions.add(createRectangleSlope(ScenarioService.SLOPE_ID, 30, -20, 20, 40));
            }
        });
        addScenario(new Scenario("Group vs obstacle") {
            @Override
            public void createSyncItems() {
                DecimalPosition destination = new DecimalPosition(80, 0);
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(4 * x, 4 * y), 0, destination);
                    }
                }
            }

            @Override
            public void setupTerrain(List<TerrainSlopePosition> slopePositions, List<TerrainObjectPosition> terrainObjectPositions) {
                slopePositions.add(createRectangleSlope(ScenarioService.SLOPE_ID, 40, -5, 20, 10));
            }
        });
        addScenario(new Scenario("Group vs bottle neck") {
            @Override
            public void createSyncItems() {
                DecimalPosition destination = new DecimalPosition(80, 0);
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        createSyncBaseItem(ScenarioService.SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(4 * x, 4 * y), 0, destination);
                    }
                }
            }

            @Override
            public void setupTerrain(List<TerrainSlopePosition> slopePositions, List<TerrainObjectPosition> terrainObjectPositions) {
                slopePositions.add(createRectangleSlope(ScenarioService.SLOPE_ID, 25, 12, 20, 15));
                slopePositions.add(createRectangleSlope(ScenarioService.SLOPE_ID, 25, -27, 20, 15));
            }
        });
    }
}
