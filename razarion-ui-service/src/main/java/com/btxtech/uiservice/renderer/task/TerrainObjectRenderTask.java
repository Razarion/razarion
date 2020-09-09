package com.btxtech.uiservice.renderer.task;

import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.uiservice.Shape3DUiService;
import com.btxtech.uiservice.renderer.AbstractVertexContainerRenderUnit;
import com.btxtech.uiservice.renderer.CommonRenderComposite;
import com.btxtech.uiservice.renderer.ModelRenderer;
import com.btxtech.uiservice.renderer.RenderUnitControl;
import com.btxtech.uiservice.terrain.TerrainUiService;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 31.08.2016.
 */
@Singleton
public class TerrainObjectRenderTask extends AbstractModelRenderTask {
    private Logger logger = Logger.getLogger(TerrainObjectRenderTask.class.getName());
    @Inject
    private Shape3DUiService shape3DUiService;
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private TerrainTypeService terrainTypeService;

    @PostConstruct
    public void postConstruct() {
        setupTerrainObject(false);
    }

    public void reloadEditMode() {
        clear();
        setupTerrainObject(true);
    }

    private void setupTerrainObject(boolean fillBuffer) {
        terrainTypeService.getTerrainObjectConfigs().forEach(terrainObjectConfig -> {
            if (terrainObjectConfig.getShape3DId() != null) {
                ModelRenderer<TerrainObjectConfig> modelRenderer = create();
                modelRenderer.init(terrainObjectConfig, timeStamp -> terrainUiService.provideTerrainObjectModelMatrices(terrainObjectConfig.getId()));
                Shape3D shape3D = shape3DUiService.getShape3D(terrainObjectConfig.getShape3DId());
                for (Element3D element3D : shape3D.getElement3Ds()) {
                    for (VertexContainer vertexContainer : element3D.getVertexContainers()) {
                        CommonRenderComposite<AbstractVertexContainerRenderUnit, VertexContainer> compositeRenderer = modelRenderer.create();
                        compositeRenderer.init(vertexContainer);
                        compositeRenderer.setRenderUnit(AbstractVertexContainerRenderUnit.class);
                        // compositeRenderer.setDepthBufferRenderUnit(AbstractVertexContainerRenderUnit.class);
                        // compositeRenderer.setNormRenderUnit(AbstractVertexContainerRenderUnit.class);
                        compositeRenderer.setupAnimation(shape3D, element3D, vertexContainer.getShapeTransform());
                        modelRenderer.add(RenderUnitControl.TERRAIN, compositeRenderer);
                        if (fillBuffer) {
                            compositeRenderer.fillBuffers();
                        }
                    }
                }
                add(modelRenderer);
            } else {
                logger.warning("TerrainObjectRenderTask: No shape3DId for TerrainObjectConfig: " + terrainObjectConfig);
            }
        });
    }


}
