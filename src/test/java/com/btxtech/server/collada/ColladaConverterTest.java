package com.btxtech.server.collada;

import com.btxtech.client.terrain.VertexList;
import com.google.common.primitives.Doubles;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Beat
 * 14.08.2015.
 */
public class ColladaConverterTest {

    @Test
    public void testRead() throws Exception {
        VertexList vertexList = ColladaConverter.read(getClass().getResourceAsStream("/collada/plane1.dae"));
        Assert.assertEquals(6, vertexList.getVerticesCount());
        Assert.assertArrayEquals(new double[]{-10.0, 10.0, 10.0, -10.0, -10.0, 10.0, 10.0, -10.0, 10.0, 10.0, 10.0, 10.0, -10.0, 10.0, 10.0, 10.0, -10.0, 10.0}, Doubles.toArray(vertexList.createPositionDoubles()), 0.0001);
        Assert.assertArrayEquals(new double[]{0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0}, Doubles.toArray(vertexList.createNormPositionDoubles()), 0.0001);
    }
}