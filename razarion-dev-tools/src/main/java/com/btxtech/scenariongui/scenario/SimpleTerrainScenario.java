package com.btxtech.scenariongui.scenario;

import com.btxtech.ExtendedGraphicsContext;
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
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSlopeTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainSlopeTileContext;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTileContext;
import com.btxtech.shared.system.JsInteropObjectFactory;
import com.btxtech.webglemulator.razarion.DevToolTerrainSlopeTile;
import com.btxtech.webglemulator.razarion.DevToolTerrainTile;
import javafx.scene.paint.Color;

import javax.enterprise.inject.Instance;
import javax.enterprise.util.TypeLiteral;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

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
        TerrainSlopeTile terrainSlopeTile = terrainTile1.getTerrainSlopeTile()[0];

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
        TerrainSlopeTile terrainSlopeTile = terrainTile1.getTerrainSlopeTile()[0];
        for (int i = 0; i < terrainSlopeTile.getSlopeVertexCount(); i++) {
            System.out.println("Assert.assertEquals(" + String.format(Locale.US, "%.4f", terrainSlopeTile.getGroundSplattings()[i]) + ", terrainSlopeTile.getGroundSplattings()[" + i + "], 0.0001);");

        }
    }

    protected TerrainTile generateTerrainTileSlope(Index terrainTileIndex, double[][] splattings, List<TerrainSlopePosition> terrainSlopePositions) {
        // Setup TerrainService
        TerrainService terrainService = new TerrainService();
        injectTerrainTileContextInstance(terrainService);
        // Mock ObstacleContainer
        ObstacleContainer obstacleContainer = new ObstacleContainer();
        injectService("obstacleContainer", terrainService, obstacleContainer);

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
        SlopeSkeletonConfig slopeSkeletonConfigLand = new SlopeSkeletonConfig();
        slopeSkeletonConfigLand.setId(1).setType(SlopeSkeletonConfig.Type.LAND);
        slopeSkeletonConfigLand.setRows(4).setSegments(1).setVerticalSpace(20).setWidth(20).setHeight(4);
        SlopeNode[][] slopeNodes = new SlopeNode[][]{
                {createSlopeNode(0, 0, 0.0),},
                {createSlopeNode(5, 1, 0.2),},
                {createSlopeNode(10, 2, 0.4),},
                {createSlopeNode(15, 3, 0.6),},
                {createSlopeNode(20, 4, 0.8),},
        };
        slopeSkeletonConfigLand.setSlopeNodes(toColumnRow(slopeNodes));
        slopeSkeletonConfigs.add(slopeSkeletonConfigLand);
        SlopeSkeletonConfig slopeSkeletonConfigWater = new SlopeSkeletonConfig();
        slopeSkeletonConfigWater.setId(2).setType(SlopeSkeletonConfig.Type.WATER);
        slopeSkeletonConfigWater.setRows(4).setSegments(1).setWidth(4).setVerticalSpace(6).setHeight(1.5);
        slopeNodes = new SlopeNode[][]{
                {createSlopeNode(4, 0.5, 0.3),},
                {createSlopeNode(2, 1, 1),},
                {createSlopeNode(1, -0.5, 1.0),},
                {createSlopeNode(0, -1, 1.0),},
        };
        slopeSkeletonConfigWater.setSlopeNodes(toColumnRow(slopeNodes));
        slopeSkeletonConfigs.add(slopeSkeletonConfigWater);

        gameEngineConfig.setSlopeSkeletonConfigs(slopeSkeletonConfigs);

        terrainTypeService.init(gameEngineConfig);
        injectService("terrainTypeService", terrainService, terrainTypeService);

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
        expect(mockJsInteropObjectFactory.generateTerrainTile()).andReturn(new DevToolTerrainTile());
        expect(mockJsInteropObjectFactory.generateTerrainSlopeTile()).andReturn(new DevToolTerrainSlopeTile());
        injectJsInteropObjectFactory("jsInteropObjectFactory", object, mockJsInteropObjectFactory);
        replay(mockJsInteropObjectFactory);
    }

    private void injectTerrainTileContextInstance(TerrainService terrainService) {
        injectInstance("terrainTileContextInstance", terrainService, () -> {
            TerrainTileContext terrainTileContext = new TerrainTileContext();
            mockJsInteropObjectFactory(terrainTileContext);
            injectInstance("terrainSlopeTileContextInstance", terrainTileContext, () -> {
                TerrainSlopeTileContext terrainSlopeTileContext = new TerrainSlopeTileContext();
                mockJsInteropObjectFactory(terrainSlopeTileContext);
                return terrainSlopeTileContext;
            });
            return terrainTileContext;
        });
    }

    private static void injectInstance(String fieldName, Object object, Supplier getSupplier) {
        Instance instance = new Instance() {
            @Override
            public Instance select(Annotation... qualifiers) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Instance select(Class subtype, Annotation... qualifiers) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Instance select(TypeLiteral subtype, Annotation... qualifiers) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isUnsatisfied() {
                return false;
            }

            @Override
            public boolean isAmbiguous() {
                return false;
            }

            @Override
            public void destroy(Object instance) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Iterator iterator() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Object get() {
                return getSupplier.get();
            }
        };

        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, instance);
            field.setAccessible(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void injectJsInteropObjectFactory(String fieldName, Object service, JsInteropObjectFactory jsInteropObjectFactory) {
        try {
            Field field = service.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(service, jsInteropObjectFactory);
            field.setAccessible(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void injectService(String fieldName, Object service, Object serviceToInject) {
        try {
            Field field = service.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(service, serviceToInject);
            field.setAccessible(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
