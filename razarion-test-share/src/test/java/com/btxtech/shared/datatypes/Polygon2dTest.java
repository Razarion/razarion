package com.btxtech.shared.datatypes;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by Beat
 * 11.03.2016.
 */
public class Polygon2dTest {

    @Test
    public void testIsInside1() throws Exception {
        Polygon2D polygon2D = new Polygon2D(Arrays.asList(new DecimalPosition(200, 200), new DecimalPosition(800, 200), new DecimalPosition(800, 600)));
        Assert.assertTrue(polygon2D.isInside(new DecimalPosition(620, 300)));
        Assert.assertTrue(polygon2D.isInside(new DecimalPosition(380, 260)));
        Assert.assertTrue(polygon2D.isInside(new DecimalPosition(700, 256)));
        Assert.assertTrue(polygon2D.isInside(new DecimalPosition(316, 260)));
        Assert.assertTrue(polygon2D.isInside(new DecimalPosition(766, 516)));
    }

    @Test
    public void testIsInside2() throws Exception {
        Polygon2D polygon2D = new Polygon2D(Arrays.asList(new DecimalPosition(400, 400), new DecimalPosition(600, 400), new DecimalPosition(600, 600)));
        Assert.assertFalse(polygon2D.isInside(new DecimalPosition(384, 384)));
    }

    @Test
    public void testIsInside3() throws Exception {
        Polygon2D polygon2D = new Polygon2D(Arrays.asList(new DecimalPosition(200, 200), new DecimalPosition(800, 200), new DecimalPosition(800, 600)));
        Assert.assertFalse(polygon2D.isInside(new DecimalPosition(128, 128)));
        Assert.assertFalse(polygon2D.isInside(new DecimalPosition(192, 128)));
    }

    @Test
    public void testIsInside4() throws Exception {
        Polygon2D polygon2D = new Polygon2D(Arrays.asList(new DecimalPosition(199.99999999999986, 199.99999999999997), new DecimalPosition(800, 200), new DecimalPosition(800, 600)));
        Assert.assertFalse(polygon2D.isInside(new DecimalPosition(128, 128)));
        Assert.assertFalse(polygon2D.isInside(new DecimalPosition(192, 128)));
        Assert.assertFalse(polygon2D.isInside(new DecimalPosition(384, 64)));
    }

    @Test
    public void combine() {
        Polygon2D poly1 = new Polygon2D(Arrays.asList(new DecimalPosition(580, 500), new DecimalPosition(1000, 500), new DecimalPosition(1000, 1120)));
        Polygon2D poly2 = new Polygon2D(Arrays.asList(new DecimalPosition(700.2491159541822, 652.9908027857709), new DecimalPosition(675.2491159541822, 696.2920729749928), new DecimalPosition(625.2491159541822, 696.2920729749928), new DecimalPosition(600.2491159541822, 652.9908027857709), new DecimalPosition(625.2491159541822, 609.689532596549), new DecimalPosition(675.2491159541822, 609.689532596549)));
        Polygon2D polyResult = poly1.combine(poly2);
    }

}