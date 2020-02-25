package com.btxtech.client.renderer.engine;

import elemental2.webgl.WebGLRenderingContext;

/**
 * Created by Beat
 * 27.05.2016.
 */
public class TextureIdHandler {
    private static int LAST_UNIFORM_VALUE = 0;

    public enum WebGlTextureId {
        TEXTURE0(WebGLRenderingContext.TEXTURE0),
        TEXTURE1(WebGLRenderingContext.TEXTURE1),
        TEXTURE2(WebGLRenderingContext.TEXTURE2),
        TEXTURE3(WebGLRenderingContext.TEXTURE3),
        TEXTURE4(WebGLRenderingContext.TEXTURE4),
        TEXTURE5(WebGLRenderingContext.TEXTURE5),
        TEXTURE6(WebGLRenderingContext.TEXTURE6),
        TEXTURE7(WebGLRenderingContext.TEXTURE7),
        TEXTURE8(WebGLRenderingContext.TEXTURE8),
        TEXTURE9(WebGLRenderingContext.TEXTURE9),
        TEXTURE10(WebGLRenderingContext.TEXTURE10),
        TEXTURE11(WebGLRenderingContext.TEXTURE11),
        TEXTURE12(WebGLRenderingContext.TEXTURE12),
        TEXTURE13(WebGLRenderingContext.TEXTURE13),
        TEXTURE14(WebGLRenderingContext.TEXTURE14),
        TEXTURE15(WebGLRenderingContext.TEXTURE15),
        TEXTURE16(WebGLRenderingContext.TEXTURE16),
        TEXTURE17(WebGLRenderingContext.TEXTURE17),
        TEXTURE18(WebGLRenderingContext.TEXTURE18),
        TEXTURE19(WebGLRenderingContext.TEXTURE19),
        TEXTURE20(WebGLRenderingContext.TEXTURE20),
        TEXTURE21(WebGLRenderingContext.TEXTURE21),
        TEXTURE22(WebGLRenderingContext.TEXTURE22),
        TEXTURE23(WebGLRenderingContext.TEXTURE23),
        TEXTURE24(WebGLRenderingContext.TEXTURE24),
        TEXTURE25(WebGLRenderingContext.TEXTURE25),
        TEXTURE26(WebGLRenderingContext.TEXTURE26),
        TEXTURE27(WebGLRenderingContext.TEXTURE27),
        TEXTURE28(WebGLRenderingContext.TEXTURE28),
        TEXTURE29(WebGLRenderingContext.TEXTURE29),
        TEXTURE30(WebGLRenderingContext.TEXTURE30),
        TEXTURE31(WebGLRenderingContext.TEXTURE31);

        private double webGlTextureId;
        private int uniformValue;

        WebGlTextureId(double webGlTextureId) {
            this.webGlTextureId = webGlTextureId;
            uniformValue = LAST_UNIFORM_VALUE++;
        }

        public double getWebGlTextureId() {
            return webGlTextureId;
        }

        public int getUniformValue() {
            return uniformValue;
        }
    }

    private int last;

    public WebGlTextureId create() {
        if (WebGlTextureId.values().length < last + 1) {
            throw new IllegalStateException("Running out of WebGlTextureId");
        }
        WebGlTextureId webGlTextureId = WebGlTextureId.values()[last];
        last++;
        return webGlTextureId;
    }
}
