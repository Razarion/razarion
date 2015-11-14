package com.btxtech.client.renderer.model;

import com.btxtech.TestHelper;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.shared.primitives.Triangle;
import com.btxtech.shared.primitives.Vertex;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Beat
 * 01.11.2015.
 */
public class MeshTest {

    @Test
    public void testAdjustNormFlat() throws Exception {
        Mesh mesh = new Mesh();
        mesh.fill(200, 200, 100);
        mesh.generateAllTriangle();
        mesh.adjustNorm();

        mesh.iterateOverTriangles(new Mesh.TriangleVisitor() {
            @Override
            public void onVisit(Index bottomLeftIndex, Vertex bottomLeftVertex, Triangle triangle1, Triangle triangle2) {
                Assert.assertEquals(new Vertex(0, 0, 1), triangle1.getVertexNormA());
                Assert.assertEquals(new Vertex(0, 0, 1), triangle1.getVertexNormB());
                Assert.assertEquals(new Vertex(0, 0, 1), triangle1.getVertexNormC());
                Assert.assertEquals(new Vertex(1, 0, 0), triangle1.getVertexTangentA());
                Assert.assertEquals(new Vertex(1, 0, 0), triangle1.getVertexTangentB());
                Assert.assertEquals(new Vertex(1, 0, 0), triangle1.getVertexTangentC());

                Assert.assertEquals(new Vertex(0, 0, 1), triangle2.getVertexNormA());
                Assert.assertEquals(new Vertex(0, 0, 1), triangle2.getVertexNormB());
                Assert.assertEquals(new Vertex(0, 0, 1), triangle2.getVertexNormC());
                Assert.assertEquals(new Vertex(1, 0, 0), triangle2.getVertexTangentA());
                Assert.assertEquals(new Vertex(1, 0, 0), triangle2.getVertexTangentB());
                Assert.assertEquals(new Vertex(1, 0, 0), triangle2.getVertexTangentC());
            }
        });

    }

    @Test
    public void testAdjustNormSingleSlope() throws Exception {
        Mesh mesh = new Mesh();
        mesh.fill(200, 200, 100);
        mesh.getVertexDataSafe(new Index(1, 1)).addZValue(100);
        mesh.generateAllTriangle();
        mesh.adjustNorm();

        // Not complete
//        TestHelper.assertVertex(0, 0, 1, mesh.getVertexDataSafe(new Index(0, 0)).getTriangle1().getVertexNormA());
//        TestHelper.assertVertex(0, 0, 1, mesh.getVertexDataSafe(new Index(0, 0)).getTriangle1().getVertexNormB());
//        TestHelper.assertVertex(0, 0, 1, mesh.getVertexDataSafe(new Index(0, 0)).getTriangle1().getVertexNormC());


        // Submit
        TestHelper.assertVertex(0, 0, 1, mesh.getVertexDataSafe(new Index(0, 0)).getTriangle2().getVertexNormB());
        TestHelper.assertVertex(1, 0, 0, mesh.getVertexDataSafe(new Index(0, 0)).getTriangle2().getVertexTangentB());
        TestHelper.assertVertex(0, 0, 1, mesh.getVertexDataSafe(new Index(1, 0)).getTriangle1().getVertexNormC());
        TestHelper.assertVertex(1, 0, 0, mesh.getVertexDataSafe(new Index(1, 0)).getTriangle1().getVertexTangentC());
        TestHelper.assertVertex(0, 0, 1, mesh.getVertexDataSafe(new Index(1, 0)).getTriangle2().getVertexNormC());
        TestHelper.assertVertex(1, 0, 0, mesh.getVertexDataSafe(new Index(1, 0)).getTriangle2().getVertexTangentC());
        TestHelper.assertVertex(0, 0, 1, mesh.getVertexDataSafe(new Index(0, 1)).getTriangle1().getVertexNormB());
        TestHelper.assertVertex(1, 0, 0, mesh.getVertexDataSafe(new Index(0, 1)).getTriangle1().getVertexTangentB());
        TestHelper.assertVertex(0, 0, 1, mesh.getVertexDataSafe(new Index(0, 1)).getTriangle2().getVertexNormA());
        TestHelper.assertVertex(1, 0, 0, mesh.getVertexDataSafe(new Index(0, 1)).getTriangle2().getVertexTangentA());
        TestHelper.assertVertex(0, 0, 1, mesh.getVertexDataSafe(new Index(1, 1)).getTriangle1().getVertexNormA());
        TestHelper.assertVertex(1, 0, 0, mesh.getVertexDataSafe(new Index(1, 1)).getTriangle1().getVertexTangentA());


    }

}