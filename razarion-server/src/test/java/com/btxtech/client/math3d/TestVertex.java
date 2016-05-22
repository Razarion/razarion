package com.btxtech.client.math3d;

import com.btxtech.shared.primitives.Vertex;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 13.04.2015.
 */
public class TestVertex {
    @Test
    public void getter() {
        Vertex vertex = new Vertex(1.01, 2, 3);
        Assert.assertEquals(1.01, vertex.getX(), 0.0001);
        Assert.assertEquals(2, vertex.getY(), 0.0001);
        Assert.assertEquals(3, vertex.getZ(), 0.0001);
    }

    @Test
    public void add1() {
        Vertex vertex = new Vertex(1.01, 2, 3);
        Vertex vertex2 = vertex.add(-3, 0.5, 15);
        Assert.assertEquals(1.01 - 3, vertex2.getX(), 0.0001);
        Assert.assertEquals(2 + 0.5, vertex2.getY(), 0.0001);
        Assert.assertEquals(3 + 15, vertex2.getZ(), 0.0001);
        // Old will remain
        Assert.assertEquals(1.01, vertex.getX(), 0.0001);
        Assert.assertEquals(2, vertex.getY(), 0.0001);
        Assert.assertEquals(3, vertex.getZ(), 0.0001);
    }

    @Test
    public void add2() {
        Vertex vertex = new Vertex(1.01, 2, 3);
        Vertex vertex2 = new Vertex(-3, 0.5, 15);

        Assert.assertEquals(new Vertex(1.01 - 3, 2 + 0.5, 3 + 15), vertex.add(vertex2));
        // Old will remain
        Assert.assertEquals(new Vertex(1.01, 2, 3), vertex);
        Assert.assertEquals(new Vertex(-3, 0.5, 15), vertex2);
    }

    @Test
    public void sub() {
        Assert.assertEquals(new Vertex(0, 0, 0), new Vertex(1.01, 2, 3).sub(new Vertex(1.01, 2, 3)));
        Assert.assertEquals(new Vertex(222, -58, 9), new Vertex(234, 4, 6).sub(new Vertex(12, 62, -3)));
    }

    @Test
    public void dot() {
        Assert.assertEquals(14.0201, new Vertex(1.01, 2, 3).dot(new Vertex(1.01, 2, 3)), 0.001);
        Assert.assertEquals(32, new Vertex(1, 2, 3).dot(new Vertex(4, 5, 6)), 0.001);
        Assert.assertEquals(1, new Vertex(1, 0, 0).dot(new Vertex(1, 1, 0)), 0.001);
        Assert.assertEquals(-1, new Vertex(1, 0, 0).dot(new Vertex(-1, 1, 0)), 0.001);
    }

    @Test
    public void cross1() {
        Assert.assertEquals(new Vertex(-3, 6, -3), new Vertex(1, 2, 3).cross(new Vertex(4, 5, 6)));
        Assert.assertEquals(new Vertex(527.455, 37225.23, 23791.5), new Vertex(423, -6, 0.01).cross(new Vertex(123, 54.5, -88)));
    }

    @Test
    public void cross2() {
        Assert.assertEquals(new Vertex(-3, 6, -3), new Vertex(0, 0, 0).cross(new Vertex(1, 2, 3), new Vertex(4, 5, 6)));
        Assert.assertEquals(new Vertex(-3, 6, -3), new Vertex(12, -3, 0.5).cross(new Vertex(1 + 12, 2 - 3, 3 + 0.5), new Vertex(4 + 12, 5 - 3, 6 + 0.5)));
    }

