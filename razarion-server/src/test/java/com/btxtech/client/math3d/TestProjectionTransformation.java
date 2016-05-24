package com.btxtech.client.math3d;

import com.btxtech.TestHelper;
import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.shared.primitives.Matrix4;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Beat
 * 13.04.2015.
 */
public class TestProjectionTransformation {

    @Test
    public void gameCamera() throws Exception {
        Camera camera = new Camera();
        ProjectionTransformation projectionTransformation = new ProjectionTransformation();
        TerrainSurface terrainSurface = new TerrainSurface();
        TestHelper.setPrivateField(terrainSurface, "highestPointInView", 101);
        TestHelper.setPrivateField(terrainSurface, "lowestPointInView", -9);
        TestHelper.setPrivateField(projectionTransformation, "terrainSurface", terrainSurface);
        TestHelper.setPrivateField(projectionTransformation, "camera", camera);

        camera.setTranslateX(1104.0);
        camera.setTranslateY(-218);
        camera.setTranslateZ(720.0);
        camera.setRotateX(Math.toRadians(40.0));
        camera.setRotateZ(Math.toRadians(0));
        projectionTransformation.setFovY(Math.toRadians(45.0));
        projectionTransformation.setAspectRatio(0.9982078853046595);
        Matrix4 actual = projectionTransformation.createMatrix();

        TestHelper.assertMatrix(new Matrix4(new double[][]{{2.4185, 0.0000, 0.0000, 0.0000}, {0.0000, 2.4142, 0.0000, 0.0000}, {0.0000, 0.0000, -2.3962, -2036.4613}, {0.0000, 0.0000, -1.0000, 0.0000}}), actual, 0.0001);
    }

    @Test
    public void gameCameraTopView() throws Exception {
        Camera camera = new Camera();
        ProjectionTransformation projectionTransformation = new ProjectionTransformation();
        TerrainSurface terrainSurface = new TerrainSurface();
        TestHelper.setPrivateField(terrainSurface, "highestPointInView", 101);
        TestHelper.setPrivateField(terrainSurface, "lowestPointInView", -9);
        TestHelper.setPrivateField(projectionTransformation, "terrainSurface", terrainSurface);
        TestHelper.setPrivateField(projectionTransformation, "camera", camera);

        camera.setTranslateX(1200);
        camera.setTranslateY(1000);
        camera.setTranslateZ(720.0);
        camera.setRotateX(Math.toRadians(0));
        camera.setRotateZ(Math.toRadians(0));
        projectionTransformation.setFovY(Math.toRadians(45.0));
        projectionTransformation.setAspectRatio(0.9982078853046595);
        Matrix4 actual = projectionTransformation.createMatrix();

        TestHelper.assertMatrix(new Matrix4(new double[][]{{2.4185, 0.0000, 0.0000, 0.0000}, {0.0000, 2.4142, 0.0000, 0.0000}, {0.0000, 0.0000, -12.2545, -8204.5636}, {0.0000, 0.0000, -1.0000, 0.0000}}), actual, 0.0001);
    }

    @Test
    public void shallowCamera() throws Exception {
        Camera camera = new Camera();
        ProjectionTransformation projectionTransformation = new ProjectionTransformation();
        TerrainSurface terrainSurface = new TerrainSurface();
        TestHelper.setPrivateField(terrainSurface, "highestPointInView", 101);
        TestHelper.setPrivateField(terrainSurface, "lowestPointInView", -9);
        TestHelper.setPrivateField(projectionTransformation, "terrainSurface", terrainSurface);
        TestHelper.setPrivateField(projectionTransformation, "camera", camera);

        camera.setTranslateX(1104.0);
        camera.setTranslateY(-218);
        camera.setTranslateZ(720.0);
        camera.setRotateX(Math.toRadians(73.87096774193549));
        camera.setRotateZ(Math.toRadians(0));
        projectionTransformation.setFovY(Math.toRadians(45.0));
        projectionTransformation.setAspectRatio(0.9982078853046595);
        Matrix4 actual = projectionTransformation.createMatrix();

        TestHelper.assertMatrix(new Matrix4(new double[][]{{2.4185, 0.0000, 0.0000, 0.0000}, {0.0000, 2.4142, 0.0000, 0.0000}, {0.0000, 0.0000, -1.0004, -1832.4801}, {0.0000, 0.0000, -1.0000, 0.0000}}), actual, 0.0001);
    }

