package com.btxtech;

import com.btxtech.shared.primitives.Vertex;
import org.junit.Assert;

/**
 * Created by Beat
 * 01.11.2015.
 */
public class TestHelper {
    public static void assertVertex(Vertex expected, Vertex actual) {
        String message = "Expected: " + expected + " but was: " + actual;
        Assert.assertEquals(message, expected.getX(), actual.getX(), 0.001);
        Assert.assertEquals(message, expected.getY(), actual.getY(), 0.001);
        Assert.assertEquals(message, expected.getZ(), actual.getZ(), 0.001);
    }

    public static void assertVertex(double expectedX, double expectedY, double expectedZ, Vertex actual) {
        assertVertex(new Vertex(expectedX, expectedY, expectedZ), actual);
    }
}
