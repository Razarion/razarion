package com.btxtech.shared.utils;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Line;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Beat
 * 21.01.2017.
 */
public class GeometricUtilTest {
    @Test
    public void rasterizeLineEast() throws Exception {
        List<Index> actual = GeometricUtil.rasterizeLine(new Line(new DecimalPosition(4.375, 3.5), new DecimalPosition(50.125, 4.75)), 8);
        List<Index> positions = Arrays.asList(new Index(0, 0), new Index(1, 0), new Index(2, 0), new Index(3, 0), new Index(4, 0), new Index(5, 0), new Index(6, 0));
        GeometricUtilTest.assertIndices(positions, actual);
    }

    @Test
    public void rasterizeLineNorth() throws Exception {
        List<Index> actual = GeometricUtil.rasterizeLine(new Line(new DecimalPosition(4.125, 3.0), new DecimalPosition(3.375, 27.75)), 8);
        List<Index> positions = Arrays.asList(new Index(0, 0), new Index(0, 1), new Index(0, 2), new Index(0, 3));
        GeometricUtilTest.assertIndices(positions, actual);
    }

    @Test
    public void rasterizeLineWest() throws Exception {
        List<Index> actual = GeometricUtil.rasterizeLine(new Line(new DecimalPosition(5.875, 3.0), new DecimalPosition(-26.625, 5.5)), 8);
        List<Index> positions = Arrays.asList(new Index(0, 0), new Index(-1, 0), new Index(-2, 0), new Index(-3, 0), new Index(-4, 0));
        GeometricUtilTest.assertIndices(positions, actual);
    }

    @Test
    public void rasterizeLineSouth() throws Exception {
        List<Index> actual = GeometricUtil.rasterizeLine(new Line(new DecimalPosition(5.625, 4.75), new DecimalPosition(4.375, -34.0)), 8);
        List<Index> positions = Arrays.asList(new Index(0, 0), new Index(0, -1), new Index(0, -2), new Index(0, -3), new Index(0, -4), new Index(0, -5));
        GeometricUtilTest.assertIndices(positions, actual);
    }

    @Test
    public void rasterizeLineCornerLine() throws Exception {
        List<Index> actual = GeometricUtil.rasterizeLine(new Line(new DecimalPosition(600.1913417161826, 567.9619397662557), new DecimalPosition(600.0, 568.0)), 8);
        GeometricUtilTest.assertIndices(Collections.singletonList(new Index(75, 70)), actual);
    }

    public static void assertIndices(Collection<Index> expected, Collection<Index> actual) {
        Assert.assertEquals("Collection do have different sizes", expected.size(), actual.size());
        List<Index> expectedList = new ArrayList<>(expected);
        List<Index> actualList = new ArrayList<>(actual);
        for (int i = 0; i < actualList.size(); i++) {
            Index expectedIndex = expectedList.get(i);
            Index actualIndex = actualList.get(i);
            if (!expectedIndex.equals(actualIndex)) {
                Assert.fail("Index are different at index: " + i + ". Expected: " + expectedIndex + " Actual: " + actualIndex);
            }
        }
    }

}