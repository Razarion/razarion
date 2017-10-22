package com.btxtech.shared.gameengine.planet.basic;

import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.planet.GameTestContent;
import com.btxtech.shared.gameengine.planet.GameTestHelper;
import com.btxtech.shared.gameengine.planet.WeldMasterBaseTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * on 21.10.2017.
 */
public class BaseBasic extends WeldMasterBaseTest {

    protected void setup() {
        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        // Land slope
        TerrainSlopePosition terrainSlopePositionLand = new TerrainSlopePosition();
        terrainSlopePositionLand.setId(1);
        terrainSlopePositionLand.setSlopeConfigId(GameTestContent.SLOPE_SKELETON_CONFIG_LAND_ID);
        terrainSlopePositionLand.setPolygon(Arrays.asList(GameTestHelper.createTerrainSlopeCorner(50, 40, null), GameTestHelper.createTerrainSlopeCorner(100, 40, null),
                GameTestHelper.createTerrainSlopeCorner(100, 60, GameTestContent.DRIVEWAY_ID_ID), GameTestHelper.createTerrainSlopeCorner(100, 90, GameTestContent.DRIVEWAY_ID_ID), // driveway
                GameTestHelper.createTerrainSlopeCorner(100, 110, null), GameTestHelper.createTerrainSlopeCorner(50, 110, null)));
        terrainSlopePositions.add(terrainSlopePositionLand);
        // Water slope
        TerrainSlopePosition terrainSlopePositionWater = new TerrainSlopePosition();
        terrainSlopePositionWater.setId(2);
        terrainSlopePositionWater.setSlopeConfigId(GameTestContent.SLOPE_SKELETON_CONFIG_WATER_ID);
        terrainSlopePositionWater.setPolygon(Arrays.asList(GameTestHelper.createTerrainSlopeCorner(64, 200, null), GameTestHelper.createTerrainSlopeCorner(231, 200, null),
                GameTestHelper.createTerrainSlopeCorner(231, 256, null), GameTestHelper.createTerrainSlopeCorner(151, 257, null), // driveway
                GameTestHelper.createTerrainSlopeCorner(239, 359, null), GameTestHelper.createTerrainSlopeCorner(49, 360, null)));
        terrainSlopePositions.add(terrainSlopePositionWater);

        setupMasterEnvironment(GameTestContent.setupStaticGameConfig(), terrainSlopePositions);
    }

}
