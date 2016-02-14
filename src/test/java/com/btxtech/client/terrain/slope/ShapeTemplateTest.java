package com.btxtech.client.terrain.slope;

import com.btxtech.TestHelper;
import com.btxtech.game.jsre.client.common.Index;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;

/**
 * Created by Beat
 * 30.01.2016.
 */
@SuppressWarnings("NonJREEmulationClassesInClientCode")
public class ShapeTemplateTest {

    @Test
    public void testSimple1() throws Exception {
        Shape shape = new Shape(ShapeTest.toShapeEntryEntity(new Index(0, 100), new Index(0, 0)));
        ShapeTemplate shapeTemplate = new ShapeTemplate(1, shape);
        shapeTemplate.sculpt(0, 0);

        ShapeTemplateEntry[][] nodes = getNodes(shapeTemplate);
        Assert.assertEquals(1, nodes.length);
        Assert.assertEquals(2, nodes[0].length);

        TestHelper.assertVertex(0, 0, 0, nodes[0][0].getPosition());
        TestHelper.assertVertex(0, 0, 100, nodes[0][1].getPosition());
    }

    @Test
    public void testSimple2() throws Exception {
        Shape shape = new Shape(ShapeTest.toShapeEntryEntity(new Index(0, 100), new Index(0, 50), new Index(0, 0)));
        ShapeTemplate shapeTemplate = new ShapeTemplate(1, shape);
        shapeTemplate.sculpt(0, 0);

        ShapeTemplateEntry[][] nodes = getNodes(shapeTemplate);
        Assert.assertEquals(1, nodes.length);
        Assert.assertEquals(3, nodes[0].length);

        TestHelper.assertVertex(0, 0, 0, nodes[0][0].getPosition());
        TestHelper.assertVertex(0, 0, 50, nodes[0][1].getPosition());
        TestHelper.assertVertex(0, 0, 100, nodes[0][2].getPosition());
    }

    @Test
    public void testMultiple() throws Exception {
        Shape shape = new Shape(ShapeTest.toShapeEntryEntity(new Index(0, 100), new Index(20, 50), new Index(40, 0)));
        ShapeTemplate shapeTemplate = new ShapeTemplate(10, shape);
        shapeTemplate.sculpt(0, 0);

        ShapeTemplateEntry[][] nodes = getNodes(shapeTemplate);
        Assert.assertEquals(10, nodes.length);

        for (ShapeTemplateEntry[] rows : nodes) {
            Assert.assertEquals(3, rows.length);
            TestHelper.assertVertex(40, 0, 0, rows[0].getPosition());
            TestHelper.assertVertex(20, 0, 50, rows[1].getPosition());
            TestHelper.assertVertex(0, 0, 100, rows[2].getPosition());
        }
    }

    @Test
    public void testMultipleFractal() throws Exception {
        Shape shape = new Shape(ShapeTest.toShapeEntryEntity(new Index(0, 100), new Index(20, 50), new Index(40, 0)));
        ShapeTemplate shapeTemplate = new ShapeTemplate(100, shape);
        shapeTemplate.sculpt(10, 1);

        ShapeTemplateEntry[][] nodes = getNodes(shapeTemplate);
        Assert.assertEquals(100, nodes.length);

        for (ShapeTemplateEntry[] rows : nodes) {
            Assert.assertEquals(3, rows.length);

            TestHelper.assertVertex(40, 0, 0, rows[0].getPosition());

            // Assert.assertEquals("Shift to big: " + rows[1], 2.5, MathHelper.getPythagorasC(rows[1].getX() - 20, rows[1].getZ() - 50), 2.5);
            Assert.assertEquals(0, rows[1].getPosition().getY(), 0.001);

            TestHelper.assertVertex(0, 0, 100, rows[2].getPosition());
        }
    }

    private ShapeTemplateEntry[][] getNodes(ShapeTemplate shapeTemplate) throws NoSuchFieldException, IllegalAccessException {
        Field field = shapeTemplate.getClass().getDeclaredField("nodes");
        field.setAccessible(true);
        ShapeTemplateEntry[][] nodes = (ShapeTemplateEntry[][]) field.get(shapeTemplate);
        field.setAccessible(false);

        return nodes;
    }

}
