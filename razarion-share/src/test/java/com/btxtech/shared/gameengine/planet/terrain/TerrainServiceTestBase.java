package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.SimpleTestEnvironment;
import com.btxtech.shared.TestTerrainNode;
import com.btxtech.shared.TestTerrainSlopeTile;
import com.btxtech.shared.TestTerrainSubNode;
import com.btxtech.shared.TestTerrainTile;
import com.btxtech.shared.TestTerrainWaterTile;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.DrivewayConfig;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainSlopeCorner;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.dto.WaterConfig;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.container.nativejs.NativeTerrainShapeAccess;
import com.btxtech.shared.system.JsInteropObjectFactory;
import com.btxtech.shared.utils.MathHelper;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 03.04.2017.
 */
public class TerrainServiceTestBase {
    private TerrainTypeService terrainTypeService;
    private NativeTerrainShapeAccess nativeTerrainShapeAccess;
    private TerrainService terrainService;

    protected void setupTerrainTypeService(double[][] heights, double[][] splattings, List<SlopeSkeletonConfig> slopeSkeletonConfigs) {
        terrainTypeService = new TerrainTypeService();
        StaticGameConfig staticGameConfig = new StaticGameConfig();
        staticGameConfig.setWaterConfig(new WaterConfig().setWaterLevel(-0.7));
        GroundSkeletonConfig groundSkeletonConfig = new GroundSkeletonConfig();
        staticGameConfig.setGroundSkeletonConfig(groundSkeletonConfig);
        groundSkeletonConfig.setHeights(toColumnRow(heights));
        groundSkeletonConfig.setHeightXCount(heights[0].length);
        groundSkeletonConfig.setHeightYCount(heights.length);
        groundSkeletonConfig.setSplattings(toColumnRow(splattings));
        groundSkeletonConfig.setSplattingXCount(splattings[0].length);
        groundSkeletonConfig.setSplattingYCount(splattings.length);
        staticGameConfig.setSlopeSkeletonConfigs(slopeSkeletonConfigs);
        List<DrivewayConfig> drivewayConfigs =  new ArrayList<>();
        drivewayConfigs.add(new DrivewayConfig().setId(1).setAngle(Math.toRadians(45)));
        staticGameConfig.setDrivewayConfigs(drivewayConfigs);
        terrainTypeService.init(staticGameConfig);
    }

    protected void setupNativeTerrainShapeAccess(PlanetConfig planetConfig, List<TerrainSlopePosition> terrainSlopePositions) {
        TerrainShape terrainShape = new TerrainShape(planetConfig, getTerrainTypeService(), terrainSlopePositions, null);
        nativeTerrainShapeAccess = (planetId, loadedCallback, failCallback) -> loadedCallback.accept(terrainShape.toNativeTerrainShape());
    }

    protected void setupTerrainService(double[][] heights, double[][] splattings, List<SlopeSkeletonConfig> slopeSkeletonConfigs, List<TerrainSlopePosition> terrainSlopePositions) {
        terrainService = new TerrainService();

        TerrainTileFactory terrainTileFactory = new TerrainTileFactory();
        injectTerrainTileContextInstance(terrainTileFactory);
        injectTerrainWaterTileContextInstance(terrainTileFactory);

        SimpleTestEnvironment.injectExceptionHandler(terrainTileFactory);
        SimpleTestEnvironment.injectService("terrainTileFactory", terrainService, terrainTileFactory);

        setupTerrainTypeService(heights, splattings, slopeSkeletonConfigs);

        SimpleTestEnvironment.injectService("terrainTypeService", terrainService, terrainTypeService);
        SimpleTestEnvironment.injectService("terrainTypeService", terrainTileFactory, terrainTypeService);

        PlanetConfig planetConfig = new PlanetConfig();
        // TODO planetConfig.setTerrainSlopePositions(terrainSlopePositions);
        planetConfig.setTerrainTileDimension(new Rectangle(0, 0, 4, 4));

        setupNativeTerrainShapeAccess(planetConfig, terrainSlopePositions);

        SimpleTestEnvironment.injectService("nativeTerrainShapeAccess", terrainService, nativeTerrainShapeAccess);
        terrainService.setup(planetConfig, () -> {
        }, Assert::fail);
    }

    protected TerrainTile generateTerrainTile(Index terrainTileIndex) {
        return terrainService.generateTerrainTile(terrainTileIndex);
    }

    private void mockJsInteropObjectFactory(Object object) {
        JsInteropObjectFactory jsInteropObjectFactory = new JsInteropObjectFactory() {
            @Override
            public TerrainTile generateTerrainTile() {
                return new TestTerrainTile();
            }

            @Override
            public TerrainSlopeTile generateTerrainSlopeTile() {
                return new TestTerrainSlopeTile();
            }

            @Override
            public TerrainWaterTile generateTerrainWaterTile() {
                return new TestTerrainWaterTile();
            }

            @Override
            public TerrainNode generateTerrainNode() {
                return new TestTerrainNode();
            }

            @Override
            public TerrainSubNode generateTerrainSubNode() {
                return new TestTerrainSubNode();
            }
        };
        SimpleTestEnvironment.injectJsInteropObjectFactory("jsInteropObjectFactory", object, jsInteropObjectFactory);
    }

    private void injectTerrainTileContextInstance(TerrainTileFactory terrainTileFactory) {
        mockJsInteropObjectFactory(terrainTileFactory);
        SimpleTestEnvironment.injectInstance("terrainTileContextInstance", terrainTileFactory, () -> {
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

    private void injectTerrainWaterTileContextInstance(TerrainTileFactory terrainTileFactory) {
        SimpleTestEnvironment.injectInstance("terrainWaterTileContextInstance", terrainTileFactory, () -> {
            TerrainWaterTileContext terrainWaterTileContextInstance = new TerrainWaterTileContext();
            mockJsInteropObjectFactory(terrainWaterTileContextInstance);
            return terrainWaterTileContextInstance;
        });
    }

    protected SlopeNode createSlopeNode(double x, double z, double slopeFactor) {
        return new SlopeNode().setPosition(new Vertex(x, 0, z)).setSlopeFactor(slopeFactor);
    }

    protected TerrainSlopeCorner createTerrainSlopeCorner(double x, double y, Integer slopeDrivewayId) {
        return new TerrainSlopeCorner().setPosition(new DecimalPosition(x, y)).setSlopeDrivewayId(slopeDrivewayId);
    }

    protected double[][] toColumnRow(double[][] rowColumn) {
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

    protected SlopeNode[][] toColumnRow(SlopeNode[][] rowColumn) {
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

    protected TerrainTypeService getTerrainTypeService() {
        return terrainTypeService;
    }

    protected TerrainService getTerrainService() {
        return terrainService;
    }

    protected TerrainShape getTerrainShape() {
        return (TerrainShape) SimpleTestEnvironment.readField("terrainShape", terrainService);
    }
}
