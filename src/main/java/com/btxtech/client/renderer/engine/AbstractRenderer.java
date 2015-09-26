package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.ImageDescriptor;
import com.btxtech.client.renderer.webgl.WebGlProgram;
import com.btxtech.client.renderer.webgl.WebGlUtil;
import com.btxtech.game.jsre.common.ImageLoader;
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
    // protected static final ImageDescriptor CHESS_TEXTURE_32 = new ImageDescriptor("chess32.jpg", 512, 512);
    public static final ImageDescriptor CHESS_TEXTURE_08 = new ImageDescriptor("chess08.jpg", 512, 512);
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

    protected int getAndEnableAttributeLocation(String attributeName) {
        return webGlProgram.getAndEnableAttributeLocation(attributeName);
    }

    protected WebGLUniformLocation getUniformLocation(String uniformName) {
        return webGlProgram.getUniformLocation(uniformName);
    }

    protected void useProgram() {
        webGlProgram.useProgram();
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
            }
        });
        return webGLTexture;
    }

}
