package com.btxtech.server;

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
}