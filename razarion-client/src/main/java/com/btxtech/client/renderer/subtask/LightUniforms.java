package com.btxtech.client.renderer.subtask;

import com.btxtech.client.renderer.webgl.WebGlFacade;
import elemental2.webgl.WebGLUniformLocation;

/**
 * Created by Beat
 * on 27.01.2019.
 */
public class LightUniforms {
    private WebGLUniformLocation directLightDirection;
    private WebGLUniformLocation directLightColor;
    private WebGLUniformLocation ambientLightColor;

    public LightUniforms(WebGlFacade webGlFacade) {
        directLightDirection = webGlFacade.getUniformLocationAlarm("directLightDirection");
        directLightColor = webGlFacade.getUniformLocationAlarm("directLightColor");
        ambientLightColor = webGlFacade.getUniformLocationAlarm("ambientLightColor");
    }

    @Deprecated
    public LightUniforms(String postfix, WebGlFacade webGlFacade) {
        if (postfix == null) {
            postfix = "";
        }
        directLightDirection = webGlFacade.getUniformLocation("uLightDirection" + postfix);
        directLightColor = webGlFacade.getUniformLocation("uLightDiffuse" + postfix);
        ambientLightColor = webGlFacade.getUniformLocation("uLightAmbient" + postfix);
    }

    public void setLightUniforms(WebGlFacade webGlFacade) {
        webGlFacade.uniform3f(directLightDirection, webGlFacade.getVisualUiService().getLightDirection());
        webGlFacade.uniform3fNoAlpha(directLightColor, webGlFacade.getVisualUiService().getDiffuse());
        webGlFacade.uniform3fNoAlpha(ambientLightColor, webGlFacade.getVisualUiService().getAmbient());
    }

}
