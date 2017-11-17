package com.btxtech.shared.gameengine.planet.basic;

import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
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
public class BaseBasicTest extends WeldMasterBaseTest {

    protected void setup() {
        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();

        List<SlopeSkeletonConfig> slopeSkeletonConfigs = new ArrayList<>();
        SlopeSkeletonConfig slopeSkeletonConfigLand = new SlopeSkeletonConfig();
        slopeSkeletonConfigLand.setId(1).setType(SlopeSkeletonConfig.Type.LAND);
        slopeSkeletonConfigLand.setRows(3).setSegments(1).setWidth(7).setVerticalSpace(5).setHeight(20);
        slopeSkeletonConfigLand.setSlopeNodes(toColumnRow(new SlopeNode[][]{
                {GameTestHelper.createSlopeNode(2, 5, 1),},
                {GameTestHelper.createSlopeNode(4, 10, 0.7),},
                {GameTestHelper.createSlopeNode(7, 20, 0.7),},
        }));
        slopeSkeletonConfigLand.setOuterLineGameEngine(1).setInnerLineGameEngine(6);
        slopeSkeletonConfigs.add(slopeSkeletonConfigLand);

        SlopeSkeletonConfig slopeSkeletonConfigWater = new SlopeSkeletonConfig();
        slopeSkeletonConfigWater.setId(2).setType(SlopeSkeletonConfig.Type.WATER);
        slopeSkeletonConfigWater.setRows(4).setSegments(1).setWidth(20).setVerticalSpace(6).setHeight(-2);
        slopeSkeletonConfigWater.setSlopeNodes(toColumnRow(new SlopeNode[][]{
                {GameTestHelper.createSlopeNode(5, 0.5, 0.5),},
                {GameTestHelper.createSlopeNode(10, -0.1, 1),},
                {GameTestHelper.createSlopeNode(15, -0.8, 1),},
                {GameTestHelper.createSlopeNode(20, -2, 1),}
        }));
        slopeSkeletonConfigWater.setOuterLineGameEngine(8).setCoastDelimiterLineGameEngine(10).setInnerLineGameEngine(16);
        slopeSkeletonConfigs.add(slopeSkeletonConfigWater);

        // Land slope
        TerrainSlopePosition terrainSlopePositionLand = new TerrainSlopePosition();
        terrainSlopePositionLand.setId(1);
        terrainSlopePositionLand.setSlopeConfigId(1);
        terrainSlopePositionLand.setPolygon(Arrays.asList(GameTestHelper.createTerrainSlopeCorner(50, 40, null), GameTestHelper.createTerrainSlopeCorner(100, 40, null),
                GameTestHelper.createTerrainSlopeCorner(100, 60, GameTestContent.DRIVEWAY_ID_ID), GameTestHelper.createTerrainSlopeCorner(100, 90, GameTestContent.DRIVEWAY_ID_ID), // driveway
                GameTestHelper.createTerrainSlopeCorner(100, 110, null), GameTestHelper.createTerrainSlopeCorner(50, 110, null)));
        terrainSlopePositions.add(terrainSlopePositionLand);
        // Water slope
        TerrainSlopePosition terrainSlopePositionWater = new TerrainSlopePosition();
        terrainSlopePositionWater.setId(2);
        terrainSlopePositionWater.setSlopeConfigId(2);
        terrainSlopePositionWater.setPolygon(Arrays.asList(GameTestHelper.createTerrainSlopeCorner(64, 200, null), GameTestHelper.createTerrainSlopeCorner(231, 200, null),
                GameTestHelper.createTerrainSlopeCorner(231, 256, null), GameTestHelper.createTerrainSlopeCorner(151, 257, null), // driveway
                GameTestHelper.createTerrainSlopeCorner(239, 359, null), GameTestHelper.createTerrainSlopeCorner(49, 360, null)));
        terrainSlopePositions.add(terrainSlopePositionWater);


        StaticGameConfig staticGameConfig = GameTestContent.setupStaticGameConfig();
        staticGameConfig.setSlopeSkeletonConfigs(slopeSkeletonConfigs);

        setupMasterEnvironment(staticGameConfig, terrainSlopePositions);
    }

}
