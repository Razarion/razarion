package com.btxtech.server.collada;

import com.btxtech.shared.MathHelper2;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.TextureCoordinate;
import com.btxtech.shared.primitives.Vertex;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 14.08.2015.
 */
public class ColladaConverterTest {
    private static Logger LOGGER = Logger.getLogger(ColladaConverterTest.class.getName());

    @Test
    public void testReadPlane() throws Exception {
        VertexList vertexList = ColladaConverter.read(getClass().getResourceAsStream("/collada/plane1.dae")).get(0);
        Assert.assertEquals(6, vertexList.getVerticesCount());
        // Assert.assertArrayEquals(new double[]{-10.0, 10.0, 10.0, -10.0, -10.0, 10.0, 10.0, -10.0, 10.0, 10.0, 10.0, 10.0, -10.0, 10.0, 10.0, 10.0, -10.0, 10.0}, Doubles.toArray(vertexList.createPositionDoubles()), 0.0001);
        // Assert.assertArrayEquals(new double[]{0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0}, Doubles.toArray(vertexList.createNormPositionDoubles()), 0.0001);
    }

    @Test
    public void testReadStone() throws Exception {
        VertexList vertexList = ColladaConverter.read(getClass().getResourceAsStream("/collada/stone1.dae")).get(0);
        // System.out.println("vertexList: " + vertexList);
    }

    @Test
    public void testReadBush1() throws Exception {
        VertexList vertexList = ColladaConverter.read(getClass().getResourceAsStream("/collada/bush1.dae")).get(0);
        // System.out.println("vertexList: " + vertexList);
    }

    @Test
    public void testTerrain() throws Exception {
        VertexList vertexList = ColladaConverter.read(getClass().getResourceAsStream("/collada/terrain.dae")).get(0);
        List<Double> doubleListX = new ArrayList<>();
        List<Double> doubleListY = new ArrayList<>();
        List<Double> doubleListZ = new ArrayList<>();
        for (Vertex vertex : vertexList.getVertices()) {
            doubleListX.add(vertex.getX());
            doubleListY.add(vertex.getY());
            doubleListZ.add(vertex.getZ());
        }

        System.out.println("X min: " + MathHelper2.getMax(doubleListX));
        System.out.println("X max: " + MathHelper2.getMin(doubleListX));
        System.out.println("Y min: " + MathHelper2.getMax(doubleListY));
        System.out.println("Y max: " + MathHelper2.getMin(doubleListY));
        System.out.println("Z min: " + MathHelper2.getMax(doubleListZ));
        System.out.println("Z max: " + MathHelper2.getMin(doubleListZ));

        System.out.println("vertexList count: " + vertexList.getVertices().size());
        System.out.println("vertexList: " + vertexList.getVertices());
    }

    @Test
    public void testTree() throws Exception {
        VertexList vertexList = ColladaConverter.read(getClass().getResourceAsStream("/collada/tree02.dae")).get(0);

        System.out.println("vertexList count: " + vertexList.getVertices().size());
        System.out.println("Triangles: " + vertexList.getVertices().size() / 3);
        System.out.println("vertexList: " + vertexList.getVertices());
    }

    @Test
    public void testTree_03() throws Exception {
        VertexList vertexList = ColladaConverter.read(getClass().getResourceAsStream("/collada/tree03_test.dae")).get(0);

        LOGGER.severe("vertexList count: " + vertexList.getVertices().size());
        LOGGER.severe("Triangles: " + vertexList.getVertices().size() / 3);
        LOGGER.severe("vertexList: " + vertexList.getVertices());

//        for (int index = 0; index < vertexList.getTriangleCount(); index++) {
//            LOGGER.severe("Triangle " + index + " :" + vertexList.toTriangleString(index));
//        }
    }


    @Test
    public void testTree_03_03() throws Exception {
        List<VertexList> vertexLists = ColladaConverter.read(getClass().getResourceAsStream("/collada/tree03.dae"));
        // List<VertexList> vertexLists = ColladaConverter.read(getClass().getResourceAsStream("/collada/tree03_test1.dae"));

        for (VertexList vertexList : vertexLists) {
            LOGGER.severe("VertexList: " + vertexList.getName() + " --------------");
            LOGGER.severe("Count: " + vertexList.getVertices().size());
            LOGGER.severe("Triangles: " + vertexList.getVertices().size() / 3);
            // LOGGER.severe("vertexList: " + vertexList);

            for (int index = 0; index < vertexList.getTriangleCount(); index++) {
                LOGGER.severe("-Triangle " + index + " :" + toTriangleString(vertexList, index));
            }
        }
    }

