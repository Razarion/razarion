package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainSlopeCorner;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.planet.terrain.gui.TerrainTestApplication;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 03.04.2017.
 */
public class DrivewaySlopeTerrainServiceTest extends TerrainServiceTestBase {
    @Test
    public void testEdge() {
        setup(createTerrainSlopeCorner(50, 40, null), createTerrainSlopeCorner(100, 40, null),
                createTerrainSlopeCorner(100, 60, 1), createTerrainSlopeCorner(100, 90, 1), // driveway
                createTerrainSlopeCorner(100, 110, null), createTerrainSlopeCorner(50, 110, null));
    }

    @Test
    public void testCorner() {
        setup(createTerrainSlopeCorner(50, 40, null),
                createTerrainSlopeCorner(70, 40, 1), createTerrainSlopeCorner(90, 40, 1), createTerrainSlopeCorner(100, 40, 1), createTerrainSlopeCorner(100, 50, 1), createTerrainSlopeCorner(100, 80, 1),// driveway
                createTerrainSlopeCorner(100, 110, null), createTerrainSlopeCorner(50, 110, null));
    }

    @Test
    public void testFromFile() throws IOException {
        String string = new String(Files.readAllBytes(new File("C:\\dev\\projects\\razarion\\code\\tmp\\slopedriveway.json").toPath()));
        List<TerrainSlopeCorner> terrainSlopeCorners = new ObjectMapper().readValue(string, new TypeReference<List<TerrainSlopeCorner>>() {
        });
        setup(terrainSlopeCorners.toArray(new TerrainSlopeCorner[terrainSlopeCorners.size()]));
    }

    private void setup(TerrainSlopeCorner... slopePolygon) {
        List<SlopeSkeletonConfig> slopeSkeletonConfigs = new ArrayList<>();
        SlopeSkeletonConfig slopeSkeletonConfigLand = new SlopeSkeletonConfig();
        slopeSkeletonConfigLand.setId(1).setType(SlopeSkeletonConfig.Type.LAND);
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
        TerrainSlopePosition terrainSlopePositionLand = new TerrainSlopePosition();
        terrainSlopePositionLand.setId(1);
        terrainSlopePositionLand.setSlopeConfigEntity(1);
        terrainSlopePositionLand.setPolygon(Arrays.asList(slopePolygon));
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

        TerrainTestApplication.show(null, terrainTiles);
    }
}
