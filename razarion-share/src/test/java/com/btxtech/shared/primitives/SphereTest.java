package com.btxtech.shared.primitives;

import com.btxtech.shared.VertexList;
import org.junit.Assert;
import org.junit.Test;


/**
 * Created by Beat
 * 17.09.2015.
 */
public class SphereTest {

    @Test
    public void testProvideVertexList() throws Exception {
        Sphere sphere = new Sphere(100, 10, 10);
        // VertexList vertexList = sphere.provideVertexList(new ImageDescriptor("xxx", 512, 512));
        // Assert.assertEquals(540, vertexList.getVerticesCount());
    }
}