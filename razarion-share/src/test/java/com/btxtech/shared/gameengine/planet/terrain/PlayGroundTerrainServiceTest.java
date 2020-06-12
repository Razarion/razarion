package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.gameengine.planet.GameTestHelper;
import com.btxtech.shared.gameengine.planet.terrain.asserthelper.AssertShapeAccess;
import com.btxtech.shared.gameengine.planet.terrain.asserthelper.AssertTerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.asserthelper.AssertTerrainTile;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * on 15.11.2017.
 */
public class PlayGroundTerrainServiceTest extends WeldTerrainServiceTestBase {

    @Test
    public void testPlayGround4Corners() {
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

        // topRigt and topLeft missing

        setup(null, terrainSlopePositions);
        // showDisplay();

        Collection<TerrainTile> terrainTiles = generateTerrainTiles(new Index(0, 0), new Index(0, 1), new Index(1, 0), new Index(1, 1));
        // AssertTerrainTile.saveTerrainTiles(terrainTiles, "testPlayGround4CornersTile1.json");
        AssertTerrainTile assertTerrainTile = new AssertTerrainTile(getClass(), "testPlayGround4CornersTile1.json");
        assertTerrainTile.assertEquals(terrainTiles);

        // AssertShapeAccess.saveShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(250, 220), "testPlayGround4CornersShape1HNT1.json");
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(250, 220), getClass(), "testPlayGround4CornersShape1HNT1.json");

        // AssertTerrainShape.saveTerrainShape(getTerrainShape(), "testPlayGround4CornersShape1.json");
        AssertTerrainShape.assertTerrainShape(getClass(), "testPlayGround4CornersShape1.json", getTerrainShape());

    }

    private void setup(List<TerrainObjectPosition> terrainObjectPositions, List<TerrainSlopePosition> terrainSlopePositions) {
        List<SlopeConfig> slopeConfigs = new ArrayList<>();

        SlopeConfig slopeConfigLand = new SlopeConfig();
        slopeConfigLand.id(1);
        slopeConfigLand.setHorizontalSpace(5);
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
        slopeConfigWater.id(2).waterConfigId(FallbackConfig.WATER_CONFIG_ID);
        slopeConfigWater.setHorizontalSpace(5);
        slopeConfigWater.setSlopeNodes(toColumnRow(new SlopeNode[][]{
                {GameTestHelper.createSlopeNode(2, 0, 0),},
                {GameTestHelper.createSlopeNode(5, -0.5, 1),},
                {GameTestHelper.createSlopeNode(10, -1, 1),},
                {GameTestHelper.createSlopeNode(20, -2, 1),},
        }));
        slopeConfigWater.setOuterLineGameEngine(5).setInnerLineGameEngine(18).setCoastDelimiterLineGameEngine(12);
        slopeConfigs.add(slopeConfigWater);

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

        PlanetConfig planetConfig = FallbackConfig.setupPlanetConfig();
        planetConfig.setSize(new DecimalPosition(320, 320));

        setupTerrainTypeService(slopeConfigs, null, terrainObjectConfigs, planetConfig, terrainSlopePositions, terrainObjectPositions, null);
    }

}