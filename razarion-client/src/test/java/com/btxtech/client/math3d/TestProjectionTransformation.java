package com.btxtech.client.math3d;

import com.btxtech.test.TestHelper;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.dto.VisualConfig;
import com.btxtech.uiservice.VisualUiService;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.ShadowUiService;
import com.btxtech.uiservice.terrain.TerrainUiService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Beat
 * 13.04.2015.
 */
public class TestProjectionTransformation {
    private Camera camera;
    private ProjectionTransformation projectionTransformation;

    @Before
    public void before() throws Exception {
        VisualUiService visualUiService = new VisualUiService();
        TestHelper.setPrivateField(visualUiService, "visualConfig", new VisualConfig());
        camera = new Camera();
        ShadowUiService shadowUiService = new ShadowUiService();
        projectionTransformation = new ProjectionTransformation();
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
    public void gameCamera() throws Exception {
        camera.setTranslateX(1104.0);
        camera.setTranslateY(-218);
        camera.setTranslateZ(720.0);
        camera.setRotateX(Math.toRadians(40.0));
        camera.setRotateZ(Math.toRadians(0));
        projectionTransformation.setFovY(Math.toRadians(45.0));
        projectionTransformation.setAspectRatio(0.9982078853046595);
        Matrix4 actual = projectionTransformation.getMatrix();

        TestHelper.assertMatrix(Matrix4.fromField(new double[][]{{2.4185, 0.0000, 0.0000, 0.0000}, {0.0000, 2.4142, 0.0000, 0.0000}, {0.0000, 0.0000, -2.3962, -2036.4613}, {0.0000, 0.0000, -1.0000, 0.0000}}), actual, 0.0001);
    }

    @Test
    public void gameCameraTopView() throws Exception {
        camera.setTranslateX(1200);
        camera.setTranslateY(1000);
        camera.setTranslateZ(720.0);
        camera.setRotateX(Math.toRadians(0));
        camera.setRotateZ(Math.toRadians(0));
        projectionTransformation.setFovY(Math.toRadians(45.0));
        projectionTransformation.setAspectRatio(0.9982078853046595);
        Matrix4 actual = projectionTransformation.getMatrix();

        TestHelper.assertMatrix(Matrix4.fromField(new double[][]{{2.4185, 0.0000, 0.0000, 0.0000}, {0.0000, 2.4142, 0.0000, 0.0000}, {0.0000, 0.0000, -12.2545, -8204.5636}, {0.0000, 0.0000, -1.0000, 0.0000}}), actual, 0.0001);
    }

    @Test
    public void shallowCamera() throws Exception {
        camera.setTranslateX(1104.0);
        camera.setTranslateY(-218);
        camera.setTranslateZ(720.0);
        camera.setRotateX(Math.toRadians(73.87096774193549));
        camera.setRotateZ(Math.toRadians(0));
        projectionTransformation.setFovY(Math.toRadians(45.0));
        projectionTransformation.setAspectRatio(0.9982078853046595);
        Matrix4 actual = projectionTransformation.getMatrix();

        TestHelper.assertMatrix(Matrix4.fromField(new double[][]{{2.4185, 0.0000, 0.0000, 0.0000}, {0.0000, 2.4142, 0.0000, 0.0000}, {0.0000, 0.0000, -1.0004, -1832.4801}, {0.0000, 0.0000, -1.0000, 0.0000}}), actual, 0.0001);
    }

    @Test
    public void cameraInTheSky() throws Exception {
        camera.setTranslateX(1104.0);
        camera.setTranslateY(-218);
        camera.setTranslateZ(720.0);
        camera.setRotateX(Math.toRadians(125.16129032258061));
        camera.setRotateZ(Math.toRadians(0));
        projectionTransformation.setFovY(Math.toRadians(45.0));
        projectionTransformation.setAspectRatio(0.9982078853046595);
        Matrix4 actual = projectionTransformation.getMatrix();

        TestHelper.assertMatrix(Matrix4.fromField(new double[][]{{2.4185, 0.0000, 0.0000, 0.0000}, {0.0000, 2.4142, 0.0000, 0.0000}, {0.0000, 0.0000, -1.0000, -20.0000}, {0.0000, 0.0000, -1.0000, 0.0000}}), actual, 0.0001);
    }

    @Test
    public void cameraExtremelyClose() throws Exception {
        camera.setTranslateX(1027);
        camera.setTranslateY(884);
        camera.setTranslateZ(1);
        camera.setRotateX(Math.toRadians(81.61290322580645));
        camera.setRotateZ(Math.toRadians(0));
        projectionTransformation.setFovY(Math.toRadians(45.0));
        projectionTransformation.setAspectRatio(0.9982078853046595);
        Matrix4 actual = projectionTransformation.getMatrix();

        TestHelper.assertMatrix(Matrix4.fromField(new double[][]{{2.4185, 0.0000, 0.0000, 0.0000}, {0.0000, 2.4142, 0.0000, 0.0000}, {0.0000, 0.0000, -1.0000, -20.0000}, {0.0000, 0.0000, -1.0000, 0.0000}}), actual, 0.0001);
    }

    @Test
    public void makePerspective() {
        Matrix4 actual = ProjectionTransformation.makePerspectiveFrustum(Math.toRadians(45), 480.0 / 480.0, 0.1, 100.0);
        Matrix4 expected = Matrix4.fromField(new double[][]{
                {2.4142135623730954, 0.0, 0.0, 0.0},
                {0.0, 2.4142135623730954, 0.0, 0.0},
                {0.0, 0.0, -1.002002002002002, -0.20020020020020018},
                {0.0, 0.0, -1.0, 0.0}
        });
        Assert.assertEquals(expected, actual);
        actual = ProjectionTransformation.makePerspectiveFrustum(Math.toRadians(90), 640.0 / 640.0, 0.1, 100.0);
        expected = Matrix4.fromField(new double[][]{
                {1.0000000000000002, 0.0, 0.0, 0.0},
                {0.0, 1.0000000000000002, 0.0, 0.0},
                {0.0, 0.0, -1.002002002002002, -0.20020020020020018},
                {0.0, 0.0, -1.0, 0.0}
        });
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void makeBalancedFrustumToGetRight() {
        Matrix4 actual = Matrix4.makeBalancedPerspectiveFrustum(12, 56, 0.1, 100);
        Matrix4 expected = Matrix4.fromField(new double[][]{
                {0.008333333333333333, 0.0, 0.0, 0.0},
                {0.0, 0.0017857142857142859, 0.0, 0.0},
                {0.0, 0.0, -1.002002002002002, -0.20020020020020018},
                {0.0, 0.0, -1.0, 0.0}
        });
        Assert.assertEquals(expected, actual);
    }
}
