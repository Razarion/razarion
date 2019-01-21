package com.btxtech.shared.datatypes;

import com.btxtech.shared.TestHelper;
import com.btxtech.shared.dto.SlopeShape;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * on 14.01.2019.
 */
public class ShapeTest {

    @Test
    public void test2VerticesUpright() {
        Shape shape = new Shape(toSlopeShapeList(new DecimalPosition(0, 100), new DecimalPosition(0, 0)));
        Assert.assertEquals(2, shape.getVertexCount());
        Assert.assertEquals(0, shape.getShiftableCount());
        Assert.assertFalse(shape.isShiftableEntry(0));
        Assert.assertFalse(shape.isShiftableEntry(1));
        Assert.assertEquals(0, shape.getDistance(), 0.001);
        TestHelper.assertVertex(0, 0, 100, shape.getVertex(0));
        TestHelper.assertVertex(0, 0, 0, shape.getVertex(1));
    }

    @Test
    public void test3VerticesUpright() {
        Shape shape = new Shape(toSlopeShapeList(new DecimalPosition(0, 100), new DecimalPosition(0, 50), new DecimalPosition(0, 0)));
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
    public void test3Vertices() {
        Shape shape = new Shape(toSlopeShapeList(new DecimalPosition(0, 100), new DecimalPosition(50, 50), new DecimalPosition(100, 0)));
        Assert.assertEquals(3, shape.getVertexCount());
        Assert.assertEquals(1, shape.getShiftableCount());
        Assert.assertFalse(shape.isShiftableEntry(0));
        Assert.assertTrue(shape.isShiftableEntry(1));
        Assert.assertFalse(shape.isShiftableEntry(2));
        Assert.assertEquals(100, shape.getDistance(), 0.001);
        TestHelper.assertVertex(0, 0, 100, shape.getVertex(0));
        TestHelper.assertVertex(50, 0, 50, shape.getVertex(1));
        TestHelper.assertVertex(100, 0, 0, shape.getVertex(2));

        TestHelper.assertVertex(57.0710, 0, 57.0710, shape.getNormShiftedVertex(1, 10));
        TestHelper.assertVertex(42.9289, 0, 42.9289, shape.getNormShiftedVertex(1, -10));

    }

    @Test
    public void test3Vertices2() {
        Shape shape = new Shape(toSlopeShapeList(new DecimalPosition(0, 0), new DecimalPosition(50, -25), new DecimalPosition(100, -50)));
        Assert.assertEquals(3, shape.getVertexCount());
        Assert.assertEquals(1, shape.getShiftableCount());
        Assert.assertFalse(shape.isShiftableEntry(0));
        Assert.assertTrue(shape.isShiftableEntry(1));
        Assert.assertFalse(shape.isShiftableEntry(2));
        Assert.assertEquals(100, shape.getDistance(), 0.001);
        TestHelper.assertVertex(0, 0, 0, shape.getVertex(0));
        TestHelper.assertVertex(50, 0, -25, shape.getVertex(1));
        TestHelper.assertVertex(100, 0, -50, shape.getVertex(2));

        TestHelper.assertVertex(54.47213, 0, -16.05572, shape.getNormShiftedVertex(1, 10));
        TestHelper.assertVertex(45.52786, 0, -33.94427, shape.getNormShiftedVertex(1, -10));

    }

    public static List<SlopeShape> toSlopeShapeList(DecimalPosition... positions) {
        List<SlopeShape> slopeShapes = new ArrayList<>();
        for (DecimalPosition position : positions) {
            slopeShapes.add(new SlopeShape(position, 0));
        }
        return slopeShapes;
    }

}