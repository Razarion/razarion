package com.btxtech.scenariongui.scenario;

import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.dto.DrivewayConfig;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainSlopeCorner;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.dto.WaterConfig;
import com.btxtech.shared.gameengine.StaticGameInitEvent;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShape;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainShapeTile;
import com.btxtech.webglemulator.razarion.DevToolNativeTerrainShapeAccess;
import javafx.scene.paint.Color;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * 22.01.2017.
 */
public abstract class AbstractTerrainScenario extends Scenario {
    private static final DecimalPosition FROM = new DecimalPosition(0, 0);
    private static final double LENGTH = 200;
    private WeldContainer weldContainer;

//    @Override
//    public void init() {
//        JsonProviderEmulator jsonProviderEmulator = new JsonProviderEmulator();
//        StaticGameConfig staticGameConfig = jsonProviderEmulator.readFromFile(false).getStaticGameConfig();
//        // StaticGameConfig staticGameConfig = jsonProviderEmulator.readGameEngineConfigFromFile("C:\\dev\\projects\\razarion\\code\\tmp\\TmpGameUiControlConfig.json");
//        Weld weld = new Weld();
//        weldContainer = weld.initialize();
//        TerrainTypeService terrainTypeService = weldContainer.instance().select(TerrainTypeService.class).get();
//        terrainTypeService.onGameEngineInit(new StaticGameInitEvent(staticGameConfig));
//        TerrainService terrainService = weldContainer.instance().select(TerrainService.class).get();
//        // TODO terrainService.setup(staticGameConfig.getPlanetConfig());
//    }


    @Override
    public void init() {
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

        List<SlopeSkeletonConfig> slopeSkeletonConfigs = new ArrayList<>();
        SlopeSkeletonConfig slopeSkeletonConfigLand = new SlopeSkeletonConfig();
        slopeSkeletonConfigLand.setId(1).setType(SlopeSkeletonConfig.Type.LAND);
        slopeSkeletonConfigLand.setRows(4).setSegments(1).setWidth(7).setVerticalSpace(5).setHeight(20);
        SlopeNode[][] slopeNodes = new SlopeNode[][]{
                {FrameworkHelper.createSlopeNode(0, 0, 0.3),},
                {FrameworkHelper.createSlopeNode(2, 5, 1),},
                {FrameworkHelper.createSlopeNode(4, 10, 0.7),},
                {FrameworkHelper.createSlopeNode(7, 20, 0.7),},
        };
        slopeSkeletonConfigLand.setSlopeNodes(toColumnRow(slopeNodes));
        slopeSkeletonConfigs.add(slopeSkeletonConfigLand);

        staticGameConfig.setSlopeSkeletonConfigs(slopeSkeletonConfigs);
        List<DrivewayConfig> drivewayConfigs = new ArrayList<>();
        drivewayConfigs.add(new DrivewayConfig().setId(1).setAngle(Math.toRadians(45)));
        staticGameConfig.setDrivewayConfigs(drivewayConfigs);


        Weld weld = new Weld();
        weldContainer = weld.initialize();
        getTerrainTypeService().init(staticGameConfig);

        getTerrainTypeService().onGameEngineInit(new StaticGameInitEvent(staticGameConfig));
        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        TerrainSlopePosition terrainSlopePosition = new TerrainSlopePosition();
        terrainSlopePosition.setId(1);
        terrainSlopePosition.setSlopeConfigId(1);
        terrainSlopePosition.setPolygon(Arrays.asList(createTerrainSlopeCorner(50, 40, null), createTerrainSlopeCorner(100, 40, null), createTerrainSlopeCorner(100, 110, null), createTerrainSlopeCorner(50, 110, null)));
        terrainSlopePositions.add(terrainSlopePosition);
        DevToolNativeTerrainShapeAccess devToolNativeTerrainShapeAccess = getBean(DevToolNativeTerrainShapeAccess.class);
        devToolNativeTerrainShapeAccess.setTerrainSlopePositions(terrainSlopePositions);


        PlanetConfig planetConfig = new PlanetConfig();
        planetConfig.setTerrainTileDimension(new Rectangle(-7, -7, 14, 14)).setHouseSpace(1000).setStartRazarion(100);
        devToolNativeTerrainShapeAccess.setPlanetConfig(planetConfig);

        getTerrainService().setup(planetConfig, () -> {
        }, (s) -> {
            throw new RuntimeException(s);
        });
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

    protected TerrainSlopeCorner createTerrainSlopeCorner(double x, double y, Integer slopeDrivewayId) {
        return new TerrainSlopeCorner().setPosition(new DecimalPosition(x, y)).setSlopeDrivewayId(slopeDrivewayId);
    }

    protected TerrainService getTerrainService() {
        return getBean(TerrainService.class);
    }

    protected TerrainTypeService getTerrainTypeService() {
        return getBean(TerrainTypeService.class);
    }

    protected <T> T getBean(Class<T> theClass) {
        return weldContainer.instance().select(theClass).get();
    }

    protected void drawObstacle(ExtendedGraphicsContext extendedGraphicsContext) {
        TerrainShape terrainShape;
        try {
            Field field = TerrainService.class.getDeclaredField("terrainShape");
            field.setAccessible(true);
            terrainShape = (TerrainShape) field.get(getTerrainService());
            field.setAccessible(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        for (int x = terrainShape.getTileOffset().getX(); x < terrainShape.getTileOffset().getX() + terrainShape.getTileYCount(); x++) {
            for (int y = terrainShape.getTileOffset().getY(); y < terrainShape.getTileOffset().getY() + terrainShape.getTileYCount(); y++) {
                Index index = new Index(x, y);
                TerrainShapeTile terrainShapeTile = terrainShape.getTerrainShapeTile(index);
                if (terrainShapeTile != null) {
                    terrainShapeTile.iterateOverTerrainNodes((nodeRelativeIndex, terrainShapeNode, iterationControl) -> {
                        if (terrainShapeNode != null && terrainShapeNode.getObstacles() != null) {
                            terrainShapeNode.getObstacles().forEach(obstacle -> extendedGraphicsContext.drawObstacle(obstacle, Color.BLACK, Color.BLACK));
                        }
                    });
                }
            }
        }
    }

    protected void renderFree(ExtendedGraphicsContext egc) {
        TerrainService terrainService = getTerrainService();
        for (double x = FROM.getX(); x < FROM.getX() + LENGTH; x++) {
            for (double y = FROM.getY(); y < FROM.getY() + LENGTH; y++) {
                DecimalPosition samplePosition = new DecimalPosition(x + 0.5, y + 0.5);
                // double z = terrainService.getSurfaceAccess().getInterpolatedZ(samplePosition);
                // boolean free = terrainService.getPathingAccess().isTerrainFree(samplePosition);
                // double v = InterpolationUtils.interpolate(0.0, 1.0, min, max, z);
                // egc.getGc().setFill(new Color(v, v, v, 1));
                // egc.getGc().fillRect(x, y, 1, 1);
//                if (free) {
//                    egc.getGc().setFill(Color.GREEN);
//                } else {
//                    egc.getGc().setFill(Color.RED);
//                }
                egc.getGc().fillRect(x, y, 0.6, 0.6);
            }
        }
    }

}
