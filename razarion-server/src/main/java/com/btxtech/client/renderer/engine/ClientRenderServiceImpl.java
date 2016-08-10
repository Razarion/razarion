package com.btxtech.client.renderer.engine;

import com.btxtech.client.editor.terrain.TerrainEditor;
import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.unit.ClientGroundDepthBufferRendererUnit;
import com.btxtech.client.renderer.unit.ClientGroundRendererUnit;
import com.btxtech.client.renderer.unit.ClientSlopeRendererUnit;
import com.btxtech.client.renderer.unit.ClientSlopeDepthBufferRendererUnit;
import com.btxtech.client.renderer.webgl.WebGlException;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.CompositeRenderer;
import com.btxtech.uiservice.renderer.RenderService;
import com.btxtech.uiservice.renderer.RenderServiceInitEvent;
import com.btxtech.uiservice.terrain.TerrainObjectService;
import com.btxtech.uiservice.terrain.TerrainUiService;
import elemental.html.WebGLFramebuffer;
import elemental.html.WebGLRenderingContext;
import elemental.html.WebGLTexture;

import javax.enterprise.event.Observes;
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
public class ClientRenderServiceImpl extends RenderService {
    public static final int DEPTH_BUFFER_SIZE = 1024;
    public static final int RENDER_FRAME_COUNT = 1;
    public static final int RENDER_FRAME_COUNT_MILLIS = RENDER_FRAME_COUNT * 1000;
    private Logger logger = Logger.getLogger(ClientRenderServiceImpl.class.getName());
    @Inject
    private Instance<AbstractRenderUnit> renderInstance;
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private Camera camera;
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private TerrainObjectService terrainObjectService;
    @Inject
    private TerrainEditor terrainEditor;
    @Deprecated
    private List<CompositeRenderer> renderQueue;
    @Deprecated
    private Collection<CompositeRenderer> terrainObjectRenders;
    private Collection<TerrainEditorUnitRenderer> terrainEditorRenderers;
    private TerrainEditorCursorUnitRenderer terrainEditorCursorRenderer;
    private TerrainObjectEditorUnitRenderer terrainObjectEditorRenderer;
    private boolean wire;
    private WebGLFramebuffer shadowFrameBuffer;
    private WebGLTexture colorTexture;
    private WebGLTexture depthTexture;
    private boolean showMonitor = false;
    private boolean showNorm = false;
    private boolean showDeep = false;
    private boolean showSlopeEditor = false;
    private boolean showObjectEditor = false;
    private CompositeRenderer monitor;
    private CompositeRenderer terrainNorm;
    @Deprecated
    private Collection<CompositeRenderer> terrainObjectNorms;
    private int framesCount = 0;
    private long lastTime = 0;

    public void onRenderServiceInitEvent(@Observes RenderServiceInitEvent renderServiceInitEvent) {
        initFrameBuffer();
    }

    @Override
    protected void setupRenderers() {
        renderQueue = new ArrayList<>();
        terrainObjectNorms = new ArrayList<>();
        terrainObjectRenders = new ArrayList<>();
        createAndAddRenderSwitch(ClientGroundRendererUnit.class, ClientGroundDepthBufferRendererUnit.class, GroundWireRender.class, 0);
        for (int id : terrainUiService.getSlopeIds()) {
            createAndAddRenderSwitch(ClientSlopeRendererUnit.class, ClientSlopeDepthBufferRendererUnit.class, SlopeWireUnitRenderer.class, id);
        }
        terrainEditorRenderers = new ArrayList<>();
        for (int id : terrainEditor.getSlopePolygonIds()) {
            TerrainEditorUnitRenderer terrainEditorRenderer = renderInstance.select(TerrainEditorUnitRenderer.class).get();
            // TODO terrainEditorRenderer.setId(id);
            terrainEditorRenderers.add(terrainEditorRenderer);
        }
        setupTerrainObjectRenderer();
        createAndAddRenderSwitch(WaterUnitRenderer.class, null, WaterWireUnitRenderer.class, 0);
        monitor = createAndAddRenderSwitch(MonitorUnitRenderer.class, null, null, 0);
        terrainNorm = createAndAddRenderSwitch(GroundNormUnitRenderer.class, null, GroundNormUnitRenderer.class, 0);
        terrainEditorCursorRenderer = renderInstance.select(TerrainEditorCursorUnitRenderer.class).get();
        terrainEditorCursorRenderer.fillBuffers();
        terrainObjectEditorRenderer = renderInstance.select(TerrainObjectEditorUnitRenderer.class).get();
        terrainObjectEditorRenderer.fillBuffers();
    }

    // TODO
//    @Override
//    protected void initBaseItemTypeRenderer(CompositeRenderer compositeRenderer) {
//        compositeRenderer.setRenderUnit(renderInstance.select(ItemUnitRenderer.class).get());
//        compositeRenderer.setDepthBufferRenderUnit(renderInstance.select(ItemDepthBufferUnitRenderer.class).get());
//        compositeRenderer.setWireRenderUnit(renderInstance.select(ItemWireUnitRenderer.class).get());
//        compositeRenderer.setNormRenderUnit(renderInstance.select(ItemNormUnitRenderer.class).get());
//    }

