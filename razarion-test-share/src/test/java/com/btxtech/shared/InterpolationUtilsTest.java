package com.btxtech.shared;

import com.btxtech.shared.utils.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Beat
 * 08.12.2015.
 */
public class InterpolationUtilsTest {

    @Test
    public void testGetMaxMin() throws Exception {
        double[] list1 = {1.0, -4.0, 12.0, 0.002, 12.0};
        double[] list2 = {0.001, 0.002, 0.003, 0.004, 0.005};

        Assert.assertEquals(12.0, CollectionUtils.getMax(list1), 0.0001);
        Assert.assertEquals(-4.0, CollectionUtils.getMin(list1), 0.0001);

        Assert.assertEquals(0.005, CollectionUtils.getMax(list2), 0.0001);
        Assert.assertEquals(0.001, CollectionUtils.getMin(list2), 0.0001);
    }
}