package com.btxtech.uiservice.renderer;

import com.btxtech.shared.datatypes.MapCollection;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.terrain.TerrainUiService;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 12.07.2016.
 */
public abstract class RenderService {
    private Logger logger = Logger.getLogger(RenderService.class.getName());
    @Inject
    private TerrainTypeService terrainTypeService;
    @Inject
    private TerrainService terrainService;
    @Inject
    private TerrainUiService terrainUiService;
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
    @ColorBufferRenderer
    private Instance<AbstractRenderUnit> rendererInstance;
    @Inject
    @ColorBufferRenderer
    private Instance<AbstractRenderUnit> depthBufferRendererInstance;
    @Inject
    @NormRenderer
    private Instance<AbstractRenderUnit> normRendererInstance;
    @Inject
    private Instance<Shape3DRenderer> shape3DRendererInstance;
    private List<CompositeRenderer> renderQueue = new ArrayList<>();
    private Map<TerrainObjectConfig, Shape3DRenderer> terrainObjectRenderers = new HashMap<>();
    private MapCollection<BaseItemType, Shape3DRenderer> baseItemTypeRenderers = new MapCollection<>();
    private boolean showNorm;

    protected abstract void prepareMainRendering();

    protected abstract void prepareDepthBufferRendering();

    public void setup() {
        serviceInitEvent.fire(new RenderServiceInitEvent());
        renderQueue.clear();

        setupGround();
        setupSlopes();
        setupTerrainObjects();
        setupBaseItemTypes();
        setupWater();

        fillBuffers();
    }

    private void setupGround() {
        CompositeRenderer compositeRenderer = new CompositeRenderer();
        compositeRenderer.setRenderUnit(rendererInstance.select(AbstractGroundRendererUnit.class).get());
        compositeRenderer.setDepthBufferRenderUnit(depthBufferRendererInstance.select(AbstractGroundRendererUnit.class).get());
        compositeRenderer.setNormRenderUnit(normRendererInstance.select(AbstractGroundRendererUnit.class).get());
        renderQueue.add(compositeRenderer);
    }

    private void setupSlopes() {
        for (Slope slope : terrainService.getSlopes()) {
            CompositeRenderer compositeRenderer = new CompositeRenderer();
            AbstractSlopeRendererUnit slopeUnitRenderer = rendererInstance.select(AbstractSlopeRendererUnit.class).get();
            slopeUnitRenderer.setSlope(slope);
            compositeRenderer.setRenderUnit(slopeUnitRenderer);
            AbstractSlopeRendererUnit slopeDepthBufferUnitRenderer = depthBufferRendererInstance.select(AbstractSlopeRendererUnit.class).get();
            slopeDepthBufferUnitRenderer.setSlope(slope);
            compositeRenderer.setDepthBufferRenderUnit(slopeDepthBufferUnitRenderer);
            AbstractSlopeRendererUnit slopeNormUnitRenderer = normRendererInstance.select(AbstractSlopeRendererUnit.class).get();
            slopeNormUnitRenderer.setSlope(slope);
            compositeRenderer.setNormRenderUnit(slopeNormUnitRenderer);
            renderQueue.add(compositeRenderer);
        }
    }

    private void setupTerrainObjects() {
        terrainTypeService.getTerrainObjectConfigs().forEach(this::setupTerrainObject);
    }

    public void onTerrainObjectChanged(@Observes TerrainObjectConfig terrainObjectConfig) {
        Shape3DRenderer oldShape3DRenderer = terrainObjectRenderers.remove(terrainObjectConfig);
        if (oldShape3DRenderer != null) {
            renderQueue.removeAll(oldShape3DRenderer.getMyRenderers());
        }
        Shape3DRenderer newShape3DRenderer = setupTerrainObject(terrainObjectConfig);
        for (CompositeRenderer compositeRenderer : newShape3DRenderer.getMyRenderers()) {
            compositeRenderer.fillBuffers();
            if (showNorm) {
                compositeRenderer.fillNormBuffer();
            }
        }
    }

    private Shape3DRenderer setupTerrainObject(TerrainObjectConfig terrainObjectConfig) {
        Shape3DRenderer shape3DRenderer = shape3DRendererInstance.get();
        if (terrainObjectConfig.getShape3DId() != null) {
            shape3DRenderer.init(terrainObjectConfig.getShape3DId(), () -> terrainUiService.provideTerrainObjectModelMatrices(terrainObjectConfig));
            shape3DRenderer.fillRenderQueue(renderQueue);
            terrainObjectRenderers.put(terrainObjectConfig, shape3DRenderer);
        } else {
            logger.warning("No shape3DId for TerrainObjectConfig: " + terrainObjectConfig);
        }
        return shape3DRenderer;
    }

