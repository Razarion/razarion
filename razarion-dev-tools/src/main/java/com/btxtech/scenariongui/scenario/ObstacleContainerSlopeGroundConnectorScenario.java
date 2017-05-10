package com.btxtech.scenariongui.scenario;

import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleContainer;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleContainerNode;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * 05.04.2017.
 */
public class ObstacleContainerSlopeGroundConnectorScenario extends Scenario {
    private ObstacleContainer obstacleContainer;
    private TerrainService terrainService;

    @Override
    public void init() {
        super.init();
        terrainService = new TerrainService();
        obstacleContainer = new ObstacleContainer();
        TerrainTypeService terrainTypeService = new TerrainTypeService();
        FrameworkHelper.injectService("obstacleContainer", terrainService, obstacleContainer);

        double[][] heights = new double[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
        };
        double[][] splattings = new double[][]{
                {0, 0, 0},
                {0, 0, 0},
                {0, 0, 0},
        };
        StaticGameConfig staticGameConfig = new StaticGameConfig();
        GroundSkeletonConfig groundSkeletonConfig = new GroundSkeletonConfig();
        staticGameConfig.setGroundSkeletonConfig(groundSkeletonConfig);
        groundSkeletonConfig.setHeights(FrameworkHelper.toColumnRow(heights));
        groundSkeletonConfig.setHeightXCount(heights[0].length);
        groundSkeletonConfig.setHeightYCount(heights.length);
        groundSkeletonConfig.setSplattings(FrameworkHelper.toColumnRow(splattings));
        groundSkeletonConfig.setSplattingXCount(splattings[0].length);
        groundSkeletonConfig.setSplattingYCount(splattings.length);

        List<SlopeSkeletonConfig> slopeSkeletonConfigs = new ArrayList<>();
        SlopeSkeletonConfig slopeSkeletonConfigLand = new SlopeSkeletonConfig();
        slopeSkeletonConfigLand.setId(1).setType(SlopeSkeletonConfig.Type.LAND);
        slopeSkeletonConfigLand.setRows(4).setSegments(1).setVerticalSpace(5).setWidth(20).setHeight(4);
        SlopeNode[][] slopeNodes = new SlopeNode[][]{
                {FrameworkHelper.createSlopeNode(0, 0, 0.0),},
                {FrameworkHelper.createSlopeNode(5, 1, 0.2),},
                {FrameworkHelper.createSlopeNode(10, 2, 0.4),},
                {FrameworkHelper.createSlopeNode(15, 3, 0.6),},
                {FrameworkHelper.createSlopeNode(20, 4, 0.8),},
        };
        slopeSkeletonConfigLand.setSlopeNodes(FrameworkHelper.toColumnRow(slopeNodes));
        slopeSkeletonConfigs.add(slopeSkeletonConfigLand);

        staticGameConfig.setSlopeSkeletonConfigs(slopeSkeletonConfigs);

        terrainTypeService.init(staticGameConfig);
        FrameworkHelper.injectService("terrainTypeService", terrainService, terrainTypeService);

        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        TerrainSlopePosition terrainSlopePositionLand = new TerrainSlopePosition();
        terrainSlopePositionLand.setId(1);
        terrainSlopePositionLand.setSlopeConfigEntity(1);
        terrainSlopePositionLand.setPolygon(Arrays.asList(new DecimalPosition(50, 40), new DecimalPosition(100, 40), new DecimalPosition(100, 110), new DecimalPosition(50, 110)));
        terrainSlopePositions.add(terrainSlopePositionLand);

        PlanetConfig planetConfig = new PlanetConfig();
        planetConfig.setTerrainSlopePositions(terrainSlopePositions);
        planetConfig.setGroundMeshDimension(new Rectangle(0, 0, 30, 30));
        terrainService.setup(planetConfig);
    }

    @Override
    public void render(ExtendedGraphicsContext extendedGraphicsContext) {
        for (int x = 0; x < obstacleContainer.getXCount(); x++) {
            for (int y = 0; y < obstacleContainer.getYCount(); y++) {
                Index index = new Index(x, y);
                ObstacleContainerNode obstacleContainerNode = obstacleContainer.getObstacleContainerNode(index);
                DecimalPosition absolutePosition = obstacleContainer.toAbsolute(index);
                extendedGraphicsContext.getGc().setFill(Color.BLACK);
                extendedGraphicsContext.getGc().fillRect(absolutePosition.getX(), absolutePosition.getY(), TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH, TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH);
                if (obstacleContainerNode != null) {
                    extendedGraphicsContext.getGc().setFill(Color.GREEN);
                    extendedGraphicsContext.getGc().fillRect(absolutePosition.getX(), absolutePosition.getY(), TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH - 0.1, TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH - 0.1);
                } else {
                    extendedGraphicsContext.getGc().setFill(Color.LIGHTGRAY);
                    extendedGraphicsContext.getGc().fillRect(absolutePosition.getX(), absolutePosition.getY(), TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH - 0.1, TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH - 0.1);
                }
            }
        }


        System.out.println("-------------------------------------------------------------");
        Index testPintIndex = new Index(15, 5);
        DecimalPosition absolutePosition = obstacleContainer.toAbsolute(testPintIndex);
        extendedGraphicsContext.getGc().setFill(Color.YELLOW);
        extendedGraphicsContext.getGc().fillRect(absolutePosition.getX(), absolutePosition.getY(), TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH, TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH);
        List<DecimalPosition> vertices1 = new ArrayList<>(obstacleContainer.getObstacleContainerNode(testPintIndex).getOuterSlopeGroundPiercingLine()).get(0);
        // extendedGraphicsContext.strokeCurve(vertices1, 0.2, Color.RED, true);

//        for (Slope slope : terrainService.getSlopes()) {
//            extendedGraphicsContext.strokeVertices(slope.getOuterLine(), Color.BLUE, 1);
//        }

//        for (int x = 0; x < obstacleContainer.getXCount(); x++) { // TODO make test case
//            for (int y = 0; y < obstacleContainer.getYCount(); y++) {
//                Index index = new Index(x, y);
//                ObstacleContainerNode obstacleContainerNode = obstacleContainer.getObstacleContainerNode(index);
//                if (obstacleContainerNode != null) {
//                    if (obstacleContainerNode.getOuterSlopeGroundPiercingLine() != null) {
//                        for (List<Vertex> vertices : obstacleContainerNode.getOuterSlopeGroundPiercingLine()) {
//                            if (vertices.size() < 3) {
//                                System.out.println("x: " + x + " y: " + y + " size: " + vertices.size()); // TODO make test case
//                            }
//                            //
//
//                        }
//                    }
//                }
//            }
//        }

    }
}
