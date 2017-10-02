package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopeCorner;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.GameTestContent;
import com.btxtech.shared.gameengine.planet.terrain.WeldTerrainServiceTestBase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * on 30.06.2017.
 */
public class TerrainShapeTest extends WeldTerrainServiceTestBase {

    protected TerrainShape setup(SlopeSkeletonConfig.Type type, List<TerrainObjectPosition> terrainObjectPositions, TerrainSlopeCorner... terrainSlopeCorners) {
        List<SlopeSkeletonConfig> slopeSkeletonConfigs = new ArrayList<>();
        SlopeSkeletonConfig slopeSkeletonConfigLand = new SlopeSkeletonConfig();
        slopeSkeletonConfigLand.setId(1).setType(type);
        slopeSkeletonConfigLand.setRows(4).setSegments(1).setWidth(10).setVerticalSpace(5).setHeight(20);
        SlopeNode[][] slopeNodes = new SlopeNode[][]{
                {createSlopeNode(0, 0, 0.3),},
                {createSlopeNode(2, 5, 1),},
                {createSlopeNode(4, 10, 0.7),},
                {createSlopeNode(10, 20, 0.7),},
        };
        slopeSkeletonConfigLand.setSlopeNodes(toColumnRow(slopeNodes));
        slopeSkeletonConfigs.add(slopeSkeletonConfigLand);

        List<TerrainObjectConfig> terrainObjectConfigs = new ArrayList<>();
        terrainObjectConfigs.add(new TerrainObjectConfig().setId(1).setRadius(1));
        terrainObjectConfigs.add(new TerrainObjectConfig().setId(2).setRadius(5));
        terrainObjectConfigs.add(new TerrainObjectConfig().setId(3).setRadius(10));

        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        TerrainSlopePosition terrainSlopePositionLand = new TerrainSlopePosition();
        terrainSlopePositionLand.setId(1);
        terrainSlopePositionLand.setSlopeConfigId(1);
        terrainSlopePositionLand.setPolygon(Arrays.asList(terrainSlopeCorners));
        terrainSlopePositions.add(terrainSlopePositionLand);

        double[][] heights = new double[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 10, 0, 0},
                {0, 0, 0, 0}
        };
        double[][] splattings = new double[][]{
                {0.7, 0.8, 0.9},
                {0.4, 0.5, 0.6},
                {0.1, 0.2, 0.3}
        };

        PlanetConfig planetConfig = GameTestContent.setupPlanetConfig();
        planetConfig.setTerrainTileDimension(new Rectangle(0, 0, 1, 1));
        planetConfig.setTerrainObjectPositions(terrainObjectPositions);
        setupTerrainTypeService(heights, splattings, slopeSkeletonConfigs, terrainObjectConfigs, planetConfig, terrainSlopePositions);
        return getTerrainShape();
    }

    @Test
    public void testSimpleSlope() {
        TerrainShape terrainShape = setup(SlopeSkeletonConfig.Type.LAND, null, createTerrainSlopeCorner(50, 40, null), createTerrainSlopeCorner(100, 40, null), createTerrainSlopeCorner(100, 110, null), createTerrainSlopeCorner(50, 110, null));
        // AssertTerrainShape.saveTerrainShape( terrainShape, "testSimpleSlopeShape1.json");
        // TerrainShapeTestDisplay.show(terrainShape);
        AssertTerrainShape.assertTerrainShape(TerrainShapeTest.class, "testSimpleSlopeShape1.json", terrainShape);
    }

    @Test
    public void testSlopeDrivewayShape() {
        TerrainShape terrainShape = setup(SlopeSkeletonConfig.Type.LAND, null, createTerrainSlopeCorner(30, 40, null), createTerrainSlopeCorner(78, 40, null),
                createTerrainSlopeCorner(78, 60, 1), createTerrainSlopeCorner(78, 90, 1), // driveway
                createTerrainSlopeCorner(78, 110, null), createTerrainSlopeCorner(30, 110, null));
        // AssertTerrainShape.saveTerrainShape( terrainShape, "testSlopeDrivewayShape1.json");
        // TerrainShapeTestDisplay.show(terrainShape);
        AssertTerrainShape.assertTerrainShape(TerrainShapeTest.class, "testSlopeDrivewayShape1.json", terrainShape);
    }

//    @Test
//    public void testWaterShape() {
//        TerrainShape terrainShape = setup(SlopeSkeletonConfig.Type.WATER, null, createTerrainSlopeCorner(50, 40, null), createTerrainSlopeCorner(100, 40, null), createTerrainSlopeCorner(100, 110, null), createTerrainSlopeCorner(50, 110, null));
//        TerrainShapeTestDisplay.show(terrainShape);
//        Assert.fail("TODO assert");
//    }

    @Test
    public void testTerrainObject() {
        List<TerrainObjectPosition> terrainObjectPositions = Arrays.asList(
                new TerrainObjectPosition().setTerrainObjectId(1).setPosition(new DecimalPosition(10, 10)),
                new TerrainObjectPosition().setTerrainObjectId(2).setPosition(new DecimalPosition(21, 32)),
                new TerrainObjectPosition().setTerrainObjectId(3).setPosition(new DecimalPosition(135, 130)),
                new TerrainObjectPosition().setTerrainObjectId(2).setPosition(new DecimalPosition(44, 27.5)),
                new TerrainObjectPosition().setTerrainObjectId(2).setPosition(new DecimalPosition(72, 88)),
                new TerrainObjectPosition().setTerrainObjectId(2).setPosition(new DecimalPosition(20, 60)),
                new TerrainObjectPosition().setTerrainObjectId(2).setPosition(new DecimalPosition(92, 64))
        );
        TerrainShape terrainShape = setup(SlopeSkeletonConfig.Type.LAND, terrainObjectPositions, createTerrainSlopeCorner(30, 40, null), createTerrainSlopeCorner(80, 40, null),
                createTerrainSlopeCorner(80, 60, 1), createTerrainSlopeCorner(80, 90, 1), // driveway
                createTerrainSlopeCorner(80, 110, null), createTerrainSlopeCorner(30, 110, null));
        // TerrainShapeTestDisplay.show(terrainShape);
        // AssertTerrainShape.saveTerrainShape( terrainShape, "testTerrainObjectShape1.json");
        AssertTerrainShape.assertTerrainShape(TerrainShapeTest.class, "testTerrainObjectShape1.json", terrainShape);
    }
}