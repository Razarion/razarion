package com.btxtech.client.renderer;

import com.btxtech.TestHelper;
import com.btxtech.client.renderer.model.Camera;
import com.btxtech.shared.VertexList;
import com.btxtech.shared.primitives.TextureCoordinate;
import com.btxtech.shared.primitives.Triangle;
import com.btxtech.shared.primitives.Vertex;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Beat
 * 20.12.2015.
 */
public class DepthSorterTest {
    private static final Triangle TRIANGLE_1 = new Triangle(new Vertex(1, 2, 0), new Vertex(5, 2, 0), new Vertex(1, 6, 0));
    private static final Triangle TRIANGLE_2 = new Triangle(new Vertex(1, 2, 1), new Vertex(5, 2, 1), new Vertex(1, 6, 1));

    @Test
    public void testDepthSort1() throws Exception {
        VertexList vertexList = new VertexList();
        vertexList.add(TRIANGLE_1);
        vertexList.add(TRIANGLE_2);
        Camera camera = new Camera();
        camera.setTranslateX(0);
        camera.setTranslateY(0);
        camera.setTranslateZ(2);
        camera.setRotateX(0);
        camera.setRotateZ(0);

        TestHelper.assertTriangle(TRIANGLE_1, 0, vertexList);
        TestHelper.assertTriangle(TRIANGLE_2, 1, vertexList);
        VertexList sortedVertexList = DepthSorter.depthSort(vertexList, camera.createMatrix());
        TestHelper.assertTriangle(TRIANGLE_1, 0, sortedVertexList);
        TestHelper.assertTriangle(TRIANGLE_2, 1, sortedVertexList);
    }

    @Test
    public void testDepthSort2() throws Exception {
        VertexList vertexList = new VertexList();
        vertexList.add(TRIANGLE_2);
        vertexList.add(TRIANGLE_1);
        Camera camera = new Camera();
        camera.setTranslateX(0);
        camera.setTranslateY(0);
        camera.setTranslateZ(2);
        camera.setRotateX(0);
        camera.setRotateZ(0);

        TestHelper.assertTriangle(TRIANGLE_2, 0, vertexList);
        TestHelper.assertTriangle(TRIANGLE_1, 1, vertexList);
        VertexList sortedVertexList = DepthSorter.depthSort(vertexList, camera.createMatrix());
        TestHelper.assertTriangle(TRIANGLE_1, 0, sortedVertexList);
        TestHelper.assertTriangle(TRIANGLE_2, 1, sortedVertexList);
    }

    @Test
    public void testDepthSort3() throws Exception {
        VertexList vertexList = new VertexList();
        vertexList.add(TRIANGLE_1);
        vertexList.add(TRIANGLE_2);
        Camera camera = new Camera();
        camera.setTranslateX(0);
        camera.setTranslateY(0);
        camera.setTranslateZ(-2);
        camera.setRotateX(Math.toDegrees(180));
        camera.setRotateZ(0);

        TestHelper.assertTriangle(TRIANGLE_1, 0, vertexList);
        TestHelper.assertTriangle(TRIANGLE_2, 1, vertexList);
        VertexList sortedVertexList = DepthSorter.depthSort(vertexList, camera.createMatrix());
        TestHelper.assertTriangle(TRIANGLE_2, 0, sortedVertexList);
        TestHelper.assertTriangle(TRIANGLE_1, 1, sortedVertexList);
    }

    @Test
    public void testDepthSort4() throws Exception {
        VertexList vertexList = new VertexList();
        vertexList.add(new Triangle(new Vertex(1, 2, 1), new TextureCoordinate(1, 0.5), new Vertex(5, 2, 1), new TextureCoordinate(1, 0.5), new Vertex(1, 6, 1), new TextureCoordinate(1, 0.5)));
        vertexList.add(new Triangle(new Vertex(1, 2, 0), new TextureCoordinate(1, 0), new Vertex(5, 2, 0), new TextureCoordinate(1, 1), new Vertex(1, 6, 0), new TextureCoordinate(0, 0)));
        Camera camera = new Camera();
        camera.setTranslateX(0);
        camera.setTranslateY(0);
        camera.setTranslateZ(2);
        camera.setRotateX(0);
        camera.setRotateZ(0);

        VertexList sortedVertexList = DepthSorter.depthSort(vertexList, camera.createMatrix());

    }
}