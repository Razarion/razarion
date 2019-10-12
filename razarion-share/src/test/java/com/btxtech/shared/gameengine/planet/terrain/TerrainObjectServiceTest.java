package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
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
 * 03.04.2017.
 */
public class TerrainObjectServiceTest extends WeldTerrainServiceTestBase {

    @Test
    public void testTerrainObjectTileGeneration4Tiles() {
        List<SlopeSkeletonConfig> slopeSkeletonConfigs = new ArrayList<>();
        SlopeSkeletonConfig slopeSkeletonConfigLand = new SlopeSkeletonConfig();
        slopeSkeletonConfigLand.setId(1).setType(SlopeSkeletonConfig.Type.LAND);
        slopeSkeletonConfigLand.setRows(3).setSegments(1).setWidth(7).setHorizontalSpace(5).setHeight(20);
        SlopeNode[][] slopeNodes = new SlopeNode[][]{
                {GameTestHelper.createSlopeNode(2, 5, 1),},
                {GameTestHelper.createSlopeNode(4, 10, 0.7),},
                {GameTestHelper.createSlopeNode(7, 20, 0.7),},
        };
        slopeSkeletonConfigLand.setSlopeNodes(toColumnRow(slopeNodes));
        slopeSkeletonConfigLand.setOuterLineGameEngine(2).setInnerLineGameEngine(5);
        slopeSkeletonConfigs.add(slopeSkeletonConfigLand);

        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        TerrainSlopePosition terrainSlopePosition = new TerrainSlopePosition();
        terrainSlopePosition.setId(1);
        terrainSlopePosition.setSlopeConfigId(1);
        terrainSlopePosition.setPolygon(Arrays.asList(GameTestHelper.createTerrainSlopeCorner(120, 120, null), GameTestHelper.createTerrainSlopeCorner(260, 120, null), GameTestHelper.createTerrainSlopeCorner(260, 250, null), GameTestHelper.createTerrainSlopeCorner(120, 250, null)));
        terrainSlopePositions.add(terrainSlopePosition);

        double[][] heights = new double[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        };
        double[][] splattings = new double[][]{
                {0.7, 0.8, 0.1},
                {0.4, 0.9, 0.6},
                {0.5, 0.2, 0.3}
        };

        List<TerrainObjectConfig> terrainObjectConfigs = new ArrayList<>();
        terrainObjectConfigs.add(new TerrainObjectConfig().setId(1).setRadius(1));
        terrainObjectConfigs.add(new TerrainObjectConfig().setId(2).setRadius(5));
        terrainObjectConfigs.add(new TerrainObjectConfig().setId(3).setRadius(10));

        List<TerrainObjectPosition> terrainObjectPositions = new ArrayList<>();
        terrainObjectPositions.add(new TerrainObjectPosition().setId(1).setTerrainObjectId(1).setPosition(new DecimalPosition(50, 40)).setScale(10).setRotationZ(0));
        terrainObjectPositions.add(new TerrainObjectPosition().setId(1).setTerrainObjectId(1).setPosition(new DecimalPosition(100, 80)).setScale(10).setRotationZ(0));
        terrainObjectPositions.add(new TerrainObjectPosition().setId(2).setTerrainObjectId(1).setPosition(new DecimalPosition(150, 120)).setScale(1).setRotationZ(0));
        terrainObjectPositions.add(new TerrainObjectPosition().setId(2).setTerrainObjectId(2).setPosition(new DecimalPosition(200, 160)).setScale(1).setRotationZ(0));
        terrainObjectPositions.add(new TerrainObjectPosition().setId(1).setTerrainObjectId(3).setPosition(new DecimalPosition(250, 200)).setScale(0.2).setRotationZ(0));
        terrainObjectPositions.add(new TerrainObjectPosition().setId(2).setTerrainObjectId(3).setPosition(new DecimalPosition(300, 240)).setScale(0.5).setRotationZ(0));
        terrainObjectPositions.add(new TerrainObjectPosition().setId(1).setTerrainObjectId(3).setPosition(new DecimalPosition(50, 280)).setScale(1).setRotationZ(Math.toRadians(90)));
        terrainObjectPositions.add(new TerrainObjectPosition().setId(2).setTerrainObjectId(3).setPosition(new DecimalPosition(100, 40)).setScale(2).setRotationZ(0));


        setupTerrainTypeService(slopeSkeletonConfigs, terrainObjectConfigs, heights, null, terrainSlopePositions, terrainObjectPositions, null);

        // showDisplay();

        Collection<TerrainTile> terrainTiles = generateTerrainTiles(new Index(0, 0), new Index(0, 1), new Index(1, 0), new Index(1, 1));
        // AssertTerrainTile.saveTerrainTiles(terrainTiles, "testTerrainObjectTileGeneration4Tiles1.json");
        AssertTerrainTile assertTerrainTile = new AssertTerrainTile(getClass(), "testTerrainObjectTileGeneration4Tiles1.json");
        assertTerrainTile.assertEquals(terrainTiles);

        // AssertShapeAccess.saveShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(320, 320), "testTerrainObjectTileGeneration4TilesHNT1.json");
        AssertShapeAccess.assertShape(getTerrainService(), new DecimalPosition(0, 0), new DecimalPosition(320, 320), getClass(), "testTerrainObjectTileGeneration4TilesHNT1.json");

        // AssertTerrainShape.saveTerrainShape(getTerrainShape(), "testTerrainObjectTileGeneration4TilesShape1.json");
        AssertTerrainShape.assertTerrainShape(getClass(), "testTerrainObjectTileGeneration4TilesShape1.json", getTerrainShape());

    }
}
