package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.gameengine.planet.gui.userobject.MouseMoveCallback;
import com.btxtech.shared.gameengine.planet.gui.userobject.PositionMarker;
import com.btxtech.shared.gameengine.planet.pathing.AStarContext;
import com.btxtech.shared.gameengine.planet.terrain.DaggerTerrainServiceTestBase;
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
public class ProvideSouthSuccessors extends DaggerTerrainServiceTestBase {

    private void setup() {
        List<TerrainObjectConfig> terrainObjectConfigs = new ArrayList<>();
        terrainObjectConfigs.add(new TerrainObjectConfig().id(1).radius(1));
        terrainObjectConfigs.add(new TerrainObjectConfig().id(2).radius(10));
        terrainObjectConfigs.add(new TerrainObjectConfig().id(3).radius(20));

        List<TerrainObjectPosition> terrainObjectPositions = Arrays.asList(
                new TerrainObjectPosition().terrainObjectConfigId(3).position(new DecimalPosition(50, 51)),
                new TerrainObjectPosition().terrainObjectConfigId(3).position(new DecimalPosition(70, 82))
        );

        setupTerrainTypeService(null, terrainObjectConfigs, null, terrainObjectPositions, null, null);
    }

    @Test
    public void testProvideSouthSuccessors() {
        setup();

        PathingNodeWrapper pathingNodeWrapper = getTerrainService().getTerrainAnalyzer().getPathingNodeWrapper(new DecimalPosition(41, 71));
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
            PathingNodeWrapper movePathingNodeWrapper = getTerrainService().getTerrainAnalyzer().getPathingNodeWrapper(position);
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
//        if (pathingNodeWrapper.getNodeIndex() != null) {
//            Rectangle2D currentNode = TerrainUtil.toAbsoluteNodeRectangle(pathingNodeWrapper.getNodeIndex());
//            positionMarker.addRectangle2D(currentNode, color);
//        } else if (pathingNodeWrapper.getSubNodePosition() != null) {
//            double length = TerrainUtil.calculateSubNodeLength(pathingNodeWrapper.getTerrainShapeSubNode().getDepth());
//            positionMarker.addRectangle2D(new Rectangle2D(pathingNodeWrapper.getSubNodePosition().getX(), pathingNodeWrapper.getSubNodePosition().getY(), length, length), color);
//        }
    }
}