    @Test
    public void testViper2_6() throws Exception {
        List<VertexList> vertexLists = ColladaConverter.read(getClass().getResourceAsStream("/collada/Viper2_6.dae"));
        // List<VertexList> vertexLists = ColladaConverter.read(getClass().getResourceAsStream("/collada/tree03_test1.dae"));

        for (VertexList vertexList : vertexLists) {
            LOGGER.severe("VertexList: " + vertexList.getName() + " --------------");
            LOGGER.severe("Count: " + vertexList.getVertices().size());
            LOGGER.severe("Triangles: " + vertexList.getVertices().size() / 3);
            // LOGGER.severe("vertexList: " + vertexList);
            List<Double> doubleListX = new ArrayList<>();
            List<Double> doubleListY = new ArrayList<>();
            List<Double> doubleListZ = new ArrayList<>();
            for (Vertex vertex : vertexList.getVertices()) {
                doubleListX.add(vertex.getX());
                doubleListY.add(vertex.getY());
                doubleListZ.add(vertex.getZ());
            }

            LOGGER.severe("X min: " + MathHelper2.getMax(doubleListX));
            LOGGER.severe("X max: " + MathHelper2.getMin(doubleListX));
            LOGGER.severe("Y min: " + MathHelper2.getMax(doubleListY));
            LOGGER.severe("Y max: " + MathHelper2.getMin(doubleListY));
            LOGGER.severe("Z min: " + MathHelper2.getMax(doubleListZ));
            LOGGER.severe("Z max: " + MathHelper2.getMin(doubleListZ));

//            for (int index = 0; index < vertexList.getTriangleCount(); index++) {
//                LOGGER.severe("-Triangle " + index + " :" + toTriangleString(vertexList, index));
//            }
        }
    }

    @Test
    public void testViperBeat1() throws Exception {
        List<VertexList> vertexLists = ColladaConverter.read(getClass().getResourceAsStream("/collada/ViperBeat1.dae"));
        // List<VertexList> vertexLists = ColladaConverter.read(getClass().getResourceAsStream("/collada/tree03_test1.dae"));

        for (VertexList vertexList : vertexLists) {
            LOGGER.severe("VertexList: " + vertexList.getName() + " --------------");
            LOGGER.severe("Count: " + vertexList.getVertices().size());
            LOGGER.severe("Triangles: " + vertexList.getVertices().size() / 3);
            LOGGER.severe("TextureCoordinates: " + vertexList.getTextureCoordinates().size() / 3);
        }
    }

    // ********************** Stupid helpers
    public static String toTriangleString(VertexList vertexList, int index) {
        List<Vertex> vertices = vertexList.getVertices();
        List<TextureCoordinate> textureCoordinates = vertexList.getTextureCoordinates();


        if(textureCoordinates.isEmpty()) {
            return "A: " + toSimpleString(vertices.get(index)) + " B: " + toSimpleString(vertices.get(index + 1)) + " C: " + toSimpleString(vertices.get(index + 2));
        } else {
            return "A: " + toSimpleString(vertices.get(index)) + " B: " + toSimpleString(vertices.get(index + 1)) + " C: " + toSimpleString(vertices.get(index + 2))
                    + " tex A: " + toSimpleString(textureCoordinates.get(index)) + " tex B: " + toSimpleString(textureCoordinates.get(index + 1)) + " tex C: " + toSimpleString(textureCoordinates.get(index + 2));
        }
    }

    public static String toSimpleString(Vertex vertex) {
        return String.format("%.2f/%.2f/%.2f", vertex.getX(), vertex.getY(), vertex.getZ());
    }

    public static String toSimpleString(TextureCoordinate textureCoordinate) {
        return String.format("%.2f/%.2f", textureCoordinate.getS(), textureCoordinate.getT());
    }

}