package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainSlopeCorner;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.GameTestContent;
import com.btxtech.shared.gameengine.planet.GameTestHelper;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * on 21.11.2017.
 */
public class InvertedSlopeTerrainServiceTest extends WeldTerrainServiceTestBase {

    private void setup(List<TerrainSlopePosition> terrainSlopePositions) {
        List<SlopeSkeletonConfig> slopeSkeletonConfigs = new ArrayList<>();

        SlopeSkeletonConfig slopeSkeletonConfigLand = new SlopeSkeletonConfig();
        slopeSkeletonConfigLand.setId(1).setType(SlopeSkeletonConfig.Type.LAND);
        slopeSkeletonConfigLand.setRows(5).setSegments(1).setWidth(11).setVerticalSpace(5).setHeight(20);
        slopeSkeletonConfigLand.setSlopeNodes(toColumnRow(new SlopeNode[][]{
                {GameTestHelper.createSlopeNode(2, 0, 1),},
                {GameTestHelper.createSlopeNode(4, 8, 0.7),},
                {GameTestHelper.createSlopeNode(7, 12, 0.7),},
                {GameTestHelper.createSlopeNode(10, 20, 0.7),},
                {GameTestHelper.createSlopeNode(11, 20, 0.7),},
        }));
        slopeSkeletonConfigLand.setOuterLineGameEngine(3).setInnerLineGameEngine(7);
        slopeSkeletonConfigs.add(slopeSkeletonConfigLand);

        SlopeSkeletonConfig slopeSkeletonConfigWater = new SlopeSkeletonConfig();
        slopeSkeletonConfigWater.setId(2).setType(SlopeSkeletonConfig.Type.WATER);
        slopeSkeletonConfigWater.setRows(5).setSegments(1).setWidth(12).setVerticalSpace(5).setHeight(-2);
        slopeSkeletonConfigWater.setSlopeNodes(toColumnRow(new SlopeNode[][]{
                {GameTestHelper.createSlopeNode(2, 0, 1),},
                {GameTestHelper.createSlopeNode(4, 0, 0.7),},
                {GameTestHelper.createSlopeNode(8, 0, 0.7),},
                {GameTestHelper.createSlopeNode(10, -3, 0.7),},
                {GameTestHelper.createSlopeNode(12, -10, 0.7),},
        }));
        slopeSkeletonConfigWater.setOuterLineGameEngine(4).setCoastDelimiterLineGameEngine(8).setInnerLineGameEngine(11);
        slopeSkeletonConfigs.add(slopeSkeletonConfigWater);


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

        PlanetConfig planetConfig = GameTestContent.setupPlanetConfig();
        planetConfig.setPlayGround(new Rectangle2D(50, 50, 5000, 5000));
        planetConfig.setTerrainTileDimension(new Rectangle(0, 0, 32, 32));

        setupTerrainTypeService(heights, splattings, slopeSkeletonConfigs, null, planetConfig, terrainSlopePositions, null);
    }

    private List<TerrainSlopePosition> setupSlope(int slopeConfigId, boolean inverted, List<TerrainSlopePosition> children, TerrainSlopeCorner... slopePolygon) {
        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        TerrainSlopePosition terrainSlopePosition = new TerrainSlopePosition();
        terrainSlopePosition.setId(slopeConfigId);
        terrainSlopePosition.setSlopeConfigId(slopeConfigId);
        terrainSlopePosition.setPolygon(Arrays.asList(slopePolygon));
        terrainSlopePosition.setChildren(children);
        terrainSlopePosition.setInverted(inverted);
        terrainSlopePositions.add(terrainSlopePosition);
        return terrainSlopePositions;
    }

    @Test
    public void testWater() {
        List<TerrainSlopePosition> children = setupSlope(2, true, null, GameTestHelper.createTerrainSlopeCorner(90, 100, null), GameTestHelper.createTerrainSlopeCorner(180, 100, null), GameTestHelper.createTerrainSlopeCorner(180, 170, null), GameTestHelper.createTerrainSlopeCorner(80, 180, null));
        // List<TerrainSlopePosition> children = null;

        List<TerrainSlopePosition> parent = setupSlope(2, false, children, GameTestHelper.createTerrainSlopeCorner(40, 40, null), GameTestHelper.createTerrainSlopeCorner(250, 40, null), GameTestHelper.createTerrainSlopeCorner(260, 270, null), GameTestHelper.createTerrainSlopeCorner(40, 250, null));

        setup(parent);

        showDisplay();
    }


    @Test
    public void testLand() {
        List<TerrainSlopePosition> children = setupSlope(1, true, null, GameTestHelper.createTerrainSlopeCorner(90, 100, null), GameTestHelper.createTerrainSlopeCorner(180, 100, null), GameTestHelper.createTerrainSlopeCorner(180, 170, null), GameTestHelper.createTerrainSlopeCorner(80, 180, null));
        // List<TerrainSlopePosition> children = null;

        List<TerrainSlopePosition> parent = setupSlope(1, false, children, GameTestHelper.createTerrainSlopeCorner(40, 40, null), GameTestHelper.createTerrainSlopeCorner(250, 40, null), GameTestHelper.createTerrainSlopeCorner(260, 270, null), GameTestHelper.createTerrainSlopeCorner(40, 250, null));

        setup(parent);

        showDisplay();
    }


}
