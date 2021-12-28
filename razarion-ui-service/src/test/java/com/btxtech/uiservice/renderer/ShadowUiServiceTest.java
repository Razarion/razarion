package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.Vertex4;
import com.btxtech.shared.dto.PlanetVisualConfig;
import com.btxtech.uiservice.AssertHelper;
import com.btxtech.uiservice.WeldUiBaseIntegrationTest;
import org.junit.Test;

/**
 * Created by Beat
 * on 29.11.2018.
 */
public class ShadowUiServiceTest extends WeldUiBaseIntegrationTest {

    @Test
    public void testPerpendicular() {
        setupUiEnvironment(new PlanetVisualConfig().setLightDirection(new Vertex(0, 0, -1)));
        setCamera(100, 100, 0);
        ShadowUiService shadowUiService = getWeldBean(ShadowUiService.class);
        shadowUiService.setupMatrices();

        // View field center
        AssertHelper.assertVertex(0, 0, 1, runDepthBufferShaderNdc(new Vertex(100, 100, 0)));
        // View field center z = 20
        AssertHelper.assertVertex(0, 0, -1, runDepthBufferShaderNdc(new Vertex(100, 100, 20)));
        // View field center z = -2
        // AssertHelper.assertVertex(0, 0, 4, runDepthBufferShaderNdc(new Vertex(100, 100, -2)));
        // View field BL
        AssertHelper.assertVertex(-1, -1, 1, runDepthBufferShaderNdc(new Vertex(55.81722001353653, 66.86291501015239, 0)));
        // View field BR
        AssertHelper.assertVertex(1, -1, 1, runDepthBufferShaderNdc(new Vertex(144.18277998646346, 66.86291501015239, 0)));
        // View field TR
        AssertHelper.assertVertex(1, 1, 1, runDepthBufferShaderNdc(new Vertex(144.18277998646346, 133.1370849898476, 0)));
        // View field TL
        AssertHelper.assertVertex(-1, 1, 1, runDepthBufferShaderNdc(new Vertex(55.81722001353653, 133.1370849898476, 0)));
    }

    @Test
    public void testLightX45Deg() {
        Vertex lightDirection = Matrix4.createXRotation(Math.toRadians(45)).multiply(new Vertex(0, 0, -1), 1);
        setupUiEnvironment(new PlanetVisualConfig().setLightDirection(lightDirection));
        setCamera(100, 100, 0);
        ShadowUiService shadowUiService = getWeldBean(ShadowUiService.class);
        shadowUiService.setupMatrices();

        // View field center
        AssertHelper.assertVertex(0, 0, 0.3763, runDepthBufferShaderNdc(new Vertex(100, 100, 0)));
        // View field center z = 20
        // AssertHelper.assertVertex(0, 0, -40, runDepthBufferShaderNdc(new Vertex(100, 100, 0.6035)));
        // View field center z = -2
        // AssertHelper.assertVertex(0, 0, 4, runDepthBufferShaderNdc(new Vertex(100, 100, 0.0603)));
        // View field BL
        AssertHelper.assertVertex(-1, -1, -0.2472, runDepthBufferShaderNdc(new Vertex(55.81722001353653, 66.86291501015239, 0)));
        // View field BR
        AssertHelper.assertVertex(1, -1, -0.2472, runDepthBufferShaderNdc(new Vertex(144.18277998646346, 66.86291501015239, 0)));
        // View field TR
        AssertHelper.assertVertex(1, 1, 1, runDepthBufferShaderNdc(new Vertex(144.18277998646346, 133.1370849898476, 0)));
        // View field TL
        AssertHelper.assertVertex(-1, 1, 1, runDepthBufferShaderNdc(new Vertex(55.81722001353653, 133.1370849898476, 0)));
    }

    @Test
    public void testLightY45Deg() {
        Vertex lightDirection = Matrix4.createYRotation(Math.toRadians(45)).multiply(new Vertex(0, 0, -1), 1);
        setupUiEnvironment(new PlanetVisualConfig().setLightDirection(lightDirection));
        setCamera(100, 100, 0);
        ShadowUiService shadowUiService = getWeldBean(ShadowUiService.class);
        shadowUiService.setupMatrices();

        // View field center
        AssertHelper.assertVertex(0, 0, 0.3116, runDepthBufferShaderNdc(new Vertex(100, 100, 0)));
        // View field center z = 20
        // AssertHelper.assertVertex(0, 0, -40, runDepthBufferShaderNdc(new Vertex(100, 100, 0.6035)));
        // View field center z = -2
        // AssertHelper.assertVertex(0, 0, 4, runDepthBufferShaderNdc(new Vertex(100, 100, 0.0603)));
        // View field BL
        AssertHelper.assertVertex(-1, -1, 1, runDepthBufferShaderNdc(new Vertex(55.81722001353653, 66.86291501015239, 0)));
        // View field BR
        AssertHelper.assertVertex(1, -1, -0.3767, runDepthBufferShaderNdc(new Vertex(144.18277998646346, 66.86291501015239, 0)));
        // View field TR
        AssertHelper.assertVertex(1, 1, -0.3767, runDepthBufferShaderNdc(new Vertex(144.18277998646346, 133.1370849898476, 0)));
        // View field TL
        AssertHelper.assertVertex(-1, 1, 1, runDepthBufferShaderNdc(new Vertex(55.81722001353653, 133.1370849898476, 0)));
    }