    @Test
    public void cameraInTheSky() throws Exception {
        Camera camera = new Camera();
        ProjectionTransformation projectionTransformation = new ProjectionTransformation();
        TerrainSurface terrainSurface = new TerrainSurface();
        TestHelper.setPrivateField(terrainSurface, "highestPointInView", 101);
        TestHelper.setPrivateField(terrainSurface, "lowestPointInView", -9);
        TestHelper.setPrivateField(projectionTransformation, "terrainSurface", terrainSurface);
        TestHelper.setPrivateField(projectionTransformation, "camera", camera);

        camera.setTranslateX(1104.0);
        camera.setTranslateY(-218);
        camera.setTranslateZ(720.0);
        camera.setRotateX(Math.toRadians(125.16129032258061));
        camera.setRotateZ(Math.toRadians(0));
        projectionTransformation.setFovY(Math.toRadians(45.0));
        projectionTransformation.setAspectRatio(0.9982078853046595);
        Matrix4 actual = projectionTransformation.createMatrix();

        TestHelper.assertMatrix(new Matrix4(new double[][]{{2.4185, 0.0000, 0.0000, 0.0000}, {0.0000, 2.4142, 0.0000, 0.0000}, {0.0000, 0.0000, -1.0000, -20.0000}, {0.0000, 0.0000, -1.0000, 0.0000}}), actual, 0.0001);
    }

    @Test
    public void cameraExtremelyClose() throws Exception {
        Camera camera = new Camera();
        ProjectionTransformation projectionTransformation = new ProjectionTransformation();
        TerrainSurface terrainSurface = new TerrainSurface();
        TestHelper.setPrivateField(terrainSurface, "highestPointInView", 101);
        TestHelper.setPrivateField(terrainSurface, "lowestPointInView", -9);
        TestHelper.setPrivateField(projectionTransformation, "terrainSurface", terrainSurface);
        TestHelper.setPrivateField(projectionTransformation, "camera", camera);

        camera.setTranslateX(1027);
        camera.setTranslateY(884);
        camera.setTranslateZ(1);
        camera.setRotateX(Math.toRadians(81.61290322580645));
        camera.setRotateZ(Math.toRadians(0));
        projectionTransformation.setFovY(Math.toRadians(45.0));
        projectionTransformation.setAspectRatio(0.9982078853046595);
        Matrix4 actual = projectionTransformation.createMatrix();

        TestHelper.assertMatrix(new Matrix4(new double[][]{{2.4185, 0.0000, 0.0000, 0.0000}, {0.0000, 2.4142, 0.0000, 0.0000}, {0.0000, 0.0000, -1.0000, -20.0000}, {0.0000, 0.0000, -1.0000, 0.0000}}), actual, 0.0001);
    }

     @Test
    public void makePerspective() {
        Matrix4 actual = ProjectionTransformation.makePerspectiveFrustum(Math.toRadians(45), 480.0 / 480.0, 0.1, 100.0);
        Matrix4 expected = new Matrix4(new double[][]{
                {2.4142135623730954, 0.0, 0.0, 0.0},
                {0.0, 2.4142135623730954, 0.0, 0.0},
                {0.0, 0.0, -1.002002002002002, -0.20020020020020018},
                {0.0, 0.0, -1.0, 0.0}
        });
        Assert.assertEquals(expected, actual);
        actual = ProjectionTransformation.makePerspectiveFrustum(Math.toRadians(90), 640.0 / 640.0, 0.1, 100.0);
        expected = new Matrix4(new double[][]{
                {1.0000000000000002, 0.0, 0.0, 0.0},
                {0.0, 1.0000000000000002, 0.0, 0.0},
                {0.0, 0.0, -1.002002002002002, -0.20020020020020018},
                {0.0, 0.0, -1.0, 0.0}
        });
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void makeBalancedFrustumToGetRight() {
        Matrix4 actual = ProjectionTransformation.makeBalancedPerspectiveFrustum(12, 56, 0.1, 100);
        Matrix4 expected = new Matrix4(new double[][]{
                {0.008333333333333333, 0.0, 0.0, 0.0},
                {0.0, 0.0017857142857142859, 0.0, 0.0},
                {0.0, 0.0, -1.002002002002002, -0.20020020020020018},
                {0.0, 0.0, -1.0, 0.0}
        });
        Assert.assertEquals(expected, actual);
    }
}