    private void setupBaseItemTypes() {
        baseItemUiService.getBaseItemTypes().forEach(this::setupBaseItemType);
    }

    private Collection<Shape3DRenderer> setupBaseItemType(BaseItemType baseItemType) {
        Collection<Shape3DRenderer> shape3DRenderers = new ArrayList<>();
        // Spawn
        if (baseItemType.getSpawnShape3DId() != null) {
            Shape3DRenderer spawnShape3DRenderer = shape3DRendererInstance.get();
            spawnShape3DRenderer.init(baseItemType.getSpawnShape3DId(), () -> baseItemUiService.provideSpawnModelMatrices());
            spawnShape3DRenderer.fillRenderQueue(renderQueue);
            baseItemTypeRenderers.put(baseItemType, spawnShape3DRenderer);
            shape3DRenderers.add(spawnShape3DRenderer);
        } else {
            logger.warning("No spawnShape3DId for BaseItemType: " + baseItemType);
        }
        // Alive
        if (baseItemType.getShape3DId() != null) {
            Shape3DRenderer aliveShape3DRenderer = shape3DRendererInstance.get();
            aliveShape3DRenderer.init(baseItemType.getShape3DId(), () -> baseItemUiService.provideAliveModelMatrices());
            aliveShape3DRenderer.fillRenderQueue(renderQueue);
            baseItemTypeRenderers.put(baseItemType, aliveShape3DRenderer);
            shape3DRenderers.add(aliveShape3DRenderer);
        } else {
            logger.warning("No shape3DId for BaseItemType: " + baseItemType);
        }

        return shape3DRenderers;
    }

    public void onBaseItemTypeChanged(@Observes BaseItemType baseItemType) {
        Collection<Shape3DRenderer> oldRenderers = baseItemTypeRenderers.get(baseItemType);
        if (oldRenderers != null) {
            for (Shape3DRenderer shape3DRenderer : oldRenderers) {
                renderQueue.removeAll(shape3DRenderer.getMyRenderers());
            }
            baseItemTypeRenderers.remove(baseItemType);
        }

        Collection<Shape3DRenderer> shape3DRenderers = setupBaseItemType(baseItemType);
        for (Shape3DRenderer shape3DRenderer : shape3DRenderers) {
            for (CompositeRenderer compositeRenderer : shape3DRenderer.getMyRenderers()) {
                compositeRenderer.fillBuffers();
                if (showNorm) {
                    compositeRenderer.fillNormBuffer();
                }
            }
        }
    }

    private void setupWater() {
        CompositeRenderer compositeRenderer = new CompositeRenderer();
        compositeRenderer.setRenderUnit(rendererInstance.select(AbstractWaterRendererUnit.class).get());
        compositeRenderer.setNormRenderUnit(normRendererInstance.select(AbstractWaterRendererUnit.class).get());
        renderQueue.add(compositeRenderer);
    }

    public void render() {
        preRenderEvent.fire(new PreRenderEvent());
        prepareDepthBufferRendering();
        renderQueue.forEach(CompositeRenderer::drawDepthBuffer);
        prepareMainRendering();
        renderQueue.forEach(CompositeRenderer::draw);
        if (showNorm) {
            renderQueue.forEach(CompositeRenderer::drawNorm);
        }
    }

    @Deprecated
    public void fillBuffers() {
        for (CompositeRenderer compositeRenderer : renderQueue) {
            try {
                compositeRenderer.fillBuffers();
            } catch (Throwable t) {
                exceptionHandler.handleException(t);
            }
        }
    }

    public int getRenderQueueSize() {
        return renderQueue.size();
    }

    public boolean isShowNorm() {
        return showNorm;
    }

    public void setShowNorm(boolean showNorm) {
        this.showNorm = showNorm;
        if (showNorm) {
            for (CompositeRenderer compositeRenderer : renderQueue) {
                try {
                    compositeRenderer.fillNormBuffer();
                } catch (Throwable t) {
                    exceptionHandler.handleException(t);
                }
            }
        }
    }
}
