package com.btxtech.server.collada;

import com.btxtech.client.terrain.VertexList;
import org.junit.Test;

/**
 * Created by Beat
 * 14.08.2015.
 */
public class ColladaConverterTest {

    @Test
    public void testRead() throws Exception {
        VertexList vertexList = ColladaConverter.read(getClass().getResourceAsStream("/collada/cube1.dae"));
        System.out.println(vertexList.getVerticesCount());
        System.out.println(vertexList);
    }
}