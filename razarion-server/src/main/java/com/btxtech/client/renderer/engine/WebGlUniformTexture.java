package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.webgl.WebGlUtil;
import elemental.html.WebGLRenderingContext;
import elemental.html.WebGLTexture;
import elemental.html.WebGLUniformLocation;

/**
 * Created by Beat
 * 19.12.2015.
 */
public class WebGlUniformTexture {
    private WebGLRenderingContext ctx3d;
    private WebGlFacade webGlFacade;
    private WebGLTexture webGLTexture;
    private String samplerUniformName;
    private TextureIdHandler.WebGlTextureId webGlTextureId;

    public WebGlUniformTexture(WebGLRenderingContext ctx3d, WebGlFacade webGlFacade, WebGLTexture webGLTexture, String samplerUniformName, TextureIdHandler.WebGlTextureId webGlTextureId) {
        this.ctx3d = ctx3d;
        this.webGlFacade = webGlFacade;
        this.webGLTexture = webGLTexture;
        this.samplerUniformName = samplerUniformName;
        this.webGlTextureId = webGlTextureId;
    }

    public void activate() {
        WebGLUniformLocation tUniform = webGlFacade.getUniformLocation(samplerUniformName);
        ctx3d.uniform1i(tUniform, webGlTextureId.getUniformValue());
        WebGlUtil.checkLastWebGlError("uniform1i", ctx3d);
        ctx3d.activeTexture(webGlTextureId.getWebGlTextureId());
        WebGlUtil.checkLastWebGlError("activeTexture", ctx3d);
        ctx3d.bindTexture(WebGLRenderingContext.TEXTURE_2D, webGLTexture);
        WebGlUtil.checkLastWebGlError("bindTexture", ctx3d);
    }
}
