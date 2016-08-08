package com.btxtech.uiservice.renderer;

import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;
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
    // private Logger logger = Logger.getLogger(RenderService.class.getName());
    @Inject
    private TerrainService terrainService;
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

    public void setup() {
        serviceInitEvent.fire(new RenderServiceInitEvent());
        renderQueue.clear();

        setupGround();
        setupSlopes();
        setupBaseItemTypes();

        fillBuffers();
    }

    private void setupGround() {
        CompositeRenderer compositeRenderer = new CompositeRenderer();
        compositeRenderer.setRenderUnit(instance.select(AbstractGroundUnitRenderer.class).get());
        renderQueue.add(compositeRenderer);
    }

    private void setupSlopes() {
        for (Slope slope : terrainService.getSlopes()) {
            CompositeRenderer compositeRenderer = new CompositeRenderer();
            AbstractSlopeUnitRenderer slopeUnitRenderer = instance.select(AbstractSlopeUnitRenderer.class).get();
            slopeUnitRenderer.setSlope(slope);
            compositeRenderer.setRenderUnit(slopeUnitRenderer);
            renderQueue.add(compositeRenderer);
        }
    }

    private void setupBaseItemTypes() {
        for (BaseItemType baseItemType : baseItemUiService.getBaseItemTypes()) {
            // Spawn
            SpawnItemTypeShape3DRenderer spawnItemTypeShape3DRenderer = instance.select(SpawnItemTypeShape3DRenderer.class).get();
            spawnItemTypeShape3DRenderer.init(baseItemType);
            spawnItemTypeShape3DRenderer.fillRenderQueue(renderQueue);
            // Alive
            AliveItemTypeShape3DRenderer aliveItemTypeShape3DRenderer = instance.select(AliveItemTypeShape3DRenderer.class).get();
            aliveItemTypeShape3DRenderer.init(baseItemType);
            aliveItemTypeShape3DRenderer.fillRenderQueue(renderQueue);
        }
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
}
