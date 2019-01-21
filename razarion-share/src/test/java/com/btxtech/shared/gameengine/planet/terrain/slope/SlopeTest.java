package com.btxtech.shared.gameengine.planet.terrain.slope;

import com.btxtech.shared.datatypes.Vertex;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created by Beat
 * 29.01.2016.
 */
public class SlopeTest {

    @Test
    public void test3CornersUpright1() throws Exception {
        Assert.fail("... TODO ...");
//        Shape shape = new Shape(ShapeTest.toSlopeShapeList(new Index(0, 100), new Index(0, 0)));
//        ShapeTemplate shapeTemplate = new ShapeTemplate(1, shape);
//        shapeTemplate.sculpt(0, 0);
//        Plateau plateau = new Plateau(shapeTemplate, 1000, Arrays.asList(new DecimalPosition(0, 0), new DecimalPosition(200, 0), new DecimalPosition(200, 200)));
//        plateau.wrap(null);
//        Mesh mesh = plateau.getMesh();
//
//        Assert.assertEquals(18, mesh.OLDgetVertices().size());
//
//        List<Vertex> vertices = mesh.OLDgetVertices();
//        assertTriangle(vertices, 0, new Vertex(0.0, 0.0, 0.0), new Vertex(200.0, 0.0, 0.0), new Vertex(0.0, 0.0, 100.0));
//        assertTriangle(vertices, 1, new Vertex(200.0, 0.0, 0.0), new Vertex(200.0, 0.0, 100.0), new Vertex(0.0, 0.0, 100.0));
//        assertTriangle(vertices, 2, new Vertex(200.0, 0.0, 0.0), new Vertex(200.0, 200.0, 0.0), new Vertex(200.0, 0.0, 100.0));
//        assertTriangle(vertices, 3, new Vertex(200.0, 200.0, 0.0), new Vertex(200.0, 200.0, 100.0), new Vertex(200.0, 0.0, 100.0));
//        assertTriangle(vertices, 4, new Vertex(200.0, 200.0, 0.0), new Vertex(0.0, 0.0, 0.0), new Vertex(200.0, 200.0, 100.0));
//        assertTriangle(vertices, 5, new Vertex(0.0, 0.0, 0.0), new Vertex(0.0, 0.0, 100.0), new Vertex(200.0, 200.0, 100.0));
    }

