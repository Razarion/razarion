package com.btxtech.shared.primitives;

import com.btxtech.game.jsre.client.common.DecimalPosition;
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
        Polygon2d polygon2d = new Polygon2d(Arrays.asList(new DecimalPosition(200, 200), new DecimalPosition(800, 200), new DecimalPosition(800, 600)));
        Assert.assertTrue(polygon2d.isInside(new DecimalPosition(620, 300)));
        Assert.assertTrue(polygon2d.isInside(new DecimalPosition(380, 260)));
        Assert.assertTrue(polygon2d.isInside(new DecimalPosition(700, 256)));
        Assert.assertTrue(polygon2d.isInside(new DecimalPosition(316, 260)));
        Assert.assertTrue(polygon2d.isInside(new DecimalPosition(766, 516)));
    }

    @Test
    public void testIsInside2() throws Exception {
        Polygon2d polygon2d = new Polygon2d(Arrays.asList(new DecimalPosition(400, 400), new DecimalPosition(600, 400), new DecimalPosition(600, 600)));
        Assert.assertFalse(polygon2d.isInside(new DecimalPosition(384, 384)));
    }

    @Test
    public void testIsInside3() throws Exception {
        Polygon2d polygon2d = new Polygon2d(Arrays.asList(new DecimalPosition(200, 200), new DecimalPosition(800, 200), new DecimalPosition(800, 600)));
        Assert.assertFalse(polygon2d.isInside(new DecimalPosition(128, 128)));
        Assert.assertFalse(polygon2d.isInside(new DecimalPosition(192, 128)));
    }

    @Test
    public void testIsInside4() throws Exception {
        Polygon2d polygon2d = new Polygon2d(Arrays.asList(new DecimalPosition(199.99999999999986, 199.99999999999997), new DecimalPosition(800, 200), new DecimalPosition(800, 600)));
        Assert.assertFalse(polygon2d.isInside(new DecimalPosition(128, 128)));
        Assert.assertFalse(polygon2d.isInside(new DecimalPosition(192, 128)));
        Assert.assertFalse(polygon2d.isInside(new DecimalPosition(384, 64)));
    }

}