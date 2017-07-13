package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.DecimalPosition;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Beat
 * on 13.07.2017.
 */
public class NodeHandlerHelper {
    int actualCount;
    private Collection<DecimalPosition> expectedDecimalPositions = new ArrayList<>();

    public void increaseActualCount() {
        actualCount++;
    }

    public void assertCount(int expected) {
        Assert.assertEquals(expected, actualCount);
    }

    public NodeHandlerHelper addExpectedDecimalPosition(double x, double y) {
        expectedDecimalPositions.add(new DecimalPosition(x, y));
        return this;
    }

    public void handleExpectedPosition(DecimalPosition actual) {
        if (!expectedDecimalPositions.remove(actual)) {
            Assert.fail("Position not expected: " + actual);
        }
    }

    public void assertExpectedPosition() {
        if(expectedDecimalPositions.isEmpty()) {
            return;
        }

        String leftoverPosition = "";
        for (DecimalPosition expectedDecimalPosition : expectedDecimalPositions) {
            leftoverPosition += (expectedDecimalPosition);
            leftoverPosition += "; ";
        }

        Assert.fail("Some position where not removed: " + leftoverPosition);
    }
}
