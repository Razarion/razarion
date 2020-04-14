package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.TerrainSlopeCorner;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.GameTestHelper;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * on 11.11.2017.
 */
public class BigTerrainServiceTest extends WeldTerrainServiceTestBase {
    private void setup(int slopeConfigId, TerrainSlopeCorner... slopePolygon) {
        List<SlopeConfig> slopeConfigs = new ArrayList<>();

        SlopeConfig slopeConfigLand = new SlopeConfig();
        slopeConfigLand.id(1);
        slopeConfigLand.setRows(5).setSegments(1).setWidth(11).setHorizontalSpace(5).setHeight(20);
        slopeConfigLand.setSlopeNodes(toColumnRow(new SlopeNode[][]{
                {GameTestHelper.createSlopeNode(2, 0, 1),},
                {GameTestHelper.createSlopeNode(4, 8, 0.7),},
                {GameTestHelper.createSlopeNode(7, 12, 0.7),},
                {GameTestHelper.createSlopeNode(10, 20, 0.7),},
                {GameTestHelper.createSlopeNode(11, 20, 0.7),},
        }));
        slopeConfigLand.setOuterLineGameEngine(3).setInnerLineGameEngine(7);
        slopeConfigs.add(slopeConfigLand);

        SlopeConfig slopeConfigWater = new SlopeConfig();
        slopeConfigWater.id(2).setWaterConfigId(FallbackConfig.WATER_CONFIG_ID);
        slopeConfigWater.setRows(5).setSegments(1).setWidth(12).setHorizontalSpace(5).setHeight(-2);
        slopeConfigWater.setSlopeNodes(toColumnRow(new SlopeNode[][]{
                {GameTestHelper.createSlopeNode(2, 0, 1),},
                {GameTestHelper.createSlopeNode(4, 0, 0.7),},
                {GameTestHelper.createSlopeNode(8, -1, 0.7),},
                {GameTestHelper.createSlopeNode(10, -1.5, 0.7),},
                {GameTestHelper.createSlopeNode(12, -2, 0.7),},
        }));
        slopeConfigWater.setOuterLineGameEngine(4).setCoastDelimiterLineGameEngine(7).setInnerLineGameEngine(11);
        slopeConfigs.add(slopeConfigWater);

        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        TerrainSlopePosition terrainSlopePositionLand = new TerrainSlopePosition();
        terrainSlopePositionLand.setId(1);
        terrainSlopePositionLand.setSlopeConfigId(slopeConfigId);
        terrainSlopePositionLand.setPolygon(Arrays.asList(slopePolygon));
        terrainSlopePositions.add(terrainSlopePositionLand);

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

        PlanetConfig planetConfig = FallbackConfig.setupPlanetConfig();
        planetConfig.setPlayGround(new Rectangle2D(50, 50, 5000, 5000));
        planetConfig.setTerrainTileDimension(new Rectangle(0, 0, 32, 32));

        setupTerrainTypeService(slopeConfigs, null, planetConfig, terrainSlopePositions, null, null);
    }


    @Test
    public void testBigSkewAreaSlope() {
        setup(1, GameTestHelper.createTerrainSlopeCorner(100, 4000, null), GameTestHelper.createTerrainSlopeCorner(4000, 100, null), GameTestHelper.createTerrainSlopeCorner(4800, 800, null), GameTestHelper.createTerrainSlopeCorner(800, 4800, null));

        // showDisplay();
    }

    @Test
    public void testBigSkewWaterAreaSlope() {
        setup(2, GameTestHelper.createTerrainSlopeCorner(100, 4000, null), GameTestHelper.createTerrainSlopeCorner(4000, 100, null), GameTestHelper.createTerrainSlopeCorner(4800, 800, null), GameTestHelper.createTerrainSlopeCorner(800, 4800, null));

        // showDisplay();
    }
}
