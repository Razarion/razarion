package com.btxtech.uiservice.renderer.task;

import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.uiservice.renderer.AbstractRenderTask;
import com.btxtech.uiservice.renderer.CompositeRenderer;
import com.btxtech.uiservice.renderer.Shape3DRenderer;
import com.btxtech.uiservice.terrain.TerrainUiService;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 31.08.2016.
 */
@Singleton
public class TerrainObjectRenderTask extends AbstractRenderTask {
    private Logger logger = Logger.getLogger(TerrainObjectRenderTask.class.getName());
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private Instance<Shape3DRenderer> shape3DRendererInstance;
    @Inject
    private TerrainTypeService terrainTypeService;
    private Map<TerrainObjectConfig, Shape3DRenderer> terrainObjectRenderers = new HashMap<>();

    @PostConstruct
    public void postConstruct() {
        terrainTypeService.getTerrainObjectConfigs().forEach(this::setupTerrainObject);
    }

    public void onTerrainObjectChanged(@Observes TerrainObjectConfig terrainObjectConfig) {
        Shape3DRenderer oldShape3DRenderer = terrainObjectRenderers.remove(terrainObjectConfig);
        if (oldShape3DRenderer != null) {
            removeAll(oldShape3DRenderer.getMyRenderers());
        }
        Shape3DRenderer newShape3DRenderer = setupTerrainObject(terrainObjectConfig);
        for (CompositeRenderer compositeRenderer : newShape3DRenderer.getMyRenderers()) {
            compositeRenderer.fillBuffers();
            if (isShowNorm()) {
                compositeRenderer.fillNormBuffer();
            }
        }
    }

    private Shape3DRenderer setupTerrainObject(TerrainObjectConfig terrainObjectConfig) {
        Shape3DRenderer shape3DRenderer = shape3DRendererInstance.get();
        if (terrainObjectConfig.getShape3DId() != null) {
            shape3DRenderer.init(terrainObjectConfig.getShape3DId(), () -> terrainUiService.provideTerrainObjectModelMatrices(terrainObjectConfig));
            shape3DRenderer.fillRenderQueue(getAll());
            terrainObjectRenderers.put(terrainObjectConfig, shape3DRenderer);
        } else {
            logger.warning("No shape3DId for TerrainObjectConfig: " + terrainObjectConfig);
        }
        return shape3DRenderer;
    }


}
