package com.btxtech.uiservice.renderer.task;

import com.btxtech.shared.datatypes.MapCollection;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.renderer.AbstractRenderTask;
import com.btxtech.uiservice.renderer.CompositeRenderer;
import com.btxtech.uiservice.renderer.Shape3DRenderer;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 31.08.2016.
 */
public class BaseItemRenderTask extends AbstractRenderTask {
    private Logger logger = Logger.getLogger(BaseItemRenderTask.class.getName());
    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    private Instance<Shape3DRenderer> shape3DRendererInstance;
    private MapCollection<BaseItemType, Shape3DRenderer> baseItemTypeRenderers = new MapCollection<>();

    @PostConstruct
    public void postConstruct() {
        baseItemUiService.getBaseItemTypes().forEach(this::setupBaseItemType);
    }

    private Collection<Shape3DRenderer> setupBaseItemType(BaseItemType baseItemType) {
        Collection<Shape3DRenderer> shape3DRenderers = new ArrayList<>();
        // Spawn
        if (baseItemType.getSpawnShape3DId() != null) {
            Shape3DRenderer spawnShape3DRenderer = shape3DRendererInstance.get();
            spawnShape3DRenderer.init(baseItemType.getSpawnShape3DId(), () -> baseItemUiService.provideSpawnModelMatrices());
            spawnShape3DRenderer.fillRenderQueue(getAll());
            baseItemTypeRenderers.put(baseItemType, spawnShape3DRenderer);
            shape3DRenderers.add(spawnShape3DRenderer);
        } else {
            logger.warning("No spawnShape3DId for BaseItemType: " + baseItemType);
        }
        // Alive
        if (baseItemType.getShape3DId() != null) {
            Shape3DRenderer aliveShape3DRenderer = shape3DRendererInstance.get();
            aliveShape3DRenderer.init(baseItemType.getShape3DId(), () -> baseItemUiService.provideAliveModelMatrices());
            aliveShape3DRenderer.fillRenderQueue(getAll());
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
                removeAll(shape3DRenderer.getMyRenderers());
            }
            baseItemTypeRenderers.remove(baseItemType);
        }

        Collection<Shape3DRenderer> shape3DRenderers = setupBaseItemType(baseItemType);
        for (Shape3DRenderer shape3DRenderer : shape3DRenderers) {
            for (CompositeRenderer compositeRenderer : shape3DRenderer.getMyRenderers()) {
                compositeRenderer.fillBuffers();
                if (isShowNorm()) {
                    compositeRenderer.fillNormBuffer();
                }
            }
        }
    }

}
