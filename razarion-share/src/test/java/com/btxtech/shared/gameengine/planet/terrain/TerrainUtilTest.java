package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.dto.SlopeShape;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class TerrainUtilTest {
    @Test
    public void setupSegmentLookup1() {
        List<SlopeShape> slopeShapes = Arrays.asList(
                new SlopeShape().setSlopeFactor(0.0f),
                new SlopeShape().setSlopeFactor(1.0f),
                new SlopeShape().setSlopeFactor(0.0f));
        TerrainSlopeTileBuilder.Segment[] segments = TerrainUtil.setupSegmentLookup(slopeShapes);
        Assert.assertArrayEquals(new TerrainSlopeTileBuilder.Segment[]{
                        TerrainSlopeTileBuilder.Segment.OUTER,
                        TerrainSlopeTileBuilder.Segment.INNER}
                , segments);
    }

    @Test
    public void setupSegmentLookup2() {
        List<SlopeShape> slopeShapes = Arrays.asList(
                new SlopeShape().setSlopeFactor(1.0f),
                new SlopeShape().setSlopeFactor(1.0f),
                new SlopeShape().setSlopeFactor(1.0f),
                new SlopeShape().setSlopeFactor(1.0f));
        TerrainSlopeTileBuilder.Segment[] segments = TerrainUtil.setupSegmentLookup(slopeShapes);
        Assert.assertArrayEquals(new TerrainSlopeTileBuilder.Segment[]{
                        TerrainSlopeTileBuilder.Segment.CENTER,
                        TerrainSlopeTileBuilder.Segment.CENTER,
                        TerrainSlopeTileBuilder.Segment.CENTER}
                , segments);
    }

    @Test
    public void setupSegmentLookup3() {
        List<SlopeShape> slopeShapes = Arrays.asList(
                new SlopeShape().setSlopeFactor(0.0f),
                new SlopeShape().setSlopeFactor(0.0f),
                new SlopeShape().setSlopeFactor(0.0f),
                new SlopeShape().setSlopeFactor(0.0f));
        TerrainSlopeTileBuilder.Segment[] segments = TerrainUtil.setupSegmentLookup(slopeShapes);
        Assert.assertArrayEquals(new TerrainSlopeTileBuilder.Segment[]{
                        TerrainSlopeTileBuilder.Segment.INNER,
                        TerrainSlopeTileBuilder.Segment.INNER,
                        TerrainSlopeTileBuilder.Segment.INNER}
                , segments);
    }
    @Test
    public void setupSegmentLookup4() {
        List<SlopeShape> slopeShapes = Arrays.asList(
                new SlopeShape().setSlopeFactor(0.0f),
                new SlopeShape().setSlopeFactor(1.0f),
                new SlopeShape().setSlopeFactor(1.0f),
                new SlopeShape().setSlopeFactor(0.0f));
        TerrainSlopeTileBuilder.Segment[] segments = TerrainUtil.setupSegmentLookup(slopeShapes);
        Assert.assertArrayEquals(new TerrainSlopeTileBuilder.Segment[]{
                        TerrainSlopeTileBuilder.Segment.OUTER,
                        TerrainSlopeTileBuilder.Segment.CENTER,
                        TerrainSlopeTileBuilder.Segment.INNER}
                , segments);
    }
}