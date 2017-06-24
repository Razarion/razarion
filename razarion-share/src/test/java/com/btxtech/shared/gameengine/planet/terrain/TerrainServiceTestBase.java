package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.SimpleTestEnvironment;
import com.btxtech.shared.TestTerrainSlopeTile;
import com.btxtech.shared.TestTerrainTile;
import com.btxtech.shared.TestTerrainWaterTile;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainSlopeCorner;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.dto.WaterConfig;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.system.JsInteropObjectFactory;

import java.util.List;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

/**
 * Created by Beat
 * 03.04.2017.
 */
public class TerrainServiceTestBase {
    private TerrainService terrainService;

    protected void setupTerrainService(double[][] heights, double[][] splattings, List<SlopeSkeletonConfig> slopeSkeletonConfigs, List<TerrainSlopePosition> terrainSlopePositions) {
        terrainService = new TerrainService();

        TerrainTileFactory terrainTileFactory = new TerrainTileFactory();
        injectTerrainTileContextInstance(terrainTileFactory);
        injectTerrainWaterTileContextInstance(terrainTileFactory);
        SimpleTestEnvironment.injectExceptionHandler(terrainTileFactory);
        SimpleTestEnvironment.injectService("terrainTileFactory", terrainService, terrainTileFactory);

        TerrainTypeService terrainTypeService = new TerrainTypeService();
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

        terrainTypeService.init(staticGameConfig);
        SimpleTestEnvironment.injectService("terrainTypeService", terrainService, terrainTypeService);
        SimpleTestEnvironment.injectService("terrainTypeService", terrainTileFactory, terrainTypeService);

        PlanetConfig planetConfig = new PlanetConfig();
        planetConfig.setTerrainSlopePositions(terrainSlopePositions);
        planetConfig.setTerrainTileDimension(new Rectangle(0, 0, 4, 4));
        terrainService.setup(planetConfig);
    }

    protected TerrainTile generateTerrainTile(Index terrainTileIndex) {
        return terrainService.generateTerrainTile(terrainTileIndex);
    }

    private void mockJsInteropObjectFactory(Object object) {
        JsInteropObjectFactory mockJsInteropObjectFactory = createNiceMock(JsInteropObjectFactory.class);
        expect(mockJsInteropObjectFactory.generateTerrainTile()).andReturn(new TestTerrainTile());
        expect(mockJsInteropObjectFactory.generateTerrainSlopeTile()).andReturn(new TestTerrainSlopeTile());
        expect(mockJsInteropObjectFactory.generateTerrainWaterTile()).andReturn(new TestTerrainWaterTile());
        SimpleTestEnvironment.injectJsInteropObjectFactory("jsInteropObjectFactory", object, mockJsInteropObjectFactory);
        replay(mockJsInteropObjectFactory);
    }

    private void injectTerrainTileContextInstance(TerrainTileFactory terrainTileFactory) {
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
        return new TerrainSlopeCorner().setPosition(new DecimalPosition(x,y)).setSlopeDrivewayId(slopeDrivewayId);
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

}
