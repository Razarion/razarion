package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.GameTestContent;
import com.btxtech.shared.gameengine.planet.GameTestHelper;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * on 15.11.2017.
 */
public class PlayGroundTerrainServiceTest extends WeldTerrainServiceTestBase {

    @Test
    public void test() {
        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        TerrainSlopePosition bottomLeftLandSlope = new TerrainSlopePosition();
        bottomLeftLandSlope.setId(1);
        bottomLeftLandSlope.setSlopeConfigId(1);
        bottomLeftLandSlope.setPolygon(Arrays.asList(GameTestHelper.createTerrainSlopeCorner(26, 24, null), GameTestHelper.createTerrainSlopeCorner(76, 24, null), GameTestHelper.createTerrainSlopeCorner(76, 94, null), GameTestHelper.createTerrainSlopeCorner(26, 94, null)));
        terrainSlopePositions.add(bottomLeftLandSlope);

        TerrainSlopePosition bottomRightWaterSlope = new TerrainSlopePosition();
        bottomRightWaterSlope.setId(2);
        bottomRightWaterSlope.setSlopeConfigId(2);
        bottomRightWaterSlope.setPolygon(Arrays.asList(GameTestHelper.createTerrainSlopeCorner(200, 40, null), GameTestHelper.createTerrainSlopeCorner(280, 40, null), GameTestHelper.createTerrainSlopeCorner(280, 100, null), GameTestHelper.createTerrainSlopeCorner(200, 100, null)));
        terrainSlopePositions.add(bottomRightWaterSlope);


        setup(null, terrainSlopePositions);
        // showDisplay();
    }

    private void setup(List<TerrainObjectPosition> terrainObjectPositions, List<TerrainSlopePosition> terrainSlopePositions) {
        List<SlopeSkeletonConfig> slopeSkeletonConfigs = new ArrayList<>();

        SlopeSkeletonConfig slopeSkeletonConfigLand = new SlopeSkeletonConfig();
        slopeSkeletonConfigLand.setId(1).setType(SlopeSkeletonConfig.Type.LAND);
        slopeSkeletonConfigLand.setRows(5).setSegments(1).setWidth(11).setVerticalSpace(5).setHeight(20);
        slopeSkeletonConfigLand.setSlopeNodes(toColumnRow(new SlopeNode[][]{
                {GameTestHelper.createSlopeNode(2, 0, 1),},
                {GameTestHelper.createSlopeNode(4, 8, 0.7),},
                {GameTestHelper.createSlopeNode(7, 12, 0.7),},
                {GameTestHelper.createSlopeNode(10, 20, 0.7),},
                {GameTestHelper.createSlopeNode(11, 20, 0.7),},
        }));
        slopeSkeletonConfigLand.setOuterLineGameEngine(3).setInnerLineGameEngine(7);
        slopeSkeletonConfigs.add(slopeSkeletonConfigLand);

        SlopeSkeletonConfig slopeSkeletonConfigWater = new SlopeSkeletonConfig();
        slopeSkeletonConfigWater.setId(2).setType(SlopeSkeletonConfig.Type.WATER);
        slopeSkeletonConfigWater.setRows(4).setSegments(1).setWidth(20).setVerticalSpace(5).setHeight(-2);
        slopeSkeletonConfigWater.setSlopeNodes(toColumnRow(new SlopeNode[][]{
                {GameTestHelper.createSlopeNode(2, 0, 0),},
                {GameTestHelper.createSlopeNode(5, -0.5, 1),},
                {GameTestHelper.createSlopeNode(10, -1, 1),},
                {GameTestHelper.createSlopeNode(20, -2, 1),},
        }));
        slopeSkeletonConfigWater.setOuterLineGameEngine(5).setInnerLineGameEngine(18).setCoastDelimiterLineGameEngine(12);
        slopeSkeletonConfigs.add(slopeSkeletonConfigWater);

        double[][] heights = new double[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        };
        double[][] splattings = new double[][]{
                {0.7, 0.8, 0.9},
                {0.4, 0.5, 0.6},
                {0.1, 0.2, 0.3}
        };

        List<TerrainObjectConfig> terrainObjectConfigs = new ArrayList<>();
        terrainObjectConfigs.add(new TerrainObjectConfig().setId(1).setRadius(1));
        terrainObjectConfigs.add(new TerrainObjectConfig().setId(2).setRadius(5));
        terrainObjectConfigs.add(new TerrainObjectConfig().setId(3).setRadius(10));

        PlanetConfig planetConfig = GameTestContent.setupPlanetConfig();
        planetConfig.setPlayGround(new Rectangle2D(50, 50, 200, 200));
        planetConfig.setTerrainTileDimension(new Rectangle(0, 0, 2, 2));

        setupTerrainTypeService(heights, splattings, slopeSkeletonConfigs, terrainObjectConfigs, planetConfig, terrainSlopePositions, terrainObjectPositions);
    }

}