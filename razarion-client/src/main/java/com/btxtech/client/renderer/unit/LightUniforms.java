package com.btxtech.client.renderer.unit;

import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.shared.dto.SpecularLightConfig;
import elemental2.webgl.WebGLUniformLocation;

/**
 * Created by Beat
 * on 27.01.2019.
 */
public class LightUniforms {
    private WebGLUniformLocation uLightDirection;
    private WebGLUniformLocation uLightDiffuse;
    private WebGLUniformLocation uLightAmbient;

    public LightUniforms(String postfix, WebGlFacade webGlFacade) {
        if (postfix == null) {
            postfix = "";
        }
        uLightDirection = webGlFacade.getUniformLocation("uLightDirection" + postfix);
        uLightDiffuse = webGlFacade.getUniformLocation("uLightDiffuse" + postfix);
        uLightAmbient = webGlFacade.getUniformLocation("uLightAmbient" + postfix);
    }

    public void setLightUniforms(WebGlFacade webGlFacade) {
        webGlFacade.uniform3f(uLightDirection, webGlFacade.getVisualUiService().getLightDirection());
        webGlFacade.uniform3fNoAlpha(uLightDiffuse, webGlFacade.getVisualUiService().getDiffuse());
        webGlFacade.uniform3fNoAlpha(uLightAmbient, webGlFacade.getVisualUiService().getAmbient());
    }

}
