package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.SimpleTestEnvironment;
import com.btxtech.shared.TestTerrainSlopeTile;
import com.btxtech.shared.TestTerrainTile;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.PlanetActivationEvent;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleContainer;
import com.btxtech.shared.system.JsInteropObjectFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

/**
 * Created by Beat
 * 03.04.2017.
 */
public class TerrainServiceTestBase {

    protected TerrainTile generateTerrainTileGround(Index terrainTileIndex, double[][] heights, double[][] splattings) {
        // Setup TerrainService
        TerrainService terrainService = new TerrainService();
        injectTerrainTileContextInstance(terrainService);
        // Mock ObstacleContainer
        ObstacleContainer obstacleContainerMock = createNiceMock(ObstacleContainer.class);
        expect(obstacleContainerMock.isSlope(anyObject(Index.class))).andReturn(false);
        expect(obstacleContainerMock.getInsideSlopeHeight(anyObject(Index.class))).andReturn(0.0);
        SimpleTestEnvironment.injectService("obstacleContainer", terrainService, obstacleContainerMock);

        replay(obstacleContainerMock);

        TerrainTypeService terrainTypeService = new TerrainTypeService();
        GameEngineConfig gameEngineConfig = new GameEngineConfig();
        GroundSkeletonConfig groundSkeletonConfig = new GroundSkeletonConfig();
        gameEngineConfig.setGroundSkeletonConfig(groundSkeletonConfig);
        groundSkeletonConfig.setHeights(toColumnRow(heights));
        groundSkeletonConfig.setHeightXCount(heights[0].length);
        groundSkeletonConfig.setHeightYCount(heights.length);
        groundSkeletonConfig.setSplattings(toColumnRow(splattings));
        groundSkeletonConfig.setSplattingXCount(splattings[0].length);
        groundSkeletonConfig.setSplattingYCount(splattings.length);

        terrainTypeService.init(gameEngineConfig);
        SimpleTestEnvironment.injectService("terrainTypeService", terrainService, terrainTypeService);

        return terrainService.generateTerrainTile(terrainTileIndex);
    }

    protected TerrainTile generateTerrainTileSlope(Index terrainTileIndex, double[][] splattings, List<TerrainSlopePosition> terrainSlopePositions) {
        // Setup TerrainService
        TerrainService terrainService = new TerrainService();
        injectTerrainTileContextInstance(terrainService);
        // Mock ObstacleContainer
        ObstacleContainer obstacleContainer = new ObstacleContainer();
        SimpleTestEnvironment.injectService("obstacleContainer", terrainService, obstacleContainer);

        double[][] heights = new double[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 10, 0, 0},
                {0, 0, 0, 0},
        };
        TerrainTypeService terrainTypeService = new TerrainTypeService();
        GameEngineConfig gameEngineConfig = new GameEngineConfig();

        GroundSkeletonConfig groundSkeletonConfig = new GroundSkeletonConfig();
        gameEngineConfig.setGroundSkeletonConfig(groundSkeletonConfig);
        groundSkeletonConfig.setHeights(toColumnRow(heights));
        groundSkeletonConfig.setHeightXCount(heights[0].length);
        groundSkeletonConfig.setHeightYCount(heights.length);
        groundSkeletonConfig.setSplattings(toColumnRow(splattings));
        groundSkeletonConfig.setSplattingXCount(splattings[0].length);
        groundSkeletonConfig.setSplattingYCount(splattings.length);

        List<SlopeSkeletonConfig> slopeSkeletonConfigs = new ArrayList<>();
        SlopeSkeletonConfig slopeSkeletonConfig = new SlopeSkeletonConfig();
        slopeSkeletonConfig.setId(1).setType(SlopeSkeletonConfig.Type.LAND);
        slopeSkeletonConfig.setRows(3).setSegments(1).setWidth(2).setVerticalSpace(2);
        SlopeNode[][] slopeNodes = new SlopeNode[][]{
                {createSlopeNode(0, 10, 0.3),},
                {createSlopeNode(1, 5, 1),},
                {createSlopeNode(2, 0, 0.7),},
        };
        slopeSkeletonConfig.setSlopeNodes(toColumnRow(slopeNodes));
        slopeSkeletonConfigs.add(slopeSkeletonConfig);
        gameEngineConfig.setSlopeSkeletonConfigs(slopeSkeletonConfigs);

        terrainTypeService.init(gameEngineConfig);
        SimpleTestEnvironment.injectService("terrainTypeService", terrainService, terrainTypeService);

        PlanetConfig planetConfig = new PlanetConfig();
        planetConfig.setTerrainSlopePositions(terrainSlopePositions);
        planetConfig.setGroundMeshDimension(new Rectangle(0, 0, 64, 64));
        terrainService.onPlanetActivation(new PlanetActivationEvent(planetConfig));

        return terrainService.generateTerrainTile(terrainTileIndex);
    }

    private SlopeNode createSlopeNode(double x, double z, double slopeFactor) {
        return new SlopeNode().setPosition(new Vertex(x, 0, z)).setSlopeFactor(slopeFactor);
    }

    private void mockJsInteropObjectFactory(Object object) {
        JsInteropObjectFactory mockJsInteropObjectFactory = createNiceMock(JsInteropObjectFactory.class);
        expect(mockJsInteropObjectFactory.generateTerrainTile()).andReturn(new TestTerrainTile());
        expect(mockJsInteropObjectFactory.generateTerrainSlopeTile()).andReturn(new TestTerrainSlopeTile());
        SimpleTestEnvironment.injectJsInteropObjectFactory("jsInteropObjectFactory", object, mockJsInteropObjectFactory);
        replay(mockJsInteropObjectFactory);
    }

    private void injectTerrainTileContextInstance(TerrainService terrainService) {
        SimpleTestEnvironment.injectInstance("terrainTileContextInstance", terrainService, () -> {
            TerrainTileContext terrainTileContext = new TerrainTileContext();
            mockJsInteropObjectFactory(terrainTileContext);
            SimpleTestEnvironment.injectInstance("terrainSlopeTileContextInstance", terrainTileContext, () -> {
                TerrainSlopeTileContext terrainSlopeTileContext = new TerrainSlopeTileContext();
                mockJsInteropObjectFactory(terrainSlopeTileContext);
                return terrainSlopeTileContext;
            });
            return terrainTileContext;
        });
    }

    private double[][] toColumnRow(double[][] rowColumn) {
        int xCount = rowColumn[0].length;
        int yCount = rowColumn.length;
        double[][] columnRow = new double[xCount][yCount];
        for (int x = 0; x < xCount; x++) {
            for (int y = 0; y < yCount; y++) {
                columnRow[x][y] = rowColumn[y][x];
            }
        }
        return columnRow;
    }

    private SlopeNode[][] toColumnRow(SlopeNode[][] rowColumn) {
        int xCount = rowColumn[0].length;
        int yCount = rowColumn.length;
        SlopeNode[][] columnRow = new SlopeNode[xCount][yCount];
        for (int x = 0; x < xCount; x++) {
            for (int y = 0; y < yCount; y++) {
                columnRow[x][y] = rowColumn[y][x];
            }
        }
        return columnRow;
    }

}
