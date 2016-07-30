package com.btxtech.server;

import com.btxtech.shared.utils.InterpolationUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * 29.11.2015.
 */
public class InterpolationUtilsTest {

    @Test
    public void testInterpolate1() throws Exception {
        List<Double> references = Arrays.asList(5.0, 4.0, 3.0, 2.0, 1.0, 0.0);

        // Check out of range
        Assert.assertEquals(5.0, InterpolationUtils.interpolate(-1, references), 0.0001);
        Assert.assertEquals(5.0, InterpolationUtils.interpolate(-200, references), 0.0001);
        Assert.assertEquals(0.0, InterpolationUtils.interpolate(6, references), 0.0001);
        Assert.assertEquals(0.0, InterpolationUtils.interpolate(1000, references), 0.0001);

        // Borders
        Assert.assertEquals(5.0, InterpolationUtils.interpolate(0, references), 0.0001);
        Assert.assertEquals(0.0, InterpolationUtils.interpolate(5, references), 0.0001);

        // intermediate point 0-1
        Assert.assertEquals(4.8, InterpolationUtils.interpolate(0.2, references), 0.0001);
        Assert.assertEquals(4.5, InterpolationUtils.interpolate(0.5, references), 0.0001);
        Assert.assertEquals(4.0, InterpolationUtils.interpolate(1.0, references), 0.0001);
        // intermediate point 1-2
        Assert.assertEquals(3.8, InterpolationUtils.interpolate(1.2, references), 0.0001);
        Assert.assertEquals(3.5, InterpolationUtils.interpolate(1.5, references), 0.0001);
        Assert.assertEquals(3.0, InterpolationUtils.interpolate(2.0, references), 0.0001);
        // intermediate point 2-3
        Assert.assertEquals(2.8, InterpolationUtils.interpolate(2.2, references), 0.0001);
        Assert.assertEquals(2.5, InterpolationUtils.interpolate(2.5, references), 0.0001);
        Assert.assertEquals(2.0, InterpolationUtils.interpolate(3.0, references), 0.0001);
        // intermediate point 3-4
        Assert.assertEquals(1.8, InterpolationUtils.interpolate(3.2, references), 0.0001);
        Assert.assertEquals(1.5, InterpolationUtils.interpolate(3.5, references), 0.0001);
        Assert.assertEquals(1.0, InterpolationUtils.interpolate(4.0, references), 0.0001);
        // intermediate point 4-5
        Assert.assertEquals(0.8, InterpolationUtils.interpolate(4.2, references), 0.0001);
        Assert.assertEquals(0.5, InterpolationUtils.interpolate(4.5, references), 0.0001);
        Assert.assertEquals(0.0, InterpolationUtils.interpolate(5.0, references), 0.0001);
    }

    @Test
    public void testInterpolate2() throws Exception {
        List<Double> references = Arrays.asList(1.0, 2.0, -1.0, 0.0);

        Assert.assertEquals(1.0, InterpolationUtils.interpolate(0.0, references), 0.0001);
        Assert.assertEquals(1.2, InterpolationUtils.interpolate(0.2, references), 0.0001);
        Assert.assertEquals(1.8, InterpolationUtils.interpolate(0.8, references), 0.0001);
        Assert.assertEquals(2.0, InterpolationUtils.interpolate(1.0, references), 0.0001);
        Assert.assertEquals(1.0, InterpolationUtils.interpolate(1.3333333, references), 0.0001);
        Assert.assertEquals(0.0, InterpolationUtils.interpolate(1.6666666, references), 0.0001);
        Assert.assertEquals(-1.0, InterpolationUtils.interpolate(2.0, references), 0.0001);
        Assert.assertEquals(-0.5, InterpolationUtils.interpolate(2.5, references), 0.0001);
        Assert.assertEquals(0.0, InterpolationUtils.interpolate(3.0, references), 0.0001);
        Assert.assertEquals(0.0, InterpolationUtils.interpolate(3.1, references), 0.0001);
    }
}