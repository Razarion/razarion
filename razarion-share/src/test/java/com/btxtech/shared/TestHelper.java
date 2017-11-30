package com.btxtech.shared;

import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Matrix4;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 01.11.2015.
 */
public class TestHelper {
    public static void assertItemTypeCountMap(Map<Integer, Integer> actual, int... expectedItemTypeIdCount) {
        Assert.assertTrue("expectedItemTypeIdCount must have an even count. first itemTypeId, second count", expectedItemTypeIdCount.length % 2 == 0);
        int count = expectedItemTypeIdCount.length / 2;
        Assert.assertEquals("Map has wrong size", count, actual.size());
        for (int i = 0; i < count; i++) {
            int expectedItemTypeId = expectedItemTypeIdCount[i * 2];
            Integer actualItemTypeCount = actual.get(expectedItemTypeId);
            Assert.assertNotNull("No item type id: " + expectedItemTypeId, actualItemTypeCount);
            Assert.assertEquals("Wrong count for item type id: " + expectedItemTypeId, expectedItemTypeIdCount[i * 2 + 1], (int) actualItemTypeCount);
        }
    }

    public static void assertIds(Collection<Integer> actualIds, Integer... expectedIds) {
        Assert.assertEquals("Wrong size", expectedIds.length, actualIds.size());
        Collection<Integer> expectedCollection = new ArrayList<>(Arrays.asList(expectedIds));
        expectedCollection.removeAll(actualIds);
        Assert.assertTrue(expectedCollection.isEmpty());
    }

    public static <T> void assertObjects(Collection<T> actualObjects, T... expectedObjects) {
        Assert.assertEquals("Wrong size", expectedObjects.length, actualObjects.size());
        Collection<T> expectedCollection = new ArrayList<>(Arrays.asList(expectedObjects));
        expectedCollection.removeAll(actualObjects);
        Assert.assertTrue(expectedCollection.isEmpty());
    }

    public static void assertVertex(Vertex expected, Vertex actual) {
        String message = "Expected: " + expected + " but was: " + actual;
        assertVertex(message, expected, actual);
    }

    public static void assertVertex(String message, Vertex expected, Vertex actual) {
        Assert.assertEquals(message, expected.getX(), actual.getX(), 0.001);
        Assert.assertEquals(message, expected.getY(), actual.getY(), 0.001);
        Assert.assertEquals(message, expected.getZ(), actual.getZ(), 0.001);
    }

    public static void assertVertex(Vertex expected, double[] vertices, int vertexIndex) {
        assertVertex(expected, createVertex(vertices, vertexIndex));
    }

    public static Vertex createVertex(double[] vertices, int vertexIndex) {
        int scalarIndex = vertexIndex * Vertex.getComponentsPerVertex();
        return new Vertex(vertices[scalarIndex], vertices[scalarIndex + 1], vertices[scalarIndex + 2]);
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

    public static void assertDouble(String message, Double expected, Double actual) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected != null && actual == null) {
            Assert.fail("expected != null && actual == null: " + message);
        }
        if (expected == null) {
            Assert.fail("expected != null: " + message);
        }
        Assert.assertEquals(message, expected, actual, 0.001);
    }


    public static void assertDoubleArray(String message, double[] expected, double[] actual) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected != null && actual == null) {
            Assert.fail("expected != null && actual == null: " + message);
        }
        if (expected == null) {
            Assert.fail("expected != null: " + message);
        }
        Assert.assertArrayEquals(message, expected, actual, 0.001);
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

    public static double[] floatList2DoubleArray(List<Float> textureCoordinates) {
        double[] doubleArray = new double[textureCoordinates.size()];
        for (int i = 0; i < textureCoordinates.size(); i++) {
            doubleArray[i] = textureCoordinates.get(i);
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

    public static double[] transform(List<Float> input, Matrix4 transformation) {
        double[] output = new double[input.size()];
        for (int i = 0; i < input.size(); i += 3) {
            Vertex vertex = new Vertex(input.get(i), input.get(i + 1), input.get(i + 2));
            Vertex transformedVertex = transformation.multiply(vertex, 1.0);
            output[i] = transformedVertex.getX();
            output[i + 1] = transformedVertex.getY();
            output[i + 2] = transformedVertex.getZ();
        }
        return output;

    }

    public static double[] transformNorm(List<Float> input, Matrix4 transformation) {
        double[] output = new double[input.size()];
        for (int i = 0; i < input.size(); i += 3) {
            Vertex vertex = new Vertex(input.get(i), input.get(i + 1), input.get(i + 2));
            Vertex transformedVertex = transformation.multiply(vertex, 0.0).normalize(1.0);
            output[i] = transformedVertex.getX();
            output[i + 1] = transformedVertex.getY();
            output[i + 2] = transformedVertex.getZ();
        }
        return output;
    }

    public static void assertDecimalPosition(String message, DecimalPosition expected, DecimalPosition actual) {
        assertDecimalPosition(message, expected, actual, 0.001);
    }

    public static void assertDecimalPosition(String message, DecimalPosition expected, DecimalPosition actual, double delta) {
        if (expected == null && actual == null) {
            return;
        } else if (expected != null && actual == null) {
            Assert.fail("Expected is: " + expected + ". Actual is null");
        } else if (expected == null) {
            Assert.fail("Expected is null. Actual: " + actual);
        }
        Assert.assertTrue(message + " Expected: " + expected + " Actual: " + actual, expected.equalsDelta(actual, delta));
    }

    public static void assertDecimalPositions(List<DecimalPosition> expected, List<DecimalPosition> actual) {
        Assert.assertEquals("Size is not same", expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertDecimalPosition("At position: " + i + ".", expected.get(i), actual.get(i));
        }
    }

    public static void printDecimalPositions(List<DecimalPosition> indexList) {
        System.out.println("-----------------------------------------------------------");
        System.out.println("List<DecimalPosition> positions = Arrays.asList(" + decimalPositionsToString(indexList) + ");");
        System.out.println("-----------------------------------------------------------");
    }

    public static String decimalPositionsToString(List<DecimalPosition> indexList) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < indexList.size(); i++) {
            DecimalPosition decimalPosition = indexList.get(i);
            builder.append("new DecimalPosition(").append(decimalPosition.getX()).append(", ").append(decimalPosition.getY()).append(")");
            if (i < indexList.size() - 1) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }
}
