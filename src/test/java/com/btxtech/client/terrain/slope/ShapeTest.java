package com.btxtech.client.terrain.slope;

import com.btxtech.TestHelper;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.shared.ShapeEntryEntity;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * 30.01.2016.
 */
public class ShapeTest {

    @Test
    public void test2VerticesUpright() throws Exception {
        Shape shape = new Shape(toShapeEntryEntity(new Index(0, 100), new Index(0, 0)));
        Assert.assertEquals(2, shape.getVertexCount());
        Assert.assertEquals(0, shape.getShiftableCount());
        Assert.assertFalse(shape.isShiftableEntry(0));
        Assert.assertFalse(shape.isShiftableEntry(1));
        Assert.assertEquals(0, shape.getDistance(), 0.001);
        TestHelper.assertVertex(0, 0, 100, shape.getVertex(0));
        TestHelper.assertVertex(0, 0, 0, shape.getVertex(1));
    }

    @Test
    public void test3VerticesUpright() throws Exception {
        Shape shape = new Shape(toShapeEntryEntity(new Index(0, 100), new Index(0, 50), new Index(0, 0)));
        Assert.assertEquals(3, shape.getVertexCount());
        Assert.assertEquals(1, shape.getShiftableCount());
        Assert.assertFalse(shape.isShiftableEntry(0));
        Assert.assertTrue(shape.isShiftableEntry(1));
        Assert.assertFalse(shape.isShiftableEntry(2));
        Assert.assertEquals(0, shape.getDistance(), 0.001);
        TestHelper.assertVertex(0, 0, 100, shape.getVertex(0));
        TestHelper.assertVertex(0, 0, 50, shape.getVertex(1));
        TestHelper.assertVertex(0, 0, 0, shape.getVertex(2));

        TestHelper.assertVertex(10, 0, 50, shape.getNormShiftedVertex(1, 10));
        TestHelper.assertVertex(-10, 0, 50, shape.getNormShiftedVertex(1, -10));

    }

    @Test
    public void test3Vertices() throws Exception {
        Shape shape = new Shape(toShapeEntryEntity(new Index(0, 100), new Index(50, 50), new Index(100, 0)));
        Assert.assertEquals(3, shape.getVertexCount());
        Assert.assertEquals(1, shape.getShiftableCount());
        Assert.assertFalse(shape.isShiftableEntry(0));
        Assert.assertTrue(shape.isShiftableEntry(1));
        Assert.assertFalse(shape.isShiftableEntry(2));
        Assert.assertEquals(100, shape.getDistance(), 0.001);
        TestHelper.assertVertex(0, 0, 100, shape.getVertex(0));
        TestHelper.assertVertex(50, 0, 50, shape.getVertex(1));
        TestHelper.assertVertex(100, 0, 0, shape.getVertex(2));

        TestHelper.assertVertex(57, 0, 57, shape.getNormShiftedVertex(1, 10));
        TestHelper.assertVertex(43, 0, 43, shape.getNormShiftedVertex(1, -10));

    }

    @Test
    public void test3Vertices2() throws Exception {
        Shape shape = new Shape(toShapeEntryEntity(new Index(0, 0), new Index(50, -25), new Index(100, -50)));
        Assert.assertEquals(3, shape.getVertexCount());
        Assert.assertEquals(1, shape.getShiftableCount());
        Assert.assertFalse(shape.isShiftableEntry(0));
        Assert.assertTrue(shape.isShiftableEntry(1));
        Assert.assertFalse(shape.isShiftableEntry(2));
        Assert.assertEquals(100, shape.getDistance(), 0.001);
        TestHelper.assertVertex(0, 0, 0, shape.getVertex(0));
        TestHelper.assertVertex(50, 0, -25, shape.getVertex(1));
        TestHelper.assertVertex(100, 0, -50, shape.getVertex(2));

        TestHelper.assertVertex(54, 0, -16, shape.getNormShiftedVertex(1, 10));
        TestHelper.assertVertex(46, 0, -34, shape.getNormShiftedVertex(1, -10));

    }

    public static List<ShapeEntryEntity> toShapeEntryEntity(Index ... indices) {
        List<ShapeEntryEntity> shapeEntryEntities = new ArrayList<>();
        for (Index index : indices) {
            shapeEntryEntities.add(new ShapeEntryEntity(index, 0f));
        }
        return shapeEntryEntities;
    }

}
