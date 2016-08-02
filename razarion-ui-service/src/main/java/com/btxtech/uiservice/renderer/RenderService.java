package com.btxtech.uiservice.renderer;

import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.item.BaseItemUiService;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
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
    private Event<RenderServiceInitEvent> serviceInitEvent;
    @Inject
    private Event<PreRenderEvent> preRenderEvent;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private Instance<Object> instance;
    private List<CompositeRenderer> renderQueue = new ArrayList<>();
    private List<Shape3DRenderer> shape3DRenderers = new ArrayList<>();

    public void setup() {
        serviceInitEvent.fire(new RenderServiceInitEvent());
        renderQueue.clear();

//     TODO   renderQueue.clear();
//        // Base Item type renderer
//        for (int id : baseItemUiService.getBaseItemTypeIds()) {
//            CompositeRenderer compositeRenderer = new CompositeRenderer();
//            compositeRenderer.setId(id);
//            compositeRenderer.setModelMatricesProvider(baseItemUiService);
//            initBaseItemTypeRenderer(compositeRenderer);
//            renderQueue.add(compositeRenderer);
//        }
        // Spawn item type renderer
        shape3DRenderers.clear();
        for (BaseItemType baseItemType : baseItemUiService.getBaseItemTypes()) {
            SpawnItemTypeShape3DRenderer spawnItemTypeShape3DRenderer = instance.select(SpawnItemTypeShape3DRenderer.class).get();
            spawnItemTypeShape3DRenderer.init(baseItemType);
            addShape3DRenderer(spawnItemTypeShape3DRenderer);
//            CompositeRenderer compositeRenderer = new CompositeRenderer();
//            compositeRenderer.setId(id);
//            compositeRenderer.setModelMatricesProvider(spawnItemUiService);
//            initSpawnItemTypeRenderer(compositeRenderer);
//            renderQueue.add(compositeRenderer);
        }


        setupRenderers();

        fillBuffers();
    }

    private void addShape3DRenderer(SpawnItemTypeShape3DRenderer itemTypeShape3DRenderer) {
        shape3DRenderers.add(itemTypeShape3DRenderer);
        itemTypeShape3DRenderer.fillRenderQueue(renderQueue);
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

    @Deprecated
    protected abstract void setupRenderers();

    @Deprecated
    protected void doRender() {

    }

    public abstract void enrollAnimation(int animatedMeshId);

    public abstract void disenrollAnimation(int animatedMeshId);
}
