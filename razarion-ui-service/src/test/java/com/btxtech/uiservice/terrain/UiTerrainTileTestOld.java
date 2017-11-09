package com.btxtech.uiservice.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.uiservice.UiTestHelper;
import com.btxtech.uiservice.gui.UiTestGuiDisplay;
import com.btxtech.uiservice.renderer.ModelRenderer;
import com.btxtech.uiservice.renderer.task.ground.GroundRenderTask;
import com.btxtech.uiservice.terrain.helpers.TestTerrainNode;
import com.btxtech.uiservice.terrain.helpers.TestTerrainSubNode;
import com.btxtech.uiservice.terrain.helpers.TestToolTerrainTile;
import com.btxtech.uiservice.terrain.helpers.TestUiTerrainTileRenderer;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import java.util.function.Consumer;

/**
 * Created by Beat
 * on 03.07.2017.
 */
@Deprecated // Use WeldUiBaseTest
public class UiTerrainTileTestOld {

    protected UiTerrainTile setup(TerrainTile terrainTile) {
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
        GroundSkeletonConfig groundSkeletonConfig = new GroundSkeletonConfig();
        groundSkeletonConfig.setHeights(toColumnRow(heights));
        groundSkeletonConfig.setHeightXCount(heights[0].length);
        groundSkeletonConfig.setHeightYCount(heights.length);
        groundSkeletonConfig.setSplattings(toColumnRow(splattings));
        groundSkeletonConfig.setSplattingXCount(splattings[0].length);
        groundSkeletonConfig.setSplattingYCount(splattings.length);


        GroundRenderTask groundRenderTask = EasyMock.createNiceMock(GroundRenderTask.class);
        ModelRenderer modelRenderer = EasyMock.createNiceMock(ModelRenderer.class);
        EasyMock.expect(groundRenderTask.createModelRenderer(EasyMock.anyObject())).andReturn(modelRenderer);

        TerrainUiService terrainUiService = EasyMock.createNiceMock(TerrainUiService.class);
        Capture<Consumer<TerrainTile>> cap = EasyMock.newCapture();
        terrainUiService.requestTerrainTile(EasyMock.eq(new Index(0, 0)), EasyMock.capture(cap));
        EasyMock.replay(terrainUiService, groundRenderTask, modelRenderer);


        UiTerrainTile uiTerrainTile = new UiTerrainTile();
        UiTestHelper.injectService("terrainUiService", uiTerrainTile, terrainUiService);
        UiTestHelper.injectService("groundRenderTask", uiTerrainTile, groundRenderTask);
        uiTerrainTile.init(new Index(0, 0), groundSkeletonConfig);
        Consumer<TerrainTile> terrainTileConsumer = cap.getValue();
        terrainTileConsumer.accept(terrainTile);

        return uiTerrainTile;
    }

    @Test
    public void testSimpleLand() {
        UiTerrainTile uiTerrainTile = setup(new TestToolTerrainTile());
        for (double x = 0.5; x < TerrainUtil.TERRAIN_TILE_ABSOLUTE_LENGTH; x++) {
            for (double y = 0.5; y < TerrainUtil.TERRAIN_TILE_ABSOLUTE_LENGTH; y++) {
                // TODO Assert.assertTrue(x + ":" + y + " is not free", uiTerrainTile.isTerrainFree(new DecimalPosition(x, y)));
            }
        }
    }

    @Test
    public void testNodes() {
        TestToolTerrainTile testToolTerrainTile = new TestToolTerrainTile();
        testToolTerrainTile.initTerrainNodeField(TerrainUtil.TERRAIN_TILE_NODES_COUNT);
        // Node
        TestTerrainNode testTerrainNode = new TestTerrainNode();
        testToolTerrainTile.insertTerrainNode(0, 0, testTerrainNode);
        // Sub node depth = 1
        testTerrainNode = new TestTerrainNode();
        testToolTerrainTile.insertTerrainNode(3, 4, testTerrainNode);
        testTerrainNode.initTerrainSubNodeField(2);
        TestTerrainSubNode terrainSubNode1 = new TestTerrainSubNode();
        // terrainSubNode1.setLand(true);
        testTerrainNode.insertTerrainSubNode(0, 0, terrainSubNode1);
        // Sub node depth = 2
        testTerrainNode = new TestTerrainNode();
        testToolTerrainTile.insertTerrainNode(5, 6, testTerrainNode);
        testTerrainNode.initTerrainSubNodeField(2);
        terrainSubNode1 = new TestTerrainSubNode();
        testTerrainNode.insertTerrainSubNode(0, 0, terrainSubNode1);
        terrainSubNode1.initTerrainSubNodeField(2);
        TestTerrainSubNode terrainSubNode2 = new TestTerrainSubNode();
        // terrainSubNode2.setLand(true);
        terrainSubNode1.insertTerrainSubNode(0, 1, terrainSubNode2);
        // Sub node depth = 3
        testTerrainNode = new TestTerrainNode();
        testToolTerrainTile.insertTerrainNode(2, 6, testTerrainNode);
        testTerrainNode.initTerrainSubNodeField(2);
        terrainSubNode1 = new TestTerrainSubNode();
        testTerrainNode.insertTerrainSubNode(0, 1, terrainSubNode1);
        terrainSubNode1.initTerrainSubNodeField(2);
        terrainSubNode2 = new TestTerrainSubNode();
        terrainSubNode1.insertTerrainSubNode(1, 1, terrainSubNode2);
        terrainSubNode2.initTerrainSubNodeField(2);
        TestTerrainSubNode terrainSubNode3 = new TestTerrainSubNode();
        // terrainSubNode3.setLand(true);
        terrainSubNode2.insertTerrainSubNode(1, 0, terrainSubNode3);

        UiTerrainTile uiTerrainTile = setup(testToolTerrainTile);
        UiTestGuiDisplay.show(new TestUiTerrainTileRenderer(uiTerrainTile));
        Assert.fail("TODO assert");
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

}