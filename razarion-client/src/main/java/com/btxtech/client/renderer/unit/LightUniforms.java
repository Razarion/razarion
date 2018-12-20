package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.shared.dto.SpecularLightConfig;
import elemental.html.WebGLUniformLocation;

/**
 * Created by Beat
 * 26.03.2017.
 */
public class LightUniforms {
    private WebGLUniformLocation uLightDirection;
    private WebGLUniformLocation uLightDiffuse;
    private WebGLUniformLocation uLightAmbient;
    private WebGLUniformLocation uLightSpecularIntensity;
    private WebGLUniformLocation uLightSpecularHardness;

    public LightUniforms(String postfix, WebGlFacade webGlFacade) {
        if (postfix == null) {
            postfix = "";
        }
        uLightDirection = webGlFacade.getUniformLocation("uLightDirection" + postfix);
        uLightDiffuse = webGlFacade.getUniformLocation("uLightDiffuse" + postfix);
        uLightAmbient = webGlFacade.getUniformLocation("uLightAmbient" + postfix);
        uLightSpecularIntensity = webGlFacade.getUniformLocation("uLightSpecularIntensity" + postfix);
        uLightSpecularHardness = webGlFacade.getUniformLocation("uLightSpecularHardness" + postfix);
    }

    public void setLightUniforms(SpecularLightConfig specularLightConfig, WebGlFacade webGlFacade) {
        webGlFacade.uniform3f(uLightDirection, webGlFacade.getVisualUiService().getLightDirection());
        webGlFacade.uniform3fNoAlpha(uLightDiffuse, webGlFacade.getVisualUiService().getDiffuse());
        webGlFacade.uniform3fNoAlpha(uLightAmbient, webGlFacade.getVisualUiService().getAmbient());
        webGlFacade.uniform1f(uLightSpecularIntensity, specularLightConfig.getSpecularIntensity());
        webGlFacade.uniform1f(uLightSpecularHardness, specularLightConfig.getSpecularHardness());
    }


}