    @Test
    public void projection() {
        Assert.assertEquals(1, new Vertex(0, 0, 0).projection(new Vertex(1, 0, 0), new Vertex(1, 0, 0)), 0.001);
        Assert.assertEquals(1, new Vertex(0, 0, 0).projection(new Vertex(1, 0, 0), new Vertex(1, 1, 0)), 0.001);
        Assert.assertEquals(3, new Vertex(0, 0, 0).projection(new Vertex(1, 0, 0), new Vertex(3, 3, 0)), 0.001);
        Assert.assertEquals(-3, new Vertex(0, 0, 0).projection(new Vertex(1, 0, 0), new Vertex(-3, 3, 0)), 0.001);
        Assert.assertEquals(-34.076935439873828512, new Vertex(0, 0, 0).projection(new Vertex(45, -564, 1), new Vertex(123, 44, 0.5)), 0.001);
        Assert.assertEquals(0, new Vertex(0, 0, 0).projection(new Vertex(1, 0, 0), new Vertex(0, 0, 0)), 0.001);
        Assert.assertEquals(-34.076935439873828512, new Vertex(10, 22, -18).projection(new Vertex(45 + 10, -564 + 22, 1 - 18), new Vertex(123 + 10, 44 + 22, 0.5 - 18)), 0.001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void projectionException() {
        Assert.assertEquals(0, new Vertex(0, 0, 0).projection(new Vertex(0, 0, 0), new Vertex(1, 0, 0)), 0.001);
    }

    @Test
    public void divide() {
        Assert.assertEquals(new Vertex(1.2, 2.4, 0.01), new Vertex(-12, -24, -0.1).divide(-10));
        Assert.assertEquals(new Vertex(0.3258064516129032, -0.6451612903225806, 0.9677419354838709), new Vertex(1.01, -2, 3).divide(3.1));
    }

    @Test
    public void magnitude() {
        Assert.assertEquals(5, new Vertex(5, 0, 0).magnitude(), 0.0001);
        Assert.assertEquals(4, new Vertex(0, 4, 0).magnitude(), 0.0001);
        Assert.assertEquals(3, new Vertex(0, 0, 3).magnitude(), 0.0001);
        Assert.assertEquals(34.49637662132068, new Vertex(10, 33, 1).magnitude(), 0.0001);
        Assert.assertEquals(8.065829157625394, new Vertex(0.24, -1, 8).magnitude(), 0.0001);
    }

    @Test
    public void normalize() {
        Assert.assertEquals(1.0, new Vertex(1, 1, 1).normalize(1).magnitude(), 0.0001);
        Assert.assertEquals(1.0, new Vertex(13, -41, 0.001).normalize(1).magnitude(), 0.0001);

        Assert.assertEquals(new Vertex(2, 0, 0), new Vertex(1, 0, 0).normalize(2.0));
        Assert.assertEquals(new Vertex(0, -3, 0), new Vertex(0, -1, 0).normalize(3.0));
        Assert.assertEquals(new Vertex(0, 0, 0.25), new Vertex(0, 0, 1).normalize(0.25));

        Assert.assertEquals(new Vertex(1.9066304222982975, 0.06809394365351062, -2.3151940842193612), new Vertex(14, 0.5, -17).normalize(3));
    }

    @Test
    public void distance() {
        Assert.assertEquals(0, new Vertex(0, 0, 0).distance(new Vertex(0, 0, 0)), 0.0001);
        Assert.assertEquals(26.004807247891687, new Vertex(-7, -4, 3).distance(new Vertex(17, 6, 2.50)), 0.0001);
        Assert.assertEquals(312.82134660217804, new Vertex(234, -0.001, 222).distance(new Vertex(47, 116, 444.333)), 0.0001);
    }

    @Test
    public void appendTo() {
        Vertex vertex = new Vertex(1.01, 2, 3);
        List<Double> doubleList = new ArrayList<>();
        vertex.appendTo(doubleList);
        Assert.assertEquals(3, doubleList.size());
        Assert.assertEquals(1.01, doubleList.get(0), 0.001);
        Assert.assertEquals(2, doubleList.get(1), 0.001);
        Assert.assertEquals(3, doubleList.get(2), 0.001);
    }

    @SuppressWarnings("EqualsWithItself")
    @Test
    public void equals() {
        Vertex vertex11 = new Vertex(1.01, 2, 3);
        Vertex vertex12 = new Vertex(1.01, 2, 3);
        Vertex vertex2 = new Vertex(15, -0.2, 100);

        Assert.assertTrue(vertex11.equals(vertex11));
        Assert.assertTrue(vertex12.equals(vertex12));
        Assert.assertTrue(vertex11.equals(vertex12));
        Assert.assertTrue(vertex12.equals(vertex11));

        Assert.assertFalse(vertex11.equals(vertex2));
        Assert.assertFalse(vertex12.equals(vertex2));
    }

    @Test
    public void hash() {
        Vertex vertex1 = new Vertex(1.01, 2, 3);
        Vertex vertex12 = new Vertex(1.01, 2, 3);
        Vertex vertex2 = new Vertex(15, -98, 0.001);

        Assert.assertEquals(-1686262308, vertex1.hashCode());
        Assert.assertEquals(-1686262308, vertex12.hashCode());
        Assert.assertEquals(621235121, vertex2.hashCode());
    }

    @Test
    public void getComponentsPerVertex() {
        Assert.assertEquals(3, Vertex.getComponentsPerVertex());
    }

    @Test
    public void multiply() {
        Vertex vertex = new Vertex(1.01, 2, -3);
        Assert.assertEquals(new Vertex(10.1, 20, -30), vertex.multiply(10));
    }

    @Test
    public void interpolate() {
        Vertex origin = new Vertex(0, 0, 0);

        Assert.assertEquals(new Vertex(10, 0, 0), origin.interpolate(10, new Vertex(1, 0, 0)));
        Assert.assertEquals(new Vertex(-10, 0, 0), origin.interpolate(-10, new Vertex(1, 0, 0)));
        Assert.assertEquals(new Vertex(0, 10, 0), origin.interpolate(10, new Vertex(0, 10, 0)));
        Assert.assertEquals(new Vertex(0, -10, 0), origin.interpolate(-10, new Vertex(0, 10, 0)));
        Assert.assertEquals(new Vertex(0, 0, 10), origin.interpolate(10, new Vertex(0, 0, 10)));
        Assert.assertEquals(new Vertex(0, 0, -10), origin.interpolate(-10, new Vertex(0, 0, 10)));

        origin = new Vertex(13, 0.25, -7);
        Assert.assertEquals(10, origin.interpolate(10, new Vertex(1, 0, 0)).distance(origin), 0.001);
        Assert.assertEquals(10, origin.interpolate(10, new Vertex(0, 10, 0)).distance(origin), 0.001);
        Assert.assertEquals(10, origin.interpolate(10, new Vertex(0, 0, 10)).distance(origin), 0.001);

        Assert.assertEquals(5, origin.interpolate(-5, new Vertex(12, 4, -55)).distance(origin), 0.001);
    }

    @Test
    public void unsignedAngle() {
        Assert.assertEquals(Math.toRadians(90), new Vertex(0, 0, 0).unsignedAngle(new Vertex(1, 0, 0), new Vertex(0, 1, 0)), 0.0001);
        Assert.assertEquals(Math.toRadians(180), new Vertex(0, 0, 0).unsignedAngle(new Vertex(1, 0, 0), new Vertex(-1, 0, 0)), 0.0001);
        Assert.assertEquals(Math.toRadians(90), new Vertex(0, 0, 0).unsignedAngle(new Vertex(1, 0, 0), new Vertex(0, -1, 0)), 0.0001);

        Assert.assertEquals(Math.toRadians(90), new Vertex(1, 45, -33).unsignedAngle(new Vertex(1 + 1, 45, -33), new Vertex(1, 1 + 45, -33)), 0.0001);
        Assert.assertEquals(Math.toRadians(180), new Vertex(1, 45, -33).unsignedAngle(new Vertex(1 + 1, 45, -33), new Vertex(-1 + 1, 45, -33)), 0.0001);
        Assert.assertEquals(Math.toRadians(90), new Vertex(1, 45, -33).unsignedAngle(new Vertex(1 + 1, 45, 33), new Vertex(1, -1 + 45, -33)), 0.0001);

    }

    public static void assertVertex(Vertex expected, Vertex actual) {
        String message = "expected:<" + expected + "> but was:<" + actual + ">";
        Assert.assertEquals(message, expected.getX(), actual.getX(), 0.0001);
        Assert.assertEquals(message, expected.getY(), actual.getY(), 0.0001);
        Assert.assertEquals(message, expected.getZ(), actual.getZ(), 0.0001);
    }

    public static void assertVertex(double x, double y, double z, Vertex actual) {
        assertVertex(new Vertex(x, y, z), actual);
    }
}
