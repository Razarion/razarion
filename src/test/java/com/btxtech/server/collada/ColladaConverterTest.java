package com.btxtech.server.collada;

import com.btxtech.shared.MathHelper2;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.Vertex;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 14.08.2015.
 */
public class ColladaConverterTest {

    @Test
    public void testReadPlane() throws Exception {
        VertexList vertexList = ColladaConverter.read(getClass().getResourceAsStream("/collada/plane1.dae"));
        Assert.assertEquals(6, vertexList.getVerticesCount());
        // Assert.assertArrayEquals(new double[]{-10.0, 10.0, 10.0, -10.0, -10.0, 10.0, 10.0, -10.0, 10.0, 10.0, 10.0, 10.0, -10.0, 10.0, 10.0, 10.0, -10.0, 10.0}, Doubles.toArray(vertexList.createPositionDoubles()), 0.0001);
        // Assert.assertArrayEquals(new double[]{0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0}, Doubles.toArray(vertexList.createNormPositionDoubles()), 0.0001);
    }

    @Test
    public void testReadStone() throws Exception {
        VertexList vertexList = ColladaConverter.read(getClass().getResourceAsStream("/collada/stone1.dae"));
        // System.out.println("vertexList: " + vertexList);
    }

    @Test
    public void testReadBush1() throws Exception {
        VertexList vertexList = ColladaConverter.read(getClass().getResourceAsStream("/collada/bush1.dae"));
        // System.out.println("vertexList: " + vertexList);
    }

    @Test
    public void testTerrain() throws Exception {
        VertexList vertexList = ColladaConverter.read(getClass().getResourceAsStream("/collada/terrain.dae"));
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

}