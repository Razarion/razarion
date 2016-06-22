package com.btxtech.client.renderer.engine;

import com.btxtech.client.editor.terrain.TerrainEditor;
import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.webgl.WebGlException;
import com.btxtech.client.terrain.TerrainObjectService;
import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.client.units.ItemService;
import elemental.html.WebGLFramebuffer;
import elemental.html.WebGLRenderingContext;
import elemental.html.WebGLTexture;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 20.05.2015.
 */
@Singleton
public class RenderService {
    public static final int DEPTH_BUFFER_SIZE = 1024;
    public static final int RENDER_FRAME_COUNT = 1;
    public static final int RENDER_FRAME_COUNT_MILLIS = RENDER_FRAME_COUNT * 1000;
    private Logger logger = Logger.getLogger(RenderService.class.getName());
    @Inject
    private Instance<Renderer> renderInstance;
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private Camera camera;
    @Inject
    private ItemService itemService;
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    private TerrainObjectService terrainObjectService;
    @Inject
    private TerrainEditor terrainEditor;
    private List<RenderSwitch> renderQueue;
    private Collection<RenderSwitch> terrainObjectRenders;
    private Collection<TerrainEditorRenderer> terrainEditorRenderers;
    private TerrainEditorCursorRenderer terrainEditorCursorRenderer;
    private TerrainObjectEditorRenderer terrainObjectEditorRenderer;
    private boolean wire;
    private WebGLFramebuffer shadowFrameBuffer;
    private WebGLTexture colorTexture;
    private WebGLTexture depthTexture;
    private boolean showMonitor = false;
    private boolean showNorm = false;
    private boolean showDeep = false;
    private boolean showSlopeEditor = false;
    private boolean showObjectEditor = false;
    private RenderSwitch monitor;
    private RenderSwitch terrainNorm;
    private Collection<RenderSwitch> unitNorms;
    private Collection<RenderSwitch> terrainObjectNorms;
    private int framesCount = 0;
    private long lastTime = 0;

    public void setupRenderers() {
        initFrameBuffer();
        renderQueue = new ArrayList<>();
        unitNorms = new ArrayList<>();
        terrainObjectNorms = new ArrayList<>();
        terrainObjectRenders = new ArrayList<>();
        createAndAddRenderSwitch(GroundRenderer.class, GroundDepthBufferRenderer.class, GroundWireRender.class, 0);
        for (int id : terrainSurface.getSlopeIds()) {
            createAndAddRenderSwitch(SlopeRenderer.class, SlopeDepthBufferRenderer.class, SlopeWireRenderer.class, id);
        }
        terrainEditorRenderers = new ArrayList<>();
        for (int id : terrainEditor.getSlopePolygonIds()) {
            TerrainEditorRenderer terrainEditorRenderer = renderInstance.select(TerrainEditorRenderer.class).get();
            terrainEditorRenderer.setId(id);
            terrainEditorRenderers.add(terrainEditorRenderer);
        }
        setupTerrainObjectRenderer();
        for (int id : itemService.getItemTypeIds()) {
            createAndAddRenderSwitch(UnitRenderer.class, UnitDepthBufferRenderer.class, UnitWireRenderer.class, id);
            unitNorms.add(createAndAddRenderSwitch(UnitNormRenderer.class, null, UnitNormRenderer.class, id));
        }
        createAndAddRenderSwitch(WaterRenderer.class, null, WaterWireRenderer.class, 0);
        monitor = createAndAddRenderSwitch(MonitorRenderer.class, null, null, 0);
        terrainNorm = createAndAddRenderSwitch(TerrainNormRenderer.class, null, TerrainNormRenderer.class, 0);
        terrainEditorCursorRenderer = renderInstance.select(TerrainEditorCursorRenderer.class).get();
        terrainEditorCursorRenderer.fillBuffers();
        terrainObjectEditorRenderer = renderInstance.select(TerrainObjectEditorRenderer.class).get();
        terrainObjectEditorRenderer.fillBuffers();
    }

    public void setupTerrainObjectRenderer() {
        if(terrainObjectRenders != null) {
            renderQueue.removeAll(terrainObjectRenders);
            terrainObjectRenders.clear();
        }
        if(terrainObjectNorms != null) {
            renderQueue.removeAll(terrainObjectNorms);
            terrainObjectNorms.clear();
        }
        for (int id : terrainObjectService.getVertexContainerIds()) {
            terrainObjectRenders.add(createAndAddRenderSwitch(TerrainObjectRenderer.class, TerrainObjectDepthBufferRenderer.class, TerrainObjectWireRender.class, id));
            terrainObjectNorms.add(createAndAddRenderSwitch(TerrainObjectNormRenderer.class, null, TerrainObjectNormRenderer.class, id));
        }
    }

    public void createTerrainEditorRenderer(int id) {
        TerrainEditorRenderer terrainEditorRenderer = renderInstance.select(TerrainEditorRenderer.class).get();
        terrainEditorRenderer.setId(id);
        terrainEditorRenderers.add(terrainEditorRenderer);
        terrainEditorRenderer.fillBuffers();
    }

