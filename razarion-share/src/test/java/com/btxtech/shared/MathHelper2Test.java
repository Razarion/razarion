package com.btxtech.shared;

import com.btxtech.shared.utils.MathHelper2;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * 08.12.2015.
 */
public class MathHelper2Test {

    @Test
    public void testGetMaxMin() throws Exception {
        List<Double> list1 = Arrays.asList(1.0, -4.0, 12.0, 0.002, 12.0);
        List<Double> list2 = Arrays.asList(0.001, 0.002, 0.003, 0.004, 0.005);

        Assert.assertEquals(12.0, MathHelper2.getMax(list1), 0.0001);
        Assert.assertEquals(-4.0, MathHelper2.getMin(list1), 0.0001);

        Assert.assertEquals(0.005, MathHelper2.getMax(list2), 0.0001);
        Assert.assertEquals(0.001, MathHelper2.getMin(list2), 0.0001);
    }
}