package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.uiservice.renderer.RenderService;
import com.btxtech.uiservice.renderer.RenderUnitControl;
import elemental2.webgl.WebGLFramebuffer;
import elemental2.webgl.WebGLRenderingContext;
import elemental2.webgl.WebGLTexture;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 20.05.2015.
 */
@ApplicationScoped
public class ClientRenderServiceImpl extends RenderService {
    public static final int DEPTH_BUFFER_SIZE = 1024;
    private Logger logger = Logger.getLogger(ClientRenderServiceImpl.class.getName());
    @Inject
    private GameCanvas gameCanvas;
    private WebGLFramebuffer shadowFrameBuffer;
    private WebGLTexture colorTexture;
    private WebGLTexture depthTexture;

    @Override
    protected void internalSetup() {
        initFrameBuffer();
    }

    @Override
    protected void prepareDepthBufferRendering() {
        if (!depthTextureSupported()) {
            return;
        }
        gameCanvas.getCtx3d().bindFramebuffer(WebGLRenderingContext.FRAMEBUFFER, shadowFrameBuffer);
        gameCanvas.getCtx3d().viewport(0, 0, DEPTH_BUFFER_SIZE, DEPTH_BUFFER_SIZE);
        gameCanvas.getCtx3d().clear((int) (WebGLRenderingContext.COLOR_BUFFER_BIT) | (int) (WebGLRenderingContext.DEPTH_BUFFER_BIT));
    }

    @Override
    protected void prepareMainRendering() {
        gameCanvas.getCtx3d().bindFramebuffer(WebGLRenderingContext.FRAMEBUFFER, null);
        gameCanvas.getCtx3d().viewport(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        gameCanvas.getCtx3d().clear((int) (WebGLRenderingContext.COLOR_BUFFER_BIT) | (int) (WebGLRenderingContext.DEPTH_BUFFER_BIT));
    }

    @Override
    protected void prepare(RenderUnitControl renderUnitControl) {
        if (renderUnitControl.isDpDepthTest()) {
            gameCanvas.getCtx3d().enable(WebGLRenderingContext.DEPTH_TEST);
        } else {
            gameCanvas.getCtx3d().disable(WebGLRenderingContext.DEPTH_TEST);
        }
        gameCanvas.getCtx3d().depthMask(renderUnitControl.isWriteDepthBuffer());
        gameCanvas.getCtx3d().enable(WebGLRenderingContext.CULL_FACE);
        gameCanvas.getCtx3d().cullFace(WebGLRenderingContext.BACK);
        if (renderUnitControl.getBlend() != null) {
            gameCanvas.getCtx3d().enable(WebGLRenderingContext.BLEND);
            switch (renderUnitControl.getBlend()) {
                case SOURCE_ALPHA:
                    gameCanvas.getCtx3d().blendFunc(WebGLRenderingContext.SRC_ALPHA, WebGLRenderingContext.ONE_MINUS_SRC_ALPHA);
                    break;
                case CONST_ALPHA:
                    gameCanvas.getCtx3d().blendColor(1f, 1f, 1f, renderUnitControl.getConstAlpha());
                    gameCanvas.getCtx3d().blendFunc(WebGLRenderingContext.CONSTANT_ALPHA, WebGLRenderingContext.ONE_MINUS_CONSTANT_ALPHA);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown Blend mode: " + renderUnitControl.getBlend());
            }
        } else {
            gameCanvas.getCtx3d().disable(WebGLRenderingContext.BLEND);
        }
    }

    private void initFrameBuffer() {
        Object depthTextureExtension = gameCanvas.getCtx3d().getExtension("WEBGL_depth_texture");
//        if (depthTextureExtension == null) {
//            throw new WebGlException("WEBGL_depth_texture is no supported");
//        }

        colorTexture = gameCanvas.getCtx3d().createTexture();
        gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, colorTexture);
        gameCanvas.getCtx3d().texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MAG_FILTER, WebGLRenderingContext.NEAREST);
        gameCanvas.getCtx3d().texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MIN_FILTER, WebGLRenderingContext.NEAREST);
        gameCanvas.getCtx3d().texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_WRAP_S, WebGLRenderingContext.CLAMP_TO_EDGE);
        gameCanvas.getCtx3d().texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_WRAP_T, WebGLRenderingContext.CLAMP_TO_EDGE);
        gameCanvas.getCtx3d().texImage2D(WebGLRenderingContext.TEXTURE_2D, 0, WebGLRenderingContext.RGBA, DEPTH_BUFFER_SIZE, DEPTH_BUFFER_SIZE, 0, WebGLRenderingContext.RGBA, WebGLRenderingContext.UNSIGNED_BYTE, null);

        if (depthTextureExtension != null) {
            depthTexture = gameCanvas.getCtx3d().createTexture();
            gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, depthTexture);
            gameCanvas.getCtx3d().texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MAG_FILTER, WebGLRenderingContext.LINEAR);
            gameCanvas.getCtx3d().texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MIN_FILTER, WebGLRenderingContext.LINEAR);
            gameCanvas.getCtx3d().texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_WRAP_S, WebGLRenderingContext.CLAMP_TO_EDGE);
            gameCanvas.getCtx3d().texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_WRAP_T, WebGLRenderingContext.CLAMP_TO_EDGE);
            gameCanvas.getCtx3d().texImage2D(WebGLRenderingContext.TEXTURE_2D, 0, WebGLRenderingContext.DEPTH_COMPONENT, DEPTH_BUFFER_SIZE, DEPTH_BUFFER_SIZE, 0, WebGLRenderingContext.DEPTH_COMPONENT, WebGLRenderingContext.UNSIGNED_SHORT, null);

            shadowFrameBuffer = gameCanvas.getCtx3d().createFramebuffer();
            gameCanvas.getCtx3d().bindFramebuffer(WebGLRenderingContext.FRAMEBUFFER, shadowFrameBuffer);
            gameCanvas.getCtx3d().framebufferTexture2D(WebGLRenderingContext.FRAMEBUFFER, WebGLRenderingContext.COLOR_ATTACHMENT0, WebGLRenderingContext.TEXTURE_2D, colorTexture, 0);
            gameCanvas.getCtx3d().framebufferTexture2D(WebGLRenderingContext.FRAMEBUFFER, WebGLRenderingContext.DEPTH_ATTACHMENT, WebGLRenderingContext.TEXTURE_2D, depthTexture, 0);
        } else {
            logger.severe("WEBGL_depth_texture not supported");
        }

        gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, null);
        gameCanvas.getCtx3d().bindRenderbuffer(WebGLRenderingContext.RENDERBUFFER, null);
        gameCanvas.getCtx3d().bindFramebuffer(WebGLRenderingContext.FRAMEBUFFER, null);
    }

    public WebGLTexture getColorTexture() {
        return colorTexture;
    }

    public WebGLTexture getDepthTexture() {
        return depthTexture;
    }

    @Override
    public boolean depthTextureSupported() {
        return shadowFrameBuffer != null;
    }
}
