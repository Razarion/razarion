package com.btxtech.uiservice.renderer.task.tip;

import com.btxtech.shared.datatypes.shape.Element3D;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.datatypes.shape.VertexContainer;
import com.btxtech.uiservice.Shape3DUiService;
import com.btxtech.uiservice.renderer.AbstractRenderTask;
import com.btxtech.uiservice.renderer.AbstractVertexContainerRenderUnit;
import com.btxtech.uiservice.renderer.CommonRenderComposite;
import com.btxtech.uiservice.renderer.ModelRenderer;
import com.btxtech.uiservice.renderer.RenderUnitControl;
import com.btxtech.uiservice.tip.visualization.InGameDirectionVisualization;
import com.btxtech.uiservice.tip.visualization.InGameTipVisualization;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 05.12.2016.
 */
@ApplicationScoped
public class TipRenderTask extends AbstractRenderTask<InGameTipVisualization> {
    private Logger logger = Logger.getLogger(TipRenderTask.class.getName());
    @Inject
    private Shape3DUiService shape3DUiService;
    private boolean active;
    private InGameTipVisualization inGameTipVisualization;
    private InGameDirectionVisualization inGameDirectionVisualization;

    @Override
    public boolean isActive() {
        return active;
    }

    public void activate(InGameTipVisualization inGameTipVisualization) {
        deactivate();
        this.inGameTipVisualization = inGameTipVisualization;
        setupCorners();
        setupShape3D();
        setupOutOfViewShape3D();
        active = true;
    }

    public void activate(InGameDirectionVisualization inGameDirectionVisualization) {
        deactivate();
        this.inGameDirectionVisualization = inGameDirectionVisualization;
        setupDirectionShape3D();
        active = true;
    }

    public void deactivate() {
        active = false;
        clear();
        inGameTipVisualization = null;
        inGameDirectionVisualization = null;
    }

    @Override
    protected void preRender(long timeStamp) {
        if (inGameTipVisualization != null) {
            inGameTipVisualization.preRender();
        }
    }

    private void setupCorners() {
        ModelRenderer<InGameTipVisualization, CommonRenderComposite<AbstractInGameTipCornerRendererUnit, InGameTipVisualization>, AbstractInGameTipCornerRendererUnit, InGameTipVisualization> modelRenderer = create();
        modelRenderer.init(inGameTipVisualization, timeStamp -> inGameTipVisualization.provideCornerModelMatrices(timeStamp));
        CommonRenderComposite<AbstractInGameTipCornerRendererUnit, InGameTipVisualization> compositeRenderer = modelRenderer.create();
        compositeRenderer.init(inGameTipVisualization);
        compositeRenderer.setRenderUnit(AbstractInGameTipCornerRendererUnit.class);
        modelRenderer.add(RenderUnitControl.TERRAIN_TIP_CORNERS, compositeRenderer);
        add(modelRenderer);
        compositeRenderer.fillBuffers();
    }

    private void setupShape3D() {
        if (inGameTipVisualization.getShape3DId() == null) {
            logger.warning("TipRenderTask: no shape3DId for InGameTipVisualization: " + inGameTipVisualization);
            return;
        }

        ModelRenderer<InGameTipVisualization, CommonRenderComposite<AbstractVertexContainerRenderUnit, VertexContainer>, AbstractVertexContainerRenderUnit, VertexContainer> modelRenderer = create();
        modelRenderer.init(inGameTipVisualization, timeStamp -> inGameTipVisualization.provideShape3DModelMatrices());

        Shape3D shape3D = shape3DUiService.getShape3D(inGameTipVisualization.getShape3DId());
        for (Element3D element3D : shape3D.getElement3Ds()) {
            for (VertexContainer vertexContainer : element3D.getVertexContainers()) {
                CommonRenderComposite<AbstractVertexContainerRenderUnit, VertexContainer> renderComposite = modelRenderer.create();
                renderComposite.init(vertexContainer);
                renderComposite.setRenderUnit(AbstractVertexContainerRenderUnit.class);
                renderComposite.setupAnimation(shape3D, element3D, vertexContainer.getShapeTransform());
                renderComposite.setNormRenderUnit(AbstractVertexContainerRenderUnit.class);
                modelRenderer.add(RenderUnitControl.TERRAIN_TIP_IMAGE, renderComposite);
                renderComposite.fillBuffers();
            }
        }
        add(modelRenderer);
    }


    private void setupOutOfViewShape3D() {
        if (inGameTipVisualization.getOutOfViewShape3DId() == null) {
            logger.warning("TipRenderTask: no getOutOfViewShape3DId for InGameTipVisualization: " + inGameTipVisualization);
            return;
        }

        ModelRenderer<InGameTipVisualization, CommonRenderComposite<AbstractVertexContainerRenderUnit, VertexContainer>, AbstractVertexContainerRenderUnit, VertexContainer> modelRenderer = create();
        modelRenderer.init(inGameTipVisualization, timeStamp -> inGameTipVisualization.provideOutOfViewShape3DModelMatrices());

        Shape3D shape3D = shape3DUiService.getShape3D(inGameTipVisualization.getOutOfViewShape3DId());
        for (Element3D element3D : shape3D.getElement3Ds()) {
            for (VertexContainer vertexContainer : element3D.getVertexContainers()) {
                CommonRenderComposite<AbstractVertexContainerRenderUnit, VertexContainer> renderComposite = modelRenderer.create();
                renderComposite.init(vertexContainer);
                renderComposite.setRenderUnit(AbstractVertexContainerRenderUnit.class);
                renderComposite.setupAnimation(shape3D, element3D, vertexContainer.getShapeTransform());
                renderComposite.setNormRenderUnit(AbstractVertexContainerRenderUnit.class);
                modelRenderer.add(RenderUnitControl.TERRAIN_TIP_IMAGE, renderComposite);
                renderComposite.fillBuffers();
            }
        }
        add(modelRenderer);
    }


    private void setupDirectionShape3D() {
        if (inGameDirectionVisualization.getShape3DId() == null) {
            logger.warning("TipRenderTask: no getOutOfViewShape3DId for InGameDirectionVisualization: " + inGameDirectionVisualization);
            return;
        }

        ModelRenderer<InGameDirectionVisualization, CommonRenderComposite<AbstractVertexContainerRenderUnit, VertexContainer>, AbstractVertexContainerRenderUnit, VertexContainer> modelRenderer = create();
        modelRenderer.init(inGameDirectionVisualization, timeStamp -> inGameDirectionVisualization.provideDModelMatrices());

        Shape3D shape3D = shape3DUiService.getShape3D(inGameDirectionVisualization.getShape3DId());
        for (Element3D element3D : shape3D.getElement3Ds()) {
            for (VertexContainer vertexContainer : element3D.getVertexContainers()) {
                CommonRenderComposite<AbstractVertexContainerRenderUnit, VertexContainer> renderComposite = modelRenderer.create();
                renderComposite.init(vertexContainer);
                renderComposite.setRenderUnit(AbstractVertexContainerRenderUnit.class);
                renderComposite.setupAnimation(shape3D, element3D, vertexContainer.getShapeTransform());
                renderComposite.setNormRenderUnit(AbstractVertexContainerRenderUnit.class);
                modelRenderer.add(RenderUnitControl.TERRAIN_TIP_IMAGE, renderComposite);
                renderComposite.fillBuffers();
            }
        }
        add(modelRenderer);
    }

}
