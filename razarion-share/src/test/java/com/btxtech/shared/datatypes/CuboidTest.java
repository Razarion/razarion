package com.btxtech.shared.datatypes;

import com.btxtech.shared.dto.VertexList;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beat
 * 18.09.2015.
 */
public class CuboidTest {

    @Test
    public void testProvideVertexList() throws Exception {
        Cuboid cuboid = new Cuboid(2, 2, 2);
        VertexList vertexList = cuboid.provideVertexList();
        Assert.assertEquals(36, vertexList.getVerticesCount());
        Map<Vertex, Integer> corners = new HashMap<>();
        for (Vertex vertex : vertexList.getVertices()) {
            Integer count = corners.get(vertex);
            if (count == null) {
                corners.put(vertex, 1);
            } else {
                corners.put(vertex, count + 1);
            }
        }
        int totalCount = 0;
        for (Map.Entry<Vertex, Integer> entry : corners.entrySet()) {
            totalCount += entry.getValue();
        }
        Assert.assertEquals(8, corners.size());
        Assert.assertEquals(36, totalCount);
    }
}