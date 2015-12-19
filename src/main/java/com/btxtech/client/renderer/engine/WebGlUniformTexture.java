package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.webgl.WebGlProgram;
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
    private AbstractRenderer abstractRenderer;
    private WebGLTexture webGLTexture;
    private String samplerUniformName;
    private final int textureId;
    private final int uniformValue;

    public WebGlUniformTexture(WebGLRenderingContext ctx3d, AbstractRenderer abstractRenderer, WebGLTexture webGLTexture, String samplerUniformName, int textureId, int uniformValue) {
        this.ctx3d = ctx3d;
        this.abstractRenderer = abstractRenderer;
        this.webGLTexture = webGLTexture;
        this.samplerUniformName = samplerUniformName;
        this.textureId = textureId;
        this.uniformValue = uniformValue;
    }

    public void activate() {
        WebGLUniformLocation tUniform = abstractRenderer.getUniformLocation(samplerUniformName);
        ctx3d.activeTexture(textureId);
        WebGlUtil.checkLastWebGlError("activeTexture", ctx3d);
        ctx3d.bindTexture(WebGLRenderingContext.TEXTURE_2D, webGLTexture);
        WebGlUtil.checkLastWebGlError("bindTexture", ctx3d);
        ctx3d.uniform1i(tUniform, uniformValue);
        WebGlUtil.checkLastWebGlError("uniform1i", ctx3d);
    }
}
