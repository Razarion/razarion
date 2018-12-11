package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.PlanetVisualConfig;
import com.btxtech.test.TestHelper;
import com.btxtech.uiservice.WeldUiBaseTest;
import com.btxtech.uiservice.gui.AbstractUiTestGuiRenderer;
import com.btxtech.uiservice.gui.UiTestGuiDisplay;
import javafx.scene.paint.Color;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by Beat
 * on 06.12.2018.
 */
public class ProjectionTransformationTest extends WeldUiBaseTest {

    @Test
    public void cameraTopView() {
        setupUiEnvironment(new PlanetVisualConfig().setLightDirection(new Vertex(0, 0, -1)));
        setCamera(100, 100, 0);

        // setCamera(1200, 1000, 720.0, Math.toRadians(0), Math.toRadians(0));
        // getProjectionTransformation().setFovY(Math.toRadians(45.0));
        // getProjectionTransformation().setAspectRatio(0.9982078853046595);
        Matrix4 actual = getProjectionTransformation().getMatrix();


        UiTestGuiDisplay.show(new AbstractUiTestGuiRenderer() {
            @Override
            protected void doRender() {
                strokeCurve(getProjectionTransformation().calculateViewField(0).toList(), 0.1, Color.BLACK, true);
                strokeCurve(getProjectionTransformation().calculateViewField(20).toList(), 0.1, Color.RED, true);
                strokeCurve(getProjectionTransformation().calculateViewField(-2).toList(), 0.1, Color.BLUE, true);
                drawPosition(getProjectionTransformation().calculateViewField(0).calculateCenter(), 0.1, Color.PINK);
            }
        });

        Assert.assertEquals(new DecimalPosition(902.3007042041111, 701.7662350913715), getProjectionTransformation().calculateViewField(0).calculateCenter());

        Assert.assertEquals(Arrays.asList(new DecimalPosition(902.3007042041111, 701.7662350913715), new DecimalPosition(1497.699295795889, 701.7662350913715), new DecimalPosition(1497.699295795889, 1298.2337649086285), new DecimalPosition(902.3007042041111, 1298.2337649086285)), getProjectionTransformation().calculateViewField(0).toList());
        Assert.assertEquals(Arrays.asList(new DecimalPosition(910.5701290873301, 710.0505063388334), new DecimalPosition(1489.42987091267, 710.0505063388334), new DecimalPosition(1489.42987091267, 1289.9494936611666), new DecimalPosition(910.5701290873301, 1289.9494936611666)), getProjectionTransformation().calculateViewField(20).toList());
        Assert.assertEquals(Arrays.asList(new DecimalPosition(901.4737617157891, 700.9378079666253), new DecimalPosition(1498.5262382842109, 700.9378079666253), new DecimalPosition(1498.5262382842109, 1299.0621920333747), new DecimalPosition(901.4737617157891, 1299.0621920333747)), getProjectionTransformation().calculateViewField(-2).toList());

        TestHelper.assertMatrix(Matrix4.fromField(new double[][]{{2.4185, 0.0000, 0.0000, 0.0000}, {0.0000, 2.4142, 0.0000, 0.0000}, {0.0000, 0.0000, -12.2545, -8204.5636}, {0.0000, 0.0000, -1.0000, 0.0000}}), actual, 0.0001);
    }

    @Test
    public void gameCamera() {
        setupUiEnvironment(new PlanetVisualConfig().setLightDirection(new Vertex(0, 0, -1)));
        setCamera(1104.0, -218, 720.0, Math.toRadians(40.0), Math.toRadians(0));
        getProjectionTransformation().setFovY(Math.toRadians(45.0));
        getProjectionTransformation().setAspectRatio(0.9982078853046595);

        ViewField viewField = getProjectionTransformation().calculateViewField(0);
        UiTestGuiDisplay.show(new AbstractUiTestGuiRenderer() {
            @Override
            protected void doRender() {
                strokePolygon(getProjectionTransformation().calculateViewField(0).toList(), 1, Color.BLACK, true);
                strokePolygon(getProjectionTransformation().calculateViewField(20).toList(), 1, Color.RED, true);
                strokePolygon(getProjectionTransformation().calculateViewField(-10).toList(), 1, Color.BLUE, true);
            }
        });


        Matrix4 actual = getProjectionTransformation().getMatrix();

        TestHelper.assertMatrix(Matrix4.fromField(new double[][]{{2.4185, 0.0000, 0.0000, 0.0000}, {0.0000, 2.4142, 0.0000, 0.0000}, {0.0000, 0.0000, -2.3962, -2036.4613}, {0.0000, 0.0000, -1.0000, 0.0000}}), actual, 0.0001);
    }

    @Test
    public void shallowCamera() {
        setupUiEnvironment(new PlanetVisualConfig().setLightDirection(new Vertex(0, 0, -1)));
        setCamera(1104.0, -218, 720.0, Math.toRadians(73.87096774193549), Math.toRadians(0));
        getProjectionTransformation().setFovY(Math.toRadians(45.0));
        getProjectionTransformation().setAspectRatio(0.9982078853046595);
        Matrix4 actual = getProjectionTransformation().getMatrix();

        TestHelper.assertMatrix(Matrix4.fromField(new double[][]{{2.4185, 0.0000, 0.0000, 0.0000}, {0.0000, 2.4142, 0.0000, 0.0000}, {0.0000, 0.0000, -1.0004, -1832.4801}, {0.0000, 0.0000, -1.0000, 0.0000}}), actual, 0.0001);
    }

    @Test
    public void cameraInTheSky() {
        setupUiEnvironment(new PlanetVisualConfig().setLightDirection(new Vertex(0, 0, -1)));
        setCamera(1104.0, -218, 720.0, Math.toRadians(125.16129032258061), Math.toRadians(0));
        getProjectionTransformation().setFovY(Math.toRadians(45.0));
        getProjectionTransformation().setAspectRatio(0.9982078853046595);
        Matrix4 actual = getProjectionTransformation().getMatrix();

        TestHelper.assertMatrix(Matrix4.fromField(new double[][]{{2.4185, 0.0000, 0.0000, 0.0000}, {0.0000, 2.4142, 0.0000, 0.0000}, {0.0000, 0.0000, -1.0000, -20.0000}, {0.0000, 0.0000, -1.0000, 0.0000}}), actual, 0.0001);
    }

    @Test
    public void cameraExtremelyClose() {
        setupUiEnvironment(new PlanetVisualConfig().setLightDirection(new Vertex(0, 0, -1)));
        setCamera(1027, 884, 1, Math.toRadians(81.61290322580645), Math.toRadians(0));
        getProjectionTransformation().setFovY(Math.toRadians(45.0));
        getProjectionTransformation().setAspectRatio(0.9982078853046595);
        Matrix4 actual = getProjectionTransformation().getMatrix();

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