    @Test
    public void testLightXY45Deg() {
        Vertex lightDirection = Matrix4.createXRotation(Math.toRadians(45)).multiply(Matrix4.createYRotation(Math.toRadians(45))).multiply(new Vertex(0, 0, -1), 1);
        setupUiEnvironment(new PlanetVisualConfig().setLightDirection(lightDirection));
        setCamera(100, 100, 0);
        ShadowUiService shadowUiService = getWeldBean(ShadowUiService.class);
        shadowUiService.setupMatrices();

//        ///////////////
//        System.out.println(runDepthBufferShaderNdc(new Vertex(100, 100, 0)));
//        // System.out.println(runDepthBufferShaderNdc(new Vertex(100, 100, 20)));
//        // System.out.println(runDepthBufferShaderNdc(new Vertex(100, 100, -2)));
//        // View filed z = 0
//        System.out.println(runDepthBufferShaderNdc(new Vertex(55.81722001353653, 66.86291501015239, 0)));
//        System.out.println(runDepthBufferShaderNdc(new Vertex(144.18277998646346, 66.86291501015239, 0)));
//        System.out.println(runDepthBufferShaderNdc(new Vertex(144.18277998646346, 133.1370849898476, 0)));
//        System.out.println(runDepthBufferShaderNdc(new Vertex(55.81722001353653, 133.1370849898476, 0)));
//
//        UiTestGuiDisplay.show(new AbstractUiTestGuiRenderer() {
//            @Override
//            protected void doRender() {
//                strokePolygon(getProjectionTransformation().calculateViewField(0).toList(), 0.1, Color.BLACK, true);
//                strokePolygon(getProjectionTransformation().calculateViewField(getTerrainUiService().getHighestPointInView()).toList(), 0.1, Color.RED, true);
//                strokePolygon(getProjectionTransformation().calculateViewField(getTerrainUiService().getLowestPointInView()).toList(), 0.1, Color.BLUE, true);
//                drawPosition(getProjectionTransformation().calculateViewField(0).calculateCenter(), 0.1, Color.PINK);
//            }
//        });
//        ///////////////

        // View field center
        AssertHelper.assertVertex(0, 0, 0.2686, runDepthBufferShaderNdc(new Vertex(100, 100, 0)));
        // View field center z = 20
        // AssertHelper.assertVertex(0, 0, -40, runDepthBufferShaderNdc(new Vertex(100, 100, 0.6035)));
        // View field center z = -2
        // AssertHelper.assertVertex(0, 0, 4, runDepthBufferShaderNdc(new Vertex(100, 100, 0.0603)));
        // View field BL
        AssertHelper.assertVertex(-1, -1, 0.4657, runDepthBufferShaderNdc(new Vertex(55.81722001353653, 66.86291501015239, 0)));
        // View field BR
        AssertHelper.assertVertex(0.5808, -0.4523, -0.3735, runDepthBufferShaderNdc(new Vertex(144.18277998646346, 66.86291501015239, 0)));
        // View field TR
        AssertHelper.assertVertex(1, 1, 0.07155, runDepthBufferShaderNdc(new Vertex(144.18277998646346, 133.1370849898476, 0)));
        // View field TL
        AssertHelper.assertVertex(-0.5808, 0.4523, 0.9109, runDepthBufferShaderNdc(new Vertex(55.81722001353653, 133.1370849898476, 0)));
    }

    private Vertex runDepthBufferShaderNdc(Vertex vertex) {
        Matrix4 matrix4 = getWeldBean(ShadowUiService.class).getDepthProjectionTransformation().multiply(getWeldBean(ShadowUiService.class).getDepthViewTransformation());
        Vertex4 result = new Vertex4(matrix4.multiply(vertex, 1.0), matrix4.multiplyW(vertex, 1.0));
        return toNdcVertex(result);
    }
}