    public void setupTerrainObjectRenderer() {
        if (terrainObjectRenders != null) {
            renderQueue.removeAll(terrainObjectRenders);
            terrainObjectRenders.clear();
        }
        if (terrainObjectNorms != null) {
            renderQueue.removeAll(terrainObjectNorms);
            terrainObjectNorms.clear();
        }
        for (int id : terrainObjectService.getVertexContainerIds()) {
            terrainObjectRenders.add(createAndAddRenderSwitch(TerrainObjectUnitRenderer.class, TerrainObjectDepthBufferUnitRenderer.class, TerrainObjectWireRender.class, id));
            terrainObjectNorms.add(createAndAddRenderSwitch(TerrainObjectNormUnitRenderer.class, null, TerrainObjectNormUnitRenderer.class, id));
        }
        for (CompositeRenderer terrainObjectRender : terrainObjectRenders) {
            terrainObjectRender.fillBuffers();
        }
        for (TerrainEditorUnitRenderer terrainEditorRenderer : terrainEditorRenderers) {
            terrainEditorRenderer.fillBuffers();
        }
    }

    public void createTerrainEditorRenderer(int id) {
        TerrainEditorUnitRenderer terrainEditorRenderer = renderInstance.select(TerrainEditorUnitRenderer.class).get();
        // TODO terrainEditorRenderer.setId(id);
        terrainEditorRenderers.add(terrainEditorRenderer);
        terrainEditorRenderer.fillBuffers();
    }


    public void removeTerrainEditorRenderer(int id) {
        // TODO
//        for (TerrainEditorUnitRenderer terrainEditorRenderer : terrainEditorRenderers) {
//            if (terrainEditorRenderer.getId() == id) {
//                terrainEditorRenderers.remove(terrainEditorRenderer);
//                return;
//            }
//        }
    }

    private CompositeRenderer createAndAddRenderSwitch(Class<? extends AbstractRenderUnit> normalRendererClass, Class<? extends AbstractRenderUnit> depthBufferRendererClass, Class<? extends AbstractRenderUnit> wireRendererClass, int id) {
        AbstractRenderUnit normalRenderUnit = null;
        if (normalRendererClass != null) {
            normalRenderUnit = renderInstance.select(normalRendererClass).get();
            // TODO normalRenderUnit.setId(id);
            normalRenderUnit.setupImages();
        }
        AbstractRenderUnit depthBufferRenderUnit = null;
        if (depthBufferRendererClass != null) {
            depthBufferRenderUnit = renderInstance.select(depthBufferRendererClass).get();
            // TODO depthBufferRenderUnit.setId(id);
            depthBufferRenderUnit.setupImages();
        }
        AbstractRenderUnit wireRenderUnit = null;
        if (wireRendererClass != null) {
            wireRenderUnit = renderInstance.select(wireRendererClass).get();
            // TODO wireRenderUnit.setId(id);
            wireRenderUnit.setupImages();
        }
        CompositeRenderer compositeRenderer = new CompositeRenderer(normalRenderUnit, depthBufferRenderUnit, wireRenderUnit, wire);
        renderQueue.add(compositeRenderer);
        return compositeRenderer;
    }

    @Override
    protected void prepareDepthBufferRendering() {
        gameCanvas.getCtx3d().bindFramebuffer(WebGLRenderingContext.FRAMEBUFFER, shadowFrameBuffer);
        gameCanvas.getCtx3d().viewport(0, 0, DEPTH_BUFFER_SIZE, DEPTH_BUFFER_SIZE);
        gameCanvas.getCtx3d().clear(WebGLRenderingContext.COLOR_BUFFER_BIT | WebGLRenderingContext.DEPTH_BUFFER_BIT);
    }

    @Override
    protected void prepareMainRendering() {
        gameCanvas.getCtx3d().bindFramebuffer(WebGLRenderingContext.FRAMEBUFFER, null);
        gameCanvas.getCtx3d().viewport(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        gameCanvas.getCtx3d().clear(WebGLRenderingContext.COLOR_BUFFER_BIT | WebGLRenderingContext.DEPTH_BUFFER_BIT);
    }

    @Override
    protected void doRender() {
//        for (CompositeRenderer compositeRenderer : renderQueue) {
//            try {
//                compositeRenderer.drawDepthBuffer();
//            } catch (Throwable t) {
//                logger.log(Level.SEVERE, "drawDepthBuffer failed", t);
//            }
//        }
//        for (CompositeRenderer compositeRenderer : renderQueue) {
//            if (!showMonitor && compositeRenderer == monitor) {
//                continue;
//            }
//            if (!showNorm && (compositeRenderer == terrainNorm || unitNorms.contains(compositeRenderer) || terrainObjectNorms.contains(compositeRenderer))) {
//                continue;
//            }
//            try {
//                compositeRenderer.draw();
//            } catch (Throwable t) {
//                logger.log(Level.SEVERE, "draw failed", t);
//            }
//        }

        // ------------------------------------------------------------------------------------
        // TODO handle the editor stuff

        // Dirty way to render wire over image (see changed files in GIT).
        gameCanvas.getCtx3d().depthFunc(WebGLRenderingContext.ALWAYS);
        gameCanvas.getCtx3d().enable(WebGLRenderingContext.BLEND);
        gameCanvas.getCtx3d().blendFunc(WebGLRenderingContext.SRC_ALPHA, WebGLRenderingContext.ONE_MINUS_SRC_ALPHA);
        gameCanvas.getCtx3d().depthMask(false);
        for (CompositeRenderer compositeRenderer : renderQueue) {
            if (!showMonitor && compositeRenderer == monitor) {
                continue;
            }
            if (!showNorm && (compositeRenderer == terrainNorm || terrainObjectNorms.contains(compositeRenderer))) {
                continue;
            }
            try {
                compositeRenderer.drawWire();
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
            for (TerrainEditorUnitRenderer terrainEditorRenderer : terrainEditorRenderers) {
                if (terrainEditorRenderer.hasElements()) {
                    terrainEditorRenderer.draw();
                }
            }
            if (terrainEditorCursorRenderer.hasElements()) {
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
