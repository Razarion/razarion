package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.TestHelper;
import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopeCorner;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.GameTestContent;
import com.btxtech.shared.gameengine.planet.GameTestHelper;
import com.btxtech.shared.gameengine.planet.gui.WeldDisplay;
import com.btxtech.shared.gameengine.planet.gui.userobject.PositionMarker;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.WeldTerrainServiceTestBase;
import com.btxtech.shared.gameengine.planet.terrain.container.PathingNodeWrapper;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.utils.GeometricUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * on 28.09.2017.
 */
public class DestinationFinderTest extends WeldTerrainServiceTestBase {

    private void setup(SlopeSkeletonConfig.Type type, TerrainSlopeCorner... terrainSlopeCorners) throws Exception {
        List<SlopeSkeletonConfig> slopeSkeletonConfigs = new ArrayList<>();
        SlopeSkeletonConfig slopeSkeletonConfigLand = new SlopeSkeletonConfig();
        slopeSkeletonConfigLand.setId(1).setType(type);
        slopeSkeletonConfigLand.setRows(3).setSegments(1).setWidth(7).setVerticalSpace(5).setHeight(20);
        SlopeNode[][] slopeNodes = new SlopeNode[][]{
                {GameTestHelper.createSlopeNode(2, 5, 1),},
                {GameTestHelper.createSlopeNode(4, 10, 0.7),},
                {GameTestHelper.createSlopeNode(7, 20, 0.7),},
        };
        slopeSkeletonConfigLand.setSlopeNodes(toColumnRow(slopeNodes));
        slopeSkeletonConfigLand.setOuterLineTerrainType(1).setCoastDelimiterLineTerrainType(3).setInnerLineTerrainType(6);
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

        List<TerrainObjectConfig> terrainObjectConfigs = new ArrayList<>();
        terrainObjectConfigs.add(new TerrainObjectConfig().setId(1).setRadius(1));
        terrainObjectConfigs.add(new TerrainObjectConfig().setId(2).setRadius(5));
        terrainObjectConfigs.add(new TerrainObjectConfig().setId(3).setRadius(10));

        PlanetConfig planetConfig = GameTestContent.setupPlanetConfig();
        planetConfig.setPlayGround(new Rectangle2D(50, 50, 5000, 5000));
        planetConfig.setTerrainTileDimension(new Rectangle(0, 0, 32, 32));
        List<TerrainObjectPosition> terrainObjectPositions = new ArrayList<>();
        terrainObjectPositions.add((new TerrainObjectPosition().setTerrainObjectId(3).setScale(1).setPosition(new DecimalPosition(76, 30))));
        terrainObjectPositions.add((new TerrainObjectPosition().setTerrainObjectId(3).setScale(1).setPosition(new DecimalPosition(114, 28))));
        terrainObjectPositions.add((new TerrainObjectPosition().setTerrainObjectId(3).setScale(1).setPosition(new DecimalPosition(95, 11))));
        terrainObjectPositions.add((new TerrainObjectPosition().setTerrainObjectId(3).setScale(1).setPosition(new DecimalPosition(223, 95))));
        terrainObjectPositions.add((new TerrainObjectPosition().setTerrainObjectId(3).setScale(1).setPosition(new DecimalPosition(191, 116))));
        terrainObjectPositions.add((new TerrainObjectPosition().setTerrainObjectId(3).setScale(1).setPosition(new DecimalPosition(48, 124))));
        terrainObjectPositions.add((new TerrainObjectPosition().setTerrainObjectId(3).setScale(1).setPosition(new DecimalPosition(132, 131))));
        planetConfig.setTerrainObjectPositions(terrainObjectPositions);

        setupTerrainTypeService(heights, splattings, slopeSkeletonConfigs, terrainObjectConfigs, planetConfig, terrainSlopePositions);
    }

    @Test
    public void find() throws Exception {
        setup(SlopeSkeletonConfig.Type.LAND, GameTestHelper.createTerrainSlopeCorner(50, 40, null), GameTestHelper.createTerrainSlopeCorner(100, 40, null),
                GameTestHelper.createTerrainSlopeCorner(100, 60, 1), GameTestHelper.createTerrainSlopeCorner(100, 90, 1), // driveway
                GameTestHelper.createTerrainSlopeCorner(100, 110, null), GameTestHelper.createTerrainSlopeCorner(50, 110, null));

        PositionMarker positionMarker = new PositionMarker();
        positionMarker.addPosition(new DecimalPosition(60, 41));
        showDisplay();

        List<Index> subNodeIndexScope = GeometricUtil.rasterizeCircle(new Circle2D(TerrainUtil.smallestSubNodeCenter(Index.ZERO), 3), (int) TerrainUtil.MIN_SUB_NODE_LENGTH);
        PathingNodeWrapper destinationNode = getTerrainService().getPathingAccess().getPathingNodeWrapper(new DecimalPosition(60, 41));
        DestinationFinder destinationFinder = new DestinationFinder(destinationNode, TerrainType.LAND, subNodeIndexScope, getTerrainService().getPathingAccess());
        PathingNodeWrapper correctedDestinationNode = destinationFinder.find();

        Assert.assertNull(correctedDestinationNode.getNodeIndex());
        TestHelper.assertDecimalPosition(null, new DecimalPosition(60, 44), correctedDestinationNode.getSubNodePosition());
    }
}