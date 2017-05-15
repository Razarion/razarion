package com.btxtech.client.renderer;

import com.btxtech.shared.datatypes.TextureCoordinate;
import com.btxtech.shared.datatypes.Triangle;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.PlanetVisualConfig;
import com.btxtech.shared.dto.VertexList;
import com.btxtech.test.TestHelper;
import com.btxtech.uiservice.VisualUiService;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.ShadowUiService;
import com.btxtech.uiservice.terrain.TerrainUiService;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Beat
 * 20.12.2015.
 */
public class DepthSorterTest {
    private static final Triangle TRIANGLE_1 = new Triangle(new Vertex(1, 2, 0), new Vertex(5, 2, 0), new Vertex(1, 6, 0));
    private static final Triangle TRIANGLE_2 = new Triangle(new Vertex(1, 2, 1), new Vertex(5, 2, 1), new Vertex(1, 6, 1));
    private Camera camera;

    @Before
    public void before() throws Exception {
        VisualUiService visualUiService = new VisualUiService();
        TestHelper.setPrivateField(visualUiService, "planetVisualConfig", new PlanetVisualConfig());
        camera = new Camera();
        ShadowUiService shadowUiService = new ShadowUiService();
        ProjectionTransformation projectionTransformation = new ProjectionTransformation();
        TerrainUiService terrainUiService = new TerrainUiService();
        TestHelper.setPrivateField(terrainUiService, "highestPointInView", 101);
        TestHelper.setPrivateField(terrainUiService, "lowestPointInView", -9);
        TestHelper.setPrivateField(projectionTransformation, "terrainUiService", terrainUiService);
        TestHelper.setPrivateField(projectionTransformation, "camera", camera);
        TestHelper.setPrivateField(projectionTransformation, "shadowUiService", shadowUiService);
        TestHelper.setPrivateField(shadowUiService, "projectionTransformation", projectionTransformation);
        TestHelper.setPrivateField(shadowUiService, "terrainUiService", terrainUiService);
        TestHelper.setPrivateField(shadowUiService, "visualUiService", visualUiService);
        TestHelper.setPrivateField(camera, "projectionTransformation", projectionTransformation);
    }

    @Test
    public void testDepthSort1() throws Exception {
        VertexList vertexList = new VertexList();
        vertexList.add(TRIANGLE_1);
        vertexList.add(TRIANGLE_2);
        camera.setTranslateX(0);
        camera.setTranslateY(0);
        camera.setTranslateZ(2);
        camera.setRotateX(0);
        camera.setRotateZ(0);

        TestHelper.assertTriangle(TRIANGLE_1, 0, vertexList);
        TestHelper.assertTriangle(TRIANGLE_2, 1, vertexList);
        VertexList sortedVertexList = DepthSorter.depthSort(vertexList, camera.getMatrix());
        TestHelper.assertTriangle(TRIANGLE_1, 0, sortedVertexList);
        TestHelper.assertTriangle(TRIANGLE_2, 1, sortedVertexList);
    }

    @Test
    public void testDepthSort2() throws Exception {
        VertexList vertexList = new VertexList();
        vertexList.add(TRIANGLE_2);
        vertexList.add(TRIANGLE_1);
        camera.setTranslateX(0);
        camera.setTranslateY(0);
        camera.setTranslateZ(2);
        camera.setRotateX(0);
        camera.setRotateZ(0);

        TestHelper.assertTriangle(TRIANGLE_2, 0, vertexList);
        TestHelper.assertTriangle(TRIANGLE_1, 1, vertexList);
        VertexList sortedVertexList = DepthSorter.depthSort(vertexList, camera.getMatrix());
        TestHelper.assertTriangle(TRIANGLE_1, 0, sortedVertexList);
        TestHelper.assertTriangle(TRIANGLE_2, 1, sortedVertexList);
    }

    @Test
    public void testDepthSort3() throws Exception {
        VertexList vertexList = new VertexList();
        vertexList.add(TRIANGLE_1);
        vertexList.add(TRIANGLE_2);
        camera.setTranslateX(0);
        camera.setTranslateY(0);
        camera.setTranslateZ(-2);
        camera.setRotateX(Math.toDegrees(180));
        camera.setRotateZ(0);

        TestHelper.assertTriangle(TRIANGLE_1, 0, vertexList);
        TestHelper.assertTriangle(TRIANGLE_2, 1, vertexList);
        VertexList sortedVertexList = DepthSorter.depthSort(vertexList, camera.getMatrix());
        TestHelper.assertTriangle(TRIANGLE_2, 0, sortedVertexList);
        TestHelper.assertTriangle(TRIANGLE_1, 1, sortedVertexList);
    }

    @Test
    public void testDepthSort4() throws Exception {
        VertexList vertexList = new VertexList();
        vertexList.add(new Triangle(new Vertex(1, 2, 1), new TextureCoordinate(1, 0.5), new Vertex(5, 2, 1), new TextureCoordinate(1, 0.5), new Vertex(1, 6, 1), new TextureCoordinate(1, 0.5)));
        vertexList.add(new Triangle(new Vertex(1, 2, 0), new TextureCoordinate(1, 0), new Vertex(5, 2, 0), new TextureCoordinate(1, 1), new Vertex(1, 6, 0), new TextureCoordinate(0, 0)));
        camera.setTranslateX(0);
        camera.setTranslateY(0);
        camera.setTranslateZ(2);
        camera.setRotateX(0);
        camera.setRotateZ(0);

        VertexList sortedVertexList = DepthSorter.depthSort(vertexList, camera.getMatrix());

    }
}