package com.btxtech.uiservice;

import com.btxtech.shared.datatypes.Vertex;
import org.junit.Assert;

/**
 * Created by Beat
 * on 11.12.2018.
 */
public interface AssertHelper {
    static void assertVertex(double expectedX,double expectedY,double expectedZ, Vertex expected) {
        Assert.assertEquals("X not equals", expectedX, expected.getX(), 0.0001);
        Assert.assertEquals("Y not equals", expectedY, expected.getY(), 0.0001);
        Assert.assertEquals("Z not equals", expectedZ, expected.getZ(), 0.0001);

    }
}
