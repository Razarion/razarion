package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.dto.SlopeShape;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.dto.WaterConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.gameengine.planet.GameTestHelper;
import com.btxtech.shared.gameengine.planet.terrain.asserthelper.AssertShapeAccess;
import com.btxtech.shared.gameengine.planet.terrain.asserthelper.AssertTerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.asserthelper.AssertTerrainTile;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Beat
 * on 15.11.2017.
 */
@Deprecated // TODO Still needed? PlayGround is removed.
public class PlayGroundTerrainServiceTest extends WeldTerrainServiceTestBase {

    @Test
    public void testPlayGround4Corners() {
        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        terrainSlopePositions.add(new TerrainSlopePosition()
                .id(1)
                .slopeConfigId(1)
                .polygon(Arrays.asList(
                        GameTestHelper.createTerrainSlopeCorner(26, 24, null),
                        GameTestHelper.createTerrainSlopeCorner(76, 24, null),
                        GameTestHelper.createTerrainSlopeCorner(76, 94, null),
                        GameTestHelper.createTerrainSlopeCorner(26, 94, null))));
        terrainSlopePositions.add(new TerrainSlopePosition()
                .id(2)
                .slopeConfigId(2)
                .polygon(Arrays.asList(
                        GameTestHelper.createTerrainSlopeCorner(200, 40, null),
                        GameTestHelper.createTerrainSlopeCorner(280, 40, null),
                        GameTestHelper.createTerrainSlopeCorner(280, 100, null),
                        GameTestHelper.createTerrainSlopeCorner(200, 100, null))));

        // topRigt and topLeft missing

        setup(terrainSlopePositions);
        // showDisplay();

        AssertTerrainShape.assertTerrainShape(getClass(), "testPlayGround4CornersShape1.json", getTerrainShape());
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(250, 220), getClass(), "testPlayGround4CornersShape1HNT1.json");
        AssertTerrainTile.assertTerrainTile(getClass(), "testPlayGround4CornersTile1.json", generateTerrainTiles(new Index(0, 0), new Index(0, 1), new Index(1, 0), new Index(1, 1)));
    }

    private void setup(List<TerrainSlopePosition> terrainSlopePositions) {
        List<SlopeConfig> slopeConfigs = new ArrayList<>();

        slopeConfigs.add(new SlopeConfig()
                .id(1)
                .horizontalSpace(5)
                .slopeShapes(Arrays.asList(
                        new SlopeShape().position(new DecimalPosition(2, 0)).slopeFactor(1),
                        new SlopeShape().position(new DecimalPosition(4, 8)).slopeFactor(0.7),
                        new SlopeShape().position(new DecimalPosition(7, 12)).slopeFactor(0.7),
                        new SlopeShape().position(new DecimalPosition(10, 20)).slopeFactor(0.7),
                        new SlopeShape().position(new DecimalPosition(11, 20)).slopeFactor(0.7)))
                .outerLineGameEngine(3)
                .innerLineGameEngine(7));
        slopeConfigs.add(new SlopeConfig()
                .id(2)
                .waterConfigId(1)
                .horizontalSpace(5)
                .slopeShapes(Arrays.asList(
                        new SlopeShape().position(new DecimalPosition(2, 0)).slopeFactor(0),
                        new SlopeShape().position(new DecimalPosition(5, -0.5)).slopeFactor(1),
                        new SlopeShape().position(new DecimalPosition(10, -1)).slopeFactor(1),
                        new SlopeShape().position(new DecimalPosition(20, -2)).slopeFactor(1)))
                .outerLineGameEngine(5)
                .innerLineGameEngine(18)
                .coastDelimiterLineGameEngine(12));

        List<WaterConfig> waterConfigs = Collections.singletonList(new WaterConfig().id(1).waterLevel(-0.2).groundLevel(0.0));

        List<TerrainObjectConfig> terrainObjectConfigs = Arrays.asList(
                new TerrainObjectConfig().id(1).radius(1),
                new TerrainObjectConfig().id(2).radius(5),
                new TerrainObjectConfig().id(3).radius(10));

        PlanetConfig planetConfig = FallbackConfig.setupPlanetConfig();
        planetConfig.setSize(new DecimalPosition(320, 320));

        setupTerrainTypeService(slopeConfigs, null, waterConfigs, terrainObjectConfigs, planetConfig, terrainSlopePositions, null, null, null);
    }

}