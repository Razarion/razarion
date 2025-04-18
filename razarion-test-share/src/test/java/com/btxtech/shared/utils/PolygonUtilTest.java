package com.btxtech.shared.utils;

import com.btxtech.shared.datatypes.DecimalPosition;
import org.junit.Assert;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * on 30.07.2017.
 */
public class PolygonUtilTest {

    @Test
    public void isCorrectDirection() throws Exception {
        List<DecimalPosition> counterclockwiseRect = Arrays.asList(new DecimalPosition(20, 20), new DecimalPosition(40, 20), new DecimalPosition(40, 40), new DecimalPosition(20, 40));
        Assert.assertTrue(PolygonUtil.isCounterclockwise(counterclockwiseRect));
        List<DecimalPosition> clockwiseRect = Arrays.asList(new DecimalPosition(40, 20), new DecimalPosition(40, 40), new DecimalPosition(60, 40), new DecimalPosition(60, 20));
        Assert.assertFalse(PolygonUtil.isCounterclockwise(clockwiseRect));

        List<DecimalPosition> counterclockwiseComplex = Arrays.asList(new DecimalPosition(84.875, 52.500), new DecimalPosition(79.625, 77.250), new DecimalPosition(64.875, 73.250), new DecimalPosition(58.625, 58.250), new DecimalPosition(39.625, 83.500), new DecimalPosition(23.625, 80.500), new DecimalPosition(20.375, 58.500), new DecimalPosition(37.875, 55.250), new DecimalPosition(39.625, 44.750), new DecimalPosition(18.375, 40.000), new DecimalPosition(-4.625, 39.000), new DecimalPosition(12.375, 22.000), new DecimalPosition(33.625, 24.000), new DecimalPosition(34.625, 7.250), new DecimalPosition(34.625, -32.250), new DecimalPosition(44.125, -25.750), new DecimalPosition(62.625, 0.000), new DecimalPosition(66.125, 10.000), new DecimalPosition(81.625, 13.000), new DecimalPosition(97.375, 13.500), new DecimalPosition(79.875, 20.500), new DecimalPosition(61.875, 30.500), new DecimalPosition(81.875, 32.250), new DecimalPosition(103.125, 35.750), new DecimalPosition(90.875, 44.250));
        Assert.assertTrue(PolygonUtil.isCounterclockwise(counterclockwiseComplex));
        List<DecimalPosition> clockwiseComplex = Arrays.asList(new DecimalPosition(54.625, 52.000), new DecimalPosition(82.375, 69.250), new DecimalPosition(98.375, 80.500), new DecimalPosition(92.625, 61.000), new DecimalPosition(80.125, 46.000), new DecimalPosition(90.875, 38.750), new DecimalPosition(118.875, 34.000), new DecimalPosition(128.375, 25.500), new DecimalPosition(98.375, 25.000), new DecimalPosition(73.375, 25.250), new DecimalPosition(66.875, 2.500), new DecimalPosition(55.125, -13.250), new DecimalPosition(29.125, -28.250), new DecimalPosition(43.125, 3.750), new DecimalPosition(43.875, 13.000), new DecimalPosition(19.625, 20.750), new DecimalPosition(3.125, 24.750), new DecimalPosition(-25.125, 30.000), new DecimalPosition(-53.875, 38.000), new DecimalPosition(-68.875, 48.500), new DecimalPosition(-48.375, 50.750), new DecimalPosition(-28.125, 48.250), new DecimalPosition(-10.125, 45.750), new DecimalPosition(14.875, 41.250), new DecimalPosition(15.625, 41.750), new DecimalPosition(12.625, 57.000), new DecimalPosition(12.125, 72.750), new DecimalPosition(6.875, 85.000), new DecimalPosition(9.625, 99.500), new DecimalPosition(16.375, 101.000), new DecimalPosition(26.625, 89.000), new DecimalPosition(30.875, 61.500), new DecimalPosition(31.375, 41.500), new DecimalPosition(33.375, 33.500), new DecimalPosition(44.625, 26.500), new DecimalPosition(52.125, 27.500), new DecimalPosition(54.375, 30.000), new DecimalPosition(57.625, 40.250), new DecimalPosition(48.125, 45.500));
        Assert.assertFalse(PolygonUtil.isCounterclockwise(clockwiseComplex));
    }

