package com.btxtech.client.renderer.engine;

import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.webgl.WebGlProgram;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.game.jsre.common.ImageLoader;
import com.btxtech.shared.primitives.Matrix4;
import com.btxtech.shared.primitives.Vertex;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.resources.client.TextResource;
import elemental.html.WebGLRenderingContext;
import elemental.html.WebGLTexture;
import elemental.html.WebGLUniformLocation;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Map;

/**
 * Created by Beat
 * 04.09.2015.
 */
public abstract class AbstractRenderer implements Renderer {
    private WebGlProgram webGlProgram;
    @Inject
    private Instance<WebGlProgram> webGlProgramInstance;
    @Inject
    private GameCanvas gameCanvas;

    protected void createProgram(TextResource vertexShaderCode, TextResource fragmentShaderCode) {
        webGlProgram = webGlProgramInstance.get();
        webGlProgram.createProgram(vertexShaderCode.getText(), fragmentShaderCode.getText());
    }

//    protected void destroy() {
//        webGlProgram.useProgram();
//        webGlProgram.destroy();
//        webGlProgram = null;
//    }

    protected VertexShaderAttribute createVertexShaderAttribute(String attributeName) {
        return new VertexShaderAttribute(gameCanvas.getCtx3d(), webGlProgram, attributeName);
    }

    protected FloatShaderAttribute createFloatShaderAttribute(String attributeName) {
        return new FloatShaderAttribute(gameCanvas.getCtx3d(), webGlProgram, attributeName);
    }

    protected IntegerShaderAttribute createIntegerShaderAttribute(String attributeName) {
        return new IntegerShaderAttribute(gameCanvas.getCtx3d(), webGlProgram, attributeName);
    }

    protected ShaderTextureCoordinateAttribute createShaderTextureCoordinateAttributee(String attributeName) {
        return new ShaderTextureCoordinateAttribute(gameCanvas.getCtx3d(), webGlProgram, attributeName);
    }

    protected int getAndEnableAttributeLocation(String attributeName) {
        return webGlProgram.getAndEnableAttributeLocation(attributeName);
    }

    protected WebGLUniformLocation getUniformLocation(String uniformName) {
        return webGlProgram.getUniformLocation(uniformName);
    }

    protected void useProgram() {
        webGlProgram.useProgram();
    }

    protected void uniformMatrix4fv(String uniformName, Matrix4 matrix) {
        WebGLUniformLocation uniformLocation = getUniformLocation(uniformName);
        gameCanvas.getCtx3d().uniformMatrix4fv(uniformLocation, false, WebGlUtil.createArrayBufferOfFloat32(matrix.toWebGlArray()));
        WebGlUtil.checkLastWebGlError("uniformMatrix4fv", gameCanvas.getCtx3d());
    }

    protected void uniform3f(String uniformName, double x, double y, double z) {
        WebGLUniformLocation uniformLocation = getUniformLocation(uniformName);
        gameCanvas.getCtx3d().uniform3f(uniformLocation, (float) x, (float) y, (float) z);
        WebGlUtil.checkLastWebGlError("uniform3f", gameCanvas.getCtx3d());
    }

    protected void uniform3f(String uniformName, Vertex vertex) {
        uniform3f(uniformName, vertex.getX(), vertex.getY(), vertex.getZ());
    }

    protected void uniform1f(String uniformName, double value) {
        WebGLUniformLocation uniformLocation = getUniformLocation(uniformName);
        gameCanvas.getCtx3d().uniform1f(uniformLocation, (float) value);
        WebGlUtil.checkLastWebGlError("uniform1f", gameCanvas.getCtx3d());
    }

    protected WebGlUniformTexture createWebGLTexture(ImageDescriptor imageDescriptor, String samplerUniformName, int textureId, int uniformValue) {
        return new WebGlUniformTexture(gameCanvas.getCtx3d(), this, setupTexture(imageDescriptor), samplerUniformName, textureId, uniformValue);
    }

