package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.Vertex4;
import com.btxtech.shared.dto.PlanetVisualConfig;
import com.btxtech.uiservice.WeldUiBaseTest;
import com.btxtech.uiservice.gui.AbstractUiTestGuiRenderer;
import com.btxtech.uiservice.gui.UiTestGuiDisplay;
import javafx.scene.paint.Color;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Beat
 * on 29.11.2018.
 */
public class ShadowUiServiceTest extends WeldUiBaseTest {

    @Test
    public void testPerpendicular() {
        setupUiEnvironment(new PlanetVisualConfig().setLightDirection(new Vertex(0, 0, -1)));
        setCamera(100, 100, 0);
        ShadowUiService shadowUiService = getWeldBean(ShadowUiService.class);
        shadowUiService.setupMatrices();

///////////////
        System.out.println(runDepthBufferShaderNdc(new Vertex(100, 100, 0)));
        System.out.println(runDepthBufferShaderNdc(new Vertex(100, 100, 20)));
        System.out.println(runDepthBufferShaderNdc(new Vertex(100, 100, -2)));
        // View filed z = 0
        System.out.println(runDepthBufferShaderNdc(new Vertex(55.81722001353653, 66.86291501015239, 0)));
        System.out.println(runDepthBufferShaderNdc(new Vertex(144.18277998646346, 66.86291501015239, 0)));
        System.out.println(runDepthBufferShaderNdc(new Vertex(144.18277998646346, 133.1370849898476, 0)));
        System.out.println(runDepthBufferShaderNdc(new Vertex(55.81722001353653, 133.1370849898476, 0)));
        // BL View filed z = 0
        System.out.println(runDepthBufferShaderNdc(new Vertex(54.712650513874934, 66.03448788540621, 0)));
//////////////

        System.out.println(getProjectionTransformation().calculateViewField(-2).toList());

        UiTestGuiDisplay.show(new AbstractUiTestGuiRenderer() {
            @Override
            protected void doRender() {
                strokePolygon(getProjectionTransformation().calculateViewField(0).toList(), 0.1, Color.BLACK, true);
                strokePolygon(getProjectionTransformation().calculateViewField(getTerrainUiService().getHighestPointInView()).toList(), 0.1, Color.RED, true);
                strokePolygon(getProjectionTransformation().calculateViewField(getTerrainUiService().getLowestPointInView()).toList(), 0.1, Color.BLUE, true);
                drawPosition(getProjectionTransformation().calculateViewField(0).calculateCenter(), 0.1, Color.PINK);
            }
        });


        Assert.fail("... VERIFY ...");
    }

    @Test
    public void testLight60Deg() {
        Vertex lightDirection = Matrix4.createXRotation(Math.toRadians(60)).multiply(new Vertex(0, 0, -1), 1);
        setupUiEnvironment(new PlanetVisualConfig().setLightDirection(lightDirection));
        setCamera(100, 100, 0);
        ShadowUiService shadowUiService = getWeldBean(ShadowUiService.class);
        shadowUiService.setupMatrices();

        UiTestGuiDisplay.show(new AbstractUiTestGuiRenderer() {
            @Override
            protected void doRender() {
                strokePolygon(getProjectionTransformation().calculateViewField(0).toList(), 0.1, Color.BLACK, true);
                strokePolygon(getProjectionTransformation().calculateViewField(getTerrainUiService().getHighestPointInView()).toList(), 0.1, Color.RED, true);
                strokePolygon(getProjectionTransformation().calculateViewField(getTerrainUiService().getLowestPointInView()).toList(), 0.1, Color.BLUE, true);
                drawPosition(getProjectionTransformation().calculateViewField(0).calculateCenter(), 0.1, Color.PINK);
            }
        });


        Assert.fail("... TODO ...");
    }

    @Test
    public void testXRot60Deg() {
        Vertex lightDirection = Matrix4.createXRotation(Math.toRadians(60)).multiply(new Vertex(0, 0, -1), 1);

        setupUiEnvironment(new PlanetVisualConfig().setLightDirection(lightDirection));
        setCamera(100, 100, 80, Math.toRadians(30), 0);
        ShadowUiService shadowUiService = getWeldBean(ShadowUiService.class);
        shadowUiService.setupMatrices();

        Assert.fail("... TODO ...");
    }

    @Test
    public void testSpecial() {
        Vertex lightDirection = new Vertex(0.025, 0.0, -0.975).normalize(1);

        setupUiEnvironment(new PlanetVisualConfig().setLightDirection(lightDirection));
        ShadowUiService shadowUiService = getWeldBean(ShadowUiService.class);
        shadowUiService.setupMatrices();

        Assert.fail("... TODO ...");
    }

    private Vertex runDepthBufferShaderNdc(Vertex vertex) {
        Matrix4 matrix4 = getWeldBean(ShadowUiService.class).getDepthProjectionTransformation().multiply(getWeldBean(ShadowUiService.class).getDepthViewTransformation());
        Vertex4 result = new Vertex4(matrix4.multiply(vertex, 1.0), matrix4.multiplyW(vertex, 1.0));
        return toNdcVertex(result);
    }
}