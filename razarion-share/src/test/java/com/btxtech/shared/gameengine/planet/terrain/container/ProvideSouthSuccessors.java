package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.planet.GameTestHelper;
import com.btxtech.shared.gameengine.planet.gui.userobject.MouseMoveCallback;
import com.btxtech.shared.gameengine.planet.gui.userobject.PositionMarker;
import com.btxtech.shared.gameengine.planet.pathing.AStarContext;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.gameengine.planet.terrain.WeldTerrainServiceTestBase;
import javafx.scene.paint.Color;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Beat
 * on 12.07.2017.
 */
public class ProvideSouthSuccessors extends WeldTerrainServiceTestBase {

    private void setup() {
        List<SlopeConfig> slopeConfigs = new ArrayList<>();
        SlopeConfig slopeConfigLand = new SlopeConfig();
        slopeConfigLand.setId(1).setType(SlopeConfig.Type.LAND);
        slopeConfigLand.setRows(4).setSegments(1).setWidth(7).setHorizontalSpace(5).setHeight(20);
        slopeConfigLand.setSlopeNodes(toColumnRow(new SlopeNode[][]{
                {GameTestHelper.createSlopeNode(2, 1, 0.3),},
                {GameTestHelper.createSlopeNode(4, 5, 1),},
                {GameTestHelper.createSlopeNode(8, 10, 0.7),},
                {GameTestHelper.createSlopeNode(10, 20, 0.7),},
        }));
        slopeConfigLand.setOuterLineGameEngine(1).setInnerLineGameEngine(8);
        slopeConfigs.add(slopeConfigLand);

        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
//        TerrainSlopePosition terrainSlopePositionLand = new TerrainSlopePosition();
//        terrainSlopePositionLand.setId(1);
//        terrainSlopePositionLand.setSlopeConfigId(1);
//        terrainSlopePositionLand.setPolygon(Arrays.asList(GameTestHelper.createTerrainSlopeCorner(50, 40, null), GameTestHelper.createTerrainSlopeCorner(100, 40, null),
//                GameTestHelper.createTerrainSlopeCorner(100, 60, 1), GameTestHelper.createTerrainSlopeCorner(100, 90, 1), // driveway
//                GameTestHelper.createTerrainSlopeCorner(100, 110, null), GameTestHelper.createTerrainSlopeCorner(50, 110, null)));
//        terrainSlopePositions.add(terrainSlopePositionLand);

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

        List<TerrainObjectConfig> terrainObjectConfigs = new ArrayList<>();
        terrainObjectConfigs.add(new TerrainObjectConfig().setId(1).setRadius(1));
        terrainObjectConfigs.add(new TerrainObjectConfig().setId(2).setRadius(10));
        terrainObjectConfigs.add(new TerrainObjectConfig().setId(3).setRadius(20));

        List<TerrainObjectPosition> terrainObjectPositions = Arrays.asList(
                new TerrainObjectPosition().setTerrainObjectId(3).setPosition(new DecimalPosition(50, 51)).setScale(1),
                new TerrainObjectPosition().setTerrainObjectId(3).setPosition(new DecimalPosition(70, 82)).setScale(1)
        );

        setupTerrainTypeService(slopeConfigs, terrainObjectConfigs, heights, null, terrainSlopePositions, terrainObjectPositions, null);
    }

    @Test
    public void testProvideSouthSuccessors() {
        setup();

        PathingNodeWrapper pathingNodeWrapper = getTerrainService().getPathingAccess().getPathingNodeWrapper(new DecimalPosition(41, 71));
        NodeHandlerHelper nodeHandlerHelper = new NodeHandlerHelper();
        nodeHandlerHelper.addExpectedSubNode2(new DecimalPosition(40, 69));
        nodeHandlerHelper.addExpectedSubNode2(new DecimalPosition(41, 69));
        pathingNodeWrapper.provideSouthSuccessors(new AStarContext(TerrainType.LAND, Collections.emptyList()), nodeHandlerHelper::addActual);

        nodeHandlerHelper.doAssert();
    }

    // @Test
    public void display() {
        setup();

        showDisplay(new MouseMoveCallback().setCallback(position -> {
            // System.out.println("-----------------------------------------------");
            PathingNodeWrapper movePathingNodeWrapper = getTerrainService().getPathingAccess().getPathingNodeWrapper(position);
            PositionMarker positionMarker = new PositionMarker();
            displayPathingNodeWrapper(movePathingNodeWrapper, positionMarker, Color.GRAY);
            calculateSubSuccessors(movePathingNodeWrapper, positionMarker);
            return new Object[]{positionMarker};
        }));
    }

    private void calculateSubSuccessors(PathingNodeWrapper pathingNodeWrapper, PositionMarker positionMarker) {
        pathingNodeWrapper.provideEastSuccessors(new AStarContext(TerrainType.LAND, Collections.emptyList()), pathingNodeWrapperSuccessor -> {
            displayPathingNodeWrapper(pathingNodeWrapperSuccessor, positionMarker, Color.BLUE);
        });
    }

    private void displayPathingNodeWrapper(PathingNodeWrapper pathingNodeWrapper, PositionMarker positionMarker, Color color) {
        if (pathingNodeWrapper.getNodeIndex() != null) {
            Rectangle2D currentNode = TerrainUtil.toAbsoluteNodeRectangle(pathingNodeWrapper.getNodeIndex());
            positionMarker.addRectangle2D(currentNode, color);
        } else if (pathingNodeWrapper.getSubNodePosition() != null) {
            double length = TerrainUtil.calculateSubNodeLength(pathingNodeWrapper.getTerrainShapeSubNode().getDepth());
            positionMarker.addRectangle2D(new Rectangle2D(pathingNodeWrapper.getSubNodePosition().getX(), pathingNodeWrapper.getSubNodePosition().getY(), length, length), color);
        }
    }
}