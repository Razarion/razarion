package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainSlopeCorner;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.planet.terrain.gui.TerrainTestApplication;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 03.04.2017.
 */
public class ChildrenSlopeTerrainServiceTest extends TerrainServiceTestBase {
    private static int SKELETON_CONFIG_ID_1 = 1;

    @Test
    public void testSingleChild() {
        List<TerrainSlopePosition> children = new ArrayList<>();

        TerrainSlopePosition child = new TerrainSlopePosition();
        child.setId(2);
        child.setSlopeConfigEntity(SKELETON_CONFIG_ID_1);
        child.setPolygon(Arrays.asList(createTerrainSlopeCorner(100, 90, null), createTerrainSlopeCorner(170, 90, null), createTerrainSlopeCorner(170, 140, null), createTerrainSlopeCorner(100, 140, null)));
        children.add(child);

        TerrainSlopePosition parent = new TerrainSlopePosition();
        parent.setId(1);
        parent.setSlopeConfigEntity(SKELETON_CONFIG_ID_1);
        parent.setPolygon(Arrays.asList(createTerrainSlopeCorner(50, 40, null), createTerrainSlopeCorner(220, 40, null), createTerrainSlopeCorner(220, 200, null), createTerrainSlopeCorner(50, 200, null)));
        parent.setChildren(children);

        Collection<TerrainTile> terrainTiles = setup(parent);

        // TerrainTileTestHelper.saveTerrainTiles(terrainTiles, "testSingleChild1.json");
        // TerrainTileTestHelper terrainTileTestHelper = new TerrainTileTestHelper(getClass(), "testSingleChild1.json");
        // terrainTileTestHelper.assertEquals(terrainTiles);
        TerrainTestApplication.show(null, terrainTiles);

    }

    private Collection<TerrainTile> setup(TerrainSlopePosition terrainSlopePosition) {
        List<SlopeSkeletonConfig> slopeSkeletonConfigs = new ArrayList<>();
        SlopeSkeletonConfig slopeSkeletonConfigLand = new SlopeSkeletonConfig();
        slopeSkeletonConfigLand.setId(SKELETON_CONFIG_ID_1).setType(SlopeSkeletonConfig.Type.LAND);
        slopeSkeletonConfigLand.setRows(6).setSegments(1).setWidth(11).setVerticalSpace(5).setHeight(20);
        SlopeNode[][] slopeNodes = new SlopeNode[][]{
                {createSlopeNode(0, 0, 0.3),},
                {createSlopeNode(2, 5, 1),},
                {createSlopeNode(4, 10, 0.7),},
                {createSlopeNode(7, 20, 0.7),},
                {createSlopeNode(10, 20, 0.7),},
                {createSlopeNode(11, 20, 0.7),},
        };
        slopeSkeletonConfigLand.setSlopeNodes(toColumnRow(slopeNodes));
        slopeSkeletonConfigs.add(slopeSkeletonConfigLand);

        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        terrainSlopePositions.add(terrainSlopePosition);

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

        setupTerrainService(heights, splattings, slopeSkeletonConfigs, terrainSlopePositions);

        Collection<TerrainTile> terrainTiles = new ArrayList<>();
        terrainTiles.add(generateTerrainTile(new Index(0, 0)));
        terrainTiles.add(generateTerrainTile(new Index(0, 1)));
        terrainTiles.add(generateTerrainTile(new Index(0, 2)));
        terrainTiles.add(generateTerrainTile(new Index(1, 0)));
        terrainTiles.add(generateTerrainTile(new Index(1, 1)));
        terrainTiles.add(generateTerrainTile(new Index(1, 2)));
        terrainTiles.add(generateTerrainTile(new Index(2, 0)));
        terrainTiles.add(generateTerrainTile(new Index(2, 1)));
        terrainTiles.add(generateTerrainTile(new Index(2, 2)));

        return terrainTiles;
    }
}