    @Test
    public void removeSelfIntersectingCorners() throws Exception {
        List<DecimalPosition> tooSmall = Arrays.asList(new DecimalPosition(38.375, 12.750), new DecimalPosition(62.875, 14.000), new DecimalPosition(78.625, 32.000));
        List<DecimalPosition> actual1 = PolygonUtil.removeSelfIntersectingCorners(tooSmall);
        ReflectionAssert.assertReflectionEquals(tooSmall, actual1);

        List<DecimalPosition> nonIntersectingPolygon = Arrays.asList(new DecimalPosition(38.375, 12.750), new DecimalPosition(62.875, 14.000), new DecimalPosition(78.625, 32.000), new DecimalPosition(76.875, 59.250), new DecimalPosition(56.375, 72.500), new DecimalPosition(31.875, 75.750), new DecimalPosition(13.875, 64.250), new DecimalPosition(6.875, 47.250), new DecimalPosition(12.125, 27.750), new DecimalPosition(21.125, 16.500));
        List<DecimalPosition> actual2 = PolygonUtil.removeSelfIntersectingCorners(nonIntersectingPolygon);
        ReflectionAssert.assertReflectionEquals(nonIntersectingPolygon, actual2);

        List<DecimalPosition> selfIntersecting1 = Arrays.asList(new DecimalPosition(27.125, 54.750), new DecimalPosition(15.125, 30.250), new DecimalPosition(23.875, 18.250), new DecimalPosition(46.875, 15.750), new DecimalPosition(52.875, 26.250), new DecimalPosition(48.875, 52.500), new DecimalPosition(78.625, 52.250), new DecimalPosition(33.375, 36.750), new DecimalPosition(68.625, 77.250), new DecimalPosition(34.375, 75.250));
        List<DecimalPosition> expectedSelfIntersecting1 = Arrays.asList(new DecimalPosition(27.125, 54.750), new DecimalPosition(15.125, 30.250), new DecimalPosition(23.875, 18.250), new DecimalPosition(46.875, 15.750), new DecimalPosition(52.875, 26.250), new DecimalPosition(78.625, 52.250), new DecimalPosition(33.375, 36.750), new DecimalPosition(68.625, 77.250), new DecimalPosition(34.375, 75.250));
        List<DecimalPosition> actualSelfIntersecting1 = PolygonUtil.removeSelfIntersectingCorners(selfIntersecting1);
        ReflectionAssert.assertReflectionEquals(expectedSelfIntersecting1, actualSelfIntersecting1);

        List<DecimalPosition> selfIntersecting2 = Arrays.asList(new DecimalPosition(34.875, 11.250), new DecimalPosition(71.875, 19.750), new DecimalPosition(85.125, 13.750), new DecimalPosition(69.125, 12.750), new DecimalPosition(64.875, 30.250), new DecimalPosition(74.625, 34.500), new DecimalPosition(90.875, 44.250), new DecimalPosition(93.625, 34.500), new DecimalPosition(77.375, 43.750), new DecimalPosition(64.875, 64.250), new DecimalPosition(51.875, 79.250), new DecimalPosition(70.625, 80.750), new DecimalPosition(81.375, 67.750), new DecimalPosition(85.625, 59.000), new DecimalPosition(72.875, 53.250), new DecimalPosition(54.125, 52.000), new DecimalPosition(37.375, 51.250), new DecimalPosition(21.875, 56.000), new DecimalPosition(14.375, 71.000), new DecimalPosition(28.875, 77.750), new DecimalPosition(38.625, 70.000), new DecimalPosition(44.125, 57.500), new DecimalPosition(45.625, 42.250), new DecimalPosition(29.375, 36.500), new DecimalPosition(15.875, 33.000), new DecimalPosition(8.625, 25.750), new DecimalPosition(10.375, 20.500), new DecimalPosition(13.625, 13.000), new DecimalPosition(17.125, 9.000));
        List<DecimalPosition> expectedSelfIntersecting2 = Arrays.asList(new DecimalPosition(34.875, 11.250), new DecimalPosition(69.125, 12.750), new DecimalPosition(64.875, 30.250), new DecimalPosition(74.625, 34.500), new DecimalPosition(93.625, 34.500), new DecimalPosition(77.375, 43.750), new DecimalPosition(85.625, 59.000), new DecimalPosition(72.875, 53.250), new DecimalPosition(54.125, 52.000), new DecimalPosition(38.625, 70.000), new DecimalPosition(44.125, 57.500), new DecimalPosition(45.625, 42.250), new DecimalPosition(29.375, 36.500), new DecimalPosition(15.875, 33.000), new DecimalPosition(8.625, 25.750), new DecimalPosition(10.375, 20.500), new DecimalPosition(13.625, 13.000), new DecimalPosition(17.125, 9.000));
        List<DecimalPosition> actualSelfIntersecting2 = PolygonUtil.removeSelfIntersectingCorners(selfIntersecting2);
        ReflectionAssert.assertReflectionEquals(expectedSelfIntersecting2, actualSelfIntersecting2);
    }
}