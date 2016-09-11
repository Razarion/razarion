package com.btxtech.uiservice.renderer.task;

import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.uiservice.Shape3DUiService;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.renderer.AbstractRenderTask;
import com.btxtech.uiservice.renderer.AbstractVertexContainerRenderUnit;
import com.btxtech.uiservice.renderer.CommonRenderComposite;
import com.btxtech.uiservice.renderer.ModelRenderer;
import com.btxtech.uiservice.renderer.RenderUnitControl;

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
public class BaseItemRenderTask extends AbstractRenderTask<BaseItemType> {
    private Logger logger = Logger.getLogger(BaseItemRenderTask.class.getName());
    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    private Shape3DUiService shape3DUiService;

    @PostConstruct
    public void postConstruct() {
        baseItemUiService.getBaseItemTypes().forEach(this::setupBaseItemType);
    }

    public void onBaseItemTypeChanged(@Observes BaseItemType baseItemType) {
        removeAll(baseItemType);
        setupBaseItemType(baseItemType);
    }

    private void setupBaseItemType(BaseItemType baseItemType) {
        // Spawn
        if (baseItemType.getSpawnShape3DId() != null) {
            ModelRenderer<BaseItemType, CommonRenderComposite<AbstractVertexContainerRenderUnit, VertexContainer>, AbstractVertexContainerRenderUnit, VertexContainer> modelRenderer = create();
            modelRenderer.init(baseItemType, () -> baseItemUiService.provideSpawnModelMatrices(baseItemType));
            Shape3D shape3D = shape3DUiService.getShape3D(baseItemType.getSpawnShape3DId());
            for (Element3D element3D : shape3D.getElement3Ds()) {
                for (VertexContainer vertexContainer : element3D.getVertexContainers()) {
                    CommonRenderComposite<AbstractVertexContainerRenderUnit, VertexContainer> compositeRenderer = modelRenderer.create();
                    compositeRenderer.init(vertexContainer);
                    compositeRenderer.setRenderUnit(AbstractVertexContainerRenderUnit.class);
                    compositeRenderer.setDepthBufferRenderUnit(AbstractVertexContainerRenderUnit.class);
                    compositeRenderer.setNormRenderUnit(AbstractVertexContainerRenderUnit.class);
                    compositeRenderer.setupAnimation(shape3D, element3D, vertexContainer.getShapeTransform());
                    modelRenderer.add(RenderUnitControl.NORMAL, compositeRenderer);
                }
            }
            add(modelRenderer);
        } else {
            logger.warning("BaseItemRenderTask: no spawnShape3DId for BaseItemType: " + baseItemType);
        }
        // Alive
        if (baseItemType.getShape3DId() != null) {
            ModelRenderer<BaseItemType, CommonRenderComposite<AbstractVertexContainerRenderUnit, VertexContainer>, AbstractVertexContainerRenderUnit, VertexContainer> modelRenderer = create();
            modelRenderer.init(baseItemType, () -> baseItemUiService.provideAliveModelMatrices(baseItemType));
            Shape3D shape3D = shape3DUiService.getShape3D(baseItemType.getShape3DId());
            for (Element3D element3D : shape3D.getElement3Ds()) {
                for (VertexContainer vertexContainer : element3D.getVertexContainers()) {
                    CommonRenderComposite<AbstractVertexContainerRenderUnit, VertexContainer> compositeRenderer = modelRenderer.create();
                    compositeRenderer.init(vertexContainer);
                    compositeRenderer.setRenderUnit(AbstractVertexContainerRenderUnit.class);
                    compositeRenderer.setDepthBufferRenderUnit(AbstractVertexContainerRenderUnit.class);
                    compositeRenderer.setNormRenderUnit(AbstractVertexContainerRenderUnit.class);
                    compositeRenderer.setupAnimation(shape3D, element3D, vertexContainer.getShapeTransform());
                    modelRenderer.add(RenderUnitControl.NORMAL, compositeRenderer);
                }
            }
            add(modelRenderer);
        } else {
            logger.warning("BaseItemRenderTask: no shape3DId for BaseItemType: " + baseItemType);
        }
    }

}
