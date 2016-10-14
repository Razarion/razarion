package com.btxtech.uiservice.renderer.task;

import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.HarvesterType;
import com.btxtech.uiservice.Shape3DUiService;
import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.renderer.AbstractRenderTask;
import com.btxtech.uiservice.renderer.AbstractVertexContainerRenderUnit;
import com.btxtech.uiservice.renderer.CommonRenderComposite;
import com.btxtech.uiservice.renderer.ModelRenderer;
import com.btxtech.uiservice.renderer.RenderUnitControl;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 31.08.2016.
 */
@ApplicationScoped
public class BaseItemRenderTask extends AbstractRenderTask<BaseItemType> {
    private Logger logger = Logger.getLogger(BaseItemRenderTask.class.getName());
    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    private Shape3DUiService shape3DUiService;

    @PostConstruct
    public void postConstruct() {
        baseItemUiService.getBaseItemTypes().forEach(baseItemType -> setupBaseItemType(baseItemType, false));
    }

    public void onBaseItemTypeChanged(BaseItemType baseItemType) {
        removeAll(baseItemType);
        setupBaseItemType(baseItemType, true);
    }

    private void setupBaseItemType(BaseItemType baseItemType, boolean fillBuffer) {
        spawn(baseItemType, fillBuffer);
        alive(baseItemType, fillBuffer);
        harvest(baseItemType, fillBuffer);
    }

    private void spawn(BaseItemType baseItemType, boolean fillBuffer) {
        if (baseItemType.getSpawnShape3DId() != null) {
            ModelRenderer<BaseItemType, CommonRenderComposite<AbstractVertexContainerRenderUnit, VertexContainer>, AbstractVertexContainerRenderUnit, VertexContainer> modelRenderer = create();
            modelRenderer.init(baseItemType, timeStamp -> baseItemUiService.provideSpawnModelMatrices(baseItemType));
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
                    if (fillBuffer) {
                        compositeRenderer.fillBuffers();
                    }
                }
            }
            add(modelRenderer);
        } else {
            logger.warning("BaseItemRenderTask: no spawnShape3DId for BaseItemType: " + baseItemType);
        }
    }

    private void alive(BaseItemType baseItemType, boolean fillBuffer) {
        if (baseItemType.getShape3DId() != null) {
            ModelRenderer<BaseItemType, CommonRenderComposite<AbstractVertexContainerRenderUnit, VertexContainer>, AbstractVertexContainerRenderUnit, VertexContainer> modelRenderer = create();
            modelRenderer.init(baseItemType, timeStamp -> baseItemUiService.provideAliveModelMatrices(baseItemType));
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
                    if (fillBuffer) {
                        compositeRenderer.fillBuffers();
                    }
                }
            }
            add(modelRenderer);
        } else {
            logger.warning("BaseItemRenderTask: no shape3DId for BaseItemType: " + baseItemType);
        }
    }

    private void harvest(BaseItemType baseItemType, boolean fillBuffer) {
        if (baseItemType.getHarvesterType() != null) {
            HarvesterType harvesterType = baseItemType.getHarvesterType();
            if(harvesterType.getAnimationShape3dId() == null) {
                logger.warning("BaseItemRenderTask: no AnimationShape3dId for harvester BaseItemType: " + baseItemType);
                return;
            }
            if(harvesterType.getAnimationOrigin() == null) {
                logger.warning("BaseItemRenderTask: no AnimationOrigin for harvester BaseItemType: " + baseItemType);
                return;
            }

            ModelRenderer<BaseItemType, CommonRenderComposite<AbstractVertexContainerRenderUnit, VertexContainer>, AbstractVertexContainerRenderUnit, VertexContainer> modelRenderer = create();
            modelRenderer.init(baseItemType, timeStamp -> baseItemUiService.provideHarvestAnimationModelMatrices(baseItemType));
            Shape3D shape3D = shape3DUiService.getShape3D(harvesterType.getAnimationShape3dId());
            for (Element3D element3D : shape3D.getElement3Ds()) {
                for (VertexContainer vertexContainer : element3D.getVertexContainers()) {
                    CommonRenderComposite<AbstractVertexContainerRenderUnit, VertexContainer> compositeRenderer = modelRenderer.create();
                    compositeRenderer.init(vertexContainer);
                    compositeRenderer.setRenderUnit(AbstractVertexContainerRenderUnit.class);
                    compositeRenderer.setDepthBufferRenderUnit(AbstractVertexContainerRenderUnit.class);
                    compositeRenderer.setNormRenderUnit(AbstractVertexContainerRenderUnit.class);
                    compositeRenderer.setupAnimation(shape3D, element3D, vertexContainer.getShapeTransform());
                    modelRenderer.add(RenderUnitControl.NORMAL, compositeRenderer);
                    if (fillBuffer) {
                        compositeRenderer.fillBuffers();
                    }
                }
            }
            add(modelRenderer);
        }
    }

}
