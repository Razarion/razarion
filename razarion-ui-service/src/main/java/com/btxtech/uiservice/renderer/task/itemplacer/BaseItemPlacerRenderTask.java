package com.btxtech.uiservice.renderer.task.itemplacer;

import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.uiservice.Shape3DUiService;
import com.btxtech.uiservice.itemplacer.BaseItemPlacer;
import com.btxtech.uiservice.renderer.AbstractModelRenderTask;
import com.btxtech.uiservice.renderer.AbstractVertexContainerRenderUnit;
import com.btxtech.uiservice.renderer.CommonRenderComposite;
import com.btxtech.uiservice.renderer.ModelRenderer;
import com.btxtech.uiservice.renderer.RenderUnitControl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 05.09.2016.
 */
@ApplicationScoped
public class BaseItemPlacerRenderTask extends AbstractModelRenderTask<BaseItemPlacer> {
    private Logger logger = Logger.getLogger(BaseItemPlacerRenderTask.class.getName());
    @Inject
    private Shape3DUiService shape3DUiService;
    private boolean active;

    @Override
    public boolean isActive() {
        return active;
    }

    public void activate(BaseItemPlacer baseItemPlacer) {
        setupCircle(baseItemPlacer);
        setupItem(baseItemPlacer);
        active = true;
    }

    public void deactivate() {
        active = false;
        clear();
    }

    private void setupCircle(BaseItemPlacer baseItemPlacer) {
        ModelRenderer<BaseItemPlacer> modelRenderer = create();
        modelRenderer.init(baseItemPlacer, timeStamp -> baseItemPlacer.provideCircleModelMatrices());
        CommonRenderComposite<AbstractBaseItemPlacerCircleRendererUnit, BaseItemPlacer> compositeRenderer = modelRenderer.create();
        compositeRenderer.init(baseItemPlacer);
        compositeRenderer.setRenderUnit(AbstractBaseItemPlacerCircleRendererUnit.class);
        modelRenderer.add(RenderUnitControl.START_POINT_CIRCLE, compositeRenderer);
        add(modelRenderer);
        compositeRenderer.fillBuffers();
    }

    private void setupItem(BaseItemPlacer baseItemPlacer) {
        if (baseItemPlacer.getBaseItemType().getShape3DId() == null) {
            logger.warning("BaseItemPlacerRenderTask: no shape3DId for BaseItemType: " + baseItemPlacer.getBaseItemType());
            return;
        }

        ModelRenderer<BaseItemType> modelRenderer = create();
        modelRenderer.init(baseItemPlacer.getBaseItemType(), timeStamp -> baseItemPlacer.provideItemModelMatrices());

        Shape3D shape3D = shape3DUiService.getShape3D(baseItemPlacer.getBaseItemType().getShape3DId());
        for (Element3D element3D : shape3D.getElement3Ds()) {
            for (VertexContainer vertexContainer : element3D.getVertexContainers()) {
                CommonRenderComposite<AbstractVertexContainerRenderUnit, VertexContainer> renderComposite = modelRenderer.create();
                renderComposite.init(vertexContainer);
                renderComposite.setRenderUnit(AbstractVertexContainerRenderUnit.class);
                renderComposite.setupAnimation(shape3D, element3D, vertexContainer.getShapeTransform());
                renderComposite.setDepthBufferRenderUnit(AbstractVertexContainerRenderUnit.class);
                renderComposite.setNormRenderUnit(AbstractVertexContainerRenderUnit.class);
                modelRenderer.add(RenderUnitControl.START_POINT_ITEM, renderComposite);
                renderComposite.fillBuffers();
            }
        }
        add(modelRenderer);
    }
}
