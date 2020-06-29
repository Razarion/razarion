package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.shared.gameengine.datatypes.config.SlopeSplattingConfig;
import elemental2.webgl.WebGLUniformLocation;

public class WebGlSlopeSplatting extends WebGlStruct {
    public static final String UNIFORM_LOCATION_TEXTURE = "texture";
    public static final String UNIFORM_LOCATION_SCALE = "scale";
    public static final String UNIFORM_LOCATION_IMPACT = "impact";
    public static final String UNIFORM_LOCATION_BLUR = "blur";
    public static final String UNIFORM_LOCATION_OFFSET = "offset";
    private final SlopeSplattingConfig splatting;
    private WebGlUniformTexture texture;
    private WebGLUniformLocation scale;
    private WebGLUniformLocation impact;
    private WebGLUniformLocation blur;
    private WebGLUniformLocation offset;


    public WebGlSlopeSplatting(WebGlFacade webGlFacade, SlopeSplattingConfig splatting, String variableName) {
        super(webGlFacade, variableName);
        this.splatting = splatting;
        if (splatting.getTextureId() != null) {
            texture = webGlFacade.createWebGLTexture(splatting.getTextureId(), variableName(UNIFORM_LOCATION_TEXTURE));
        } else {
            texture = webGlFacade.createFakeWebGLTexture(variableName(UNIFORM_LOCATION_TEXTURE));
        }
        scale = webGlFacade.getUniformLocation(variableName(UNIFORM_LOCATION_SCALE));
        impact = webGlFacade.getUniformLocation(variableName(UNIFORM_LOCATION_IMPACT));
        blur = webGlFacade.getUniformLocation(variableName(UNIFORM_LOCATION_BLUR));
        offset = webGlFacade.getUniformLocation(variableName(UNIFORM_LOCATION_OFFSET));
    }

    public void activate() {
        texture.activate();
        getWebGlFacade().uniform1f(scale, splatting.getScale());
        getWebGlFacade().uniform1f(impact, splatting.getImpact());
        getWebGlFacade().uniform1f(blur, splatting.getBlur());
        getWebGlFacade().uniform1f(offset, splatting.getOffset());
    }
}
