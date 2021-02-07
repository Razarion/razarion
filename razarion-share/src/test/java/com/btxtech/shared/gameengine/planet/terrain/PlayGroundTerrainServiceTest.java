package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.dto.SlopeShape;
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
        slopeConfigLand.setSlopeShapes(Arrays.asList(
                new SlopeShape().position(new DecimalPosition(2, 0)).slopeFactor(1),
                new SlopeShape().position(new DecimalPosition(4, 8)).slopeFactor(0.7),
                new SlopeShape().position(new DecimalPosition(7, 12)).slopeFactor(0.7),
                new SlopeShape().position(new DecimalPosition(10, 20)).slopeFactor(0.7),
                new SlopeShape().position(new DecimalPosition(11, 20)).slopeFactor(0.7)));
        slopeConfigLand.setOuterLineGameEngine(3).setInnerLineGameEngine(7);
        slopeConfigs.add(slopeConfigLand);

        SlopeConfig slopeConfigWater = new SlopeConfig();
        slopeConfigWater.id(2).waterConfigId(FallbackConfig.WATER_CONFIG_ID);
        slopeConfigWater.setHorizontalSpace(5);
        slopeConfigWater.setSlopeShapes(Arrays.asList(
                new SlopeShape().position(new DecimalPosition(2, 0)).slopeFactor(0),
                new SlopeShape().position(new DecimalPosition(5, -0.5)).slopeFactor(1),
                new SlopeShape().position(new DecimalPosition(10, -1)).slopeFactor(1),
                new SlopeShape().position(new DecimalPosition(20, -2)).slopeFactor(1)));
        slopeConfigWater.setOuterLineGameEngine(5).setInnerLineGameEngine(18).setCoastDelimiterLineGameEngine(12);
        slopeConfigs.add(slopeConfigWater);

        List<TerrainObjectConfig> terrainObjectConfigs = new ArrayList<>();
        terrainObjectConfigs.add(new TerrainObjectConfig().id(1).radius(1));
        terrainObjectConfigs.add(new TerrainObjectConfig().id(2).radius(5));
        terrainObjectConfigs.add(new TerrainObjectConfig().id(3).radius(10));

        PlanetConfig planetConfig = FallbackConfig.setupPlanetConfig();
        planetConfig.setSize(new DecimalPosition(320, 320));

        setupTerrainTypeService(slopeConfigs, null, terrainObjectConfigs, planetConfig, terrainSlopePositions, terrainObjectPositions, null);
    }

}