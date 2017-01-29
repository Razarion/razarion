package com.btxtech.shared.utils;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Line;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 21.01.2017.
 */
public class GeometricUtilTest {

    @Test
    public void rasterizeLineEast() throws Exception {
        List<Index> actual = GeometricUtil.rasterizeLine(new Line(new DecimalPosition(2.375, 2.75), new DecimalPosition(28.375, 5.5)), 8);
        List<Index> positions = Arrays.asList(new Index(0, 0), new Index(1, 0), new Index(2, 0), new Index(3, 0));
        GeometricUtilTest.assertIndices(positions, actual);
    }

    @Test
    public void rasterizeLineNorth() throws Exception {
        List<Index> actual = GeometricUtil.rasterizeLine(new Line(new DecimalPosition(45.625, 10.25), new DecimalPosition(41.625, 34.75)), 8);
        List<Index> positions = Arrays.asList(new Index(5, 1), new Index(5, 2), new Index(5, 3), new Index(5, 4));
        GeometricUtilTest.assertIndices(positions, actual);
    }

    @Test
    public void rasterizeLineSouth() throws Exception {
        List<Index> actual = GeometricUtil.rasterizeLine(new Line(new DecimalPosition(6.875, -7.5), new DecimalPosition(3.125, -33.0)), 8);
        List<Index> positions = Arrays.asList(new Index(0, -1), new Index(0, -2), new Index(0, -3), new Index(0, -4), new Index(0, -5));
        GeometricUtilTest.assertIndices(positions, actual);
    }

    @Test
    public void rasterizeLineWest() throws Exception {
        List<Index> actual = GeometricUtil.rasterizeLine(new Line(new DecimalPosition(39.125, -15.5), new DecimalPosition(11.125, -12.5)), 8);
        List<Index> positions = Arrays.asList(new Index(4, -2), new Index(3, -2), new Index(2, -2), new Index(1, -2));
        GeometricUtilTest.assertIndices(positions, actual);
    }

    @Test
    public void rasterizeLine() throws Exception {
        List<Index> actual = GeometricUtil.rasterizeLine(new Line(new DecimalPosition(3.6818181818181817, 5.2727272727272725), new DecimalPosition(21.045454545454547, 19.363636363636363)), 8);
        List<Index> positions = Arrays.asList(new Index(0, 0), new Index(0, 1), new Index(1, 1), new Index(2, 1), new Index(2, 2));
        GeometricUtilTest.assertIndices(positions, actual);
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