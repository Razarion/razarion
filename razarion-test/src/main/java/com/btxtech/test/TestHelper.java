package com.btxtech.test;

import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.TextureCoordinate;
import com.btxtech.shared.datatypes.Triangle;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.VertexList;
import org.junit.Assert;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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

    public static void assertMatrix(Matrix4 expected, Matrix4 actual, double delta) {
        if (expected.equalsDelta(actual, delta)) {
            return;
        }
        Assert.fail("Matrices are not equal. Expected: " + expected + " Actual:" + actual);
    }

    public static void assertColor(Color expected, Color actual) {
        Assert.assertEquals("R value of color is not the same", expected.getR(), actual.getR(), 0.0001);
        Assert.assertEquals("G value of color is not the same", expected.getG(), actual.getG(), 0.0001);
        Assert.assertEquals("B value of color is not the same", expected.getB(), actual.getB(), 0.0001);
        Assert.assertEquals("A value of color is not the same", expected.getA(), actual.getA(), 0.0001);
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

    public static void setPrivateField(Object object, String fieldName, Object value) throws Exception {
//        if (AopUtils.isJdkDynamicProxy(object)) {
//            object = ((Advised) object).getTargetSource().getTarget();
//        }
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
        field.setAccessible(false);
    }

    public static void writeArrayToFile(String fileName, double[] array) {
        try {
            FileOutputStream fout = new FileOutputStream(fileName);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(array);
            oos.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeArrayToConsole(double[] array) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        for (int i = 0; i < array.length; i++) {
            double d = array[i];
            builder.append(String.format(Locale.US, "%.2f", d));
            if (array.length > i + 1) {
                builder.append(", ");
            }
        }
        builder.append("}");
        System.out.println(builder);
    }

    public static double[] readArrayFromFile(InputStream inputStream) {
        try {
            ObjectInputStream ois = new ObjectInputStream(inputStream);
            double[] array = (double[]) ois.readObject();
            ois.close();
            return array;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String resource2Text(String location, Class clazz) {
        InputStream inputStream = clazz.getResourceAsStream(location);
        if (inputStream == null) {
            throw new IllegalArgumentException("Location can not be found: " + location);
        }
        try {
            try (BufferedReader buffer = new BufferedReader(new InputStreamReader(inputStream))) {
                return buffer.lines().collect(Collectors.joining());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