    private RenderSwitch createAndAddRenderSwitch(Class<? extends Renderer> normalRendererClass, Class<? extends Renderer> depthBufferRendererClass, Class<? extends Renderer> wireRendererClass, int id) {
        Renderer normalRenderer = null;
        if (normalRendererClass != null) {
            normalRenderer = renderInstance.select(normalRendererClass).get();
            normalRenderer.setId(id);
            normalRenderer.setupImages();
        }
        Renderer depthBufferRenderer = null;
        if (depthBufferRendererClass != null) {
            depthBufferRenderer = renderInstance.select(depthBufferRendererClass).get();
            depthBufferRenderer.setId(id);
            depthBufferRenderer.setupImages();
        }
        Renderer wireRenderer = null;
        if (wireRendererClass != null) {
            wireRenderer = renderInstance.select(wireRendererClass).get();
            wireRenderer.setId(id);
            wireRenderer.setupImages();
        }
        RenderSwitch renderSwitch = new RenderSwitch(normalRenderer, depthBufferRenderer, wireRenderer, wire);
        renderQueue.add(renderSwitch);
        return renderSwitch;
    }

    public void draw() {
        gameCanvas.getCtx3d().bindFramebuffer(WebGLRenderingContext.FRAMEBUFFER, shadowFrameBuffer);
        gameCanvas.getCtx3d().viewport(0, 0, DEPTH_BUFFER_SIZE, DEPTH_BUFFER_SIZE);
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
            if (!showMonitor && renderSwitch == monitor) {
                continue;
            }
            if (!showNorm && (renderSwitch == terrainNorm || unitNorms.contains(renderSwitch) || terrainObjectNorms.contains(renderSwitch))) {
                continue;
            }
            try {
                renderSwitch.draw();
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "draw failed", t);
            }
        }

        // Dirty way to render wire over image (see changed files in GIT).
        gameCanvas.getCtx3d().depthFunc(WebGLRenderingContext.ALWAYS);
        gameCanvas.getCtx3d().enable(WebGLRenderingContext.BLEND);
        gameCanvas.getCtx3d().blendFunc(WebGLRenderingContext.SRC_ALPHA, WebGLRenderingContext.ONE_MINUS_SRC_ALPHA);
        gameCanvas.getCtx3d().depthMask(false);
        for (RenderSwitch renderSwitch : renderQueue) {
            if (!showMonitor && renderSwitch == monitor) {
                continue;
            }
            if (!showNorm && (renderSwitch == terrainNorm || unitNorms.contains(renderSwitch) || terrainObjectNorms.contains(renderSwitch))) {
                continue;
            }
            try {
                renderSwitch.drawWire();
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "draw failed", t);
            }
        }
        gameCanvas.getCtx3d().depthFunc(WebGLRenderingContext.LESS);
        gameCanvas.getCtx3d().depthMask(true);
        gameCanvas.getCtx3d().disable(WebGLRenderingContext.BLEND);

        // Dirty way to render terrain editor
        if (showSlopeEditor) {
            gameCanvas.getCtx3d().depthFunc(WebGLRenderingContext.ALWAYS);
            for (TerrainEditorRenderer terrainEditorRenderer : terrainEditorRenderers) {
                if (terrainEditorRenderer.hasElements()) {
                    terrainEditorRenderer.draw();
                }
            }
            if(terrainEditorCursorRenderer.hasElements()) {
                terrainEditorCursorRenderer.draw();
            }
            gameCanvas.getCtx3d().depthFunc(WebGLRenderingContext.LESS);
        }
        if (showObjectEditor && terrainObjectEditorRenderer.hasElements()) {
            gameCanvas.getCtx3d().depthFunc(WebGLRenderingContext.ALWAYS);
            terrainObjectEditorRenderer.draw();
            gameCanvas.getCtx3d().depthFunc(WebGLRenderingContext.LESS);
        }

        framesCount++;
        if (lastTime == 0) {
            lastTime = System.currentTimeMillis() + RENDER_FRAME_COUNT_MILLIS;
        } else if (lastTime < System.currentTimeMillis()) {
            // logger.severe("Frames per seonds: " + (double) framesCount / (double) RENDER_FRAME_COUNT);
            framesCount = 0;
            lastTime = System.currentTimeMillis() + RENDER_FRAME_COUNT_MILLIS;
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
        if (terrainEditorRenderers != null) {
            for (TerrainEditorRenderer terrainEditorRenderer : terrainEditorRenderers) {
                try {
                    terrainEditorRenderer.fillBuffers();
                } catch (Throwable t) {
                    logger.log(Level.SEVERE, "fillBuffers failed", t);
                }
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
        gameCanvas.getCtx3d().texImage2D(WebGLRenderingContext.TEXTURE_2D, 0, WebGLRenderingContext.RGBA, DEPTH_BUFFER_SIZE, DEPTH_BUFFER_SIZE, 0, WebGLRenderingContext.RGBA, WebGLRenderingContext.UNSIGNED_BYTE, null);

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
    }

    public boolean isShowNorm() {
        return showNorm;
    }

    public void setShowNorm(boolean showNorm) {
        this.showNorm = showNorm;
    }

    public boolean isShowDeep() {
        return showDeep;
    }

    public void setShowDeep(boolean showDeep) {
        this.showDeep = showDeep;
    }

    public void setShowSlopeEditor(boolean showSlopeEditor) {
        this.showSlopeEditor = showSlopeEditor;
    }

    public void setShowObjectEditor(boolean showObjectEditor) {
        this.showObjectEditor = showObjectEditor;
    }
}
