package com.btxtech.scenariongui.scenario;

import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import javafx.scene.paint.Color;

import javax.enterprise.inject.Instance;
import java.lang.reflect.Field;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

/**
 * Created by Beat
 * 19.03.2016.
 */
public class SimpleTerrainScenario extends Scenario {
    private TerrainTile terrainTile1;
    private TerrainTile terrainTile2;

    public SimpleTerrainScenario() {
        double[][] heights = new double[][]{
                {4, 0, 0, 0},
                {0, 1, 0, 0},
                {0, 0, 0, 0},
                {0, -1.6, 0, 0},
                {0, 0, 0, 8},
        };
        double[][] splattings = new double[][]{
                {0.0, 0.0, 0.0},
                {0.0, 0.5, 0.8},
                {0.0, 0.1, 0.0},
                {0.0, 0.0, 0.3},
        };
        terrainTile1 = generateTerrainTile(new Index(0, 0), heights, splattings);
        terrainTile2 = generateTerrainTile(new Index(0, 1), heights, splattings);
    }


    @Override
    public void render(ExtendedGraphicsContext context) {
        context.drawTerrainTile(terrainTile1, 0.2, Color.BLACK, Color.RED, Color.BLUEVIOLET);
        context.drawTerrainTile(terrainTile2, 0.2, Color.RED, Color.BLACK, Color.GREEN);
    }

    private TerrainTile generateTerrainTile(Index terrainTileIndex, double[][] heights, double[][] splattings) {
        // Setup TerrainService
        TerrainService terrainService = new TerrainService();
        Instance mockListener = createNiceMock(Instance.class);
        expect(mockListener.get()).andReturn(new TerrainTile() {
        });
        replay(mockListener);
        injectInstance("terrainTileInstance", terrainService, mockListener);

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
        injectBean("terrainTypeService", terrainService, terrainTypeService);

        return terrainService.generateTerrainTile(terrainTileIndex);
    }

    private void injectInstance(String fieldName, Object service, Instance instanceMock) {
        try {
            Field field = service.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(service, instanceMock);
            field.setAccessible(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void injectBean(String fieldName, Object service, Object bean) {
        try {
            Field field = service.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(service, bean);
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

}
