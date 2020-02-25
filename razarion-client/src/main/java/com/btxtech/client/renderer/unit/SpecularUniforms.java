package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.shared.dto.SpecularLightConfig;
import elemental2.webgl.WebGLUniformLocation;

/**
 * Created by Beat
 * 26.03.2017.
 */
public class SpecularUniforms {
    private WebGLUniformLocation uLightSpecularIntensity;
    private WebGLUniformLocation uLightSpecularHardness;

    public SpecularUniforms(String postfix, WebGlFacade webGlFacade) {
        if (postfix == null) {
            postfix = "";
        }
        uLightSpecularIntensity = webGlFacade.getUniformLocation("uLightSpecularIntensity" + postfix);
        uLightSpecularHardness = webGlFacade.getUniformLocation("uLightSpecularHardness" + postfix);
    }

    public void setUniforms(SpecularLightConfig specularLightConfig, WebGlFacade webGlFacade) {
        webGlFacade.uniform1f(uLightSpecularIntensity, specularLightConfig.getSpecularIntensity());
        webGlFacade.uniform1f(uLightSpecularHardness, specularLightConfig.getSpecularHardness());
    }


}