    @Test
    public void test4CornersUpright() throws Exception {
        Assert.fail("... TODO ...");
//        Shape shape = new Shape(ShapeTest.toSlopeShapeList(new Index(0, 100), new Index(0, 0)));
//        ShapeTemplate shapeTemplate = new ShapeTemplate(1, shape);
//        shapeTemplate.sculpt(0, 0);
//        Plateau plateau = new Plateau(shapeTemplate, 1000, Arrays.asList(new DecimalPosition(0, 0), new DecimalPosition(200, 0), new DecimalPosition(200, 200), new DecimalPosition(0, 200)));
//        plateau.wrap(null);
//        Mesh mesh = plateau.getMesh();
//
//        Assert.assertEquals(24, mesh.OLDgetVertices().size());
//
//        List<Vertex> vertices = mesh.OLDgetVertices();
//        assertTriangle(vertices, 0, new Vertex(0.0, 0.0, 0.0), new Vertex(200.0, 0.0, 0.0), new Vertex(0.0, 0.0, 100.0));
//        assertTriangle(vertices, 1, new Vertex(200.0, 0.0, 0.0), new Vertex(200.0, 0.0, 100.0), new Vertex(0.0, 0.0, 100.0));
//        assertTriangle(vertices, 2, new Vertex(200.0, 0.0, 0.0), new Vertex(200.0, 200.0, 0.0), new Vertex(200.0, 0.0, 100.0));
//        assertTriangle(vertices, 3, new Vertex(200.0, 200.0, 0.0), new Vertex(200.0, 200.0, 100.0), new Vertex(200.0, 0.0, 100.0));
//        assertTriangle(vertices, 4, new Vertex(200.0, 200.0, 0.0), new Vertex(0.0, 200.0, 0.0), new Vertex(200.0, 200.0, 100.0));
//        assertTriangle(vertices, 5, new Vertex(0.0, 200.0, 0.0), new Vertex(0.0, 200.0, 100.0), new Vertex(200.0, 200.0, 100.0));
//        assertTriangle(vertices, 6, new Vertex(0.0, 200.0, 0.0), new Vertex(0.0, 0.0, 0.0), new Vertex(0.0, 200.0, 100.0));
//        assertTriangle(vertices, 7, new Vertex(0.0, 0.0, 0.0), new Vertex(0.0, 0.0, 100.0), new Vertex(0.0, 200.0, 100.0));
//
//        List<Vertex> norms = mesh.OLDgetNorms();
//        assertTriangle(norms, 0, new Vertex(-0.7071, -0.7071, 0.0000), new Vertex(0.7071, -0.7071, 0.0000), new Vertex(-0.7071, -0.7071, 0.0000));
//        assertTriangle(norms, 1, new Vertex(0.7071, -0.7071, 0.0000), new Vertex(0.7071, -0.7071, 0.0000), new Vertex(-0.7071, -0.7071, 0.0000));
//        assertTriangle(norms, 2, new Vertex(0.7071, -0.7071, 0.0000), new Vertex(0.7071, 0.7071, 0.0000), new Vertex(0.7071, -0.7071, 0.0000));
//        assertTriangle(norms, 3, new Vertex(0.7071, 0.7071, 0.0000), new Vertex(0.7071, 0.7071, 0.0000), new Vertex(0.7071, -0.7071, 0.0000));
//        assertTriangle(norms, 4, new Vertex(0.7071, 0.7071, 0.0000), new Vertex(-0.7071, 0.7071, 0.0000), new Vertex(0.7071, 0.7071, 0.0000));
//        assertTriangle(norms, 5, new Vertex(-0.7071, 0.7071, 0.0000), new Vertex(-0.7071, 0.7071, 0.0000), new Vertex(0.7071, 0.7071, 0.0000));
//        assertTriangle(norms, 6, new Vertex(-0.7071, 0.7071, 0.0000), new Vertex(-0.7071, -0.7071, 0.0000), new Vertex(-0.7071, 0.7071, 0.0000));
//        assertTriangle(norms, 7, new Vertex(-0.7071, -0.7071, 0.0000), new Vertex(-0.7071, -0.7071, 0.0000), new Vertex(-0.7071, 0.7071, 0.0000));
//
//        List<Vertex> tangents = mesh.getTangents();
//        assertTriangle(tangents, 0, new Vertex(0.7071, -0.7071, 0.0000), new Vertex(0.7071, 0.7071, 0.0000), new Vertex(0.7071, -0.7071, 0.0000));
//        assertTriangle(tangents, 1, new Vertex(0.7071, 0.7071, 0.0000), new Vertex(0.7071, 0.7071, 0.0000), new Vertex(0.7071, -0.7071, 0.0000));
//        assertTriangle(tangents, 2, new Vertex(0.7071, 0.7071, 0.0000), new Vertex(-0.7071, 0.7071, 0.0000), new Vertex(0.7071, 0.7071, 0.0000));
//        assertTriangle(tangents, 3, new Vertex(-0.7071, 0.7071, 0.0000), new Vertex(-0.7071, 0.7071, 0.0000), new Vertex(0.7071, 0.7071, 0.0000));
//        assertTriangle(tangents, 4, new Vertex(-0.7071, 0.7071, 0.0000), new Vertex(-0.7071, -0.7071, 0.0000), new Vertex(-0.7071, 0.7071, 0.0000));
//        assertTriangle(tangents, 5, new Vertex(-0.7071, -0.7071, 0.0000), new Vertex(-0.7071, -0.7071, 0.0000), new Vertex(-0.7071, 0.7071, 0.0000));
//        assertTriangle(tangents, 6, new Vertex(-0.7071, -0.7071, 0.0000), new Vertex(0.7071, -0.7071, 0.0000), new Vertex(-0.7071, -0.7071, 0.0000));
//        assertTriangle(tangents, 7, new Vertex(0.7071, -0.7071, 0.0000), new Vertex(0.7071, -0.7071, 0.0000), new Vertex(-0.7071, -0.7071, 0.0000));
    }

    @Test
    public void test4CornersSlope() throws Exception {
        Assert.fail("... TODO ...");
//        Shape shape = new Shape(ShapeTest.toSlopeShapeList(new Index(0, 100), new Index(50, 50), new Index(100, 0)));
//        ShapeTemplate shapeTemplate = new ShapeTemplate(1, shape);
//        shapeTemplate.sculpt(0, 0);
//        Plateau plateau = new Plateau(shapeTemplate, 1000, Arrays.asList(new DecimalPosition(0, 0), new DecimalPosition(200, 0), new DecimalPosition(200, 200), new DecimalPosition(0, 200)));
//        plateau.wrap(null);
//        Mesh mesh = plateau.getMesh();
//
//        Assert.assertEquals(96, mesh.OLDgetVertices().size());
//
//       // dumpTriangleAsserts(vertices);
    }

    private void assertTriangle(List<Vertex> vertices, int triangleIndex, Vertex a, Vertex b, Vertex c) {
        int i = triangleIndex * 3;
        // TestHelper.assertVertex(a, vertices.get(i));
        // TestHelper.assertVertex(b, vertices.get(i + 1));
        // TestHelper.assertVertex(c, vertices.get(i + 2));
    }

    private void dumpTriangleAsserts(List<Vertex> vertices) {
        for (int i = 0; i < vertices.size(); i += 3) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("assertTriangle(vertices, ");
            stringBuilder.append(i / 3);
            stringBuilder.append(", ");
            stringBuilder.append(testString(vertices.get(i)));
            stringBuilder.append(", ");
            stringBuilder.append(testString(vertices.get(i + 1)));
            stringBuilder.append(", ");
            stringBuilder.append(testString(vertices.get(i + 2)));
            stringBuilder.append(");");
            System.out.println(stringBuilder);
        }
    }

    private String testString(Vertex vertex) {
        return String.format("new Vertex(%.4f, %.4f, %.4f)", vertex.getX(), vertex.getY(), vertex.getZ());
    }

}