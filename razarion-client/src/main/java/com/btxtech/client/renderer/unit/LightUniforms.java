package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.shared.dto.LightConfig;
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

    public void setLightUniforms(LightConfig lightConfig, WebGlFacade webGlFacade) {
        webGlFacade.uniform3f(uLightDirection, lightConfig.setupDirection());
        webGlFacade.uniform3fNoAlpha(uLightDiffuse, lightConfig.getDiffuse());
        webGlFacade.uniform3fNoAlpha(uLightAmbient, lightConfig.getAmbient());
        webGlFacade.uniform1f(uLightSpecularIntensity, lightConfig.getSpecularIntensity());
        webGlFacade.uniform1f(uLightSpecularHardness, lightConfig.getSpecularHardness());
    }


}
