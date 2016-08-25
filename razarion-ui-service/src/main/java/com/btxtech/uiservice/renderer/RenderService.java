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
    private Instance<AbstractGroundRendererUnit> groundRendererUnitInstance; // Make instance pre class due to bug in errai: https://issues.jboss.org/browse/ERRAI-937?jql=project%20%3D%20ERRAI%20AND%20text%20~%20%22Instance%20qualifier%22
    @Inject
    @DepthBufferRenderer
    private Instance<AbstractGroundRendererUnit> groundDepthBufferRendererUnitInstance; // Make instance pre class due to bug in errai: https://issues.jboss.org/browse/ERRAI-937?jql=project%20%3D%20ERRAI%20AND%20text%20~%20%22Instance%20qualifier%22
    @Inject
    @ColorBufferRenderer
    private Instance<AbstractSlopeRendererUnit> slopeRendererUnitInstance; // Make instance pre class due to bug in errai: https://issues.jboss.org/browse/ERRAI-937?jql=project%20%3D%20ERRAI%20AND%20text%20~%20%22Instance%20qualifier%22
    @Inject
    @DepthBufferRenderer
    private Instance<AbstractSlopeRendererUnit> slopeDepthBufferRendererUnitInstance; // Make instance pre class due to bug in errai: https://issues.jboss.org/browse/ERRAI-937?jql=project%20%3D%20ERRAI%20AND%20text%20~%20%22Instance%20qualifier%22
    @Inject
    private Instance<AbstractWaterRendererUnit> waterRendererUnitInstance;
    @Inject
    private Instance<Shape3DRenderer> shape3DRendererInstance;
    private List<CompositeRenderer> renderQueue = new ArrayList<>();
    private Map<TerrainObjectConfig, Shape3DRenderer> terrainObjectRenderers = new HashMap<>();
    private MapCollection<BaseItemType, Shape3DRenderer> baseItemTypeRenderers = new MapCollection<>();

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
        compositeRenderer.setRenderUnit(groundRendererUnitInstance.get());
        compositeRenderer.setDepthBufferRenderUnit(groundDepthBufferRendererUnitInstance.get());
        renderQueue.add(compositeRenderer);
    }

    private void setupSlopes() {
        for (Slope slope : terrainService.getSlopes()) {
            CompositeRenderer compositeRenderer = new CompositeRenderer();
            AbstractSlopeRendererUnit slopeUnitRenderer = slopeRendererUnitInstance.get();
            slopeUnitRenderer.setSlope(slope);
            compositeRenderer.setRenderUnit(slopeUnitRenderer);
            AbstractSlopeRendererUnit slopeDepthBufferUnitRenderer = slopeDepthBufferRendererUnitInstance.get();
            slopeDepthBufferUnitRenderer.setSlope(slope);
            compositeRenderer.setDepthBufferRenderUnit(slopeDepthBufferUnitRenderer);
            renderQueue.add(compositeRenderer);
        }
    }

    private void setupTerrainObjects() {
        for (final TerrainObjectConfig terrainObjectConfig : terrainTypeService.getTerrainObjectConfigs()) {
            setupTerrainObject(terrainObjectConfig);
        }
    }

    public void onTerrainObjectChanged(@Observes TerrainObjectConfig terrainObjectConfig) {
        Shape3DRenderer oldShape3DRenderer = terrainObjectRenderers.remove(terrainObjectConfig);
        if (oldShape3DRenderer != null) {
            renderQueue.removeAll(oldShape3DRenderer.getMyRenderers());
        }
        Shape3DRenderer newShape3DRenderer = setupTerrainObject(terrainObjectConfig);
        for (CompositeRenderer compositeRenderer : newShape3DRenderer.getMyRenderers()) {
            compositeRenderer.fillBuffers();
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
        for (BaseItemType baseItemType : baseItemUiService.getBaseItemTypes()) {
            setupBaseItemType(baseItemType);
        }
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
            }
        }
    }

    private void setupWater() {
        CompositeRenderer compositeRenderer = new CompositeRenderer();
        compositeRenderer.setRenderUnit(waterRendererUnitInstance.get());
        renderQueue.add(compositeRenderer);
    }

    public void render() {
        preRenderEvent.fire(new PreRenderEvent());
        prepareDepthBufferRendering();
        renderQueue.forEach(CompositeRenderer::drawDepthBuffer);
        prepareMainRendering();
        renderQueue.forEach(CompositeRenderer::draw);
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

    public int getRenderQueueSize() {
        return renderQueue.size();
    }

    protected abstract void prepareMainRendering();

    protected abstract void prepareDepthBufferRendering();
}
