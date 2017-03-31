package com.btxtech.shared.utils;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Rectangle2D;
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

    @Test
    public void rasterizeTerrainPolygon1() throws Exception {
        Rectangle2D absAabbRect = new Rectangle2D(67.0, 58.0, 200.0, 200.0);
        Polygon2D viewPolygon = new Polygon2D(Arrays.asList(new DecimalPosition(117.0, 58.0), new DecimalPosition(217.0, 58.0), new DecimalPosition(267.0, 258.0), new DecimalPosition(67.0, 258.0)));
        Collection<Index> actual = GeometricUtil.rasterizeTerrainViewField(absAabbRect, viewPolygon);
        List<Index> positions = Arrays.asList(new Index(0, 0), new Index(0, 1), new Index(1, 0), new Index(1, 1));
        GeometricUtilTest.assertIndices(positions, actual);
    }

    @Test
    public void rasterizeTerrainPolygon2() throws Exception {
        Rectangle2D absAabbRect = new Rectangle2D(127.5, 65.0, 200.0, 200.0);
        Polygon2D viewPolygon = new Polygon2D(Arrays.asList(new DecimalPosition(177.5, 65.0), new DecimalPosition(277.5, 65.0), new DecimalPosition(327.5, 265.0), new DecimalPosition(127.5, 265.0)));
        Collection<Index> actual = GeometricUtil.rasterizeTerrainViewField(absAabbRect, viewPolygon);
        List<Index> positions = Arrays.asList(new Index(0, 0), new Index(0, 1), new Index(1, 0), new Index(1, 1), new Index(2, 1));
        GeometricUtilTest.assertIndices(positions, actual);
    }

    @Test
    public void rasterizeTerrainPolygon3() throws Exception {
        Rectangle2D absAabbRect = new Rectangle2D(137.5, 131.0, 200.0, 200.0);
        Polygon2D viewPolygon = new Polygon2D(Arrays.asList(new DecimalPosition(187.5, 131.0), new DecimalPosition(287.5, 131.0), new DecimalPosition(337.5, 331.0), new DecimalPosition(137.5, 331.0)));
        Collection<Index> actual = GeometricUtil.rasterizeTerrainViewField(absAabbRect, viewPolygon);
        List<Index> positions = Arrays.asList(new Index(0, 1), new Index(0, 2), new Index(1, 0), new Index(1, 1), new Index(1, 2), new Index(2, 1), new Index(2, 2));
        GeometricUtilTest.assertIndices(positions, actual);
    }

    @Test
    public void rasterizeTerrainPolygon4() throws Exception {
        Rectangle2D absAabbRect = new Rectangle2D(140.5, 180.0, 200.0, 200.0);
        Polygon2D viewPolygon = new Polygon2D(Arrays.asList(new DecimalPosition(190.5, 180.0), new DecimalPosition(290.5, 180.0), new DecimalPosition(340.5, 380.0), new DecimalPosition(140.5, 380.0)));
        Collection<Index> actual = GeometricUtil.rasterizeTerrainViewField(absAabbRect, viewPolygon);
        List<Index> positions = Arrays.asList(new Index(0, 1), new Index(0, 2), new Index(1, 1), new Index(1, 2), new Index(2, 1), new Index(2, 2));
        GeometricUtilTest.assertIndices(positions, actual);
    }

    @Test
    public void rasterizeTerrainPolygon5() throws Exception {
        Rectangle2D absAabbRect = new Rectangle2D(-100.5, -106.0, 200.0, 200.0);
        Polygon2D viewPolygon = new Polygon2D(Arrays.asList(new DecimalPosition(-50.5, -106.0), new DecimalPosition(49.5, -106.0), new DecimalPosition(99.5, 94.0), new DecimalPosition(-100.5, 94.0)));
        Collection<Index> actual = GeometricUtil.rasterizeTerrainViewField(absAabbRect, viewPolygon);
        List<Index> positions = Arrays.asList(new Index(-1, -1), new Index(-1, 0), new Index(0, -1), new Index(0, 0));
        GeometricUtilTest.assertIndices(positions, actual);
    }

    @Test
    public void rasterizeTerrainPolygon6() throws Exception {
        Rectangle2D absAabbRect = new Rectangle2D(173.5, -372.0, 200.0, 372.0);
        Polygon2D viewPolygon = new Polygon2D(Arrays.asList(new DecimalPosition(223.5, -372.0), new DecimalPosition(323.5, -372.0), new DecimalPosition(373.5, -172.0), new DecimalPosition(173.5, -172.0)));
        Collection<Index> actual = GeometricUtil.rasterizeTerrainViewField(absAabbRect, viewPolygon);
        List<Index> positions = Arrays.asList(new Index(1, -3), new Index(1, -2), new Index(2, -3), new Index(2, -2));
        GeometricUtilTest.assertIndices(positions, actual);
    }

}