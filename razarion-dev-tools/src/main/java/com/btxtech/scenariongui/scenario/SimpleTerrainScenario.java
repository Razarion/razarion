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
import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.PlanetActivationEvent;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleContainer;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSlopeTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.easymock.EasyMock.createNiceMock;

/**
 * Created by Beat
 * 19.03.2016.
 */
public class SimpleTerrainScenario extends Scenario {
    private TerrainTile terrainTile1;
    // private TerrainTile terrainTile2;
    // private TerrainTile terrainTile3;
    // private TerrainTile terrainTile4;

    @Override
    public void init() {
        double[][] splattings = new double[][]{
                {0.7, 0.8, 0.9},
                {0.4, 0.5, 0.6},
                {0.1, 0.2, 0.3},
        };
        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        TerrainSlopePosition terrainSlopePositionLand = new TerrainSlopePosition();
        terrainSlopePositionLand.setId(1);
        terrainSlopePositionLand.setSlopeConfigEntity(1);
        terrainSlopePositionLand.setPolygon(Arrays.asList(new DecimalPosition(50, 40), new DecimalPosition(100, 40), new DecimalPosition(100, 110), new DecimalPosition(50, 110)));
        terrainSlopePositions.add(terrainSlopePositionLand);

        terrainTile1 = generateTerrainTileSlope(new Index(0, 0), splattings, terrainSlopePositions);
        TerrainSlopeTile terrainSlopeTile = terrainTile1.getTerrainSlopeTiles()[0];

    }

    @Override
    public void render(ExtendedGraphicsContext context) {
        context.drawTerrainTile(terrainTile1, 0.05, Color.BLACK, Color.RED, Color.BLUEVIOLET);
        // context.drawTerrainTile(terrainTile2, 0.2, Color.RED, Color.BLACK, Color.GREEN);
        // context.drawTerrainTile(terrainTile3, 0.05, Color.BLACK, Color.RED, Color.BLUEVIOLET);
        // context.drawTerrainTile(terrainTile4, 0.2, Color.RED, Color.BLACK, Color.GREEN);
    }

    @Override
    public void onGenerate() {
        System.out.println("------------------------------------------");
        TerrainSlopeTile terrainSlopeTile = terrainTile1.getTerrainSlopeTiles()[0];
        for (int i = 0; i < terrainSlopeTile.getSlopeVertexCount(); i++) {
            System.out.println("Assert.assertEquals(" + String.format(Locale.US, "%.4f", terrainSlopeTile.getGroundSplattings()[i]) + ", terrainSlopeTile.getGroundSplattings()[" + i + "], 0.0001);");

        }
    }

    protected TerrainTile generateTerrainTileSlope(Index terrainTileIndex, double[][] splattings, List<TerrainSlopePosition> terrainSlopePositions) {
        // Setup TerrainService
        TerrainService terrainService = new TerrainService();
        FrameworkHelper.injectTerrainTileContextInstance(terrainService);
        // Mock ObstacleContainer
        ObstacleContainer obstacleContainer = new ObstacleContainer();
        FrameworkHelper.injectService("obstacleContainer", terrainService, obstacleContainer);

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
        SlopeSkeletonConfig slopeSkeletonConfigWater = new SlopeSkeletonConfig();
        slopeSkeletonConfigWater.setId(2).setType(SlopeSkeletonConfig.Type.WATER);
        slopeSkeletonConfigWater.setRows(4).setSegments(1).setWidth(4).setVerticalSpace(6).setHeight(1.5);
        slopeNodes = new SlopeNode[][]{
                {FrameworkHelper.createSlopeNode(4, 0.5, 0.3),},
                {FrameworkHelper.createSlopeNode(2, 1, 1),},
                {FrameworkHelper.createSlopeNode(1, -0.5, 1.0),},
                {FrameworkHelper.createSlopeNode(0, -1, 1.0),},
        };
        slopeSkeletonConfigWater.setSlopeNodes(FrameworkHelper.toColumnRow(slopeNodes));
        slopeSkeletonConfigs.add(slopeSkeletonConfigWater);

        gameEngineConfig.setSlopeSkeletonConfigs(slopeSkeletonConfigs);

        terrainTypeService.init(gameEngineConfig);
        FrameworkHelper.injectService("terrainTypeService", terrainService, terrainTypeService);

        PlanetConfig planetConfig = new PlanetConfig();
        planetConfig.setTerrainSlopePositions(terrainSlopePositions);
        planetConfig.setGroundMeshDimension(new Rectangle(0, 0, 64, 64));
        terrainService.onPlanetActivation(new PlanetActivationEvent(planetConfig));

        return terrainService.generateTerrainTile(terrainTileIndex);
    }
}
