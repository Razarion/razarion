package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.SlopeShape;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.gameengine.planet.GameTestHelper;
import com.btxtech.shared.gameengine.planet.terrain.asserthelper.AssertShapeAccess;
import com.btxtech.shared.gameengine.planet.terrain.asserthelper.AssertTerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.asserthelper.AssertTerrainTile;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * 03.04.2017.
 */
public class TerrainObjectServiceTest extends WeldTerrainServiceTestBase {

    @Test
    public void testTerrainObjectTileGeneration4Tiles() {
        List<SlopeConfig> slopeConfigs = new ArrayList<>();
        SlopeConfig slopeConfigLand = new SlopeConfig();
        slopeConfigLand.id(1);
        slopeConfigLand.setHorizontalSpace(5);
        slopeConfigLand.setSlopeShapes(Arrays.asList(
                new SlopeShape().position(new DecimalPosition(2, 5)).slopeFactor(1),
                new SlopeShape().position(new DecimalPosition(4, 10)).slopeFactor(0.7),
                new SlopeShape().position(new DecimalPosition(7, 20)).slopeFactor(0.7)));

        slopeConfigLand.setOuterLineGameEngine(2).setInnerLineGameEngine(5);
        slopeConfigs.add(slopeConfigLand);

        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        TerrainSlopePosition terrainSlopePosition = new TerrainSlopePosition();
        terrainSlopePosition.id(1);
        terrainSlopePosition.slopeConfigId(1);
        terrainSlopePosition.polygon(Arrays.asList(
                GameTestHelper.createTerrainSlopeCorner(120, 120, null),
                GameTestHelper.createTerrainSlopeCorner(260, 120, null),
                GameTestHelper.createTerrainSlopeCorner(260, 250, null),
                GameTestHelper.createTerrainSlopeCorner(120, 250, null)));
        terrainSlopePositions.add(terrainSlopePosition);

        List<TerrainObjectConfig> terrainObjectConfigs = new ArrayList<>();
        terrainObjectConfigs.add(new TerrainObjectConfig().id(1).radius(1));
        terrainObjectConfigs.add(new TerrainObjectConfig().id(2).radius(5));
        terrainObjectConfigs.add(new TerrainObjectConfig().id(3).radius(10));

        List<TerrainObjectPosition> terrainObjectPositions = Arrays.asList(
                new TerrainObjectPosition().id(1).terrainObjectId(1).position(new DecimalPosition(50, 40)).scale(new Vertex(10, 10, 10)).rotation(new Vertex(0, 0, 0)),
                new TerrainObjectPosition().id(1).terrainObjectId(1).position(new DecimalPosition(100, 80)).scale(new Vertex(10, 10, 10)).rotation(new Vertex(0, 0, 0)),
                new TerrainObjectPosition().id(2).terrainObjectId(1).position(new DecimalPosition(150, 120)).scale(new Vertex(1, 1, 1)).rotation(new Vertex(0, 0, 0)),
                new TerrainObjectPosition().id(2).terrainObjectId(2).position(new DecimalPosition(200, 160)).scale(new Vertex(1, 1, 1)).rotation(new Vertex(0, 0, 0)),
                new TerrainObjectPosition().id(1).terrainObjectId(3).position(new DecimalPosition(250, 200)).scale(new Vertex(0.2, 0.2, 0.2)).rotation(new Vertex(0, 0, 0)),
                new TerrainObjectPosition().id(2).terrainObjectId(3).position(new DecimalPosition(300, 240)).scale(new Vertex(0.5, 0.5, 0.5)).rotation(new Vertex(0, 0, 0)),
                new TerrainObjectPosition().id(1).terrainObjectId(3).position(new DecimalPosition(50, 280)).scale(new Vertex(1, 1, 1)).rotation(new Vertex(0, 0, Math.toRadians(90))),
                new TerrainObjectPosition().id(2).terrainObjectId(3).position(new DecimalPosition(100, 40)).scale(new Vertex(2, 2, 2)).rotation(new Vertex(0, 0, 0)));


        setupTerrainTypeService(slopeConfigs, null, terrainObjectConfigs, null, terrainSlopePositions, terrainObjectPositions, null);

        // showDisplay();

        AssertTerrainShape.assertTerrainShape(getClass(), "testTerrainObjectTileGeneration4TilesShape1.json", getTerrainShape());
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(160, 160), getClass(), "testTerrainObjectTileGeneration4TilesHNT1.json");
        AssertTerrainTile.assertTerrainTile(getClass(), "testTerrainObjectTileGeneration4Tiles1.json", generateTerrainTiles(new Index(0, 0), new Index(0, 1), new Index(1, 0), new Index(1, 1)));
    }
}
