package com.btxtech.client.renderer.webgl;

import com.btxtech.client.imageservice.ImageUiService;
import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.utils.GwtUtils;
import com.btxtech.uiservice.control.GameUiControl;
import com.google.gwt.dom.client.ImageElement;
import elemental.html.WebGLRenderingContext;
import elemental.html.WebGLTexture;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntConsumer;

/**
 * Created by Beat
 * 25.01.2017.
 */
@ApplicationScoped
public class WebGLTextureContainer {
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    private ImageUiService imageUiService;
    private Map<Integer, WebGLTexture> textures = new HashMap<>();
    private Map<Integer, WebGLTexture> bumpTextures = new HashMap<>();

    public void setupTextures() {
        for (Integer imageId : gameUiControl.getAllTextureIds()) {
            imageUiService.requestImage(imageId, imageElement -> setupTexture(imageId, imageElement));
        }
        for (Integer imageId : gameUiControl.getAllBumpTextureIds()) {
            imageUiService.requestImage(imageId, imageElement -> setupTextureForBumpMap(imageId, imageElement));
        }
    }

    public WebGLTexture getTexture(int imageId) {
        WebGLTexture webGLTexture = textures.get(imageId);
        if (webGLTexture != null) {
            return webGLTexture;
        }
        webGLTexture = gameCanvas.getCtx3d().createTexture();
        WebGLTexture finalWebGLTexture = webGLTexture;
        imageUiService.requestImage(imageId, imageElement -> bindTexture(imageElement, finalWebGLTexture));
        return webGLTexture;
    }

    public WebGLTexture getTextureForBumpMap(int imageId) {
        WebGLTexture webGLTexture = bumpTextures.get(imageId);
        if (webGLTexture != null) {
            return webGLTexture;
        }
        webGLTexture = gameCanvas.getCtx3d().createTexture();
        WebGLTexture finalWebGLTexture = webGLTexture;
        imageUiService.requestImage(imageId, imageElement -> bindTextureForBumpMap(imageElement, finalWebGLTexture));
        return webGLTexture;
    }

    public void handleImageSize(int imageId, IntConsumer pixelSizeConsumer) {
        imageUiService.requestImage(imageId, imageElement -> pixelSizeConsumer.accept(imageElement.getWidth()));
    }

    private void setupTexture(int imageId, ImageElement imageElement) {
        WebGLTexture webGLTexture = textures.get(imageId);
        if (webGLTexture == null) {
            webGLTexture = gameCanvas.getCtx3d().createTexture();
            textures.put(imageId, webGLTexture);
        }
        bindTexture(imageElement, webGLTexture);
    }

    protected void setupTextureForBumpMap(int imageId, ImageElement imageElement) {
        WebGLTexture webGLTexture = bumpTextures.get(imageId);
        if (webGLTexture == null) {
            webGLTexture = gameCanvas.getCtx3d().createTexture();
            bumpTextures.put(imageId, webGLTexture);
        }
        bindTextureForBumpMap(imageElement, webGLTexture);
    }

    private void bindTexture(ImageElement imageElement, WebGLTexture webGLTexture) {
        gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, webGLTexture);
        gameCanvas.getCtx3d().pixelStorei(WebGLRenderingContext.UNPACK_FLIP_Y_WEBGL, 1);
        gameCanvas.getCtx3d().texImage2D(WebGLRenderingContext.TEXTURE_2D, 0, WebGLRenderingContext.RGBA, WebGLRenderingContext.RGBA, WebGLRenderingContext.UNSIGNED_BYTE, (elemental.html.ImageElement) GwtUtils.castElementToElement(imageElement));
        gameCanvas.getCtx3d().texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MAG_FILTER, WebGLRenderingContext.NEAREST);
        gameCanvas.getCtx3d().texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MIN_FILTER, WebGLRenderingContext.LINEAR_MIPMAP_NEAREST);
        gameCanvas.getCtx3d().generateMipmap(WebGLRenderingContext.TEXTURE_2D);
        gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, null);
        WebGlUtil.checkLastWebGlError("bindTexture", gameCanvas.getCtx3d());
    }

    private void bindTextureForBumpMap(ImageElement imageElement, WebGLTexture webGLTexture) {
        gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, webGLTexture);
        gameCanvas.getCtx3d().pixelStorei(WebGLRenderingContext.UNPACK_FLIP_Y_WEBGL, 1);
        gameCanvas.getCtx3d().texImage2D(WebGLRenderingContext.TEXTURE_2D, 0, WebGLRenderingContext.RGBA, WebGLRenderingContext.RGBA, WebGLRenderingContext.UNSIGNED_BYTE, (elemental.html.ImageElement) GwtUtils.castElementToElement(imageElement));
        gameCanvas.getCtx3d().texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MAG_FILTER, WebGLRenderingContext.LINEAR);
        gameCanvas.getCtx3d().texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MIN_FILTER, WebGLRenderingContext.LINEAR);
        gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, null);
        WebGlUtil.checkLastWebGlError("bindTexture", gameCanvas.getCtx3d());
    }
}
