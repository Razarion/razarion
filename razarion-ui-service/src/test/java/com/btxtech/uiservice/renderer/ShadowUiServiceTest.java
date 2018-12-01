package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.PlanetVisualConfig;
import com.btxtech.uiservice.WeldUiBaseTest;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Beat
 * on 29.11.2018.
 */
public class ShadowUiServiceTest extends WeldUiBaseTest {

    @Test
    public void testLightDirectPerpendicular() {
        setupUiEnvironment(new PlanetVisualConfig().setLightDirection(new Vertex(0, 0, -1)));
        ShadowUiService shadowUiService = getWeldBean(ShadowUiService.class);
        shadowUiService.setupMatrices();

        Assert.fail("... TODO ...");
    }

    @Test
    public void testXRot60Dec() {
        Vertex lightDirection = Matrix4.createXRotation(Math.toRadians(60)).multiply(new Vertex(0, 0, -1), 1);

        setupUiEnvironment(new PlanetVisualConfig().setLightDirection(lightDirection));
        ShadowUiService shadowUiService = getWeldBean(ShadowUiService.class);
        shadowUiService.setupMatrices();

        Assert.fail("... TODO ...");
    }

    @Test
    public void testSpecial() {
        Vertex lightDirection = new Vertex(0.025, 0.0, -0.975);

        setupUiEnvironment(new PlanetVisualConfig().setLightDirection(lightDirection));
        ShadowUiService shadowUiService = getWeldBean(ShadowUiService.class);
        shadowUiService.setupMatrices();

        Assert.fail("... TODO ...");
    }
}