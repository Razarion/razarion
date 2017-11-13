package com.btxtech.shared.datatypes;

import com.btxtech.shared.gameengine.planet.GameTestHelper;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by Beat
 * on 11.11.2017.
 */
public class Polygon2DRasterizerTest {
    @Test
    public void test1() throws Exception {
        Polygon2DRasterizer polygon2DRasterizer = Polygon2DRasterizer.create(new Polygon2D(Arrays.asList(new DecimalPosition(23.375, 38.250), new DecimalPosition(88.625, 41.250), new DecimalPosition(28.125, -21.250))), TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH);
    }

    @Test
    public void test2() throws Exception {
        Polygon2DRasterizer polygon2DRasterizer = Polygon2DRasterizer.create(new Polygon2D(Arrays.asList(new DecimalPosition(129.125, -41.000), new DecimalPosition(129.125, 46.750), new DecimalPosition(28.875, 46.250), new DecimalPosition(28.875, -45.000))), TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH);
    }

    @Test
    public void test3() throws Exception {
        Polygon2DRasterizer polygon2DRasterizer = Polygon2DRasterizer.create(new Polygon2D(Arrays.asList(new DecimalPosition(95.875, 78.000), new DecimalPosition(-13.125, 81.750), new DecimalPosition(-17.625, 29.750), new DecimalPosition(44.125, 23.500), new DecimalPosition(45.125, -21.250), new DecimalPosition(6.375, -20.250), new DecimalPosition(-4.125, -70.000), new DecimalPosition(114.375, -67.500))), TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH);
    }

    @Test
    public void test4() throws Exception {
        Polygon2DRasterizer polygon2DRasterizer = Polygon2DRasterizer.create(new Polygon2D(Arrays.asList(new DecimalPosition(29.375, 72.250), new DecimalPosition(32.875, -22.750), new DecimalPosition(93.625, -21.750), new DecimalPosition(90.875, 69.250))), TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH);
    }

    @Test
    public void testBig() throws Exception {
        Polygon2DRasterizer polygon2DRasterizer = Polygon2DRasterizer.create(new Polygon2D(Arrays.asList(new DecimalPosition(100, 4000), new DecimalPosition(4000, 100), new DecimalPosition(4800, 800), new DecimalPosition(800, 4800))), TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH);
        System.out.println("PiercedTiles: " + polygon2DRasterizer.getPiercedTiles().size());
        System.out.println("InnerTiles: " + polygon2DRasterizer.getInnerTiles().size());
    }

}