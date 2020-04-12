package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.shared.dto.GroundSplattingConfig;
import elemental2.webgl.WebGLUniformLocation;

public class WebGlSplatting extends WebGlStruct {
    public static final String UNIFORM_LOCATION_IMAGE = "texture";
    public static final String UNIFORM_LOCATION_SCALE_1 = "scale1";
    public static final String UNIFORM_LOCATION_SCALE_2 = "scale2";
    public static final String UNIFORM_LOCATION_BLUR = "blur";
    public static final String UNIFORM_LOCATION_OFFSET = "offset";
    private final GroundSplattingConfig splatting;
    private WebGlUniformTexture texture;
    private WebGLUniformLocation scale1;
    private WebGLUniformLocation scale2;
    private WebGLUniformLocation blur;
    private WebGLUniformLocation offset;

    public WebGlSplatting(WebGlFacade webGlFacade, GroundSplattingConfig splatting, String variableName) {
        super(webGlFacade, variableName);
        this.splatting = splatting;
        if (splatting.getTextureId() != null) {
            texture = webGlFacade.createWebGLTexture(splatting.getTextureId(), variableName(UNIFORM_LOCATION_IMAGE));
        } else {
            texture = webGlFacade.createFakeWebGLTexture(variableName(UNIFORM_LOCATION_IMAGE));
        }
        scale1 = webGlFacade.getUniformLocation(variableName(UNIFORM_LOCATION_SCALE_1));
        scale2 = webGlFacade.getUniformLocation(variableName(UNIFORM_LOCATION_SCALE_2));
        blur = webGlFacade.getUniformLocation(variableName(UNIFORM_LOCATION_BLUR));
        offset = webGlFacade.getUniformLocation(variableName(UNIFORM_LOCATION_OFFSET));
    }

    public void activate() {
        texture.activate();
        getWebGlFacade().uniform1f(scale1, splatting.getScale1());
        getWebGlFacade().uniform1f(scale2, splatting.getScale2());
        getWebGlFacade().uniform1f(blur, splatting.getBlur());
        getWebGlFacade().uniform1f(offset, splatting.getOffset());
    }
}
