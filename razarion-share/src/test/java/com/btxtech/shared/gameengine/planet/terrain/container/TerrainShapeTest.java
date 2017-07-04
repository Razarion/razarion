package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainSlopeCorner;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.terrain.TerrainServiceTestBase;
import com.btxtech.shared.gameengine.planet.terrain.gui.terrainshape.TerrainShapeTestDisplay;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * on 30.06.2017.
 */
public class TerrainShapeTest extends TerrainServiceTestBase {

    protected TerrainShape setup(TerrainSlopeCorner... terrainSlopeCorners) {
        List<SlopeSkeletonConfig> slopeSkeletonConfigs = new ArrayList<>();
        SlopeSkeletonConfig slopeSkeletonConfigLand = new SlopeSkeletonConfig();
        slopeSkeletonConfigLand.setId(1).setType(SlopeSkeletonConfig.Type.LAND);
        slopeSkeletonConfigLand.setRows(4).setSegments(1).setWidth(7).setVerticalSpace(5).setHeight(20);
        SlopeNode[][] slopeNodes = new SlopeNode[][]{
                {createSlopeNode(0, 0, 0.3),},
                {createSlopeNode(2, 5, 1),},
                {createSlopeNode(4, 10, 0.7),},
                {createSlopeNode(7, 20, 0.7),},
        };
        slopeSkeletonConfigLand.setSlopeNodes(toColumnRow(slopeNodes));
        slopeSkeletonConfigs.add(slopeSkeletonConfigLand);

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

        setupTerrainTypeService(heights, splattings, slopeSkeletonConfigs);

        PlanetConfig planetConfig = new PlanetConfig();
        planetConfig.setTerrainSlopePositions(terrainSlopePositions);
        planetConfig.setTerrainTileDimension(new Rectangle(0, 0, 1, 1));

        return new TerrainShape(planetConfig, getTerrainTypeService(), terrainSlopePositions, null);
    }


    @Test
    public void testSimpleSlope() {
        TerrainShape terrainShape = setup(createTerrainSlopeCorner(50, 40, null), createTerrainSlopeCorner(100, 40, null), createTerrainSlopeCorner(100, 110, null), createTerrainSlopeCorner(50, 110, null));
        TerrainShapeTestDisplay.show(terrainShape);
        Assert.fail("TODO assert");
    }

    @Test
    public void testSlopeDriveway() {
        TerrainShape terrainShape = setup(createTerrainSlopeCorner(50, 40, null), createTerrainSlopeCorner(100, 40, null),
                createTerrainSlopeCorner(100, 60, 1), createTerrainSlopeCorner(100, 90, 1), // driveway
                createTerrainSlopeCorner(100, 110, null), createTerrainSlopeCorner(50, 110, null));
        // TerrainShapeTestDisplay.show(terrainShape);
        Assert.fail("TODO assert");
    }
}