    protected WebGlUniformTexture createWebGLBumpMapTexture(ImageDescriptor imageDescriptor, String samplerUniformName, int textureId, int uniformValue) {
        return new WebGlUniformTexture(gameCanvas.getCtx3d(), this, setupTextureForBumpMap(imageDescriptor), samplerUniformName, textureId, uniformValue);
    }

    protected WebGLTexture setupTexture(ImageDescriptor imageDescriptor) {
        final WebGLTexture webGLTexture = gameCanvas.getCtx3d().createTexture();
        ImageLoader<WebGLTexture> textureLoader = new ImageLoader<>();
        textureLoader.addImageUrl(imageDescriptor.getUrl(), webGLTexture);
        textureLoader.startLoading(new ImageLoader.Listener<WebGLTexture>() {
            @Override
            public void onLoaded(Map<WebGLTexture, ImageElement> loadedImageElements, Collection<WebGLTexture> failed) {
                if (!failed.isEmpty()) {
                    throw new IllegalStateException("Failed loading texture");
                }
                ImageElement imageElement = loadedImageElements.get(webGLTexture);
                if (imageElement == null) {
                    throw new IllegalStateException("Failed loading texture");
                }

                gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, webGLTexture);
                gameCanvas.getCtx3d().pixelStorei(WebGLRenderingContext.UNPACK_FLIP_Y_WEBGL, 1);
                gameCanvas.getCtx3d().texImage2D(WebGLRenderingContext.TEXTURE_2D, 0, WebGLRenderingContext.RGBA, WebGLRenderingContext.RGBA, WebGLRenderingContext.UNSIGNED_BYTE, (elemental.html.ImageElement) WebGlUtil.castElementToElement(imageElement));
                gameCanvas.getCtx3d().texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MAG_FILTER, WebGLRenderingContext.NEAREST);
                gameCanvas.getCtx3d().texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MIN_FILTER, WebGLRenderingContext.LINEAR_MIPMAP_NEAREST);
                gameCanvas.getCtx3d().generateMipmap(WebGLRenderingContext.TEXTURE_2D);
                gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, null);
                WebGlUtil.checkLastWebGlError("bindTexture", gameCanvas.getCtx3d());
            }
        });
        return webGLTexture;
    }

    protected WebGLTexture setupTextureForBumpMap(ImageDescriptor imageDescriptor) {
        final WebGLTexture webGLTexture = gameCanvas.getCtx3d().createTexture();
        ImageLoader<WebGLTexture> textureLoader = new ImageLoader<>();
        textureLoader.addImageUrl(imageDescriptor.getUrl(), webGLTexture);
        textureLoader.startLoading(new ImageLoader.Listener<WebGLTexture>() {
            @Override
            public void onLoaded(Map<WebGLTexture, ImageElement> loadedImageElements, Collection<WebGLTexture> failed) {
                if (!failed.isEmpty()) {
                    throw new IllegalStateException("Failed loading texture");
                }
                ImageElement imageElement = loadedImageElements.get(webGLTexture);
                if (imageElement == null) {
                    throw new IllegalStateException("Failed loading texture");
                }

                gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, webGLTexture);
                gameCanvas.getCtx3d().pixelStorei(WebGLRenderingContext.UNPACK_FLIP_Y_WEBGL, 1);
                gameCanvas.getCtx3d().texImage2D(WebGLRenderingContext.TEXTURE_2D, 0, WebGLRenderingContext.RGBA, WebGLRenderingContext.RGBA, WebGLRenderingContext.UNSIGNED_BYTE, (elemental.html.ImageElement) WebGlUtil.castElementToElement(imageElement));
                gameCanvas.getCtx3d().texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MAG_FILTER, WebGLRenderingContext.LINEAR);
                gameCanvas.getCtx3d().texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MIN_FILTER, WebGLRenderingContext.LINEAR);
                gameCanvas.getCtx3d().generateMipmap(WebGLRenderingContext.TEXTURE_2D);
                gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, null);
            }
        });
        return webGLTexture;
    }

    protected WebGLRenderingContext getCtx3d() {
        return gameCanvas.getCtx3d();
    }
}
