package com.btxtech.shared.gameengine.planet.terrain.slope;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Beat
 * 30.01.2016.
 */
public class SlopeSkeletonConfigEntityFactoryTest {

    @Test
    public void testSimple1() throws Exception {
        Assert.fail("... TODO ...");
//        Shape shape = new Shape(ShapeTest.toSlopeShapeList(new Index(0, 100), new Index(0, 0)));
//        SlopeSkeletonFactory slopeSkeletonFactory = new SlopeSkeletonFactory(1, shape);
//        slopeSkeletonFactory.sculpt(0, 0);
//
//        SlopeSkeletonEntry[][] nodes = getNodes(slopeSkeletonFactory);
//        Assert.assertEquals(1, nodes.length);
//        Assert.assertEquals(2, nodes[0].length);
//
//        TestHelper.assertVertex(0, 0, 0, nodes[0][0].toIndex());
//        TestHelper.assertVertex(0, 0, 100, nodes[0][1].toIndex());
    }

    @Test
    public void testSimple2() throws Exception {
        Assert.fail("... TODO ...");
//        Shape shape = new Shape(ShapeTest.toSlopeShapeList(new Index(0, 100), new Index(0, 50), new Index(0, 0)));
//        SlopeSkeletonFactory slopeSkeletonFactory = new SlopeSkeletonFactory(1, shape);
//        slopeSkeletonFactory.sculpt(0, 0);
//
//        SlopeSkeletonEntry[][] nodes = getNodes(slopeSkeletonFactory);
//        Assert.assertEquals(1, nodes.length);
//        Assert.assertEquals(3, nodes[0].length);
//
//        TestHelper.assertVertex(0, 0, 0, nodes[0][0].toIndex());
//        TestHelper.assertVertex(0, 0, 50, nodes[0][1].toIndex());
//        TestHelper.assertVertex(0, 0, 100, nodes[0][2].toIndex());
    }

    @Test
    public void testMultiple() throws Exception {
        Assert.fail("... TODO ...");
//        Shape shape = new Shape(ShapeTest.toSlopeShapeList(new Index(0, 100), new Index(20, 50), new Index(40, 0)));
//        SlopeSkeletonFactory slopeSkeletonFactory = new SlopeSkeletonFactory(10, shape);
//        slopeSkeletonFactory.sculpt(0, 0);
//
//        SlopeSkeletonEntry[][] nodes = getNodes(slopeSkeletonFactory);
//        Assert.assertEquals(10, nodes.length);
//
//        for (SlopeSkeletonEntry[] rows : nodes) {
//            Assert.assertEquals(3, rows.length);
//            TestHelper.assertVertex(40, 0, 0, rows[0].toIndex());
//            TestHelper.assertVertex(20, 0, 50, rows[1].toIndex());
//            TestHelper.assertVertex(0, 0, 100, rows[2].toIndex());
//        }
    }

    @Test
    public void testMultipleFractal() throws Exception {
        Assert.fail("... TODO ...");
//        Shape shape = new Shape(ShapeTest.toSlopeShapeList(new Index(0, 100), new Index(20, 50), new Index(40, 0)));
//        SlopeSkeletonFactory slopeSkeletonFactory = new SlopeSkeletonFactory(100, shape);
//        slopeSkeletonFactory.sculpt(10, 1);
//
//        SlopeSkeletonEntry[][] nodes = getNodes(slopeSkeletonFactory);
//        Assert.assertEquals(100, nodes.length);
//
//        for (SlopeSkeletonEntry[] rows : nodes) {
//            Assert.assertEquals(3, rows.length);
//
//            TestHelper.assertVertex(40, 0, 0, rows[0].toIndex());
//
//            // Assert.assertEquals("Shift to big: " + rows[1], 2.5, MathHelper.getPythagorasC(rows[1].startX() - 20, rows[1].getZ() - 50), 2.5);
//            Assert.assertEquals(0, rows[1].toIndex().startY(), 0.001);
//
//            TestHelper.assertVertex(0, 0, 100, rows[2].toIndex());
//        }
    }

//    private SlopeSkeletonEntry[][] getNodes(SlopeModeler slopeModeler) throws NoSuchFieldException, IllegalAccessException {
//        Field field = slopeModeler.getClass().getDeclaredField("nodes");
//        field.setAccessible(true);
//        SlopeSkeletonEntry[][] nodes = (SlopeSkeletonEntry[][]) field.get(slopeModeler);
//        field.setAccessible(false);
//
//        return nodes;
//    }

}