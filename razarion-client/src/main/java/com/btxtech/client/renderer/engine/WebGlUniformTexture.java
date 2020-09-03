package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.webgl.WebGlFacade;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import elemental2.webgl.WebGLRenderingContext;
import elemental2.webgl.WebGLTexture;
import elemental2.webgl.WebGLUniformLocation;

import java.util.function.Supplier;

/**
 * Created by Beat
 * 19.12.2015.
 */
public class WebGlUniformTexture {
    // private Logger logger = Logger.getLogger(WebGlUniformTexture.class.getName());
    private WebGLRenderingContext ctx3d;
    private Supplier<WebGLTexture> webGLTextureSupplier;
    private TextureIdHandler.WebGlTextureId webGlTextureId;
    private WebGLUniformLocation webGLUniformLocation;

    public WebGlUniformTexture(WebGLRenderingContext ctx3d,
                               WebGlFacade webGlFacade,
                               String uniformName,
                               TextureIdHandler.WebGlTextureId webGlTextureId,
                               Supplier<WebGLTexture> webGLTextureSupplier) {
        this.ctx3d = ctx3d;
        this.webGlTextureId = webGlTextureId;
        this.webGLTextureSupplier = webGLTextureSupplier;
        webGLUniformLocation = webGlFacade.getUniformLocation(uniformName);
    }

    public void activate() {
        ctx3d.uniform1i(webGLUniformLocation, webGlTextureId.getUniformValue());
        WebGlUtil.checkLastWebGlError("uniform1i", ctx3d);
        ctx3d.activeTexture(webGlTextureId.getWebGlTextureId());
        WebGlUtil.checkLastWebGlError("activeTexture", ctx3d);
        ctx3d.bindTexture(WebGLRenderingContext.TEXTURE_2D, webGLTextureSupplier.get());
        WebGlUtil.checkLastWebGlError("bindTexture", ctx3d);
    }
}
