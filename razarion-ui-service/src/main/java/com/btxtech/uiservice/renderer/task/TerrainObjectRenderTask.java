package com.btxtech.uiservice.renderer.task;

import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.uiservice.Shape3DUiService;
import com.btxtech.uiservice.renderer.AbstractRenderTask;
import com.btxtech.uiservice.renderer.AbstractVertexContainerRenderUnit;
import com.btxtech.uiservice.renderer.CommonRenderComposite;
import com.btxtech.uiservice.renderer.ModelRenderer;
import com.btxtech.uiservice.terrain.TerrainUiService;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 31.08.2016.
 */
@Singleton
public class TerrainObjectRenderTask extends AbstractRenderTask<TerrainObjectConfig> {
    private Logger logger = Logger.getLogger(TerrainObjectRenderTask.class.getName());
    @Inject
    private Shape3DUiService shape3DUiService;
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private TerrainTypeService terrainTypeService;

    @PostConstruct
    public void postConstruct() {
        terrainTypeService.getTerrainObjectConfigs().forEach(this::setupTerrainObject);
    }

    public void onTerrainObjectChanged(@Observes TerrainObjectConfig terrainObjectConfig) {
        removeAll(terrainObjectConfig);
        setupTerrainObject(terrainObjectConfig);
    }

    private void setupTerrainObject(TerrainObjectConfig terrainObjectConfig) {
        if (terrainObjectConfig.getShape3DId() != null) {
            ModelRenderer<TerrainObjectConfig, CommonRenderComposite<AbstractVertexContainerRenderUnit, VertexContainer>, AbstractVertexContainerRenderUnit, VertexContainer> modelRenderer = create();
            modelRenderer.init(terrainObjectConfig, () -> terrainUiService.provideTerrainObjectModelMatrices(terrainObjectConfig));
            Shape3D shape3D = shape3DUiService.getShape3D(terrainObjectConfig.getShape3DId());
            for (Element3D element3D : shape3D.getElement3Ds()) {
                for (VertexContainer vertexContainer : element3D.getVertexContainers()) {
                    CommonRenderComposite<AbstractVertexContainerRenderUnit, VertexContainer> compositeRenderer = modelRenderer.create();
                    compositeRenderer.init(vertexContainer);
                    compositeRenderer.setRenderUnit(AbstractVertexContainerRenderUnit.class);
                    compositeRenderer.setDepthBufferRenderUnit(AbstractVertexContainerRenderUnit.class);
                    compositeRenderer.setNormRenderUnit(AbstractVertexContainerRenderUnit.class);
                    compositeRenderer.setupAnimation(shape3D, element3D, vertexContainer.getShapeTransform());
                    modelRenderer.add(compositeRenderer);
                }
            }
            add(modelRenderer);
        } else {
            logger.warning("No shape3DId for TerrainObjectConfig: " + terrainObjectConfig);
        }
    }



}
