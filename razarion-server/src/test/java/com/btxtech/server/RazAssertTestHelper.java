package com.btxtech.server;

import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import org.junit.Assert;

import java.util.List;

/**
 * Created by Beat
 * 09.05.2017.
 */
public interface RazAssertTestHelper {
    static void assertPlaceConfig(List<DecimalPosition> expectedPolygon, PlaceConfig placeConfig) {
        Assert.assertNull("Position is not null", placeConfig.getPosition());
        Assert.assertNull("Position is not null", placeConfig.getRadius());
        Assert.assertNotNull("Polygon not set", placeConfig.getPolygon2D());
        assertDecimalPositions(expectedPolygon, placeConfig.getPolygon2D().getCorners());
    }

    static void assertDecimalPositions(List<DecimalPosition> expected, List<DecimalPosition> actual) {
        Assert.assertEquals("Size is not same", expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            Assert.assertEquals("Position at ", expected.size(), actual.size());
            Assert.assertTrue("Position at " + i + " is not same. Expected: " + expected.get(i) + " Actual: " + actual.get(i), expected.get(i).equalsDelta(actual.get(i), 0.001));
        }
    }

    static void assertDecimalPosition(DecimalPosition expected, DecimalPosition actual) {
        if (expected == null && actual == null) {
            return;
        } else if (expected != null && actual == null) {
            Assert.fail("Expected is: " + expected + ". Actual is null");
        } else if (expected == null) {
            Assert.fail("Expected is null. Actual: " + actual);
        }
        Assert.assertTrue("Expected: " + expected + " Actual: " + actual, expected.equalsDelta(actual, 0.001));
    }


    static void assertColor(Color expected, Color actual) {
        Assert.assertEquals("R value of color is not the same", expected.getR(), actual.getR(), 0.0001);
        Assert.assertEquals("G value of color is not the same", expected.getG(), actual.getG(), 0.0001);
        Assert.assertEquals("B value of color is not the same", expected.getB(), actual.getB(), 0.0001);
        Assert.assertEquals("A value of color is not the same", expected.getA(), actual.getA(), 0.0001);
    }


}