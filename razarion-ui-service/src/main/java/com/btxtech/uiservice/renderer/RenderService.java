package com.btxtech.uiservice.renderer;

import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.item.SpawnItemUiService;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 12.07.2016.
 */
public abstract class RenderService {
    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    private SpawnItemUiService spawnItemUiService;
    @Inject
    private Event<RenderServiceInitEvent> serviceInitEvent;
    @Inject
    private Event<PreRenderEvent> preRenderEvent;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;
    private List<CompositeRenderer> renderQueue = new ArrayList<>();

    public void setup() {
        serviceInitEvent.fire(new RenderServiceInitEvent());

        renderQueue.clear();
        // Base Item type renderer
        for (int id : baseItemUiService.getBaseItemTypeIds()) {
            CompositeRenderer compositeRenderer = new CompositeRenderer();
            compositeRenderer.setId(id);
            compositeRenderer.setModelMatricesProvider(baseItemUiService);
            initBaseItemTypeRenderer(compositeRenderer);
            renderQueue.add(compositeRenderer);
        }
        // Spawn item type renderer
        for (int id : spawnItemUiService.getSpawnItemTypeIds()) {
            CompositeRenderer compositeRenderer = new CompositeRenderer();
            compositeRenderer.setId(id);
            compositeRenderer.setModelMatricesProvider(spawnItemUiService);
            initSpawnItemTypeRenderer(compositeRenderer);
            renderQueue.add(compositeRenderer);
        }


        setupRenderers();

        fillBuffers();
    }

    public void render() {
        preRenderEvent.fire(new PreRenderEvent());
        prepareDepthBufferRendering();
        for (CompositeRenderer compositeRenderer : renderQueue) {
            compositeRenderer.drawDepthBuffer();
        }
        prepareMainRendering();
        for (CompositeRenderer compositeRenderer : renderQueue) {
            compositeRenderer.draw();
        }
        // Do old stuff render
        doRender();
    }

    public void fillBuffers() {
        for (CompositeRenderer compositeRenderer : renderQueue) {
            try {
                compositeRenderer.fillBuffers();
            } catch (Throwable t) {
                exceptionHandler.handleException(t);
            }
        }
    }

    protected abstract void prepareMainRendering();

    protected abstract void prepareDepthBufferRendering();

    protected abstract void initBaseItemTypeRenderer(CompositeRenderer compositeRenderer);

    protected abstract void initSpawnItemTypeRenderer(CompositeRenderer compositeRenderer);

    @Deprecated
    protected abstract void setupRenderers();

    @Deprecated
    protected void doRender() {

    }

    public abstract void enrollAnimation(int animatedMeshId);

    public abstract void disenrollAnimation(int animatedMeshId);
}
