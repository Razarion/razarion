package com.btxtech.client.renderer.engine;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.model.Shadowing;
import com.btxtech.client.renderer.webgl.WebGlException;
import elemental.html.WebGLFramebuffer;
import elemental.html.WebGLRenderingContext;
import elemental.html.WebGLTexture;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 20.05.2015.
 */
@Singleton
public class RenderService {
    @Inject
    private Instance<Renderer> renderInstance;
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private Camera camera;
    private List<RenderSwitch> renderQueue = new ArrayList<>();
    private boolean wire;
    private WebGLFramebuffer shadowFrameBuffer;
    private WebGLTexture colorTexture;
    private WebGLTexture depthTexture;
    private Logger logger = Logger.getLogger(RenderService.class.getName());
    private boolean showMonitor = false;
    private boolean showDeep = false;
    private RenderSwitch monitor;

    public void init() {
        initFrameBuffer();
        renderQueue.add(new RenderSwitch(renderInstance.select(TerrainSurfaceRenderer.class).get(), renderInstance.select(PlainTerrainSurfaceDepthBufferRenderer.class).get(), renderInstance.select(PlainTerrainSurfaceWireRender.class).get(), wire));
        renderQueue.add(new RenderSwitch(renderInstance.select(TerrainObjectRenderer.class).get(), renderInstance.select(TerrainObjectDepthBufferRenderer.class).get(), renderInstance.select(TerrainObjectWireRender.class).get(), wire));
        monitor = new RenderSwitch(renderInstance.select(MonitorRenderer.class).get(), null, null, wire);
        renderQueue.add(monitor);
    }

    public void draw() {
        gameCanvas.getCtx3d().bindFramebuffer(WebGLRenderingContext.FRAMEBUFFER, shadowFrameBuffer);
        gameCanvas.getCtx3d().viewport(0, 0, Shadowing.TEXTURE_SIZE, Shadowing.TEXTURE_SIZE);
        gameCanvas.getCtx3d().clear(WebGLRenderingContext.COLOR_BUFFER_BIT | WebGLRenderingContext.DEPTH_BUFFER_BIT);
        for (RenderSwitch renderSwitch : renderQueue) {
            try {
                renderSwitch.drawDepthBuffer();
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "drawDepthBuffer failed", t);
            }
        }
        gameCanvas.getCtx3d().bindFramebuffer(WebGLRenderingContext.FRAMEBUFFER, null);
        gameCanvas.getCtx3d().viewport(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        gameCanvas.getCtx3d().clear(WebGLRenderingContext.COLOR_BUFFER_BIT | WebGLRenderingContext.DEPTH_BUFFER_BIT);
        for (RenderSwitch renderSwitch : renderQueue) {
            if(!showMonitor && renderSwitch == monitor) {
                continue;
            }
            try {
                renderSwitch.draw();
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "draw failed", t);
            }
        }
    }

    public void fillBuffers() {
        for (RenderSwitch renderSwitch : renderQueue) {
            try {
                renderSwitch.fillBuffers();
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "fillBuffers failed", t);
            }
        }
    }

    public void showWire(boolean wire) {
        this.wire = wire;
        for (RenderSwitch renderSwitch : renderQueue) {
            try {
                renderSwitch.doSwitch(wire);
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "showWire failed", t);
            }
        }
    }

    public boolean isWire() {
        return wire;
    }

    private void initFrameBuffer() {
        Object extension = gameCanvas.getCtx3d().getExtension("WEBGL_depth_texture");
        if (extension == null) {
            throw new WebGlException("WEBGL_depth_texture is no supported");
        }

        colorTexture = gameCanvas.getCtx3d().createTexture();
        gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, colorTexture);
        gameCanvas.getCtx3d().texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MAG_FILTER, WebGLRenderingContext.NEAREST);
        gameCanvas.getCtx3d().texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MIN_FILTER, WebGLRenderingContext.NEAREST);
        gameCanvas.getCtx3d().texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_WRAP_S, WebGLRenderingContext.CLAMP_TO_EDGE);
        gameCanvas.getCtx3d().texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_WRAP_T, WebGLRenderingContext.CLAMP_TO_EDGE);
        gameCanvas.getCtx3d().texImage2D(WebGLRenderingContext.TEXTURE_2D, 0, WebGLRenderingContext.RGBA, Shadowing.TEXTURE_SIZE, Shadowing.TEXTURE_SIZE, 0, WebGLRenderingContext.RGBA, WebGLRenderingContext.UNSIGNED_BYTE, null);

        depthTexture = gameCanvas.getCtx3d().createTexture();
        gameCanvas.getCtx3d().bindTexture(WebGLRenderingContext.TEXTURE_2D, depthTexture);
        gameCanvas.getCtx3d().texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MAG_FILTER, WebGLRenderingContext.NEAREST);
        gameCanvas.getCtx3d().texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MIN_FILTER, WebGLRenderingContext.NEAREST);
        gameCanvas.getCtx3d().texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_WRAP_S, WebGLRenderingContext.CLAMP_TO_EDGE);
        gameCanvas.getCtx3d().texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_WRAP_T, WebGLRenderingContext.CLAMP_TO_EDGE);
        gameCanvas.getCtx3d().texImage2D(WebGLRenderingContext.TEXTURE_2D, 0, WebGLRenderingContext.DEPTH_COMPONENT, Shadowing.TEXTURE_SIZE, Shadowing.TEXTURE_SIZE, 0, WebGLRenderingContext.DEPTH_COMPONENT, WebGLRenderingContext.UNSIGNED_SHORT, null);

        shadowFrameBuffer = gameCanvas.getCtx3d().createFramebuffer();
        gameCanvas.getCtx3d().bindFramebuffer(WebGLRenderingContext.FRAMEBUFFER, shadowFrameBuffer);
        gameCanvas.getCtx3d().framebufferTexture2D(WebGLRenderingContext.FRAMEBUFFER, WebGLRenderingContext.COLOR_ATTACHMENT0, WebGLRenderingContext.TEXTURE_2D, colorTexture, 0);
        gameCanvas.getCtx3d().framebufferTexture2D(WebGLRenderingContext.FRAMEBUFFER, WebGLRenderingContext.DEPTH_ATTACHMENT, WebGLRenderingContext.TEXTURE_2D, depthTexture, 0);

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

    public boolean isShowMonitor() {
        return showMonitor;
    }

    public void setShowMonitor(boolean showMonitor) {
        this.showMonitor = showMonitor;
        logger.severe("setShowMonitor: " + this.showMonitor);
    }

    public boolean isShowDeep() {
        return showDeep;
    }

    public void setShowDeep(boolean showDeep) {
        this.showDeep = showDeep;
    }
}
