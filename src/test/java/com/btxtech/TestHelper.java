package com.btxtech;

import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.TextureCoordinate;
import com.btxtech.shared.primitives.Triangle;
import com.btxtech.shared.primitives.Vertex;
import org.junit.Assert;

import java.util.Iterator;
import java.util.List;

/**
 * Created by Beat
 * 01.11.2015.
 */
public class TestHelper {
    public static void assertVertex(Vertex expected, Vertex actual) {
        String message = "Expected: " + expected + " but was: " + actual;
        assertVertex(message, expected, actual);
    }

    public static void assertVertex(String message, Vertex expected, Vertex actual) {
        Assert.assertEquals(message, expected.getX(), actual.getX(), 0.001);
        Assert.assertEquals(message, expected.getY(), actual.getY(), 0.001);
        Assert.assertEquals(message, expected.getZ(), actual.getZ(), 0.001);
    }

    public static void assertVertex(double expectedX, double expectedY, double expectedZ, Vertex actual) {
        assertVertex(new Vertex(expectedX, expectedY, expectedZ), actual);
    }

    public static void assertTriangle(Triangle expected, int index, VertexList vertexList) {
        List<Vertex> vertexes = vertexList.getVertices().subList(index * 3, index * 3 + 3);

        String message = "Expected: " + expected + " but was: A: " + vertexes.get(0) + " B: " + vertexes.get(1) + " C: " + vertexes.get(2);
        assertVertex(message, expected.getVertexA(), vertexes.get(0));
        assertVertex(message, expected.getVertexB(), vertexes.get(1));
        assertVertex(message, expected.getVertexC(), vertexes.get(2));
    }

    public static String toVertexDoubleString(List<Vertex> vertices) {
        String s = "new double[]{";
        for (Iterator<Vertex> iterator = vertices.iterator(); iterator.hasNext(); ) {
            s += toSimpleString(iterator.next());
            if (iterator.hasNext()) {
                s += ", ";
            }
        }
        s += "}";
        return s;
    }

    public static String toSimpleString(Vertex vertex) {
        return String.format("%.2f, %.2f, %.2f", vertex.getX(), vertex.getY(), vertex.getZ());
    }

    public static String toTextureCoordinateDoubleString(List<TextureCoordinate> textureCoordinates) {
        String s = "new double[]{";
        for (Iterator<TextureCoordinate> iterator = textureCoordinates.iterator(); iterator.hasNext(); ) {
            s += toSimpleString(iterator.next());
            if (iterator.hasNext()) {
                s += ", ";
            }
        }
        s += "}";
        return s;
    }

    public static String toSimpleString(TextureCoordinate textureCoordinate) {
        return String.format("%.4f, %.4f", textureCoordinate.getS(), textureCoordinate.getT());
    }

    public static double[] vertices2DoubleArray(List<Vertex> vertices) {
        double[] doubleArray = new double[vertices.size() * 3];
        for (int i = 0; i < vertices.size(); i++) {
            Vertex vertex = vertices.get(i);
            doubleArray[i * 3] = vertex.getX();
            doubleArray[i * 3 + 1] = vertex.getY();
            doubleArray[i * 3 + 2] = vertex.getZ();
        }
        return doubleArray;
    }

    public static double[] textureCoordinates2DoubleArray(List<TextureCoordinate> textureCoordinates) {
        double[] doubleArray = new double[textureCoordinates.size() * 2];
        for (int i = 0; i < textureCoordinates.size(); i++) {
            TextureCoordinate textureCoordinate = textureCoordinates.get(i);
            doubleArray[i * 2] = textureCoordinate.getS();
            doubleArray[i * 2 + 1] = textureCoordinate.getT();
        }
        return doubleArray;
    }
}
